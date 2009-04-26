/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.u2u.filesharing.uploadpeer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jxta.peergroup.PeerGroup;
import net.jxta.socket.JxtaSocket;
import org.u2u.common.db.Chunks;
import org.u2u.common.db.SharedFiles;
import org.u2u.filesharing.U2UContentAdvertisementImpl;
import org.u2u.filesharing.U2UContentIdImpl;
import org.u2u.filesharing.U2UFileSharingService;
import org.u2u.filesharing.fsprotocol.U2UFSPOrderConnection;
import org.u2u.filesharing.fsprotocol.U2UFSPResponseConnection;
import org.u2u.filesharing.fsprotocol.U2UFSPResponseFileInfo;
import org.u2u.filesharing.fsprotocol.U2UFSPResponseGetChunk;
import org.u2u.filesharing.fsprotocol.U2UFSProtocolOrder;
import org.u2u.filesharing.fsprotocol.U2UFileSharingProtocol;

/**
 * U2UUploadingManager implementation for transmit chunks(for a specific shared content) requests
 * from remote peers that need it.
 * @author irene & sergio
 */
public class U2UUploadingManager implements Runnable{
    
    //U2UUploadingManager states
    /**
     * The uploading is running.
     */
    public static final int UPLOADING_RUNNING = 21;
    /**
     * The uploading finish the execution and stop himself.
     */
    public static final int UPLOADING_FINISHED = 22;

    /** Service Own of the U2UUploadingManager instance*/
    private final U2UFileSharingService fss;
    /** peer group = distributed environment*/
    private final PeerGroup group;
    /** Specific Content to upload, ContentID*/
    private final U2UContentIdImpl mainCid;
    /** 
     * Thread Pool for manage the U2UFileSharingProtocols instances
     */
    private final ThreadPoolExecutor fspPool;
    /** Thread Pool for manage the process*/
    private final ThreadPoolExecutor processPool;

    /**
     * References to the active U2UFileSharingProtocols instances
     * 1) protocolID = Protocol@<(remote peer's PeerId in String format)>
     * 2) U2UFileSharingProtocol reference
     */
    private final HashMap<String, U2UFileSharingProtocol> fspActiveReferences = new HashMap<String, U2UFileSharingProtocol>();
    private final int nConnections;
    /**
     * References to the in 401 Queue U2UFileSharingProtocols instances
     * 1) protocolID = Protocol@<(remote peer's PeerId in String format)>
     * 2) U2UFileSharingProtocol reference
     */
    private final HashMap<String, U2UFileSharingProtocol> fsp401References = new HashMap<String, U2UFileSharingProtocol>();
    /**
     * References to the Future instances that represents the submit of a U2UFileSharingProtocol
     * 1) protocolID = Protocol@<(remote peer's PeerId in String format)>
     * 2) Future reference
     */
    private final HashMap<String, Future<?>> fspFutures = new HashMap<String, Future<?>>();
    /** U2UUploadingManager's Thinker*/
    private final U2UUMThinker thinker;
    /** TODO Queue*/
    private final LinkedBlockingQueue<U2UUMExecutorsTask> toDo;
    /** downloading's state, eg, UPLOADING_FINISHED*/
    private int uploadingState;
    /** Uploading Manager's name or ID, Uploading@< cid >*/
    private final String uploadingID;

    /**
     * Build a instance of the U2UUploadingManager that manage(handle) the upload of the
     * specific Content for the remote peers connected in a distributed environment.
     * @param service Instance of the U2UFileSharingService
     * @param cid Specific content id to upload to the remote peers
     */
    public U2UUploadingManager(U2UFileSharingService service, U2UContentIdImpl cid)
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
        else if(cid == null)
        {
            throw new NullPointerException("the cid can't be null");
        }
        else if(!(cid.toString().length() == (5 + 40)))
        {
            throw new IllegalArgumentException("the adv is in bad state");
        }

