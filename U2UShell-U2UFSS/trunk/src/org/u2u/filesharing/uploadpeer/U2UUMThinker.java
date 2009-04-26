/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.u2u.filesharing.uploadpeer;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jxta.socket.JxtaSocket;
import org.u2u.filesharing.fsprotocol.U2UFSPOrderConnection;
import org.u2u.filesharing.fsprotocol.U2UFSPOrderFileInfo;
import org.u2u.filesharing.fsprotocol.U2UFSPOrderGetChunk;
import org.u2u.filesharing.fsprotocol.U2UFSProtocolOrder;
import org.u2u.filesharing.fsprotocol.U2UFileSharingProtocolEvent;
import org.u2u.filesharing.fsprotocol.U2UFileSharingProtocolListener;

/**
 * This class is responsable of generates the tasks based in the UploadingManager's
 * responsibilities and the responsibilities's generating criteria
 * @author Irene & Sergio
 */
public class U2UUMThinker implements Runnable, U2UFileSharingProtocolListener{

    /** ToDo Queue from the UploadingManager*/
    private final LinkedBlockingQueue<U2UUMExecutorsTask> toDoDM;
    /** ToThink Queue*/
    private final LinkedBlockingQueue<U2UUMThinkersProblem> toThink;
    /** HashSet for the remote advs, String=SocketName, Adv*/
    //private final ConcurrentHashMap<String, U2UContentAdvertisementImpl> remoteAdvs = new ConcurrentHashMap<String, U2UContentAdvertisementImpl>();
    /** HashSet for the remote advs of failed peers, String=SocketName, Integer = number of sanctions
     * when a peer reach 3 sanctions we no longer pay attention to him.
     */
    //protected final ConcurrentHashMap<String, Integer> failedRemoteAdvsSanctions = new ConcurrentHashMap<String, Integer>();
    /** RUNNING?*/
    private boolean isRunning;
    /** sha-1 of the content*/
    private final String sha1;
    /** Thinker Id*/
    private String thinkerID;


    //Timer
    /** findingSourcesTimer timer, it generates task for finding sources every n [ms]*/
    //private Timer findingSourcesTimer;
    /**
     * represents a task for finding sources
     */
    //private TimerTask findingSourcesTimerTask;

    /**
     * Build a U2UUMThinker
     * @param toDo the UploadingManager(The executor)'s ToDo queue
     * @param sha1 the Sha1 hash of the content
     * @param uploadingID Id of the UploadingManager own of the Thinker
     */
    protected U2UUMThinker(LinkedBlockingQueue<U2UUMExecutorsTask> toDo, String sha1, String uploadingID)
    {
        //sha1
        this.sha1 = sha1;

        //init toThink
        toThink = new LinkedBlockingQueue<U2UUMThinkersProblem>();
        //toDo
        this.toDoDM = toDo;

        //thinker Id
        thinkerID = "Thinker<" + uploadingID + ">";
    }

