/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.u2u.filesharing.downloadpeer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.PipeID;
import net.jxta.socket.JxtaSocket;
import org.u2u.common.db.Address;
import org.u2u.common.db.Chunks;
import org.u2u.common.db.Chunks_Add;
import org.u2u.common.db.SharedFiles;
import org.u2u.filesharing.U2UContentAdvertisementImpl;
import org.u2u.filesharing.U2UContentIdImpl;
import org.u2u.filesharing.U2UFileSharingService;
import org.u2u.filesharing.fsprotocol.U2UFSPResponseFileInfo;
import org.u2u.filesharing.fsprotocol.U2UFSPResponseGetChunk;
import org.u2u.filesharing.fsprotocol.U2UFileSharingProtocol;

/**
 * U2UDownloadingManager implementation for managing chunk's requests for a specific shared content
 * from remote peers that have it. This instance only care about one content's download
 * 
 * @author irene & sergio
 */
public class U2UDownloadingManager implements Runnable {

    //U2UDownloadingManager states
    /**
     * The downloading isn't running.
     */
    //public static final int DOWNLOADING_DISABLED = 10;

    /**
     * The downloading is running.
     */
    public static final int DOWNLOADING_RUNNING = 11;

    /**
     * The downloading finish the execution and stop himself.
     */
    public static final int DOWNLOADING_FINISHED = 12;

    /** Service Own of the U2UDownloadingManager instance*/
    private final U2UFileSharingService fss;
    /** U2UDownloadingManager's RequestManager for looking info in the network*/
    private final U2URequestManagerImpl requestManager;
    /** peer group = distributed environment*/
    private final PeerGroup group;
    /** Specific Content to download*/
    private final U2UContentAdvertisementImpl mainAdv;
    /** Thread Pool for manage the U2UFileSharingProtocols instances*/
    private final ThreadPoolExecutor fspPool;
    /** Thread Pool for manage the process*/
    private final ThreadPoolExecutor processPool;
    /**
     * References to the U2UFileSharingProtocols instances
     * 1) protocolID = Protocol@<(PipeId(SocketAdv of remote peer) in String format)>
     * 2) U2UFileSharingProtocol reference
     */
    private final HashMap<String, U2UFileSharingProtocol> fspReferences = new HashMap<String, U2UFileSharingProtocol>();
    /**
     * References to the Future instances that represents the submit of a U2UFileSharingProtocol
     * 1) protocolID = Protocol@<(PipeId(SocketAdv of remote peer) in String format)>
     * 2) Future reference
     */
    private final HashMap<String, Future<?>> fspFutures = new HashMap<String, Future<?>>();
    /** U2UDownloadingManager's Thinker*/
    private final U2UDMThinker thinker;
    /** TODO Queue*/
    private final LinkedBlockingQueue<U2UDMExecutorsTask> toDo;
    /** downloading's state, eg, DOWNLOADING_FINISHED*/
    private int downloadingState;
    /** Downloadign Manager's name or ID, Downloading@< cid >*/
    private final String downloadingID;

    private long initDownload;

    /**
     * represents the soTimeOut assigned to the sockets
     */
    private int soTimeOut;

    //about chunks
    /**
     * we have download at least one chunk
     */
    private boolean haveAtLeastOneChunk;