        fss = service;
        group = fss.getGroup();
        this.mainCid = cid;
        //pool n for the active connections and 2 for the Queue 401
        //try to read the Properties file
        int nConn = 5;
        File properties = new File("conf/.config.properties");
        if(properties.exists())
        {
            FileInputStream fis = null;
            try {

                fis = new FileInputStream(properties);
                Properties settings = new Properties();
                settings.load(fis);

                String cu = settings.getProperty("ConUpload", "5");

                nConn = Integer.parseInt(cu);
                if(nConn < 0)
                {
                    nConn = 5;
                }

            } catch (NumberFormatException ex) {
                Logger.getLogger(U2UUploadingManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(U2UUploadingManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(U2UUploadingManager.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    if(fis != null)
                    {
                        fis.close();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(U2UUploadingManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        nConnections = nConn;

        fspPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(nConnections);//2 for the 401 Queue
        //fspPool = (ThreadPoolExecutor) Executors.newScheduledThreadPool(5);

        //process Pool
        processPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);

        //toDo
        toDo = new LinkedBlockingQueue<U2UUMExecutorsTask>();

        //download ID
        String cidString = this.mainCid.toString();
        uploadingID = "Uploading@<" + cidString + ">";

        //thinker
        thinker = new U2UUMThinker(toDo, cidString, uploadingID);
        //(new Thread(thinker, "Thinker@downloading-<"+ cid +">")).start();

        System.out.println(uploadingID + " init with a core pool size = " + nConnections);

    }

    /**
     * Stop the upload
     * @return
     */
    public void stop()
    {
        System.out.println(uploadingID + " stopping the upload");
        this.uploadingState = UPLOADING_FINISHED;
        toDo.offer(new U2UUMExecutorsTask(U2UUMExecutorsTask.FINISH_TASK, new Object[0]));
    }

    /**
     * represents this instance of the UploadingManager, like a hash
     * @return the Id of the UploadingManager, Uploading@<sha1:XXXXXX>
     */
    public String getUploadingID()
    {
        return uploadingID;
    }

    /**
     * return the number of remote connections to this uploading manager
     * @return
     */
    public int getUploadConnections()
    {
        return fspPool.getActiveCount();
    }

    //Interface for the client code with the Thinker

    /**
     * forze the Upload to try to attend new incoming connetion request
     * @param socket
     * @param ord
     */
    public void attendConnRequest(JxtaSocket socket, U2UFSProtocolOrder ord)
    {
        thinker.attendConnRequest(socket, ord);
    }

    //EOInterface for the client code with the Thinker

    //--------Executor Side

    /**
     * Executor Side of the U2UDownloadingManager, responsible for executing the tasks assigned to the
     * DownloadingManager himself
     */
    public void run()
    {
        //--------initialization of the Manager

        //start the Thinker
        (new Thread(thinker, thinker.getThinkerID())).start();

        //finding sources for download the Content
        
        uploadingState = UPLOADING_RUNNING;
        

        //--------EOInit

        //--------is RUNNING?
        System.out.println("----------------------------------\n" + uploadingID +
                " init running \n----------------------------------");
        while(uploadingState == UPLOADING_RUNNING)
        {
            try
            {
                //0. take a Task from the Queue
                U2UUMExecutorsTask task = toDo.take();//blocking
                System.out.println("-----------\n" + uploadingID + " in ToDO Queue = " + toDo.size() + "\n-----------");
                //1. Execute the task
                switch(task.getTaskType())
                {
                    /*case U2UUMExecutorsTask.FINDING_SOURCES_TASK:
                    {
                        System.out.println(downloadingID + " FINDING_SOURCES_TASK " + this.findingSourcesTask());
                        break;
                    }*/

                    case U2UUMExecutorsTask.SEND_CONN_RESPONSE_TASK:
                    {
                        //checking the args
                        Object args[] = task.getTaskData();
                        if(args.length == 2)
                        {
                            if((args[0] instanceof JxtaSocket) && (args[1] instanceof U2UFSPOrderConnection))
                            {
                                //
                                JxtaSocket socket = (JxtaSocket) args[0];
                                U2UFSPOrderConnection ord = (U2UFSPOrderConnection) args[1];
                                
                                System.out.println(uploadingID + " active threads before = " + fspPool.getActiveCount());
                                System.out.println(uploadingID + " SEND_CONN_RESPONSE_TASK " + this.attendingConnRequestTask(socket, ord));
                                System.out.println(uploadingID + " active threads after = " + fspPool.getActiveCount());

                            }
                        }
                        
                        break;
                    }

                    case U2UUMExecutorsTask.SEND_RESPONSE_FILE_INFO_QUERY_PEER_CHUNKS_TASK:
                    {
                        //checking the args
                        Object args[] = task.getTaskData();
                        if(args.length == 1)
                        {   //protocolID
                            if(args[0] instanceof String)
                            {
                                String protocolId = (String) args[0];
                                System.out.println(uploadingID + " SEND_RESPONSE_FILE_INFO_QUERY_PEER_CHUNKS_TASK to " +
                                        protocolId + " " +
                                        this.sendResponseFileInfoQueryPeerChunksTask(protocolId));
                            }
                        }

                        break;
                    }

                    case U2UUMExecutorsTask.SEND_RESPONSE_FILE_INFO_QUERY_CHUNKS_SHA1_TASK:
                    {
                        //checking the args
                        Object args[] = task.getTaskData();
                        if(args.length == 1)
                        {
                            //protocol ID
                            if(args[0] instanceof String)
                            {
                                String protocolId = (String) args[0];
                                System.out.println(uploadingID + " SEND_RESPONSE_FILE_INFO_QUERY_CHUNKS_SHA1_TASK to " +
                                            protocolId + " " +
                                            this.sendResponseFileInfoQueryChunksSha1Task(protocolId));
                            }
                        }

                        break;
                    }

                    case U2UUMExecutorsTask.SEND_RESPONSE_GET_CHUNK_TASK:
                    {
                        //checking the args
                        Object args[] = task.getTaskData();
                        if(args.length == 2)
                        {
                            //protocol ID and Index
                            if((args[0] instanceof String) &&
                                    (args[1] instanceof Short))
                            {
                                String protocolId = (String) args[0];
                                short index = ((Short) args[1]).shortValue();
                                
                                System.out.println(uploadingID + " SEND_RESPONSE_GET_CHUNK_TASK to " +
                                        protocolId + " " +
                                        this.sendResponseGetChunkTask(protocolId, index));
                            }
                        }

                        break;
                    }

                    case U2UUMExecutorsTask.INCOMING_QUIT_TASK:
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
                                    System.out.println(uploadingID + " INCOMING_QUIT_TASK from " +
                                            protocolId + " " +
                                            this.incomingQuitTask(protocolId));
                                }
                            }
                        }

                        System.out.println("--------\n" + uploadingID + " we're connect with the next remote peers (201) : ");
                        for(Map.Entry<String, U2UFileSharingProtocol> in : fspActiveReferences.entrySet())
                        {
                            System.out.println(in.getValue().getProtocolID());
                        }
                        System.out.println("--------");
                        System.out.println("--------\n" + uploadingID + " we're connect with the next remote peers (401) : ");
                        for(Map.Entry<String, U2UFileSharingProtocol> in : fsp401References.entrySet())
                        {
                            System.out.println(in.getValue().getProtocolID());
                        }
                        System.out.println("--------");

                        break;
                    }

                    case U2UUMExecutorsTask.FINISH_TASK:
                    {
                        //do nothing
                        break;
                    }
                }

            } catch (InterruptedException ex) {
                Logger.getLogger(U2UUploadingManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //--------EORunning

        //--------FINISH

        thinker.stopThinking();

        //sending finish to the connecting protocols
        for(Map.Entry<String, U2UFileSharingProtocol> in : fspActiveReferences.entrySet())
        {
            System.out.println(uploadingID + " sending finish to my partner " + in.getKey());
            in.getValue().finish();
        }

        //sending finish to the connecting protocols
        for(Map.Entry<String, U2UFileSharingProtocol> in : fsp401References.entrySet())
        {
            System.out.println(uploadingID + " sending finish to my partner " + in.getKey());
            in.getValue().finish();
        }

        //purge the pool
        for(Map.Entry<String, Future<?>> in : fspFutures.entrySet())
        {
            in.getValue().cancel(false);
        }


        fspFutures.clear();
        fspActiveReferences.clear();
        fsp401References.clear();
        toDo.clear();

        Runtime.getRuntime().gc();

        //--------EOFinish
    }

    //How to do it?

    /**
     * this task analyse the load at the instance and take a decision, and send
     * it with a U2UFileSharingProtcol instance
     * @param socket
     * @param ord
     * @return true if the response was send
     */
    private boolean attendingConnRequestTask(final JxtaSocket socket, U2UFSPOrderConnection ord) //throws InterruptedException
    {
        /*
        //FIXME spino327@gmail.com use ord for validation purpose

        boolean status = false;
        //making the protocol for response to the order
        U2UFileSharingProtocol protocol = U2UFileSharingProtocol.newUploadInstance(socket, group.getPeerID().toString());
        protocol.addProtocolListener(thinker);

        //with who we are connected, process like in the msn
        if(fspActiveReferences.containsKey(protocol.getProtocolID()))
        {
            String protocolId = protocol.getProtocolID();

            //getting the protocol
            final U2UFileSharingProtocol referenceProtocol = fspActiveReferences.get(protocolId);

            //finish the connection
            //using the fsp5XXPool for hard close the old socket connection
            processPool.submit(new Runnable() {

                public void run()
                {
                    long init = System.currentTimeMillis();
                    System.out.println(uploadingID + " fsp5XXPool - connection close = " + referenceProtocol.finish() +
                            " in (ms) = " + (System.currentTimeMillis() - init));
                }
            });
            //
            System.out.println(uploadingID + " stoping the old-protocol " + 
                    protocolId + " = "+ fspFutures.get(protocolId).cancel(false));//let the protocol to finish
            //fspPool.purge();

            //replacing the Protocol and the future
            fspActiveReferences.put(protocolId, protocol);
            fspFutures.put(protocolId, fspPool.submit(protocol));

            //send response 201
            status = protocol.responseInit(U2UFSPResponseConnection.R201);
        }
        else if(fsp401References.containsKey(protocol.getProtocolID()))
        {
            String protocolId = protocol.getProtocolID();

            //getting the protocol
            final U2UFileSharingProtocol referenceProtocol = fsp401References.get(protocolId);

            //finish the connection
            //using the fsp5XXPool for hard close the old socket connection
            processPool.submit(new Runnable() {

                public void run()
                {
                    long init = System.currentTimeMillis();
                    System.out.println(uploadingID + " fsp5XXPool - connection close = " + referenceProtocol.finish() +
                            " in (ms) = " + (System.currentTimeMillis() - init));
                }
            });
            //
            System.out.println(uploadingID + " stoping the old-protocol " +
                    protocolId + " = "+ fspFutures.get(protocolId).cancel(false));//let the protocol to finish
            //fspPool.purge();

            //replacing the Protocol and the future
            System.out.println(uploadingID + " replacing the old-Protocol for the new protocol");

            //checking the active threads, for new peers
            if(fspPool.getActiveCount() < nConnections + 2)//2 for the 401 Queue
            {
                //add to Futures Hash Map and Run the thread
                fspFutures.put(protocol.getProtocolID(), fspPool.submit(protocol));

                if(fspActiveReferences.size() < nConnections)
                {
                    //add to active threads
                    fspActiveReferences.put(protocol.getProtocolID(), protocol);

                    //send response 201
                    status = protocol.responseInit(U2UFSPResponseConnection.R201);
                }
                else
                {
                    //add to 401 Queue threads
                    fsp401References.put(protocol.getProtocolID(), protocol);

                    //send response 401
                    status = protocol.responseInit(U2UFSPResponseConnection.R401);
                }
            }
            //501 response
            else
            {
                //Run the thread
                processPool.submit(protocol);

                //send response 521
                status = protocol.responseInit(U2UFSPResponseConnection.R521);
            }
        }
        //checking the active threads, for new peers
        else if(fspPool.getActiveCount() < nConnections + 2)//2 for the 401 Queue
        {
            //add to Futures Hash Map and Run the thread
            fspFutures.put(protocol.getProtocolID(), fspPool.submit(protocol));

            if(fspActiveReferences.size() < nConnections)
            {
                //add to active threads
                fspActiveReferences.put(protocol.getProtocolID(), protocol);

                //send response 201
                status = protocol.responseInit(U2UFSPResponseConnection.R201);
            }
            //checking the 401 Queue
            else
            {
                //add to 401 Queue threads
                fsp401References.put(protocol.getProtocolID(), protocol);

                //send response 401
                status = protocol.responseInit(U2UFSPResponseConnection.R401);
            }
        }
        //501 response
        else
        {
            //Run the thread
            processPool.submit(protocol);

            //send response 521
            status = protocol.responseInit(U2UFSPResponseConnection.R521);
        }

        return status;*/

        processPool.submit(new Runnable() {

            public void run()
            {
                //FIXME spino327@gmail.com use ord for validation purpose

                boolean status = false;
                //making the protocol for response to the order
                U2UFileSharingProtocol protocol = U2UFileSharingProtocol.newUploadInstance(socket, group.getPeerID().toString());
                protocol.addProtocolListener(thinker);

                //with who we are connected, process like in the msn
                if(fspActiveReferences.containsKey(protocol.getProtocolID()))
                {
                    String protocolId = protocol.getProtocolID();

                    //getting the protocol
                    final U2UFileSharingProtocol referenceProtocol = fspActiveReferences.get(protocolId);

                    //finish the connection
                    //using the fsp5XXPool for hard close the old socket connection
                    processPool.submit(new Runnable() {

                        public void run()
                        {
                            long init = System.currentTimeMillis();
                            System.out.println(uploadingID + " fsp5XXPool - connection close = " + referenceProtocol.finish() +
                                    " in (ms) = " + (System.currentTimeMillis() - init));
                        }
                    });
                    //
                    System.out.println(uploadingID + " stoping the old-protocol " +
                            protocolId + " = "+ fspFutures.get(protocolId).cancel(false));//let the protocol to finish
                    //fspPool.purge();

                    //replacing the Protocol and the future
                    fspActiveReferences.put(protocolId, protocol);
                    fspFutures.put(protocolId, fspPool.submit(protocol));
                    try {
                        //send response 201
                        status = protocol.responseInit(U2UFSPResponseConnection.R201);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(U2UUploadingManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                else if(fsp401References.containsKey(protocol.getProtocolID()))
                {
                    String protocolId = protocol.getProtocolID();

                    //getting the protocol
                    final U2UFileSharingProtocol referenceProtocol = fsp401References.get(protocolId);

                    //finish the connection
                    //using the fsp5XXPool for hard close the old socket connection
                    processPool.submit(new Runnable() {

                        public void run()
                        {
                            long init = System.currentTimeMillis();
                            System.out.println(uploadingID + " fsp5XXPool - connection close = " + referenceProtocol.finish() +
                                    " in (ms) = " + (System.currentTimeMillis() - init));
                        }
                    });
                    //
                    System.out.println(uploadingID + " stoping the old-protocol " +
                            protocolId + " = "+ fspFutures.get(protocolId).cancel(false));//let the protocol to finish
                    //fspPool.purge();

                    //replacing the Protocol and the future
                    System.out.println(uploadingID + " replacing the old-Protocol for the new protocol");

                    //checking the active threads, for new peers
                    if(fspPool.getActiveCount() < nConnections)//2 for the 401 Queue
                    {
                        //add to Futures Hash Map and Run the thread
                        fspFutures.put(protocol.getProtocolID(), fspPool.submit(protocol));

                        if(fspActiveReferences.size() < nConnections)
                        {
                            //add to active threads
                            fspActiveReferences.put(protocol.getProtocolID(), protocol);
                            try {
                                //send response 201
                                status = protocol.responseInit(U2UFSPResponseConnection.R201);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(U2UUploadingManager.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        else
                        {
                            //add to 401 Queue threads
                            fsp401References.put(protocol.getProtocolID(), protocol);
                            try {
                                //send response 401
                                status = protocol.responseInit(U2UFSPResponseConnection.R401);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(U2UUploadingManager.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                    //501 response
                    else
                    {
                        //Run the thread
                        processPool.submit(protocol);
                        try {
                            //send response 521
                            status = protocol.responseInit(U2UFSPResponseConnection.R521);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(U2UUploadingManager.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                //checking the active threads, for new peers
                else if(fspPool.getActiveCount() < nConnections)//2 for the 401 Queue
                {
                    //add to Futures Hash Map and Run the thread
                    fspFutures.put(protocol.getProtocolID(), fspPool.submit(protocol));

                    if(fspActiveReferences.size() < nConnections)
                    {
                        //add to active threads
                        fspActiveReferences.put(protocol.getProtocolID(), protocol);
                        try {
                            //send response 201
                            status = protocol.responseInit(U2UFSPResponseConnection.R201);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(U2UUploadingManager.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    //checking the 401 Queue
                    else
                    {
                        //add to 401 Queue threads
                        fsp401References.put(protocol.getProtocolID(), protocol);
                        try {
                            //send response 401
                            status = protocol.responseInit(U2UFSPResponseConnection.R401);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(U2UUploadingManager.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                //501 response
                else
                {
                    //Run the thread
                    processPool.submit(protocol);
                    try {
                        //send response 521
                        status = protocol.responseInit(U2UFSPResponseConnection.R521);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(U2UUploadingManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                //System.out.println(uploadingID + " " + status + "");
            }
        });

        System.out.println(uploadingID + " ProcessPool active threads = " + processPool.getActiveCount());

        return true;
    }

    /**
     * this task send a response of the File Info Query asking for the peer's chunks, using
     * the appropriate U2UFileSharingProtcol instance
     * @param protocolId
     * @return true if the response was send
     */
    private boolean sendResponseFileInfoQueryPeerChunksTask(final String protocolId)// throws InterruptedException
    {
        /*
        long init = System.currentTimeMillis();
        String resNum;
        boolean status = false;
        short[] pos = null;
        //query the DB, what chunks we have of the specific Content
        String sha1_sh = mainCid.toString();

        //checking if we have all the chunks info
        int numChunksAtTheDB = Chunks.numberOfChunks(sha1_sh);
        U2UContentAdvertisementImpl adv = SharedFiles.getU2UContentAdvertisementImplFromSHA1(sha1_sh);
        short expectedNumOfChunks = (short)(adv.getLength()/(adv.getChunksize()*1024) + 1);

        if(expectedNumOfChunks == numChunksAtTheDB)
        {
            Object[] solution = Chunks.getPeerChunksList(sha1_sh);

            if((solution != null) &&
                    (solution[0] instanceof Boolean) &&
                    (solution[1] instanceof short[]))
            {
                status = ((Boolean) solution[0]).booleanValue();
                pos = (short[]) solution[1];

                //T
                if(status)
                {
                    //whole the chunks
                    if(pos.length == 0)
                    {
                        resNum = U2UFSPResponseFileInfo.R221;
                    }
                    //some chunks
                    else
                    {
                        resNum = U2UFSPResponseFileInfo.R222;
                    }
                }
                //F with info, F(1,2,...)
                else if(pos.length > 0)
                {
                    resNum = U2UFSPResponseFileInfo.R222;
                }
                //info not available
                else
                {
                    resNum = U2UFSPResponseFileInfo.R431;
                }
            }
            //541, db error
            else
            {
                resNum = U2UFSPResponseFileInfo.R541;
                status = false;
                pos = new short[0];
            }
        }
        else
        {
            resNum = U2UFSPResponseFileInfo.R431;
            status = false;
            pos = new short[0];
        }

        //send uisng the specific protocol instance
        U2UFileSharingProtocol protocol = fspActiveReferences.get(protocolId);
        System.out.println("--------------------RINFO PeerChunks in = " + (System.currentTimeMillis() - init));
        return (protocol.responseFileInfoQueryPeerChunks(resNum, status, pos));*/

        processPool.submit(new Runnable() {

            public void run()
            {
                long init = System.currentTimeMillis();
                String resNum;
                boolean status = false;
                short[] pos = null;
                //query the DB, what chunks we have of the specific Content
                String sha1_sh = mainCid.toString();

                //checking if we have all the chunks info
                int numChunksAtTheDB = Chunks.numberOfChunks(sha1_sh);
                U2UContentAdvertisementImpl adv = SharedFiles.getU2UContentAdvertisementImplFromSHA1(sha1_sh);
                short expectedNumOfChunks = (short)(adv.getLength()/(adv.getChunksize()*1024) + 1);

                if(expectedNumOfChunks == numChunksAtTheDB)
                {
                    Object[] solution = Chunks.getPeerChunksList(sha1_sh);

                    if((solution != null) &&
                            (solution[0] instanceof Boolean) &&
                            (solution[1] instanceof short[]))
                    {
                        status = ((Boolean) solution[0]).booleanValue();
                        pos = (short[]) solution[1];

                        //T
                        if(status)
                        {
                            //whole the chunks
                            if(pos.length == 0)
                            {
                                resNum = U2UFSPResponseFileInfo.R221;
                            }
                            //some chunks
                            else
                            {
                                resNum = U2UFSPResponseFileInfo.R222;
                            }
                        }
                        //F with info, F(1,2,...)
                        else if(pos.length > 0)
                        {
                            resNum = U2UFSPResponseFileInfo.R222;
                        }
                        //info not available
                        else
                        {
                            resNum = U2UFSPResponseFileInfo.R431;
                        }
                    }
                    //541, db error
                    else
                    {
                        resNum = U2UFSPResponseFileInfo.R541;
                        status = false;
                        pos = new short[0];
                    }
                }
                else
                {
                    resNum = U2UFSPResponseFileInfo.R431;
                    status = false;
                    pos = new short[0];
                }

                //send uisng the specific protocol instance
                U2UFileSharingProtocol protocol = fspActiveReferences.get(protocolId);
                System.out.println("--------------------RINFO PeerChunks in = " + (System.currentTimeMillis() - init));
                try {
                    protocol.responseFileInfoQueryPeerChunks(resNum, status, pos);
                } catch (InterruptedException ex) {
                    Logger.getLogger(U2UUploadingManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        System.out.println(uploadingID + " ProcessPool active threads = " + processPool.getActiveCount());

        return true;
    }

    /**
     * this task send a response of the File Info Query asking for the chunks' sha1, using
     * the appropriate U2UFileSharingProtcol instance
     * @param protocolId
     * @return
     */
    private boolean sendResponseFileInfoQueryChunksSha1Task(final String protocolId)// throws InterruptedException
    {
        /*
        long init = System.currentTimeMillis();
        //query the DB, what chunks we have of the specific Content
        String resNum;
        String sha1_sh = mainCid.toString();
        HashMap<Short, String> solution = null;

        //checking if we have all the chunks info
        int numChunksAtTheDB = Chunks.numberOfChunks(sha1_sh);
        U2UContentAdvertisementImpl adv = SharedFiles.getU2UContentAdvertisementImplFromSHA1(sha1_sh);
        short expectedNumOfChunks = (short)(adv.getLength()/(adv.getChunksize()*1024) + 1);

        if(expectedNumOfChunks == numChunksAtTheDB)
        {
            solution = Chunks.getChunksSHA1List(sha1_sh);

            if((solution != null) &&
                    (solution.size() > 0))
            {
                resNum = U2UFSPResponseFileInfo.R221;
            }
            //info not available
            else if((solution != null) && (solution.size() == 0))
            {
                resNum = U2UFSPResponseFileInfo.R431;
            }
            //failed
            else
            {
                //failed
                resNum = U2UFSPResponseFileInfo.R541;
            }
        }
        else
        {
            resNum = U2UFSPResponseFileInfo.R431;
        }

        //send using the specific protocol instance
        U2UFileSharingProtocol protocol = fspActiveReferences.get(protocolId);
        System.out.println("--------------------RINFO ChunksSha1 in = " + (System.currentTimeMillis() - init));

        return (protocol.responseFileInfoQueryChunksSha1(resNum, solution));*/

        processPool.submit(new Runnable() {

            public void run()
            {
                long init = System.currentTimeMillis();
                //query the DB, what chunks we have of the specific Content
                String resNum;
                String sha1_sh = mainCid.toString();
                HashMap<Short, String> solution = null;

                //checking if we have all the chunks info
                int numChunksAtTheDB = Chunks.numberOfChunks(sha1_sh);
                U2UContentAdvertisementImpl adv = SharedFiles.getU2UContentAdvertisementImplFromSHA1(sha1_sh);
                short expectedNumOfChunks = (short)(adv.getLength()/(adv.getChunksize()*1024) + 1);

                if(expectedNumOfChunks == numChunksAtTheDB)
                {
                    solution = Chunks.getChunksSHA1List(sha1_sh);

                    if((solution != null) &&
                            (solution.size() > 0))
                    {
                        resNum = U2UFSPResponseFileInfo.R221;
                    }
                    //info not available
                    else if((solution != null) && (solution.size() == 0))
                    {
                        resNum = U2UFSPResponseFileInfo.R431;
                    }
                    //failed
                    else
                    {
                        //failed
                        resNum = U2UFSPResponseFileInfo.R541;
                    }
                }
                else
                {
                    resNum = U2UFSPResponseFileInfo.R431;
                }

                //send using the specific protocol instance
                U2UFileSharingProtocol protocol = fspActiveReferences.get(protocolId);
                System.out.println("--------------------RINFO ChunksSha1 in = " + (System.currentTimeMillis() - init));
                try {
                    protocol.responseFileInfoQueryChunksSha1(resNum, solution);
                } catch (InterruptedException ex) {
                    Logger.getLogger(U2UUploadingManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        System.out.println(uploadingID + " ProcessPool active threads = " + processPool.getActiveCount());

        return true;
    }
    
    /**
     * this task send a response of the Get Chunk Query asking for the chunks' data, using
     * the appropriate U2UFileSharingProtcol instance
     * @param protocolId
     * @param index the position of the sharedFile at the SharedFile
     * @return
     */
    private boolean sendResponseGetChunkTask(final String protocolId, final short index) throws InterruptedException
    {
        /*long init = System.currentTimeMillis();
        //query the DB
        String sha1_sh = mainCid.toString();
        boolean status = false;
        U2UFileSharingProtocol protocol = fspActiveReferences.get(protocolId);

        //checking the index
        if((index >= 0) && (index <= 32767))
        {
            if(Chunks.haveTheChunk(sha1_sh, index))
            {
                /*
                 * we have all the chunks? if we have all the chunks, the fraction need to be 1.0,
                 * otherwise the faction is between [0.0 - 1.0).
                 */
                /*float downloadRelation = Chunks.haveAllTheChunks(sha1_sh);
                byte[] array = null;

                System.out.println("--------\n" + uploadingID + " send rget index = ok, HTC = true, " +
                        "downloadRelation = <" + downloadRelation + ">, " +
                        "complete = <" + SharedFiles.getCompleteSFValue(mainCid) + ">\n--------");

                //have all the chunks, we read the sharedFile from the shared file
                if((downloadRelation == 1.0f) &&
                        (SharedFiles.getCompleteSFValue(mainCid)))
                {
                    //System.out.println(uploadingID + " ENTRO if((downloadRelation == 1.0f) && (SharedFiles.getCompleteSFValue(mainCid)))");
                    File sharedFile;
                    try {

                        sharedFile = new File(new URI(SharedFiles.getPathOfTheSharedFile(mainCid)));
                        //System.out.println(uploadingID + " el sharedFile path es : " + sharedFile.getAbsolutePath());

                        if(sharedFile.exists())
                        {
                            //System.out.println(uploadingID + " ENTRAMOS el shared file si existe");
                            FileInputStream fis = new FileInputStream(sharedFile);
                            FileChannel fch = fis.getChannel();
                            //System.out.println(uploadingID + " obtubimos el canal");
                            int chunkSize = SharedFiles.getChunkSizeOfTheSharedFile(mainCid);
                            short nchunks = (short) Chunks.numberOfChunks(sha1_sh);
                            int lengthChunk = (index < (nchunks-1) ? chunkSize*1024 : (int)(sharedFile.length() - (chunkSize*1024)*(nchunks-1)));

                            //System.out.println(uploadingID + " con los calculos chunkSize = <" + chunkSize + ">, " +
                            //        " nchunks = <" + nchunks + ">, " +
                            //        " lengthChunk = <" + lengthChunk + ">");
                            ByteBuffer buffer = fch.map(FileChannel.MapMode.READ_ONLY, (long)index*(chunkSize*1024), lengthChunk);
                            //System.out.println(uploadingID + " se creo el buffer");
                            array = new byte[lengthChunk];
                            //System.out.println(uploadingID + " longitud del array = <" + array.length + ">");
                            buffer.get(array);
                            System.out.println(uploadingID + " bytes read <" + array.length + "> from chunk");
                        }
                        
                    } catch (URISyntaxException ex) {
                        Logger.getLogger(U2UUploadingManager.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(U2UUploadingManager.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(U2UUploadingManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                //haven't all the chunks, we read the sharedFile form the .chunks folder, where
                //the chunks are save with the name, fileName =  "sha1_sf" + "-" + index
                else if(downloadRelation > 0.0f)
                {
                    File chunk = new File("conf/.chunks/" + sha1_sh.substring(5) + "-" + index);

                    if(chunk.exists() &&
                            (chunk.length() <= SharedFiles.getChunkSizeOfTheSharedFile(mainCid)*1024))
                    {
                        array = new byte[(int) chunk.length()];

                        try {
                            FileInputStream fis = new FileInputStream(chunk);

                            System.out.println(uploadingID + " bytes read <" + fis.read(array) + "> from chunk");

                        } catch (FileNotFoundException ex) {
                            Logger.getLogger(U2UUploadingManager.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IOException ex) {
                            Logger.getLogger(U2UUploadingManager.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }

                //send RGET 211
                if(array != null)
                {
                    System.out.println(uploadingID + " send RGET 211");
                    status = protocol.responseRequestChunk(U2UFSPResponseGetChunk.R211, array);
                }
                //send RGET 212
                else
                {
                    System.out.println(uploadingID + " send RGET 212 HTC");
                    status = protocol.responseRequestChunk(U2UFSPResponseGetChunk.R212, null);
                }
            }
            //send RGET 212
            else
            {
                System.out.println(uploadingID + " send RGET 212");
                status = protocol.responseRequestChunk(U2UFSPResponseGetChunk.R212, null);
            }
        }
        //send RGET 511
        else
        {
            System.out.println(uploadingID + " send RGET 511");
            status = protocol.responseRequestChunk(U2UFSPResponseGetChunk.R511, null);
        }
        System.out.println("--------------------RGET CHUNK in = " + (System.currentTimeMillis() - init));
        return status;*/

        processPool.submit(new Runnable() {

            public void run()
            {
                long init = System.currentTimeMillis();
                //query the DB
                String sha1_sh = mainCid.toString();
                boolean status = false;
                U2UFileSharingProtocol protocol = fspActiveReferences.get(protocolId);

                //checking the index
                if((index >= 0) && (index <= 32767))
                {
                    if(Chunks.haveTheChunk(sha1_sh, index))
                    {
                        /*
                         * we have all the chunks? if we have all the chunks, the fraction need to be 1.0,
                         * otherwise the faction is between [0.0 - 1.0).
                         */
                        float downloadRelation = Chunks.haveAllTheChunks(sha1_sh);
                        byte[] array = null;

                        System.out.println("--------\n" + uploadingID + " send rget index = ok, HTC = true, " +
                                "downloadRelation = <" + downloadRelation + ">, " +
                                "complete = <" + SharedFiles.getCompleteSFValue(mainCid) + ">\n--------");

                        //have all the chunks, we read the sharedFile from the shared file
                        if((downloadRelation == 1.0f) &&
                                (SharedFiles.getCompleteSFValue(mainCid)))
                        {
                            //System.out.println(uploadingID + " ENTRO if((downloadRelation == 1.0f) && (SharedFiles.getCompleteSFValue(mainCid)))");
                            File sharedFile;
                            try {

                                sharedFile = new File(new URI(SharedFiles.getPathOfTheSharedFile(mainCid)));
                                //System.out.println(uploadingID + " el sharedFile path es : " + sharedFile.getAbsolutePath());

                                if(sharedFile.exists())
                                {
                                    //System.out.println(uploadingID + " ENTRAMOS el shared file si existe");
                                    FileInputStream fis = new FileInputStream(sharedFile);
                                    FileChannel fch = fis.getChannel();
                                    //System.out.println(uploadingID + " obtubimos el canal");
                                    int chunkSize = SharedFiles.getChunkSizeOfTheSharedFile(mainCid);
                                    short nchunks = (short) Chunks.numberOfChunks(sha1_sh);
                                    int lengthChunk = (index < (nchunks-1) ? chunkSize*1024 : (int)(sharedFile.length() - (chunkSize*1024)*(nchunks-1)));

                                    //System.out.println(uploadingID + " con los calculos chunkSize = <" + chunkSize + ">, " +
                                    //        " nchunks = <" + nchunks + ">, " +
                                    //        " lengthChunk = <" + lengthChunk + ">");
                                    ByteBuffer buffer = fch.map(FileChannel.MapMode.READ_ONLY, (long)index*(chunkSize*1024), lengthChunk);
                                    //System.out.println(uploadingID + " se creo el buffer");
                                    array = new byte[lengthChunk];
                                    //System.out.println(uploadingID + " longitud del array = <" + array.length + ">");
                                    buffer.get(array);
                                    System.out.println(uploadingID + " bytes read <" + array.length + "> from chunk");
                                }

                            } catch (URISyntaxException ex) {
                                Logger.getLogger(U2UUploadingManager.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (FileNotFoundException ex) {
                                Logger.getLogger(U2UUploadingManager.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (IOException ex) {
                                Logger.getLogger(U2UUploadingManager.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        //haven't all the chunks, we read the sharedFile form the .chunks folder, where
                        //the chunks are save with the name, fileName =  "sha1_sf" + "-" + index
                        else if(downloadRelation > 0.0f)
                        {
                            File chunk = new File("conf/.chunks/" + sha1_sh.substring(5) + "-" + index);

                            if(chunk.exists() &&
                                    (chunk.length() <= SharedFiles.getChunkSizeOfTheSharedFile(mainCid)*1024))
                            {
                                array = new byte[(int) chunk.length()];

                                try {
                                    FileInputStream fis = new FileInputStream(chunk);

                                    System.out.println(uploadingID + " bytes read <" + fis.read(array) + "> from chunk");

                                } catch (FileNotFoundException ex) {
                                    Logger.getLogger(U2UUploadingManager.class.getName()).log(Level.SEVERE, null, ex);
                                } catch (IOException ex) {
                                    Logger.getLogger(U2UUploadingManager.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        }

                        //send RGET 211
                        if(array != null)
                        {
                            System.out.println(uploadingID + " send RGET 211");
                            try {
                                status = protocol.responseRequestChunk(U2UFSPResponseGetChunk.R211, array);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(U2UUploadingManager.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        //send RGET 212
                        else
                        {
                            System.out.println(uploadingID + " send RGET 212 HTC");
                            try {
                                status = protocol.responseRequestChunk(U2UFSPResponseGetChunk.R212, null);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(U2UUploadingManager.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                    //send RGET 212
                    else
                    {
                        System.out.println(uploadingID + " send RGET 212");
                        try {
                            status = protocol.responseRequestChunk(U2UFSPResponseGetChunk.R212, null);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(U2UUploadingManager.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                //send RGET 511
                else
                {
                    System.out.println(uploadingID + " send RGET 511");
                    try {
                        status = protocol.responseRequestChunk(U2UFSPResponseGetChunk.R511, null);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(U2UUploadingManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                System.out.println("--------------------RGET CHUNK " + status + " in = " + (System.currentTimeMillis() - init));
            }
        });

        System.out.println(uploadingID + " ProcessPool active threads = " + processPool.getActiveCount());

        return true;
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
        if(fspActiveReferences.containsKey(protocolId))
        {
            //1. remove the protocol from the fspActiveReferences hash map
            fspActiveReferences.remove(protocolId);

            status = true;
        }
        else if(fsp401References.containsKey(protocolId))
        {
            //1. remove the protocol from the fsp401References hash map
            fsp401References.remove(protocolId);

            status = true;
        }

        if(status)
        {
            //2. cancel the future at the pool
            (fspFutures.get(protocolId)).cancel(false);//not interrupt the Thread
            fspFutures.remove(protocolId);
        }

        //FIXME spino327@gmail.com irenelizeth@gmail.com missing 401 behavior

    return status;
    }

    //EOHow to do it?

    //--------EOExecutor Side

    //Tools
    
    //EOTools
}
