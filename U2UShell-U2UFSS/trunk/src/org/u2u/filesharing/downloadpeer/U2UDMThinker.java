/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.u2u.filesharing.downloadpeer;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jxta.protocol.PipeAdvertisement;
import org.u2u.common.db.Chunks;
import org.u2u.filesharing.U2UContentAdvertisementImpl;
import org.u2u.filesharing.fsprotocol.U2UFSPResponseConnection;
import org.u2u.filesharing.fsprotocol.U2UFSPResponseFileInfo;
import org.u2u.filesharing.fsprotocol.U2UFSPResponseGetChunk;
import org.u2u.filesharing.fsprotocol.U2UFSProtocolResponse;
import org.u2u.filesharing.fsprotocol.U2UFileSharingProtocolEvent;
import org.u2u.filesharing.fsprotocol.U2UFileSharingProtocolListener;

/**
 * This class is responsable of generates the tasks based in the DownloadingManager's
 * responsibilities and the responsibilities's generating criteria
 * @author Irene & Sergio
 */
public class U2UDMThinker implements Runnable, U2UFileSharingProtocolListener, U2USearchListener {

    /**
     * HashSet for the remote advs of failed peers, 1) PipeID = (PipeId(SocketAdv of remote peer) in String format),
     * 2)Integer = number of sanctions
     * when a peer reach 3 sanctions we no longer pay attention to him.
     */
    protected final ConcurrentHashMap<String, Integer> failedRemoteAdvsSanctions = new ConcurrentHashMap<String, Integer>();
    /** 
     * ToDo Queue from the DownloadingManager
     */
    private final LinkedBlockingQueue<U2UDMExecutorsTask> toDoDM;
    /** 
     * ToThink Queue
     */
    private final LinkedBlockingQueue<U2UDMThinkersProblem> toThink;
    /** 
     * HashSet for the remote advs, String=SocketName, Adv
     */
    private final ConcurrentHashMap<String, U2UContentAdvertisementImpl> remoteAdvs = new ConcurrentHashMap<String, U2UContentAdvertisementImpl>();
    /**
     * HashMap to have conscience about the number of available chunks, base of 30% logic
     * String = ProtocolID, short[] {numAvailableChunks, numDownloads}
     */
    private final HashMap<String, short[]> numAvailableChunks = new HashMap<String, short[]>();
    /**
     * RUNNING?
     */
    private boolean isRunning;
    /** 
     * sha-1 of the content
     */
    private final String sha1;
    /** 
     * Thinker Id, "Thinker<" + downloadingID + ">";
     */
    private String thinkerID;

    //about the chunks
    //FIXME spino327@gmail.com we need to tell to the Thinker when the executor received successfully the Chunks sha1 info
    /**
     * we have the list of chunks' sha1 hash
     */
    private boolean haveTheListOfChunksHash = false;


    //Timer
    /** the user already send a forcing sources problem*/
    private boolean alreadyForcingFindSources;
    /** findingSourcesTimer timer, it generates task for finding sources every n [ms]*/
    private Timer findingSourcesTimer;
    /**
     * represents a task for finding sources
     */
    private FindingSourcesTimerTask findingSourcesTimerTask;

    private class FindingSourcesTimerTask extends TimerTask {

        private int stopVetoing = 0;

        public void resetTheStopVetoing()
        {
            stopVetoing = 0;
        }

        @Override
        public void run()
        {
            //stop vetoing the failed peers, every 5 minutes
            if((++stopVetoing) >= 2)
            {
                stopVetoing = 0;
                for(Map.Entry<String, Integer> failed : failedRemoteAdvsSanctions.entrySet())
                {
                    //remove only the peers with 3 sanctions
                    if(failed.getValue() >= 3)
                    {
                        String key = failed.getKey();
                        failedRemoteAdvsSanctions.remove(key);
                        System.out.println(thinkerID + " remove the vetoing of the failed peer(" + key +
                                "), he finish the time in the jail");
                    }
                }
            }

            System.out.println(thinkerID + " i think that we need to find more sources");
            U2UDMThinkersProblem problem = new U2UDMThinkersProblem(
                U2UDMThinkersProblem.NEED_MORE_SOURCES_PROBLEM,
                new Object[0]);

            //to the toThink queue
            toThink.offer(problem);
        }
    }