    /**
     * Build a instance of the U2UDownloadingManager that manage(handle) the download of the
     * specific Content from remote peers in a distributed environment.
     * @param service Instance of the U2UFileSharingService
     * @param adv Specific content to download from remote peers
     */
    public U2UDownloadingManager(U2UFileSharingService service, U2UContentAdvertisementImpl adv)
    {
        //checking the service
        if(service == null)
        {
            throw new NullPointerException("the service can't be null");
        }
        //checking the peer group
        else if(service.getGroup() == null)
        {
            throw new NullPointerException("the service's peer group can't be null");
        }
        //checking the adv
        else if(adv == null)
        {
            throw new NullPointerException("the adv can't be null");
        }
        else if((adv.getContentId() == null) ||
                (adv.getName() == null))
        {
            throw new IllegalArgumentException("the adv is in bad state");
        }

        fss = service;
        group = fss.getGroup();
        this.mainAdv = adv;
        //pool
        //try to read the Properties file
        int nConnections = 10;
        soTimeOut = 60000;
        File properties = new File("conf/.config.properties");
        if(properties.exists())
        {
            FileInputStream fis = null;
            try {
                
                fis = new FileInputStream(properties);
                Properties settings = new Properties();
                settings.load(fis);

                String cd = settings.getProperty("ConDown", "10");
                String sto = settings.getProperty("soTimeOut", "60000");
                
                nConnections = Integer.parseInt(cd);
                soTimeOut = Integer.parseInt(sto);

                if(nConnections < 0)
                {
                    nConnections = 10;
                }

                if(soTimeOut <= 0)
                {
                    soTimeOut = 60000;
                }
                
            } catch (NumberFormatException ex) {
                Logger.getLogger(U2UDownloadingManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(U2UDownloadingManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(U2UDownloadingManager.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    if(fis != null)
                    {
                        fis.close();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(U2UDownloadingManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        fspPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(nConnections);
        //fspPool = (ThreadPoolExecutor) Executors.newScheduledThreadPool(10);
        processPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);

        //toDo
        toDo = new LinkedBlockingQueue<U2UDMExecutorsTask>();

        //download ID
        String cid = this.mainAdv.getContentId().toString();
        downloadingID = "Downloading@<" + cid + ">";

        //thinker
        thinker = new U2UDMThinker(toDo, cid, downloadingID);
        //(new Thread(thinker, "Thinker@downloading-<"+ cid +">")).start();

        //add the thinker as listener of the requestManager
        requestManager = new U2URequestManagerImpl(fss);
        requestManager.addSearchListener(thinker);

        //haveAtLeastOneChunk?
        if(0.0f < Chunks.haveAllTheChunks(cid))
        {
            haveAtLeastOneChunk = true;
        }
        else
        {
            haveAtLeastOneChunk = false;
        }
        //

        System.out.println(downloadingID + " init with a core pool size = " + nConnections);
    }

    /**
     * return the progress of the download
     * @return
     */
    public int getProgress() {

        return (int)(Chunks.haveAllTheChunks(mainAdv.getContentId().toString())*100);
    }

    /**
     * Pause the download saving the state of them. nothing is lost
     * 
     * @return
     */
    public boolean pause()
    {
        //send to the Queue ToDo Finish
         //change the stste of the donwload
        this.downloadingState = DOWNLOADING_FINISHED;
        //send to the Queue ToDo Finish
        toDo.offer(new U2UDMExecutorsTask(U2UDMExecutorsTask.FINISH_MANAGER_TASK, new Object[0]));

        return true;
    }

    /**
     * Stop and remove the download
     *
     * @return 
     */
    public boolean stop()
    {
        //change the stste of the donwload
        this.downloadingState = DOWNLOADING_FINISHED;
        //send to the Queue ToDo Finish
        toDo.offer(new U2UDMExecutorsTask(U2UDMExecutorsTask.FINISH_MANAGER_TASK, new Object[0]));
        //Quit reference of the database U2UClient
        SharedFiles.delete((U2UContentIdImpl) mainAdv.getContentId());

        return true;
    }

    /**
     * This method force the downloading manager to find new sources.
     */
    public void forcingFindNewSources()
    {
        thinker.forcingFindNewSources();
    }

    /**
     * represents this instance of the DownloadingManager, like a hash
     * @return the Id of the DownloadingManager, Downloading@<sha1:XXXXXX>
     *
     */
    public String getDownloadingID()
    {
        return downloadingID;
    }

    /**
     * return the number of connections of this downloading manager
     * @return
     */
    public int getDownloadConnections()
    {
        return fspPool.getActiveCount();
    }


    //--------Executor Side

    /**
     * Executor Side of the U2UDownloadingManager, responsible for executing the tasks assigned to the
     * DownloadingManager himself
     */
    public void run()
    {
        //--------initialization of the Manager
        fspPool.purge();
        //we wake up from an old life? //already in the past we start to download this SharedFile?
        //expected number of chunks
        int numChunksAtTheDB = Chunks.numberOfChunks(mainAdv.getContentId().toString());
        short expectedNumOfChunks = (short)(mainAdv.getLength()/(mainAdv.getChunksize()*1024) + 1);

        if(expectedNumOfChunks == numChunksAtTheDB)
        {
            //tell the thinker that we have all the chunks' sha1
            thinker.setHaveTheListOfChunksHash();
        }

        //start the Thinker
        (new Thread(thinker, thinker.getThinkerID())).start();
        initDownload = System.currentTimeMillis();
        //finding sources for download the Content
        //locking the tracker
        //FIXME spino327@gmail.com using the tracker or not?

        //whitout tracker, loocking others adv with the same SHA-1

        //requestManager.searchContent("cid", mainAdv.getContentId().toString());
        downloadingState = DOWNLOADING_RUNNING;
        //needs at least one connection to start the download

        //--------EOInit

        //--------is RUNNING?
        
        while(downloadingState == DOWNLOADING_RUNNING)
        {
            try
            {
                //0. take a Task from the Queue
                U2UDMExecutorsTask task = toDo.take();//blocking
                System.out.println("-----------\n" + downloadingID + " in ToDO Queue = " + toDo.size() + "\n-----------");
                //1. Execute the task
                switch(task.getTaskType())
                {
                    case U2UDMExecutorsTask.FINDING_SOURCES_TASK:
                    {
                        System.out.println(downloadingID + " FINDING_SOURCES_TASK " + this.findingSourcesTask());
                        thinker.setAlreadyForcingFindSources();
                        break;
                    }

                    case U2UDMExecutorsTask.INCOMING_NEW_SOURCES_TASK:
                    {
                        //checking the args
                        Object args[] = task.getTaskData();
                        if(args.length == 2)
                        {
                            if((args[0] instanceof ConcurrentHashMap) && (args[1] instanceof ConcurrentHashMap))
                            {
                                //advertisements find
                                ConcurrentHashMap allAdvs = (ConcurrentHashMap) args[0];
                                ConcurrentHashMap newAdvs = (ConcurrentHashMap) args[1];

                                System.out.println(downloadingID + " active threads before = " + fspPool.getActiveCount());
                                System.out.println(downloadingID + " INCOMING_NEW_SOURCES_TASK " + this.connectingToNewSourcesTask(allAdvs, newAdvs));
                                System.out.println(downloadingID + " active threads after = " + fspPool.getActiveCount());

                                /*System.out.println("--------\n" + downloadingID + " we're connect with the next remote peers : ");
                                for(Map.Entry<String, U2UFileSharingProtocol> in : fspReferences.entrySet())
                                {
                                    System.out.println(in.getValue().getProtocolID());
                                }
                                System.out.println("--------");*/

                            }
                        }
                        break;
                    }

                    case U2UDMExecutorsTask.SEND_FILE_INFO_QUERY_CHUNKS_SHA1_TASK:
                    {
                        //checking the args
                        Object args[] = task.getTaskData();
                        if(args.length == 1)
                        {
                            if(args[0] instanceof String)
                            {
                                String protocolId = (String) args[0];

                                if(protocolId.startsWith("Protocol@<"))
                                {
                                    System.out.println(downloadingID + " SEND_FILE_INFO_QUERY_CHUNKS_SHA1_TASK to " +
                                            protocolId + " " +
                                            this.sendFileInfoQueryChunksSHA1Task(protocolId));
                                }
                            }
                        }

                        break;
                    }

                    case U2UDMExecutorsTask.SEND_FILE_INFO_QUERY_PEER_CHUNKS_TASK:
                    {
                        //checking the args
                        Object args[] = task.getTaskData();
                        if(args.length == 1)
                        {
                            if(args[0] instanceof String)
                            {
                                String protocolId = (String) args[0];

                                if(protocolId.startsWith("Protocol@<"))
                                {
                                    System.out.println(downloadingID + " SEND_FILE_INFO_QUERY_PEER_CHUNKS_TASK to " +
                                            protocolId + " " +
                                            this.sendFileInfoQueryPeerChunksTask(protocolId));
                                }
                            }
                        }

                        break;
                    }

                    case U2UDMExecutorsTask.SEND_TO_ALL_FILE_INFO_QUERY_PEER_CHUNKS_TASK:
                    {
                        //checking the args
                        Object args[] = task.getTaskData();
                        if(args.length == 0)
                        {
                            System.out.println(downloadingID + " SEND_TO_ALL_FILE_INFO_QUERY_PEER_CHUNKS_TASK " +
                                    this.sendToAllFileInfoQueryPeerChunksTask());
                        }
                        break;
                    }

                    case U2UDMExecutorsTask.INCOMING_CHUNKS_SHA1_INFO_TASK:
                    {
                        //checking the args
                        Object args[] =  task.getTaskData();
                        if(args.length == 2)
                        {
                            if((args[0] instanceof String) &&
                                    (args[1] instanceof U2UFSPResponseFileInfo))
                            {
                                String protocolId = (String) args[0];
                                U2UFSPResponseFileInfo res = (U2UFSPResponseFileInfo) args[1];

                                if(protocolId.startsWith("Protocol@<"))
                                {
                                    System.out.println(downloadingID + " INCOMING_CHUNKS_SHA1_INFO_TASK from " +
                                            protocolId + " " +
                                            this.incomingChunksSHA1InfoTask(protocolId, res));
                                }
                            }
                        }
                        
                        break;
                    }

                    case U2UDMExecutorsTask.INCOMING_PEER_CHUNKS_INFO_TASK:
                    {
                        //checking the args
                        Object args[] =  task.getTaskData();
                        if(args.length == 2)
                        {
                            if((args[0] instanceof String) &&
                                    (args[1] instanceof U2UFSPResponseFileInfo))
                            {
                                String protocolId = (String) args[0];
                                U2UFSPResponseFileInfo res = (U2UFSPResponseFileInfo) args[1];

                                if(protocolId.startsWith("Protocol@<"))
                                {
                                    System.out.println(downloadingID + " INCOMING_PEER_CHUNKS_INFO_TASK from " +
                                            protocolId + " " +
                                            this.incomingPeerChunksInfoTask(protocolId, res));
                                }
                            }
                        }

                        break;
                    }

                    case U2UDMExecutorsTask.DOWNLOAD_RANDOM_CHUNK_TASK:
                    {
                        //checking the args
                        Object args[] = task.getTaskData();
                        if(args.length == 1)
                        {
                            if(args[0] instanceof String)
                            {
                                String protocolId = (String) args[0];

                                if(protocolId.startsWith("Protocol@<"))
                                {
                                    System.out.println(downloadingID + " DOWNLOAD_RANDOM_CHUNK_TASK from " +
                                            protocolId + " " +
                                            this.downloadRandomChunkTask(protocolId));
                                }
                            }
                        }

                        break;
                    }

                    case U2UDMExecutorsTask.INCOMING_CHUNKS_TASK:
                    {
                        //checking the args
                        Object args[] = task.getTaskData();
                        if(args.length == 2)
                        {
                            if((args[0] instanceof String) &&
                                    (args[1] instanceof U2UFSPResponseGetChunk))
                            {
                                String protocolId = (String) args[0];
                                U2UFSPResponseGetChunk rGC = (U2UFSPResponseGetChunk) args[1];

                                if(protocolId.startsWith("Protocol@<"))
                                {
                                    System.out.println(downloadingID + " INCOMING_CHUNKS_TASK from " +
                                            protocolId + " " +
                                            this.incomingChunkTask(protocolId, rGC));
                                }
                            }
                        }
                        
                        break;
                    }

                    case U2UDMExecutorsTask.FINISH_THE_DOWNLOAD_TASK:
                    {
                        //checking the args
                        Object args[] = task.getTaskData();
                        if(args.length == 0)
                        {
                            System.out.println(downloadingID + " FINISH_THE_DOWNLOAD_TASK " +
                                    this.finishTheDownloadTask());
                        }

                        break;
                    }

                    case U2UDMExecutorsTask.INCOMING_QUIT_TASK:
                    {
                        //checking the args
                        Object args[] = task.getTaskData();
                        if(args.length == 1)
                        {
                            if(args[0] instanceof String)
                            {
                                String protocolId = (String) args[0];

                                if(protocolId.startsWith("Protocol@<"))
                                {
                                    System.out.println(downloadingID + " INCOMING_QUIT_TASK from " +
                                            protocolId + " " +
                                            this.incomingQuitTask(protocolId));
                                }
                            }
                        }

                        break;
                    }

                    case U2UDMExecutorsTask.FINISH_MANAGER_TASK:
                    {
                        System.out.println(downloadingID + " FINISH_TASK");
                        //Do nothing
                        break;
                    }
                }

            } catch (InterruptedException ex) {
                Logger.getLogger(U2UDownloadingManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //--------EORunning

        //--------FINISH

        thinker.stopThinking();

        //sending finish to the connecting protocols
        for(Map.Entry<String, U2UFileSharingProtocol> in : fspReferences.entrySet())
        {
            System.out.println(downloadingID + " sending finish to my partner " + in.getKey());
            in.getValue().finish();
        }

        //remove the chunks_add relationship
        System.out.println(downloadingID + " cleaning the chunks_add relationship = " + 
                Chunks_Add.delete(mainAdv.getContentId().toString()));

        //purge the pool
        for(Map.Entry<String, Future<?>> in : fspFutures.entrySet())
        {
            in.getValue().cancel(false);
        }
        

        fspFutures.clear();
        fspReferences.clear();
        toDo.clear();

        Runtime.getRuntime().gc();
 
        //--------EOFinish
    }

    //How to do it?

    /**
     * this task find new sources, sending a discovery request through the U2URequestManagerImpl, using
     * the SHA-1 of the Main Advertisement
     *
     * @return true if the query was send
     */
    private boolean findingSourcesTask()
    {
        return requestManager.searchContent("cid", mainAdv.getContentId().toString());
    }

    /**
     * this task try to connect to new sources, creating Sockets with the SocketAdv at the incoming Adv
     * and then create a new U2UFileSharingProtocol
     *
     * @param allAdvs all advs that comes from the thinker, have old and new advs
     * @param newAdvs only have the new advs than are arrived
     *
     * @return true if the Downloading was create new connections, false if the Downloading not need more
     * connections or all connections was failed
     */
    private boolean connectingToNewSourcesTask(final ConcurrentHashMap<String, U2UContentAdvertisementImpl> allAdvs,
            final ConcurrentHashMap<String, U2UContentAdvertisementImpl> newAdvs)
    {
        /*boolean status = false;
        
        //need more U2UFileSharingProtocols?
        int activeThreads = fspPool.getActiveCount();//getting the approximate number of threads that are actively executing tasks.

        if(activeThreads < fspPool.getCorePoolSize())
        {
            //based on the phrase of Albert
            //first trying to connect to the new Advertisements
            boolean r1 = tryToConnectWith(newAdvs, null);
            //second try with all of them
            boolean r2 = tryToConnectWith(allAdvs, newAdvs);

            status = r1 | r2;
        }

        return status;*/

        processPool.submit(new Runnable() {

            public void run()
            {
                //need more U2UFileSharingProtocols?
                int activeThreads = fspPool.getActiveCount();//getting the approximate number of threads that are actively executing tasks.

                if(activeThreads < fspPool.getCorePoolSize())
                {
                    //based on the phrase of Albert
                    //first trying to connect to the new Advertisements
                    boolean r1 = tryToConnectWith(newAdvs, null);
                    //second try with all of them
                    boolean r2 = tryToConnectWith(allAdvs, newAdvs);

                    if(r1 || r2)
                    {
                        System.out.println("--------\n" + downloadingID + " we're connect with the next remote peers : ");
                        for(Map.Entry<String, U2UFileSharingProtocol> in : fspReferences.entrySet())
                        {
                            System.out.println(in.getValue().getProtocolID());
                        }
                        System.out.println("--------");
                    }
                }
            }
        });

        System.out.println(downloadingID + " ProcessPool active threads = " + processPool.getActiveCount());

        return true;
    }

    /**
     * This task tell to the specific protocol that sends the two types of File Info orders
     * fileInfoQueryPeerChunks and fileInfoQueryChunksSha1
     * @param protocolID the protocol Id of the specific protocol instance
     * @return true if the queries was send
     */
    private boolean sendFileInfoQueryChunksSHA1Task(String protocolID)
    {
        boolean status = false;
        //checking if we have a connection with the specific protocol
        if(fspReferences.containsKey(protocolID))
        {
            U2UFileSharingProtocol protocol = fspReferences.get(protocolID);

            //send fileInfoQueryChunksSha1
            status = protocol.fileInfoQueryChunksSha1();

            System.out.println(downloadingID + " protocol " + protocolID +
                    " SEND_FILE_INFO_QUERY_CHUNKS_SHA1_TASK");
        }
        else
        {
            throw new IllegalArgumentException("this DownloadingManager instance haven't a connection with the specific PipeID");
        }

        return status;
    }

    /**
     * This task tell to the specific protocol that send the type of File Info order
     * fileInfoQueryPeerChunks
     * @param protocolID the protocol Id of the specific protocol instance
     * @return true if the query was send
     */
    private boolean sendFileInfoQueryPeerChunksTask(String protocolID)
    {
        boolean status = false;
        //checking if we have a connection with the specific protocol
        if(fspReferences.containsKey(protocolID))
        {
            U2UFileSharingProtocol protocol = fspReferences.get(protocolID);

            //send fileInfoQueryPeerChunks
            status = protocol.fileInfoQueryPeerChunks();

            System.out.println(downloadingID + " protocol " + protocolID +
                    " SEND_FILE_INFO_QUERY_PEER_CHUNKS_TASK");
        }
        else
        {
            throw new IllegalArgumentException("this DownloadingManager instance haven't a connection with the specific PipeID");
        }

        return status;
    }

    /**
     * This task tell to the specific protocol that send the type of File Info order
     * fileInfoQueryPeerChunks
     * @param protocolID the protocol Id of the specific protocol instance
     * @return true if the query was send
     */
    private boolean sendToAllFileInfoQueryPeerChunksTask()
    {
        boolean status = false;

        //loop

        for(Map.Entry<String, U2UFileSharingProtocol> in : fspReferences.entrySet())
        {
            U2UFileSharingProtocol protocol = in.getValue();
            //checking state, need to be running, because it can be waiting in a response 401 od CONN
            if(protocol.getProtocolState() == U2UFileSharingProtocol.PROTOCOL_RUNNING)
            {
                //send fileInfoQueryPeerChunks
                status = status & protocol.fileInfoQueryPeerChunks();

                System.out.println(downloadingID + " protocol " + protocol.getProtocolID() +
                    " SEND_FILE_INFO_QUERY_PEER_CHUNKS_TASK");
            }
        }

        return status;
    }
    /**
     * this task register in the database the chunks' SHA1 of the specific content.
     * 
     * @param protocolID the protocol Id of the specific protocol instance
     * @param response
     * @return true if the information was register
     */
    private boolean incomingChunksSHA1InfoTask(String protocolId, final U2UFSPResponseFileInfo response)
    {
        /*boolean status = false;
        //checking the response info
        if(response.getResponseNumber().equals(U2UFSPResponseFileInfo.R221))
        {
            String sha1_sf = mainAdv.getContentId().toString();
            //exist?
            //FIXME spino327@gmail.com we need to verify that was reived 
            if(Chunks.numberOfChunks(sha1_sf) == 0)
            {
                //expected number of chunks
                short expectedNumOfChunks = (short)(mainAdv.getLength()/(mainAdv.getChunksize()*1024) + 1);
                short incomingNumOfChunks = 0;
                //register in the database
                HashMap<Short, byte[]> hashList = (HashMap) response.getList();
                status = true;

                for(Map.Entry<Short, byte[]> in : hashList.entrySet())
                {
                    String sha1_chunk = U2UContentIdImpl.hashToString(in.getValue());
                    short pos_chunk = in.getKey();
                    status = status & Chunks.create(sha1_sf, sha1_chunk, pos_chunk, false);

                    if(status)
                    {
                      incomingNumOfChunks++;
                    }
                }

                //checking
                if(expectedNumOfChunks == incomingNumOfChunks)
                {
                    //tell the thinker
                    thinker.setHaveTheListOfChunksHash();
                    //request file info peer's chunks to all
                    thinker.availableChunksSHA1Event();
                }
                else
                {
                    //remove the inserted chunks. because an error
                    Chunks.deleteAllChunks(sha1_sf);
                }
            }
        }

        return status;*/

        processPool.submit(new Runnable() {

            public void run()
            {
                boolean status = false;

                //checking the response info
                if(response.getResponseNumber().equals(U2UFSPResponseFileInfo.R221))
                {
                    String sha1_sf = mainAdv.getContentId().toString();
                    //exist?
                    //FIXME spino327@gmail.com we need to verify that was reived
                    if(Chunks.numberOfChunks(sha1_sf) == 0)
                    {
                        //expected number of chunks
                        short expectedNumOfChunks = (short)(mainAdv.getLength()/(mainAdv.getChunksize()*1024) + 1);
                        short incomingNumOfChunks = 0;
                        //register in the database
                        HashMap<Short, byte[]> hashList = (HashMap) response.getList();
                        status = true;

                        for(Map.Entry<Short, byte[]> in : hashList.entrySet())
                        {
                            String sha1_chunk = U2UContentIdImpl.hashToString(in.getValue());
                            short pos_chunk = in.getKey();
                            status = status & Chunks.create(sha1_sf, sha1_chunk, pos_chunk, false);

                            if(status)
                            {
                                incomingNumOfChunks++;
                            }
                        }

                        //checking
                        if(expectedNumOfChunks == incomingNumOfChunks)
                        {
                            //tell the thinker
                            thinker.setHaveTheListOfChunksHash();
                            //request file info peer's chunks to all
                            thinker.availableChunksSHA1Event();
                        }
                        else
                        {
                            //remove the inserted chunks. because an error
                            Chunks.deleteAllChunks(sha1_sf);
                        }
                    }
                }
            }
        });

        System.out.println(downloadingID + " ProcessPool active threads = " + processPool.getActiveCount());

        return true;
    }

    /**
     * this task register in the database the remote peer's chunks.
     *
     * @param protocolID the protocol Id of the specific protocol instance
     * @param response
     * @return true if the information was register
     */
    private boolean incomingPeerChunksInfoTask(final String protocolId, final U2UFSPResponseFileInfo response)
    {
        /*boolean status = true;
        //if the pipeId isn't in the Database, so register it.
        //getting the pipeId from the protocolId, Protocol@<(PipeId(SocketAdv of remote peer) in String format)>
        String pipeId = protocolId.substring(10, protocolId.length()-1);
        if(!Address.exist(pipeId))
        {
            status = status & Address.create(pipeId);
        }

        if(status)
        {
            ArrayList list = (ArrayList) response.getList();

            int id_add = Address.pipeIdToIDADD(pipeId);

            //checking
            if((list.get(0) instanceof Character) && (list.get(1) instanceof short[]))
            {
                Character st = (Character) list.get(0);

                String sha1_sf = mainAdv.getContentId().toString();
                int numChunks = Chunks.numberOfChunks(sha1_sf);
                short expectedNumOfChunks = (short)(mainAdv.getLength()/(mainAdv.getChunksize()*1024) + 1);
                //checking the response number
                //R221
                if(response.getResponseNumber().equals(U2UFSPResponseFileInfo.R221))
                {
                    //register the info in the table Chunks_Add
                    if(st.equals('T'))
                    {
                        //FIXME spino327@gmail.com maybe is necesary check if the short[] size is equals to
                        //filesize/chunksize

                        //checking have the chunks' sha1 list?
                        if((expectedNumOfChunks == numChunks) && (thinker.isHaveTheListOfChunksHash()))
                        {
                            short numNewAvailableChunks = 0;
                            short[] allPos = new short[numChunks];

                            for(short i = 0; i < numChunks; i++)
                            {
                                allPos[i] = i;
                            }

                            //register the info in the table Chunks_Add, thinking in the memory
                            for(int i = 0; i < (allPos.length / 100 + 1); i++)
                            {
                                int len = (i < (allPos.length / 100) ? 100 : allPos.length - 100*i);
                                //String[] sha1_chunks = Chunks.chunkPosToChunkSha1(sha1_sf, allPos);
                                numNewAvailableChunks += (short) (Chunks_Add.create(id_add,
                                        Chunks.chunkPosToChunkSha1(sha1_sf, allPos, i*100, len)));
                            }

                            if(numNewAvailableChunks >= 0)
                            {
                                //sending the NEW_AVAILABLE_CHUNKS_EVENT_PROBLEM to the Thinker
                                thinker.newAvailableChunksEvent(protocolId, numNewAvailableChunks);
                            }
                        }
                    }
                    else
                    {
                        status = false;
                    }
                }
                //R222
                else if(response.getResponseNumber().equals(U2UFSPResponseFileInfo.R222))
                {
                    //checking
                    if(st.equals('T') || st.equals('F'))
                    {
                        //checking have the chunks' sha1 list?
                        if((expectedNumOfChunks == numChunks) && (thinker.isHaveTheListOfChunksHash()))
                        {
                            //parsing the response, getting the chunks that the remote peer have
                            short[] pos;
                            short numNewAvailableChunks = 0;

                            if(st.equals('F'))
                            {
                                pos = (short[]) list.get(1);
                                //return the chunks that the remote peer have
                                pos = Chunks.getReverseChunksPositions(sha1_sf, pos);
                            }
                            else
                            {
                                pos = (short[]) list.get(1);
                            }

                            //register the info in the table Chunks_Add
                            //String[] sha1_chunks = Chunks.chunkPosToChunkSha1(sha1_sf, pos);
                            //numNewAvailableChunks = (short) Chunks_Add.create(id_add, sha1_chunks);
                            for(int i = 0; i < (pos.length / 100 + 1); i++)
                            {
                                int len = (i < (pos.length / 100) ? 100 : pos.length - 100*i);
                                //String[] sha1_chunks = Chunks.chunkPosToChunkSha1(sha1_sf, allPos);
                                numNewAvailableChunks += (short) (Chunks_Add.create(id_add,
                                        Chunks.chunkPosToChunkSha1(sha1_sf, pos, i*100, len)));
                            }

                            if(numNewAvailableChunks >= 0)
                            {
                                //sending the NEW_AVAILABLE_CHUNKS_EVENT_PROBLEM to the Thinker
                                thinker.newAvailableChunksEvent(protocolId, numNewAvailableChunks);
                            }
                        }
                        
                    }
                    else
                    {
                        status = false;
                    }
                } 
            }
            else
            {
                status = false;
            }
        }

        //gc
        Runtime.getRuntime().gc();
        
        return status;*/

        processPool.submit(new Runnable() {

            public void run()
            {
                boolean status = true;
                //if the pipeId isn't in the Database, so register it.
                //getting the pipeId from the protocolId, Protocol@<(PipeId(SocketAdv of remote peer) in String format)>
                String pipeId = protocolId.substring(10, protocolId.length()-1);
                if(!Address.exist(pipeId))
                {
                    status = status & Address.create(pipeId);
                }

                if(status)
                {
                    ArrayList list = (ArrayList) response.getList();

                    int id_add = Address.pipeIdToIDADD(pipeId);

                    //checking
                    if((list.get(0) instanceof Character) && (list.get(1) instanceof short[]))
                    {
                        Character st = (Character) list.get(0);

                        String sha1_sf = mainAdv.getContentId().toString();
                        int numChunks = Chunks.numberOfChunks(sha1_sf);
                        short expectedNumOfChunks = (short)(mainAdv.getLength()/(mainAdv.getChunksize()*1024) + 1);
                        //checking the response number
                        //R221
                        if(response.getResponseNumber().equals(U2UFSPResponseFileInfo.R221))
                        {
                            //register the info in the table Chunks_Add
                            if(st.equals('T'))
                            {
                                //FIXME spino327@gmail.com maybe is necesary check if the short[] size is equals to
                                //filesize/chunksize

                                //checking have the chunks' sha1 list?
                                if((expectedNumOfChunks == numChunks) && (thinker.isHaveTheListOfChunksHash()))
                                {
                                    short numNewAvailableChunks = 0;
                                    short[] allPos = new short[numChunks];

                                    for(short i = 0; i < numChunks; i++)
                                    {
                                        allPos[i] = i;
                                    }

                                    //register the info in the table Chunks_Add, thinking in the memory
                                    for(int i = 0; i < (allPos.length / 100 + 1); i++)
                                    {
                                        int len = (i < (allPos.length / 100) ? 100 : allPos.length - 100*i);
                                        //String[] sha1_chunks = Chunks.chunkPosToChunkSha1(sha1_sf, allPos);
                                        numNewAvailableChunks += (short) (Chunks_Add.create(id_add,
                                                Chunks.chunkPosToChunkSha1(sha1_sf, allPos, i*100, len)));
                                    }

                                    if(numNewAvailableChunks >= 0)
                                    {
                                        //sending the NEW_AVAILABLE_CHUNKS_EVENT_PROBLEM to the Thinker
                                        thinker.newAvailableChunksEvent(protocolId, numNewAvailableChunks);
                                    }
                                }
                            }
                            else
                            {
                                status = false;
                            }
                        }
                        //R222
                        else if(response.getResponseNumber().equals(U2UFSPResponseFileInfo.R222))
                        {
                            //checking
                            if(st.equals('T') || st.equals('F'))
                            {
                                //checking have the chunks' sha1 list?
                                if((expectedNumOfChunks == numChunks) && (thinker.isHaveTheListOfChunksHash()))
                                {
                                    //parsing the response, getting the chunks that the remote peer have
                                    short[] pos;
                                    short numNewAvailableChunks = 0;

                                    if(st.equals('F'))
                                    {
                                        pos = (short[]) list.get(1);
                                        //return the chunks that the remote peer have
                                        pos = Chunks.getReverseChunksPositions(sha1_sf, pos);
                                    }
                                    else
                                    {
                                        pos = (short[]) list.get(1);
                                    }

                                    //register the info in the table Chunks_Add
                                    //String[] sha1_chunks = Chunks.chunkPosToChunkSha1(sha1_sf, pos);
                                    //numNewAvailableChunks = (short) Chunks_Add.create(id_add, sha1_chunks);
                                    for(int i = 0; i < (pos.length / 100 + 1); i++)
                                    {
                                        int len = (i < (pos.length / 100) ? 100 : pos.length - 100*i);
                                        //String[] sha1_chunks = Chunks.chunkPosToChunkSha1(sha1_sf, allPos);
                                        numNewAvailableChunks += (short) (Chunks_Add.create(id_add,
                                                Chunks.chunkPosToChunkSha1(sha1_sf, pos, i*100, len)));
                                    }

                                    if(numNewAvailableChunks >= 0)
                                    {
                                        //sending the NEW_AVAILABLE_CHUNKS_EVENT_PROBLEM to the Thinker
                                        thinker.newAvailableChunksEvent(protocolId, numNewAvailableChunks);
                                    }
                                }

                            }
                            else
                            {
                                status = false;
                            }
                        }
                    }
                    else
                    {
                        status = false;
                    }
                }

                //gc
                Runtime.getRuntime().gc();
            }
        });

        System.out.println(downloadingID + " ProcessPool active threads = " + processPool.getActiveCount());

        return true;
    }

    /**
     * this task try to download chunks from the remote peer identified by the protocolId
     *
     * @param protocolId the protocol Id of the specific protocol instance
     * @return true if the order getc was send successfully
     */
    private boolean downloadRandomChunkTask(final String protocolId)
    {
        /*
        boolean status = false;

        //1. we make the intersection between the chunks that we need and those that the remote peer has available
        // and then we choose randomly one chunk to download
        //getting the pipeId from the protocolId, Protocol@<(PipeId(SocketAdv of remote peer) in String format)>
        String pipeId = protocolId.substring(10, protocolId.length()-1);
        short randomChunk = Chunks.getRandomChunkToDownload(mainAdv.getContentId().toString(), pipeId);

        if(randomChunk > -1)
        {
            status = true;
            //2. send getc
            U2UFileSharingProtocol protocol = fspReferences.get(protocolId);
            status = status & protocol.requestChunk(randomChunk);
        }
        //FIXME missing option, if not send nothing?

        return status;*/

        processPool.submit(new Runnable() {

            public void run()
            {
                boolean status = false;

                //1. we make the intersection between the chunks that we need and those that the remote peer has available
                // and then we choose randomly one chunk to download
                //getting the pipeId from the protocolId, Protocol@<(PipeId(SocketAdv of remote peer) in String format)>
                String pipeId = protocolId.substring(10, protocolId.length()-1);
                short randomChunk = Chunks.getRandomChunkToDownload(mainAdv.getContentId().toString(), pipeId);

                if(randomChunk > -1)
                {
                    status = true;
                    //2. send getc
                    U2UFileSharingProtocol protocol = fspReferences.get(protocolId);
                    status = status & protocol.requestChunk(randomChunk);
                }
                //FIXME missing option, if not send nothing?
                else
                {
                    System.out.println("--------\n" + protocolId + " RANDOMCHUNK = -1 \n--------");
                }
            }
        });

        System.out.println(downloadingID + " ProcessPool active threads = " + processPool.getActiveCount());

        return true;
    }

    /**
     * this task register in the database the download of a chunks.
     *
     * @param protocolID the protocol Id of the specific protocol instance
     * @param response
     * @return true if the information was register
     */
    private boolean incomingChunkTask(final String protocolId, final U2UFSPResponseGetChunk response)
    {
        /*boolean status = true;
        //if the pipeId isn't in the Database, so register it.
        //getting the pipeId from the protocolId, Protocol@<(PipeId(SocketAdv of remote peer) in String format)>
        //String pipeId = protocolId.substring(10, protocolId.length()-1);

        //checking the chunk's data
        byte[] chunkData = response.getChunkBytes();

        if((chunkData != null) &&
                (chunkData.length > 0))
        {
            //calculating the SHA-1 of the incoming chunk's data
            String incomingSha1 = U2UContentIdImpl.getChunkSHA1(chunkData);

            String sha1_sf = mainAdv.getContentId().toString();
            //corrupt data?
            if(Chunks.exist(sha1_sf, incomingSha1))
            {
                boolean serializeIt = false;
                //getting the index of the incoming chunk
                short index =  Chunks.chunkSha1ToChunkPos(sha1_sf, incomingSha1);

                //serializing the data.
                File chunksFolder = new File("conf/.chunks");
                File chunk = new File("conf/.chunks/" + sha1_sf.substring(5) + "-" + index);

                if(!chunksFolder.exists())
                {
                    chunksFolder.mkdir();
                }

                if(chunk.exists())
                {
                    //delete it
                    if(chunk.delete())
                    {
                        serializeIt = true;
                    }
                }
                else
                {
                    serializeIt = true;
                }

                if(serializeIt)
                {
                    //serializing the data.
                    FileOutputStream fos = null;
                    try {
                        
                        fos = new FileOutputStream(chunk);
                        fos.write(chunkData);

                        //register the download at the Chunks table
                        if(Chunks.registerChunkDownload(sha1_sf, incomingSha1))
                        {

                            //FIXME spino327@gmail.com SEND ACKW 'T'
                            //U2UFileSharingProtocol protocol = fspReferences.get(protocolId);
                            //status = status & protocol.sendAcknowledge(true);

                            //checking if we download all the chunks
                            if(Chunks.haveAllTheChunks(sha1_sf) == 1.0f)
                            {
                                //send to the Queue ToDo Finish
                                toDo.offer(new U2UDMExecutorsTask(U2UDMExecutorsTask.FINISH_THE_DOWNLOAD_TASK, new Object[0]));
                            }
                            //calling downloadedChunkEvent
                            else
                            {
                                thinker.downloadedChunkEvent(protocolId);

                                //haveAtLeastOneChunk? if we haven't at least one chunk, and because in this method
                                //we register a incoming downloaded chunk, so we publish the content because
                                //because we finished fulfilling the "almost truths" logic
                                if(!haveAtLeastOneChunk)
                                {
                                    System.out.println("--------\nFSS and " + downloadingID + 
                                            " fulfilling the 'almost truths' logic, so publish the advertisment = " + 
                                            fss.shareIncompleteFile(mainAdv) + "\n--------");

                                    haveAtLeastOneChunk = true;//only can in at this if once.
                                }
                            }
                        }

                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(U2UDownloadingManager.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(U2UDownloadingManager.class.getName()).log(Level.SEVERE, null, ex);
                    }finally {
                        try {
                            if(fos != null)
                            {
                                fos.close();
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(U2UDownloadingManager.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
            //corrupt data
            else
            {
                //calling protocolErrorEvent

                //FIXME spino327@gmail.com SEND ACKW 'F'
                //U2UFileSharingProtocol protocol = fspReferences.get(protocolId);
                //protocol.sendAcknowledge(false);

                status = false;
            }
        }
        //corrupt data
        else
        {
            //calling protocolErrorEvent

            //FIXME spino327@gmail.com SEND ACKW 'F'
            //U2UFileSharingProtocol protocol = fspReferences.get(protocolId);
            //protocol.sendAcknowledge(false);

            status = false;
        }

        return status;*/

        processPool.submit(new Runnable() {

            public void run()
            {
                boolean status = true;
                //if the pipeId isn't in the Database, so register it.
                //getting the pipeId from the protocolId, Protocol@<(PipeId(SocketAdv of remote peer) in String format)>
                //String pipeId = protocolId.substring(10, protocolId.length()-1);

                //checking the chunk's data
                byte[] chunkData = response.getChunkBytes();

                if((chunkData != null) &&
                        (chunkData.length > 0))
                {
                    //calculating the SHA-1 of the incoming chunk's data
                    String incomingSha1 = U2UContentIdImpl.getChunkSHA1(chunkData);

                    String sha1_sf = mainAdv.getContentId().toString();
                    //corrupt data?
                    if(Chunks.exist(sha1_sf, incomingSha1))
                    {
                        boolean serializeIt = false;
                        //getting the index of the incoming chunk
                        short index =  Chunks.chunkSha1ToChunkPos(sha1_sf, incomingSha1);

                        //serializing the data.
                        File chunksFolder = new File("conf/.chunks");
                        File chunk = new File("conf/.chunks/" + sha1_sf.substring(5) + "-" + index);

                        if(!chunksFolder.exists())
                        {
                            chunksFolder.mkdir();
                        }

                        if(chunk.exists())
                        {
                            //delete it
                            if(chunk.delete())
                            {
                                serializeIt = true;
                            }
                        }
                        else
                        {
                            serializeIt = true;
                        }

                        if(serializeIt)
                        {
                            //serializing the data.
                            FileOutputStream fos = null;
                            try {

                                fos = new FileOutputStream(chunk);
                                fos.write(chunkData);

                                //register the download at the Chunks table
                                if(Chunks.registerChunkDownload(sha1_sf, incomingSha1))
                                {

                                    //FIXME spino327@gmail.com SEND ACKW 'T'
                                    //U2UFileSharingProtocol protocol = fspReferences.get(protocolId);
                                    //status = status & protocol.sendAcknowledge(true);

                                    //checking if we download all the chunks
                                    if(Chunks.haveAllTheChunks(sha1_sf) == 1.0f)
                                    {
                                        //send to the Queue ToDo Finish
                                        toDo.offer(new U2UDMExecutorsTask(U2UDMExecutorsTask.FINISH_THE_DOWNLOAD_TASK, new Object[0]));
                                    }
                                    //calling downloadedChunkEvent
                                    else
                                    {
                                        thinker.downloadedChunkEvent(protocolId);

                                        //haveAtLeastOneChunk? if we haven't at least one chunk, and because in this method
                                        //we register a incoming downloaded chunk, so we publish the content because
                                        //because we finished fulfilling the "almost truths" logic
                                        if(!haveAtLeastOneChunk)
                                        {
                                            System.out.println("--------\nFSS and " + downloadingID +
                                                    " fulfilling the 'almost truths' logic, so publish the advertisment = " +
                                                    fss.shareIncompleteFile(mainAdv) + "\n--------");

                                            haveAtLeastOneChunk = true;//only can in at this if once.
                                        }
                                    }
                                }

                            } catch (FileNotFoundException ex) {
                                Logger.getLogger(U2UDownloadingManager.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (IOException ex) {
                                Logger.getLogger(U2UDownloadingManager.class.getName()).log(Level.SEVERE, null, ex);
                            }finally {
                                try {
                                    if(fos != null)
                                    {
                                        fos.close();
                                    }
                                } catch (IOException ex) {
                                    Logger.getLogger(U2UDownloadingManager.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        }
                    }
                    //corrupt data
                    else
                    {
                        //calling protocolErrorEvent

                        //FIXME spino327@gmail.com SEND ACKW 'F'
                        //U2UFileSharingProtocol protocol = fspReferences.get(protocolId);
                        //protocol.sendAcknowledge(false);

                        status = false;
                    }
                }
                //corrupt data
                else
                {
                    //calling protocolErrorEvent

                    //FIXME spino327@gmail.com SEND ACKW 'F'
                    //U2UFileSharingProtocol protocol = fspReferences.get(protocolId);
                    //protocol.sendAcknowledge(false);

                    status = false;
                }
            }
        });

        System.out.println(downloadingID + " ProcessPool active threads = " + processPool.getActiveCount());

        return true;
    }

    /**
     * this task finish the download of the shared file.
     *
     * @return true if the was successful
     */
    private boolean finishTheDownloadTask()
    {
        //checking again
        String sha1_sf = mainAdv.getContentId().toString();
        //checking if we download all the chunks
        if(Chunks.haveAllTheChunks(sha1_sf) == 1.0f)
        {
            short nchunks = (short) Chunks.numberOfChunks(sha1_sf);
            sha1_sf = sha1_sf.substring(5);//sha1: = 5

            FileInputStream fChunkis = null;
            FileInputStream fis_sf = null;
            FileOutputStream fos_sf = null;

            try {

                File sharedFolder = new File("Shared");
                File sharedFile = new File("Shared/" + mainAdv.getName());
                //making the Shared file from the chunks
                
                if(!sharedFolder.exists())
                {
                    sharedFolder.mkdir();
                }

                fos_sf = new FileOutputStream(sharedFile);

                for(int i = 0; i < nchunks; i++)
                {
                    File fileChunk = new File("conf/.chunks/" + sha1_sf + "-" + i);
                    fChunkis = new FileInputStream(fileChunk);

                    //ByteBuffer bufferChunk = fChunkis.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, fileChunk.length());
                    byte[] bChunk = new byte[(int) fileChunk.length()];
                    //bufferChunk.get(b);
                    System.out.println(downloadingID + " read <" + fChunkis.read(bChunk) + ">, expected <" + bChunk.length + ">");

                    fChunkis.close();

                    fos_sf.write(bChunk);
                }

                //closing


                //checking the SHA-1 of the resulting file
                fis_sf = new FileInputStream(sharedFile);
                U2UContentIdImpl cid = new U2UContentIdImpl(fis_sf);
                System.out.println(downloadingID + " resulting file's SHA-1 digest = " + cid.toString().substring(5) +
                        ", expected SHA-1 digest = " + sha1_sf);

                if(mainAdv.getContentId().equals(cid))
                {
                    //register the successful download at the DB
                    if(SharedFiles.registerSharedFileDownload(cid))
                    {
                        //remove all the chunks
                        /*for(int i = 0; i < nchunks; i++)
                        {
                            File fileChunk = new File("conf/.chunks/" + sha1_sf + "-" + i);
                            
                            System.out.println(downloadingID + " remove chunk(" + i + ") = " + fileChunk.delete());
                        }*/

                        this.pause();
                        System.out.println(downloadingID + " thanks for using the U2UFileSharingService! your download was successful " + (System.currentTimeMillis() - initDownload));
                        JOptionPane.showMessageDialog(null, downloadingID + " \nthanks for using the U2UFileSharingService! " +
                                "your download was successful in [ms] = " + (System.currentTimeMillis() - initDownload));

                    }
                }


            } catch (FileNotFoundException ex) {
                Logger.getLogger(U2UDownloadingManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(U2UDownloadingManager.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    if(fChunkis != null)
                    {
                        fChunkis.close();
                    }

                    if(fos_sf != null)
                    {
                        fos_sf.close();
                    }

                    if(fis_sf != null)
                    {
                        fis_sf.close();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(U2UDownloadingManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }  
        }

        return false;
    }

    /**
     * this task finish the execution of the specify protocol
     *
     * @param protocolID the protocol Id of the specific protocol instance
     * @return true if the information was finished
     */
    private boolean incomingQuitTask(String protocolId)
    {
        boolean status = false;

        //checking if the protocol exists?
        if(fspReferences.containsKey(protocolId))
        {
            //1. remove the protocol from the fspReferences hash map
            fspReferences.remove(protocolId);

            //2. cancel the future at the pool
            (fspFutures.get(protocolId)).cancel(true);
            fspFutures.remove(protocolId);
            fspPool.purge();

            status = true;
        }
       
        return status;
    }

    //EOHow to do it?

    //--------EOExecutor Side

    //Tools
    /**
     * it try to connect with the given advertisments
     * @param advsToConnect the HashMap with we try to connect
     * @param excludeThis the adv that we know that need to be exclude from the advsToConnect
     */
    private boolean tryToConnectWith(ConcurrentHashMap<String, U2UContentAdvertisementImpl> advsToConnect,
            ConcurrentHashMap<String, U2UContentAdvertisementImpl> excludeThis)
    {
        boolean anyConnection = false;
        boolean anyFailedPeer = false;
        
        Iterator<U2UContentAdvertisementImpl> it = advsToConnect.values().iterator();
        while (it.hasNext() &&
                (fspPool.getActiveCount() < fspPool.getCorePoolSize()))
        {
            //getting the U2UContentAdvertisementImpl
            U2UContentAdvertisementImpl adv = it.next();
            
            //already exists a connection with this SocketAdv?
            PipeID remotePipeId = (PipeID) adv.getSocketAdv().getPipeID();
            if (!fspReferences.containsKey("Protocol@<" + remotePipeId.toString() + ">"))
            {
                //more exclude? if exludeThis == null then we can try to make a connect with the remote peer
                //else we try if the adv isn't in the excludeThis HashMap
                if((excludeThis != null? !excludeThis.containsValue(adv) : true))
                {
                    //FIXME spino327@gmail.com maybe is important add the peerId
                    //creating the socket
                    JxtaSocket socket = null;
                    try
                    {
                        socket = new JxtaSocket(group, null, adv.getSocketAdv(), 3000, true);
                        //setting the soTimeOut
                        socket.setSoTimeout(soTimeOut);
                        System.out.println("---------------- ACTIVE SOCKET : soTimeOut for remote socket --------------------");
                        System.out.println("socket sotimeout = " + socket.getSoTimeout());
                        
                    } catch (IOException ex)
                    {
                        //remove this failed peer's adv form the list of all advs, only whend we pass al the advs
                        /*if((ex instanceof SocketTimeoutException) &&
                                (excludeThis != null))*/
                        
                        //String failedPeer = adv.getSocketAdv().getDescription();//SocketName
                        String failedPeer = adv.getSocketAdv().getPipeID().toString();//PipeID
                        System.out.println("---------------------------FAILED PEER--------------------------------------");
                        System.out.println(downloadingID + " remove failed peer (" + failedPeer + " " +
                                adv.getSocketAdv().getDescription() + ")'s adv from the list of all and add to the list of failed");

                        //add to failed peers
                        //failedPeer = failedPeer.substring(7);//peer name
                        if(thinker.failedRemoteAdvsSanctions.containsKey(failedPeer))
                        {
                            int n = thinker.failedRemoteAdvsSanctions.get(failedPeer).intValue() + 1;
                            thinker.failedRemoteAdvsSanctions.put(failedPeer, n);

                            System.out.println(downloadingID + " failed peer " + failedPeer + " " + adv.getSocketAdv().getDescription() +" have " + n + " sanctions");
                        }
                        else
                        {
                            thinker.failedRemoteAdvsSanctions.put(failedPeer, 1);
                            System.out.println(downloadingID + " failed peer "+ failedPeer + " " + adv.getSocketAdv().getDescription() +" have 1 sanction");
                        }
                        System.out.println("----------------------------------------------------------------------------");

                        anyFailedPeer = true;
                        
                        Logger.getLogger(U2UDownloadingManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    //is connected?
                    if ((socket != null) && socket.isConnected())
                    {
                        /*try {
                            //setting the soTimeOut
                            socket.setSoTimeout(5 * 60000);
                            System.out.println("---------------- ACTIVE SOCKET : soTimeOut for remote socket --------------------");
                            System.out.println("socket sotimeout = " + socket.getSoTimeout());
                        } catch (SocketException ex) {
                            Logger.getLogger(U2UDownloadingManager.class.getName()).log(Level.SEVERE, null, ex);
                        }*/

                        //creating the U2UFileSharingProtocol instance for talk with this connection
                        U2UFileSharingProtocol protocol = U2UFileSharingProtocol.newDownloadInstance(socket, adv, group.getPeerID().toString());
                        protocol.addProtocolListener(thinker);//listener
                        String protocolID = protocol.getProtocolID();
                        //add to the HashMap of Protocols
                        fspReferences.put(protocolID, protocol);
                        //submit the Task to the Pool
                        fspFutures.put(protocolID, fspPool.submit(protocol));
                        
                        //cleaning his past.
                        //String exFailedPeer = adv.getSocketAdv().getDescription().substring(7);//peer name
                        String exFailedPeer = adv.getSocketAdv().getPipeID().toString();//PipeID
                        if(thinker.failedRemoteAdvsSanctions.containsKey(exFailedPeer))
                        {
                            thinker.failedRemoteAdvsSanctions.remove(exFailedPeer);
                            System.out.println(downloadingID + " cleaning the past of exfailed peer " + exFailedPeer + " " +
                                    adv.getSocketAdv().getDescription());
                        }
                        //trying to send the CONN order, and get connection with the remote peer
                        protocol.init();//CONN order
                        //

                        anyConnection = true;
                    }
                }

            }
        }

        //remove failed peers from the HashMap of all remote advs
        if(anyFailedPeer)
        {
            Set<String> failedPeers = thinker.failedRemoteAdvsSanctions.keySet();
            for(String key : failedPeers)
            {
                advsToConnect.remove(key.substring(7));//peer name
            }
        }
        

        return anyConnection;
    }

    //EOTools
}