    public void run()
    {
        //--------initialization of the Thinker

        isRunning = true;
        
        //--------EOInit

        //--------RUNNING
        System.out.println("----------------------------------\n" + thinkerID +
                " init running \n----------------------------------");
        while(isRunning)
        {
            try
            {
                //0. take a Problem from the Queue
                U2UUMThinkersProblem problem = toThink.take(); //blocking

                //1. Think in the problem, use the problem's data
                U2UUMExecutorsTask taskToDo = null;

                switch(problem.getProblemType())
                {
                    //FIXME spino327@gmail.com this case is a copy, fix it
                    case U2UUMThinkersProblem.INCOMING_CONN_REQUEST_PROBLEM:
                    {
                        Object[] array = problem.getProblemData();

                        if(array.length == 2)
                        {
                            if((array[0] instanceof JxtaSocket) &&
                                    (array[1] instanceof U2UFSPOrderConnection))
                            {
                                JxtaSocket socket = (JxtaSocket) array[0];
                                U2UFSPOrderConnection con = (U2UFSPOrderConnection) array[1];
                                taskToDo = this.incomingConnRequestProblem(socket, con);
                            }
                        }

                        break;
                    }

                    case U2UUMThinkersProblem.PROTOCOL_EVENT_PROBLEM:
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

                    case U2UUMThinkersProblem.FINISH_PROBLEM:
                    {
                        //do nothing
                        break;
                    }
                }

                //2. make a Task for solve the problem and put them in the Executor's toDo Queue
                if(taskToDo != null)
                {
                    toDoDM.offer(taskToDo);
                }

            } catch (InterruptedException ex) {
                Logger.getLogger(U2UUMThinker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //--------EORunning

        //--------finish

        //--------EOFinish
    }

    /**
     * represents this instance of the Thinker, like a hash
     * @return the Id of the thinker, "Thinker<" + uploadingID + ">";
     */
    public String getThinkerID()
    {
        return thinkerID;
    }

    /**
     * the thinker is Running?
     * @return
     */
    public boolean isRunning()
    {
        return isRunning;
    }



    /**
     * stop the thread, the object leave of thinking
     */
    protected void stopThinking()
    {
        System.out.println(thinkerID + " stop thinking");
        isRunning = false;
        toThink.offer(new U2UUMThinkersProblem(U2UUMThinkersProblem.FINISH_PROBLEM, new Object[0]));
    }

    //--------I/O-How it feels?

    public void protocolEvent(U2UFileSharingProtocolEvent event)
    {
        //this event are response from the remote peers
        //to the thinker
        System.out.println(thinkerID + " incoming protocolEvent");
        U2UUMThinkersProblem problem = new U2UUMThinkersProblem(
                U2UUMThinkersProblem.PROTOCOL_EVENT_PROBLEM,
                new Object[] {event});

        //to the toThink queue
        toThink.offer(problem);
    }

    /**
     * forze the Download to try to attend new incoming connetion request
     * @param socket
     * @param ord
     */
    protected void attendConnRequest(JxtaSocket socket, U2UFSProtocolOrder ord)
    {
        //checking the socket
        if(socket == null)
        {
            throw new IllegalArgumentException("the socket can't be null");
        }
        else if(!socket.isConnected())
        {
            throw new IllegalArgumentException("The socket can't be close");
        }
        //checking the order
        else if(ord == null)
        {
            throw new IllegalArgumentException("the order can't be null");
        }
        else if(!(ord instanceof U2UFSPOrderConnection))
        {
            throw new IllegalArgumentException("the order need to be U2UFSPOrderConnection");
        }

        U2UFSPOrderConnection ordCon = (U2UFSPOrderConnection) ord;

        System.out.println(thinkerID + " incoming connection request from " + ordCon.getPeerId());
        U2UUMThinkersProblem problem = new U2UUMThinkersProblem(
                U2UUMThinkersProblem.INCOMING_CONN_REQUEST_PROBLEM,
                new Object[] {socket, ordCon});

        //to the toThink queue
        toThink.offer(problem);

    }

    //--------EOI/O-How it feels?

    //--------How to think in?

    /**
     * protocolEventProblem
     * @param event the event from the protocol
     */
    private U2UUMExecutorsTask protocolEventProblem(U2UFileSharingProtocolEvent event)
    {
        U2UUMExecutorsTask task = null;

        U2UFSProtocolOrder ord = event.getOrder();
        String protocolID = (String) event.getSource();

        switch(ord.getOrderId())
        {
//            case U2UFSProtocolOrder.CONN:
//                System.out.println("CONN from PeerId = "+ ((U2UFSPOrderConnection)ord).getPeerId() +
//                        " Sha1 = " + new U2UContentIdImpl(((U2UFSPOrderConnection)ord).getSha1File()).toString() +
//                        " source = " + event.getSource());
//                break;

            case U2UFSProtocolOrder.INFO:
            {
                System.out.println("INFO");
                U2UFSPOrderFileInfo info = (U2UFSPOrderFileInfo) ord;

                //checking the query type
                int queryType = info.getQueryType();
                
                if(queryType == U2UFSPOrderFileInfo.PEER_CHUNKS)
                {
                    //send task
                    task = new U2UUMExecutorsTask(U2UUMExecutorsTask.SEND_RESPONSE_FILE_INFO_QUERY_PEER_CHUNKS_TASK,
                            new Object[] {protocolID});
                }
                else if(queryType == U2UFSPOrderFileInfo.CHUNKS_SHA1)
                {
                    //send task
                    task = new U2UUMExecutorsTask(U2UUMExecutorsTask.SEND_RESPONSE_FILE_INFO_QUERY_CHUNKS_SHA1_TASK,
                            new Object[] {protocolID});
                }

                break;
            }

            case U2UFSProtocolOrder.GETC:
            {
                System.out.println("GETC");
                
                U2UFSPOrderGetChunk getc = (U2UFSPOrderGetChunk) ord;

                //send task. 1)String = ProtcolId, 2) Short = index
                task = new U2UUMExecutorsTask(U2UUMExecutorsTask.SEND_RESPONSE_GET_CHUNK_TASK, 
                        new Object[] {protocolID, getc.getChunkIndex()});

                break;
            }

            case U2UFSProtocolOrder.QUIT:
            {
                System.out.println("QUIT");
                task = new U2UUMExecutorsTask(U2UUMExecutorsTask.INCOMING_QUIT_TASK,
                        new Object[] {protocolID});

                break;
            }

        }

        return task;
    }

    /**
     * SEND_CONN_RESPONSE_TASK
     * @param socket
     * @param ord
     * @return
     */
    private U2UUMExecutorsTask incomingConnRequestProblem(JxtaSocket socket, U2UFSProtocolOrder ord)
    {
        return (new U2UUMExecutorsTask(U2UUMExecutorsTask.SEND_CONN_RESPONSE_TASK,
                new Object[] {socket, ord}));
    }

    //--------EOHow to think in?

}