    /**
     * Build a U2UDMThinker
     * @param toDo the DownloadingManager(The executor)'s ToDo queue
     * @param sha1 the Sha1 hash of the content
     * @param downloadingID Id of the DownloadingManager own of the Thinker
     */
    protected U2UDMThinker(LinkedBlockingQueue<U2UDMExecutorsTask> toDo, String sha1, String downloadingID)
    {
        //sha1
        this.sha1 = sha1;

        //init toThink
        toThink = new LinkedBlockingQueue<U2UDMThinkersProblem>();
        //toDo
        this.toDoDM = toDo;

        //thinker Id
        thinkerID = "Thinker<" + downloadingID + ">";

        //
        alreadyForcingFindSources = false;
    }

    public void run()
    {
        //--------initialization of the Thinker

        isRunning = true;
        //whitout tracker, loocking others adv with the same SHA-1
        
        //timer
        findingSourcesTimer = new Timer("findingSourcesTimer<" + thinkerID + ">", true);
        //timer task
        findingSourcesTimerTask = new FindingSourcesTimerTask();
        //schedule
        findingSourcesTimer.schedule(findingSourcesTimerTask, 0,(long) (2.5 * 60000));
        //--------EOInit

        //--------RUNNING
        while(isRunning)
        {
            try
            {
                //0. take a Problem from the Queue
                U2UDMThinkersProblem problem = toThink.take(); //blocking

                //1. Think in the problem, use the problem's data
                U2UDMExecutorsTask taskToDo = null;

                switch(problem.getProblemType())
                {
                    case U2UDMThinkersProblem.PROTOCOL_EVENT_PROBLEM:
                    {
                        Object[] array = problem.getProblemData();

                        if(array.length == 1)
                        {
                            if(array[0] instanceof U2UFileSharingProtocolEvent)
                            {
                                taskToDo = this.protocolEventProblem(
                                        (U2UFileSharingProtocolEvent) array[0]);
                            }
                        }
                        
                        break;
                    }

                    case U2UDMThinkersProblem.CONTENT_ADVERTISEMENT_EVENT_PROBLEM:
                    {
                        Object[] array = problem.getProblemData();

                        if(array.length == 1)
                        {
                            if(array[0] instanceof U2UContentDiscoveryEvent)
                            {
                                taskToDo = this.contentAdvertisementEventProblem(
                                        (U2UContentDiscoveryEvent) array[0]);
                            }
                        }

                        break;
                    }

                    case U2UDMThinkersProblem.NEED_MORE_SOURCES_PROBLEM:
                    {
                        System.out.println(thinkerID + " NEED_MORE_SOURCES_PROBLEM");
                        taskToDo = this.needMoreSourcesProblem();

                        break;
                    }

                    case U2UDMThinkersProblem.NEW_AVAILABLE_CHUNKS_EVENT_PROBLEM:
                    {
                        Object[] array = problem.getProblemData();

                        if(array.length == 2)
                        {
                            if((array[0] instanceof String) &&
                                    (array[1] instanceof Short))
                            {
                                String pId = (String) array[0];
                                short numC = ((Short)array[1]).shortValue();
                                taskToDo = this.newAvailableChunksEventProblem(pId, numC);
                            }
                        }
                        
                        break;
                    }

                    case U2UDMThinkersProblem.DOWNLOADED_CHUNK_EVENT_PROBLEM:
                    {
                        Object[] array = problem.getProblemData();

                        if(array.length == 1)
                        {
                            if(array[0] instanceof String)
                            {
                                String pId = (String) array[0];
                                
                                taskToDo = this.downloadedChunkEventProblem(pId);
                            }
                        }

                        break;
                    }

                    case U2UDMThinkersProblem.AVAILABLE_CHUNKS_SHA1_EVENT_PROBLEM:
                    {
                        Object[] array = problem.getProblemData();

                        if(array.length == 0)
                        {
                            taskToDo = this.availableChunksSHA1EventProblem();
                        }
                        
                        break;
                    }

                    case U2UDMThinkersProblem.FINISH_PROBLEM:
                    {
                        System.out.println(thinkerID + " FINISH_TASK");
                        //Do nothing
                        break;
                    }
                }

                //2. make a Task for solve the problem and put them in the Executor's toDo Queue
                if(taskToDo != null)
                {
                    toDoDM.offer(taskToDo);
                }

            } catch (InterruptedException ex) {
                Logger.getLogger(U2UDMThinker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //--------EORunning

        //--------finish

        findingSourcesTimer.cancel();
        findingSourcesTimerTask.cancel();
        findingSourcesTimer = null;
        findingSourcesTimerTask = null;
       
        numAvailableChunks.clear();
        remoteAdvs.clear();
        failedRemoteAdvsSanctions.clear();
        toThink.clear();

        //--------EOFinish
    }

    /**
     * represents this instance of the Thinker, like a hash
     * @return the Id of the thinker, "Thinker<" + downloadingID + ">";
     */
    protected String getThinkerID()
    {
        return thinkerID;
    }

    /**
     * stop the thread, the object leave of thinking
     */
    protected void stopThinking()
    {
        isRunning = false;

        toThink.offer(new U2UDMThinkersProblem(U2UDMThinkersProblem.FINISH_PROBLEM, new Object[0]));
    }

    /**
     * this method is called when we download successfully the chunks' sha1 list from one remote peer
     */
    protected void setHaveTheListOfChunksHash()
    {
        this.haveTheListOfChunksHash = true;
    }

    /**
     * this method is called when the executor successfully execute the find of sources
     */
    protected void setAlreadyForcingFindSources()
    {
        this.alreadyForcingFindSources = false;
    }

    /**
     * Have the list of chunks' sha1?
     *
     * @return
     */
    public boolean isHaveTheListOfChunksHash()
    {
        return haveTheListOfChunksHash;
    }

    //--------I/O-How it feels?

    public void protocolEvent(U2UFileSharingProtocolEvent event)
    {
        //this event are response from the remote peers
        //to the thinker
        System.out.println(thinkerID + " incoming protocolEvent");
        U2UDMThinkersProblem problem = new U2UDMThinkersProblem(
                U2UDMThinkersProblem.PROTOCOL_EVENT_PROBLEM,
                new Object[] {event});

        //to the toThink queue
        toThink.offer(problem);
    }

    public void contentAdvertisementEvent(U2UContentDiscoveryEvent event)
    {
        System.out.println(thinkerID + " incoming contentAdvertisementEvent");
        U2UDMThinkersProblem problem = new U2UDMThinkersProblem(
                U2UDMThinkersProblem.CONTENT_ADVERTISEMENT_EVENT_PROBLEM,
                new Object[] {event});

        //to the toThink queue
        toThink.offer(problem);
    }

    /**
     * This method force the thinker to find new sources.
     */
    protected void forcingFindNewSources()
    {
        //check if we already send a forcing
        if(!alreadyForcingFindSources)
        {
            alreadyForcingFindSources = true;

            System.out.println(thinkerID + " user force the find of more sources");
            //remove all the sanctions
            failedRemoteAdvsSanctions.clear();
            System.out.println(thinkerID + " remove all the vetoings of all failed peers, They finish the time in the jail");

            U2UDMThinkersProblem problem = new U2UDMThinkersProblem(
                U2UDMThinkersProblem.NEED_MORE_SOURCES_PROBLEM,
                new Object[0]);

            //to the toThink queue
            toThink.offer(problem);
        }
        else
        {
            System.out.println(thinkerID + " user force the find of more sources, but the system reject the forcing");
        }

    }

    /**
     * This method is used for tell to the thinker about the incoming of th chunks' sha1 list
     *
     */
    protected void availableChunksSHA1Event()
    {
        if(haveTheListOfChunksHash)
        {
            System.out.println(thinkerID + " incoming availableChunksSHA1Event");
            U2UDMThinkersProblem problem = new U2UDMThinkersProblem(
                    U2UDMThinkersProblem.AVAILABLE_CHUNKS_SHA1_EVENT_PROBLEM,
                    new Object[0]);

            //to the toThink queue
            toThink.offer(problem);
        }
    }

    /**
     * This method is used for tell to the thinker about the exist of new Available Chunks to download
     * 
     * @param protocolId identifier of the remote peer's connection
     * @param numChunks number of available chunks that we can download and we haven't from the remote peer
     */
    protected void newAvailableChunksEvent(String protocolId, short numChunks)
    {
        if(protocolId.startsWith("Protocol@") /*&& (protocolId.length() == 91)*/ &&
                (numChunks >= 0) && (numChunks <= 32767))
        {
            System.out.println(thinkerID + " incoming newAvailableChunksEvent");
            U2UDMThinkersProblem problem = new U2UDMThinkersProblem(
                    U2UDMThinkersProblem.NEW_AVAILABLE_CHUNKS_EVENT_PROBLEM,
                    new Object[] {protocolId, numChunks});

            //to the toThink queue
            toThink.offer(problem);
        }
    }

    /**
     * This method is used for tell to the thinker about the download of a Chunk
     *
     * @param protocolId identifier of the remote peer's connection
     */
    protected void downloadedChunkEvent(String protocolId)
    {
        if(protocolId.startsWith("Protocol@"))
        {
            System.out.println(thinkerID + " incoming downloadedChunkEvent");
            U2UDMThinkersProblem problem = new U2UDMThinkersProblem(
                    U2UDMThinkersProblem.DOWNLOADED_CHUNK_EVENT_PROBLEM,
                    new Object[] {protocolId});

            //to the toThink queue
            toThink.offer(problem);

        }
    }

    /**
     * This method is used for tell to the thinker about the download of shared file
     *
     * @param downloadingID identifier of the DownloadingManager
     * @param numChunks number of chunks of the Shared FIle
     */
    /*protected void downloadedSharedFileEvent(String downloadingID, short numChunks)
    {
        System.out.println(thinkerID + " incoming downloadedSharedFileEvent");
        U2UDMThinkersProblem problem = new U2UDMThinkersProblem(
                U2UDMThinkersProblem.DOWNLOADED_SHARED_FILE_EVENT_PROBLEM,
                new Object[] {downloadingID, numChunks});

        //to the toThink queue
        toThink.offer(problem);
    }*/

    /*
     * To the findingSourcesTimerTask is I/O
     */

    //--------EOI/O-How it feels?

    //--------How to think in?

    /**
     * protocolEventProblem
     * @param event the event from the protocol
     */
    private U2UDMExecutorsTask protocolEventProblem(U2UFileSharingProtocolEvent event)
    {
        U2UDMExecutorsTask task = null;

        U2UFSProtocolResponse res = event.getResponse();
        
        switch(res.getResponseId())
        {
            case U2UFSProtocolResponse.RCONN:
            {
                U2UFSPResponseConnection con = (U2UFSPResponseConnection) res;

                //checking the response number
                String resNum = con.getResponseNumber();
                System.out.println("RCONN " + resNum);
                //201 connection true
                if(resNum.equals(U2UFSPResponseConnection.R201))
                {
                    /*
                     * if 201 the next task that we need to perform is request the:
                     * 1) list of chunk's hash(only once time and never again)
                     *    or
                     * 2) remote peer's chunks(when reach the 30% of downloaded chunks for a specific remote peer)
                     */
                    //perform 1
                    if(!haveTheListOfChunksHash)
                    {
                        //Object[] {protocolID = Protocol@<(PipeId(SocketAdv of remote peer) in String format)>}
                        task = new U2UDMExecutorsTask(U2UDMExecutorsTask.SEND_FILE_INFO_QUERY_CHUNKS_SHA1_TASK,
                                new Object[] {event.getSource()});
                        //Object[] {protocolID = Protocol@<(PipeId(SocketAdv of remote peer) in String format)>}
//                        U2UDMExecutorsTask pc = new U2UDMExecutorsTask(U2UDMExecutorsTask.SEND_FILE_INFO_QUERY_PEER_CHUNKS_TASK,
//                                new Object[] {event.getSource()});

                        //task = new U2UDMExecutorsTask[] {cs};
                    }
                    //perform 2
                    else
                    {
                        //Object[] {protocolID = Protocol@<(PipeId(SocketAdv of remote peer) in String format)>}
                        task = new U2UDMExecutorsTask(U2UDMExecutorsTask.SEND_FILE_INFO_QUERY_PEER_CHUNKS_TASK,
                                new Object[] {event.getSource()});

                        //task = new U2UDMExecutorsTask[] {pc};
                    }
                                        
                }
                //401 need to wait
                else if(resNum.equals(U2UFSPResponseConnection.R401))
                {
                    //FIXME spino327@gmail.com, for the moment this do nothing, but the idea is
                    //that we tracking the 401 time, and wait for a 201 only for m [ms], or minutes
                }
                //5XX the remote peer can't attend us
                else
                {
                    //vetoing the remote peer for 10 minutes
                    findingSourcesTimerTask.resetTheStopVetoing();
                    //Protocol@<(PipeId(SocketAdv of remote peer) String format)> = 91 characteres always
                    String pipeId = ((String)event.getSource()).substring(10, 90);
                    failedRemoteAdvsSanctions.put(pipeId, 3);
                }

                break;
            }            

            case U2UFSProtocolResponse.RINFO:
            {
                U2UFSPResponseFileInfo rFI = (U2UFSPResponseFileInfo)res;
                String resNum = rFI.getResponseNumber();
                System.out.println("RINFO " + resNum);
                //checking the response number
                if(resNum.equals(U2UFSPResponseFileInfo.R221) ||
                        resNum.equals(U2UFSPResponseFileInfo.R222))
                {
                    byte qt = rFI.getQueryType();
                    //checking the query type
                    if(qt == U2UFSPResponseFileInfo.PEER_CHUNKS)
                    {
                        /*
                         * Object[] {protocolID = Protocol@<(PipeId(SocketAdv of remote peer) in String format)>,
                         * U2UFSPResponseFileInfo}
                         */
                         task =  new U2UDMExecutorsTask(U2UDMExecutorsTask.INCOMING_PEER_CHUNKS_INFO_TASK,
                                new Object[] {event.getSource(), rFI});
                    }
                    else if(qt == U2UFSPResponseFileInfo.CHUNKS_SHA1)
                    {
                        if(resNum.equals(U2UFSPResponseFileInfo.R221))
                        {
                            /*
                             * Object[] {protocolID = Protocol@<(PipeId(SocketAdv of remote peer) in String format)>
                             * U2UFSPResponseFileInfo}
                             */
                            task = new U2UDMExecutorsTask(U2UDMExecutorsTask.INCOMING_CHUNKS_SHA1_INFO_TASK,
                                    new Object[] {event.getSource(), rFI});
                        }
                    }
                }
                //431
                else if(resNum.equals(U2UFSPResponseFileInfo.R431))
                {
                    byte qt = rFI.getQueryType();
                    //checking the query type
                    if(qt == U2UFSPResponseFileInfo.PEER_CHUNKS)
                    {
                        //Object[] {protocolID = Protocol@<(PipeId(SocketAdv of remote peer) in String format)>}
                        task = new U2UDMExecutorsTask(U2UDMExecutorsTask.SEND_FILE_INFO_QUERY_PEER_CHUNKS_TASK,
                                new Object[] {event.getSource()});
                    }
                    else if((qt == U2UFSPResponseFileInfo.CHUNKS_SHA1) && !haveTheListOfChunksHash)
                    {
                        //Object[] {protocolID = Protocol@<(PipeId(SocketAdv of remote peer) in String format)>}
                        task = new U2UDMExecutorsTask(U2UDMExecutorsTask.SEND_FILE_INFO_QUERY_CHUNKS_SHA1_TASK,
                                new Object[] {event.getSource()});
                    }
                    else if(qt == U2UFSPResponseFileInfo.CHUNKS_SHA1)
                    {
                        //Object[] {protocolID = Protocol@<(PipeId(SocketAdv of remote peer) in String format)>}
                        task = new U2UDMExecutorsTask(U2UDMExecutorsTask.SEND_FILE_INFO_QUERY_PEER_CHUNKS_TASK,
                                new Object[] {event.getSource()});
                    }
                }
                //541
                else if(resNum.equals(U2UFSPResponseFileInfo.R541))
                {
                    //finish the connection
                }

                break;
            }
                
            case U2UFSProtocolResponse.RGETC:
            {
                U2UFSPResponseGetChunk rGC = (U2UFSPResponseGetChunk)res;
                String resNum = rGC.getResponseNumber();
                System.out.println("RGETC " + resNum);

                //checking 211 incoming data
                if(resNum.equals(U2UFSPResponseGetChunk.R211))
                {
                    /*
                     * Object[] {protocolID = Protocol@<(PipeId(SocketAdv of remote peer) in String format)>
                     * U2UFSPResponseGetChunk}
                     */
                    task = new U2UDMExecutorsTask(U2UDMExecutorsTask.INCOMING_CHUNKS_TASK,
                            new Object[] {event.getSource(), rGC});
                }
                //tracking the fails, 212-4XX-5XX, corrupt data
                else
                {
                    //FIXME spino327@gmail.com add support for fails
                    System.out.println(thinkerID + " tracking the fails, 212-4XX-5XX, corrupt data");
                }

                break;
            }
                
            case U2UFSProtocolResponse.RQUIT:
            {
                System.out.println("RQUIT");
                task = new U2UDMExecutorsTask(U2UDMExecutorsTask.INCOMING_QUIT_TASK,
                            new Object[] {event.getSource()});
                
                break;
            }
                
        }

        return task;
    }

    /**
     * contentAdvertisementEventProblem
     * @param res the response form the protocol, null if no changes in the HashMap of advs
     */
    private U2UDMExecutorsTask contentAdvertisementEventProblem(U2UContentDiscoveryEvent event)
    {
        ConcurrentHashMap<String, U2UContentAdvertisementImpl> newRemoteAdvs = new ConcurrentHashMap<String, U2UContentAdvertisementImpl>();

        //we know that the U2UContentDiscoveryEvent only have U2UContentAdvertisementImpl advertisements
        @SuppressWarnings("unchecked")
        Enumeration<U2UContentAdvertisementImpl> en = event.getResponseAdv();

        System.out.println("queryId = "+event.getQueryID()+" source = "+event.getSource());
        //add advertisements to the HashMap
        while(en.hasMoreElements())
        {
            U2UContentAdvertisementImpl temp = en.nextElement();
            //String key = temp.getSocketAdv().getDescription().substring(7);//peer name
            PipeAdvertisement pipeAdv = temp.getSocketAdv();
            String key = pipeAdv.getPipeID().toString();

            if(!remoteAdvs.containsKey(key))
            {
                boolean status = false;
                if(!failedRemoteAdvsSanctions.containsKey(key))
                {
                    status = true;
                }
                //how many sanctions have this key?, is it have least than tree, don't worry
                else if(failedRemoteAdvsSanctions.get(key).intValue() < 3)
                {
                    status = true;
                }

                if(status)
                {
                    remoteAdvs.put(key, temp);
                    newRemoteAdvs.put(key, temp);
                }
            }
            //how many sanctions have this key?, is it have least than tree, don't worry
            else if(failedRemoteAdvsSanctions.containsKey(key) &&
                    failedRemoteAdvsSanctions.get(key).intValue() >= 3)
            {
                //remove the adv from the remoteAdvs forever
                remoteAdvs.remove(key);
                System.out.println("the peer (" + key + " " + pipeAdv.getDescription() + ") was exclude from my list because reach 3 sanctions");
            }
        }

        //making the task, if new sourcer we find

        return (new U2UDMExecutorsTask(U2UDMExecutorsTask.INCOMING_NEW_SOURCES_TASK,
                new Object[] {remoteAdvs, newRemoteAdvs}));
    }

    /**
     * NEED_MORE_SOURCES_PROBLEM
     */
    private U2UDMExecutorsTask needMoreSourcesProblem()
    {
        return (new U2UDMExecutorsTask(U2UDMExecutorsTask.FINDING_SOURCES_TASK, new Object[0]));
    }

    /**
     * NEW_AVAILABLE_CHUNKS_EVENT_PROBLEM
     * @param protocolId identifier of the remote peer's connection
     * @param numNewChunks number of new available chunks that we can download from the remote peer
     * 
     * @return
     */
    private U2UDMExecutorsTask newAvailableChunksEventProblem(String protocolId, short numNewChunks)
    {
        //to have conscience, add the info at the HashMap
        //(before - downloaded) + new
        short before = 0;
        short downloaded = 0;
        if(numAvailableChunks.containsKey(protocolId))
        {
            short[] data = numAvailableChunks.get(protocolId);

            before = data[0];
            downloaded = data[1];
        }
        //(before - downloaded) + new = numAvailableChunks
        numAvailableChunks.put(protocolId, new short[] {(short) (before - downloaded + numNewChunks), 0});

        //the executor will try to download chunks from the remote peer
        return (new U2UDMExecutorsTask(U2UDMExecutorsTask.DOWNLOAD_RANDOM_CHUNK_TASK,
                new Object[] {protocolId}));
    }

    private U2UDMExecutorsTask downloadedChunkEventProblem(String protocolId)
    {
        //30% logic
        if(numAvailableChunks.containsKey(protocolId))
        {
            short[] data = numAvailableChunks.get(protocolId);
            float numAvaCh = data[0];
            float numDowCh = data[1];
            //checking 30% logic
            int numberChunks = Chunks.numberOfChunks(sha1);

            numDowCh++;
            //we doesn't need logic, so the remote peer have all the chunks
            if((numberChunks == numAvaCh) && (numberChunks > 0) && (numAvaCh > 0))
            {
                //numAvailableChunks
                numAvailableChunks.put(protocolId, new short[] {(short) numAvaCh, (short) numDowCh});
                
                //the executor will try to download chunks from the remote peer
                return (new U2UDMExecutorsTask(U2UDMExecutorsTask.DOWNLOAD_RANDOM_CHUNK_TASK,
                        new Object[] {protocolId}));
            }
            //we need the logic
            else if((numberChunks > 0) && (numAvaCh > 0))
            {
                System.out.println("--------\n" + thinkerID + " downloadedChunkEventProblem : numberDowCh = " +
                        numDowCh + ", numAvaCh = " + numAvaCh + ", numberChunks = " + numberChunks +
                        ", numDowCh/numAvaCh = " + (numDowCh/numAvaCh) + ", from " + protocolId + "\n--------");
                //numAvailableChunks
                numAvailableChunks.put(protocolId, new short[] {(short) numAvaCh, (short) numDowCh});

                //checking the logic
                if(numDowCh/numAvaCh >= 0.3f)
                {
                    System.out.println("--------\n" + thinkerID + " we download the 30% of the available chunks at remote peer " +
                            protocolId + "\n--------");
                    //the executor will try to request File Info peer's chunks from the remote peer
                    return (new U2UDMExecutorsTask(U2UDMExecutorsTask.SEND_FILE_INFO_QUERY_PEER_CHUNKS_TASK,
                            new Object[] {protocolId}));
                }
                //else
                //{
                //the executor will try to download chunks from the remote peer
                return (new U2UDMExecutorsTask(U2UDMExecutorsTask.DOWNLOAD_RANDOM_CHUNK_TASK,
                        new Object[] {protocolId}));
                //}
            }
        }

        return null;
    }

    /**
     * AVAILABLE_CHUNKS_SHA1_EVENT_PROBLEM
     * @return
     */
    private U2UDMExecutorsTask availableChunksSHA1EventProblem()
    {
        return (new U2UDMExecutorsTask(U2UDMExecutorsTask.SEND_TO_ALL_FILE_INFO_QUERY_PEER_CHUNKS_TASK,
                            new Object[0]));
    }
    /*private U2UDMExecutorsTask downloadedSharedFileEventProblem(String downloadingID, short numChunks)
    {
        U2UDMExecutorsTask task = null;

        //checking the own
        String own = thinkerID.substring(8/*Thinker< = 8*//*, thinkerID.length()-1);

        if(downloadingID.equals(own) && (numChunks > 0))
        {
            //checking the counts
            short numChunDown = 0;

            for(Map.Entry<String, short[]> in : numAvailableChunks.entrySet())
            {
                numChunDown = in.getValue()[1];
            }

            if(numChunks == (numChunDown + 1))
            {
                task = (new U2UDMExecutorsTask(U2UDMExecutorsTask.DOWNLOAD_RANDOM_CHUNK_TASK,
                            new Object[] {protocolId}));
            }
        }

        return task;
    }*/
    
    //--------EOHow to think in?

}
