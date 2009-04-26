/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.u2u.filesharing.fsprotocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.SocketException;
import java.text.MessageFormat;

import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.jxta.peer.PeerID;
import net.jxta.pipe.PipeID;
import org.u2u.filesharing.*;

import net.jxta.socket.JxtaSocket;
import net.jxta.socket.JxtaSocketAddress;

/**
 * Protocol that make easy the exchange of chunks between DownloadPeer and UploadPeer
 *  ____________                         ____________
 * |            |                       |            |
 * |DownloadPeer|                       | UploadPeer |
 * |____________|                       |____________|
 *      |                                       |
 *      |                  CONN                 |
 *      |-------------------------------------->|
 *      |                  RCONN                |
 *      |<--------------------------------------|
 *      |                  INFO                 |
 *      |-------------------------------------->|
 *      |                  RINFO                |
 *      |<--------------------------------------|
 *      |                  GETC                 |
 *      |-------------------------------------->|
 *      |                  RGETC                |
 *      |<--------------------------------------|
 *      |                  ACKW                 |
 *      |-------------------------------------->|
 *      |                  QUIT                 |
 *      |-------------------------------------->|
 *
 *      
 * @author Irene & Sergio
 */
public class U2UFileSharingProtocol implements Runnable{

    /**
     * DownloadSide Mode
     */
    public static final int DOWNLOAD = 10;
    /**
     * UploadSide Mode
     */
    public static final int UPLOAD = 11;

    //protocol states
    /**
     * The protocol isn't running, missing connection or not order init performed yet.(before connection)
     */
    public static final int PROTOCOL_DISABLED = 20;

    /**
     * The protocol is running.
     */
    public static final int PROTOCOL_RUNNING = 21;

    /**
     * The protocol finish the connection and stop himself.(after disconnection)
     */
    public static final int PROTOCOL_FINISHED = 22;

    /**
     * Protocol Version
     */
    private static final byte PROTOCOL_VERSION = (byte) (0x86 - 0x80);//†

    //common
    private final int mode;
    private final JxtaSocket socket;
    private final U2UContentAdvertisementImpl adv;
    private Set<U2UFileSharingProtocolListener> protocolListeners = new CopyOnWriteArraySet<U2UFileSharingProtocolListener>(); //the Set of U2UFileSharingProtocolListener
    /** protocol's state, eg, PROTOCOL_RUNNING*/
    private int protocolState;
    /** represents this instance of the protocol, like a hash*/
    private final String protocolID;
    /** Peer Id in String format, this (id) identify the protocol's peer own*/
    private final String peerId;
    /** Event Dispatcher*/
    //private final U2UFSPEventDispatcher dispacher;

    /** SendBufferSize*/
    private int sendBufferSize;

    /**
     * Event dispacher
     */
//    private class U2UFSPEventDispatcher implements Runnable {
//
//        //FIXME spino327@gmail.com a Set isn't synchronized
//        private Set<U2UFileSharingProtocolListener> listeners;
//        /**queue that represents the objects send to the protocol's listeners*/
//        private LinkedBlockingQueue toListenersQueue;
//
//        private boolean isStoped;
//
//        public U2UFSPEventDispatcher(Set<U2UFileSharingProtocolListener> list)
//        {
//            listeners = list;
//            toListenersQueue = new LinkedBlockingQueue();
//        }
//
//        public void run()
//        {
//            //init
//            isStoped = false;
//
//            //FIXME spino327@gmail.com not security implemented!
//            while(!isStoped)
//            {
//                //getting object from the Queue
//                Object obj = null;//blocking
//                try
//                {
//                    obj = toListenersQueue.take();
//                } catch (InterruptedException ex)
//                {
//                    Logger.getLogger(U2UFileSharingProtocol.class.getName()).log(Level.SEVERE, null, ex);
//                }
//
//                synchronized(listeners)
//                {
//                    // are there any registered protocol listeners,
//                    // generate the event and callback.
//                    long t0 = System.currentTimeMillis();
//
//                    Object[] allListeners = this.listeners.toArray(new Object[0]);
//                    U2UFileSharingProtocolEvent event = null;
//                    String objId = null;
//
//                    if(obj instanceof U2UFSProtocolOrder)
//                    {
//                        U2UFSProtocolOrder order = (U2UFSProtocolOrder) obj;
//                        event = U2UFileSharingProtocolEvent.newOrderEvent(
//                                order,
//                                protocolID);
//
//                        objId = "order " + order.getOrderId();
//                    }
//                    else if(obj instanceof U2UFSProtocolResponse)
//                    {
//                        U2UFSProtocolResponse response = (U2UFSProtocolResponse) obj;
//                        event = U2UFileSharingProtocolEvent.newResponseEvent(
//                                response,
//                                protocolID);
//
//                        objId = "response " + response.getResponseId();
//                    }
//
//                    for (Object allListener : allListeners) {
//
//                        ((U2UFileSharingProtocolListener) allListener).protocolEvent(event);
//                    }
//
//                    System.out.println(protocolID + " Called all listenters to query " + objId +
//                            " in : " + (System.currentTimeMillis() - t0));
//                }
//            }
//
//            //free
//            this.listeners.clear();
//            this.listeners = null;
//            this.toListenersQueue = null;
//        }
//
//        public void stopEventDispatcher()
//        {
//            this.isStoped = true;
//        }
//
//        /**
//         * Sends the event to the listeners of the protocol
//         * @param obj Response or Order to send to the listeners
//         */
//        protected synchronized void invokeListenerMethod(Object obj) {
//
//            this.toListenersQueue.offer(obj);//non blocking
//        }
//    }

    //download side
    private LinkedBlockingQueue<U2UFSProtocolOrder> ordersQueue;//queue that represents the orders send to the protocol

    //upload side
    private SynchronousQueue<U2UFSProtocolResponse> responsesQueue;//queue that represents the responses send to the protocol

    //
    /**
     * builds an instance of the U2UFileSharingProtocol in the especific mode
     * @param mode UploadSide or DownloadSide mode
     * @param sk Socket instance
     * @param adv the content that is required
     * @param peerId Peer Id in String format
     */
    private U2UFileSharingProtocol(int md, JxtaSocket sk, U2UContentAdvertisementImpl adv, String peerId)
    {
        this.mode = md;
        this.socket = sk;
        this.adv = adv;
        protocolState = PROTOCOL_DISABLED;
        this.peerId = peerId;

        if(this.mode == DOWNLOAD)
        {
            //FIXME spino327@gmail.com is necesary bounded the queue
            ordersQueue = new LinkedBlockingQueue<U2UFSProtocolOrder>();
        }
        else if(this.mode == UPLOAD)
        {
            //FIFO
            responsesQueue = new SynchronousQueue<U2UFSProtocolResponse>(true);
        } 

        //send buffer size, maximun transfer piece MTP
        //try to read the Properties file
        int sbs = 64*1024;
        File properties = new File("conf/.config.properties");
        if(properties.exists())
        {
            FileInputStream fis = null;
            try {

                fis = new FileInputStream(properties);
                Properties settings = new Properties();
                settings.load(fis);

                String mtp = settings.getProperty("MTP", "65536");
                
                sbs = Integer.parseInt(mtp);
                if((sbs != 16*1024)
                        && (sbs != 32*1024)
                        && (sbs != 64*1024))
                {
                    sbs = 64*1024;
                }

            } catch (NumberFormatException ex) {
                Logger.getLogger(U2UFileSharingProtocol.class.getName()).log(Level.SEVERE, null, ex);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(U2UFileSharingProtocol.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(U2UFileSharingProtocol.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    if(fis != null)
                    {
                        fis.close();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(U2UFileSharingProtocol.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        this.sendBufferSize = sbs;
        /*try {
            this.sendBufferSize = sk.getSendBufferSize();
        } catch (SocketException ex) {
            this.sendBufferSize = 64*1024;
            Logger.getLogger(U2UFileSharingProtocol.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        //

        //protocol ID
        if(this.mode == DOWNLOAD)
        {
            //Protocol@<(PipeId(SocketAdv of remote peer) String format)>
            PipeID remotePipeId = (PipeID) this.adv.getSocketAdv().getPipeID();
            this.protocolID = "Protocol@<" + remotePipeId.toString() + ">";
        }
        else
        {
            //Protocol@<(remote peer's PeerId in String format)>
            PeerID remotePeerId = ((JxtaSocketAddress)sk.getRemoteSocketAddress()).getPeerId();
            this.protocolID = "Protocol@<" + remotePeerId.toString() + ">";
        }

        //event dispacher
        //this.dispacher = new U2UFSPEventDispatcher(protocolListeners);
        //(new Thread(this.dispacher, "eventDispatcher@"+this.protocolID)).start();

        System.out.println(protocolID + " init with MTP(Send Buffer Size) = " + this.sendBufferSize);

    }

    //DowloadSide - methods of the protocol's order
    /**
     * init the metting.
     * is equivalent in the client side to sends the first type of msg of the Protocol,
     * "DownloadPeer requests connection for the file with SHA-1"
     * @return true if the order are add to the queue
     */
    public boolean init()
    {
        boolean status = false;
        checkMode(mode);
        checkProtocolState(PROTOCOL_DISABLED);

        if(this.mode == DOWNLOAD)
        {
            System.out.println("------"+protocolID+" DOWNLOADING request CONN-------------");
            U2UFSProtocolOrder order = null;
            
            U2UContentIdImpl cid = (U2UContentIdImpl) adv.getContentId();

            order = U2UFSProtocolOrder.newOrderConnection(PROTOCOL_VERSION,
                    cid.getByteRepresentation(), peerId);

            //add to the queue
            status = ordersQueue.offer(order);
        }
        else
        {
            Logger.getLogger(U2UFileSharingProtocol.class.getName()).log(Level.WARNING, "incorrect mode");
        }

        return status;
    }

    /**
     * PEER_CHUNKS
     * executes a info query, "DP requests file's info with SHA-1", list of remote peer's chunks( T(1 4 5...) or
     * F(2 3 6...) only the most small in bytes) 
     * @return true if the order are add to the queue
     */
    public boolean fileInfoQueryPeerChunks()
    {
        boolean status = false;
        checkMode(mode);
        checkProtocolState(PROTOCOL_RUNNING);

        if(this.mode == DOWNLOAD)
        {
            System.out.println("------"+protocolID+" DOWNLOADING request file info chunks-------------");
            U2UFSProtocolOrder order = null;

            U2UContentIdImpl cid = (U2UContentIdImpl) adv.getContentId();

            order = U2UFSProtocolOrder.newOrderFileInfo(U2UFSPOrderFileInfo.PEER_CHUNKS,
                    cid.getByteRepresentation());

            //add to the queue
            status = ordersQueue.offer(order);
        }
        else
        {
            Logger.getLogger(U2UFileSharingProtocol.class.getName()).log(Level.WARNING, "incorrect mode");
        }

        return status;
    }

    /**
     * CHUNKS_SHA1
     * executes a info query, "DP requests file's info with SHA-1", sha-1 list of
     * file's chunks( H(1xxxxxxxxx 2xxxxxxxx....))
     * @return true if the order are add to the queue
     */
    public boolean fileInfoQueryChunksSha1()
    {
        boolean status = false;
        checkMode(mode);
        checkProtocolState(PROTOCOL_RUNNING);

        if(this.mode == DOWNLOAD)
        {
            System.out.println("------"+protocolID+" DOWNLOADING request file info sha1 chunks-------------");
            U2UFSProtocolOrder order = null;

            U2UContentIdImpl cid = (U2UContentIdImpl) adv.getContentId();

            order = U2UFSProtocolOrder.newOrderFileInfo(U2UFSPOrderFileInfo.CHUNKS_SHA1,
                    cid.getByteRepresentation());

            //add to the queue
            status = ordersQueue.offer(order);
        }
        else
        {
            Logger.getLogger(U2UFileSharingProtocol.class.getName()).log(Level.WARNING, "incorrect mode");
        }

        return status;
    }

    /**
     * executes a getc query, "DP requests chunk with index"
     * @param chunkIndex int specifying the zero-based chunk index [0-37767]
     * @return true if the order are add to the queue
     */
    public boolean requestChunk(int chunkIndex)
    {

        boolean status = false;
        checkMode(mode);
        checkProtocolState(PROTOCOL_RUNNING);

        if(this.mode == DOWNLOAD)
        {
            System.out.println("------"+protocolID+" DOWNLOADING request chunk-------------");
            U2UFSProtocolOrder order = null;
            
            order = U2UFSProtocolOrder.newOrderGetChunk(chunkIndex);

            //add to the queue
            status = ordersQueue.offer(order);
        }
        else
        {
            Logger.getLogger(U2UFileSharingProtocol.class.getName()).log(Level.WARNING, "incorrect mode");
        }

        return status;
    }

    public void sendAcknowledge()
    {
        throw new UnsupportedOperationException("not yet implemented");
    }

    //EODS

    //UploadSide - methods of the protocol's response

    /**
     * init the metting. is equivalent in the server side to response to the query send by the client. "UploadPeer sends a response with the decision"
     * @param decision the decision that will be send to the DownloadPeer, 201 - yes, 401 - maybe,  or 5XX - no
     * @return true if the response are add to the queue
     * @throws InterruptedException 
     */
    public boolean responseInit(String decision) throws InterruptedException
    {
        boolean status = false;
        checkMode(mode);
        checkProtocolState(PROTOCOL_DISABLED);

        if(this.mode == UPLOAD)
        {
            System.out.println("------"+protocolID+" UPLOAD response init " + decision + "-------------");
            U2UFSPResponseConnection response = null;

            response = U2UFSProtocolResponse.newResponseConnection(decision, PROTOCOL_VERSION, peerId);

            //add to the queue
            //status = responsesQueue.offer(response);//nonblocking
            responsesQueue.put(response);//blocking
            status = true;
        }
        else
        {
            Logger.getLogger(U2UFileSharingProtocol.class.getName()).log(Level.WARNING, "incorrect mode");
        }

        return status;
    }

    /**
     * PEER_CHUNKS
     * send peer's chunks that alreafy have of the file. is equivalent in the server side to response to 
     * the query send by the client. "DP requests file's info with SHA-1"
     * @param resNum response number to send to the client, 221 i have all, 222 i have some,
     *        411 request wasn't executed, 421 request was canceled, or 5XX - no
     * @param haveIt true if the peer have the chunksList or false if the peer haven't the chunksList
     * @param chunksList list of chunks position, if the peer have all the
     *        chunks then the short is new short[0] not null, eg short[] {1, 2, 3, 4}
     *
     * @return true if the response are add to the queue
     * @throws InterruptedException
     */
    public boolean responseFileInfoQueryPeerChunks(String resNum, boolean haveIt, short[] chunksList) throws InterruptedException
    {
        boolean status = false;
        checkMode(mode);
        checkProtocolState(PROTOCOL_RUNNING);

        if(this.mode == UPLOAD)
        {
            System.out.println("------"+protocolID+" UPLOAD response file info chunks-------------");
            U2UFSPResponseFileInfo response = null;

            response = U2UFSProtocolResponse.newResponseFileInfoPeerChunks(resNum, haveIt, chunksList);

            //add to the queue
            //status = responsesQueue.offer(response);//nonblocking
            responsesQueue.put(response);//blocking
            status = true;
        }
        else
        {
            Logger.getLogger(U2UFileSharingProtocol.class.getName()).log(Level.WARNING, "incorrect mode");
        }

        return status;
    }

    /**
     * CHUNKS_SHA1
     * send a list of poschunk with the chunk' hash that the file. is equivalent in the server side to response to
     * the query send by the client. "DP requests file's info with SHA-1"
     * @param resNum response number to send to the client, 221 have the list completety, 222 impossible,
     *        411 request wasn't executed, 421 request was canceled, or 5XX - no
     * @param hashList the HashMap that represents the hash of all the file's chunks
     *
     * eg. HashMap representation<br>
     * posChunks|sha1<br>
     * Short    |String<br>
     *  _________________<br>
     * |___1____|__XXXX__|<br>
     * |___2____|__XXXX__|<br>
     * |_  ... _|_  ... _|<br>
     * |___n____|__XXXX__|
     *
     * @return true if the response are add to the queue
     * @throws InterruptedException
     */
    public boolean responseFileInfoQueryChunksSha1(String resNum, HashMap<Short, String> hashList) throws InterruptedException
    {
        boolean status = false;
        checkMode(mode);
        checkProtocolState(PROTOCOL_RUNNING);

        if(this.mode == UPLOAD)
        {
            System.out.println("------"+protocolID+" UPLOAD response file inf sha1 chunks-------------");
            U2UFSPResponseFileInfo response = null;

            response = U2UFSProtocolResponse.newResponseFileInfoChunksSha1(resNum, hashList);

            //add to the queue
            //status = responsesQueue.offer(response);//nonblocking
            responsesQueue.put(response);//blocking
            status = true;
        }
        else
        {
            Logger.getLogger(U2UFileSharingProtocol.class.getName()).log(Level.WARNING, "incorrect mode");
        }

        return status;
    }

    /**
     * send chunk's bytes. is equivalent in the server side to response to
     * the query send by the client. "DP requests chunk with index"
     * @param resNum response number, must be a valid response, uses the static constanst of the class
     * 211 available, 212 not available, 411 request wasn't executed, 421 request was canceled, or 5XX - no
     * @param chunkBytes byte array representation of the chunk's data
     *
     * @return true if the response are add to the queue
     * @throws InterruptedException 
     */
    public boolean responseRequestChunk(String resNum, byte[] chunkBytes) throws InterruptedException
    {
        boolean status = false;
        checkMode(mode);
        checkProtocolState(PROTOCOL_RUNNING);

        if(this.mode == UPLOAD)
        {
            System.out.println("------"+protocolID+" UPLOAD response request chunk-------------");
            U2UFSPResponseGetChunk response = null;

            response = U2UFSProtocolResponse.newResponseGetChunk(resNum, chunkBytes);

            //add to the queue
            //status = responsesQueue.offer(response);//nonblocking
            responsesQueue.put(response);//blocking
            status = true;
        }
        else
        {
            Logger.getLogger(U2UFileSharingProtocol.class.getName()).log(Level.WARNING, "incorrect mode");
        }

        return status;
    }
    //EOUS

    //Commons

    /**
     * executes a quit query, "DP sends finish message to remote peer"
     * @return true if the order are add to the queue
     */
    public boolean finish()
    {
        boolean status = false;
        checkMode(mode);
        //checkProtocolState(PROTOCOL_RUNNING);

        if(this.mode == DOWNLOAD)
        {
            System.out.println("------" + protocolID + " finish download-------------");
            U2UFSProtocolOrder order = null;

            order = U2UFSProtocolOrder.newOrderQuit();

            //add to the queue
            status = ordersQueue.offer(order);
        }
        else if(this.mode == UPLOAD)
        {
            try {
                //close the socket
                System.out.println("------" + protocolID + " finish upload-------------");
                this.socket.setSoTimeout(1);
                this.socket.shutdownInput();
                this.socket.shutdownOutput();
                //this.socket.close();
                status = true;
            } catch (IOException ex) {
                Logger.getLogger(U2UFileSharingProtocol.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else
        {
            Logger.getLogger(U2UFileSharingProtocol.class.getName()).log(Level.WARNING, "incorrect mode");
        }

        return status;
    }
    //EOCommons

    /**
     * add a protocol listener object
     * @param listener a Protocol listener object
     */
    public synchronized void addProtocolListener(U2UFileSharingProtocolListener listener)
    {
        protocolListeners.add(listener);
    }

    /**
     * remove a protocol listener object from the set
     * @param listener a Protocol listener object
     * @return true if it can be remove from the set
     */
    public synchronized boolean removeProtocolListener(U2UFileSharingProtocolListener listener)
    {
        return protocolListeners.remove(listener);
    }

    /**
     * represents this instance of the protocol, like a hash
     * @return the Id of the protocol, DownloadInstance => Protocol@<(PipeId(SocketAdv of remote peer) in String format)>
     *  or UploadInstace => Protocol@<(remote peer's PeerId in String format)>
     *
     */
    public String getProtocolID()
    {
        return protocolID;
    }

    /**
     * return the content adv that this instance uses for the connection
     * @return
     */
    public U2UContentAdvertisementImpl getAdv()
    {
        try {
            return (U2UContentAdvertisementImpl) adv.clone();
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(U2UFileSharingProtocol.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * returns the protocol instance's mode
     * @return a int that represents the mode
     */
    public int getMode()
    {
        return mode;
    }

    /**
     * returns the protocol state, DISABLE, RUNNING, FINISHED
     * @return a int that represents the mode
     */
    public int getProtocolState()
    {
        return protocolState;
    }

    public void run()
    {
        OutputStream out = null;
        InputStream in = null;
        
        try
        {
            //--------initialization
            //get the sk output stream
            out = socket.getOutputStream();
            DataOutputStream dos = new DataOutputStream(out);
            // get the sk input stream
            in = socket.getInputStream();
            DataInputStream dis = new DataInputStream(in);
            //--------EOB

            //--------running
            if(this.mode == DOWNLOAD)
            {
                //verifier
                //CONNECTION_VERIFIER.schedule(connectionVerifierTask, 0, 60000);
                this.runDownloadSide(dis, dos);
            }
            else if(this.mode == UPLOAD)
            {
                this.runUploadSide(dis, dos);
            }
            //--------EOR
            
    
        } catch (InterruptedException ex)
        {
            Logger.getLogger(U2UFileSharingProtocol.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex)
        {
            Logger.getLogger(U2UFileSharingProtocol.class.getName()).log(Level.SEVERE, null, ex);
        } finally
        {
            //--------released resources
            protocolState = PROTOCOL_FINISHED;
            //dispacher.stopEventDispatcher();

            System.out.println("---------\n" + protocolID + " stop running! released resources\n--------");
            try
            {
                if(out != null) {
                    out.close();
                }
                if(in != null) {
                    in.close();
                }
                if(socket.isConnected())
                {
                    //reducing the closing time
                    socket.setSoTimeout(1);
                    System.out.println(protocolID + " closing the socket, soTimeOut = " + socket.getSoTimeout());
                    socket.close();
                }

            } catch (IOException ex)
            {
                Logger.getLogger(U2UFileSharingProtocol.class.getName()).log(Level.SEVERE, null, ex);
            }
            //--------EOB
        }
    }

    /**
     * run the protocol's Download Side behavior
     * @param dis DataInputStream
     * @param dos DataOutputStream
     * @throws java.lang.InterruptedException
     * @throws java.io.IOException
     */
    private void runDownloadSide(DataInputStream dis, DataOutputStream dos) throws InterruptedException, SocketException
    {
        U2UFSProtocolOrder order = null;

        //--------request connection
        {
            String responseNumber = null;
            //read order from queue
            order = ordersQueue.take();
            //outgoing query
            this.write(dis, dos,order);
            order = null;
            //incoming response
            U2UFSProtocolResponse resCon = U2UFSProtocolResponse.parseResponseFromByteArray(this.readByteArray(dis,dos));

            responseNumber = resCon.getResponseNumber();

            if(!(resCon instanceof U2UFSPResponseConnection))
            {
                this.protocolState = PROTOCOL_FINISHED;

                //send to listeners
                //this.dispacher.invokeListenerMethod(new U2UFSPResponseQuit());
                invokeListenerMethod(new U2UFSPResponseQuit());

                throw new VerifyError("the response isn't of type U2UFSPResponseConnection");
            }
            else if(U2UFSPResponseConnection.R201.equals(responseNumber))
            {
                this.protocolState = PROTOCOL_RUNNING;

                //send to listeners
                //this.dispacher.invokeListenerMethod(resCon);
                invokeListenerMethod(resCon);
            }
            //Intermediate positive response
            else if(U2UFSPResponseConnection.R401.equals(responseNumber))
            {
                //send to listeners
                //this.dispacher.invokeListenerMethod(resCon);
                invokeListenerMethod(resCon);
                //incoming response
                resCon = U2UFSProtocolResponse.parseResponseFromByteArray(this.readByteArray(dis,dos));

                if(!(resCon instanceof U2UFSPResponseConnection))
                {
                    this.protocolState = PROTOCOL_FINISHED;

                    //send to listeners
                    //this.dispacher.invokeListenerMethod(new U2UFSPResponseQuit());
                    invokeListenerMethod(new U2UFSPResponseQuit());

                    throw new VerifyError("the response isn't of type U2UFSPResponseConnection");
                }
                else if(U2UFSPResponseConnection.R201.equals(resCon.getResponseNumber()))
                {
                    this.protocolState = PROTOCOL_RUNNING;
                }
                //otherwise, response that starts with 5 and 4, eg, 501, 511, 521, and 401
                else
                {
                    this.protocolState = PROTOCOL_FINISHED;
                }

                //send to listeners
                //this.dispacher.invokeListenerMethod(resCon);
                invokeListenerMethod(resCon);
            }
            //otherwise, response that starts with 5, eg, 501, 511, 521
            else
            {
                this.protocolState = PROTOCOL_FINISHED;
                //send to listeners
                //this.dispacher.invokeListenerMethod(new U2UFSPResponseQuit());
                invokeListenerMethod(new U2UFSPOrderQuit());
            }
        }
        //--------EOB

        //--------running(order process and listeners manager)
        //diferent than PROTOCOL_DISABLED and PROTOCOL_FINISHED
        while(protocolState == PROTOCOL_RUNNING)
        {
            long init = System.currentTimeMillis();
            //read order from queue
            //order = ordersQueue.take();
            order = ordersQueue.poll(2*socket.getSoTimeout(), TimeUnit.MILLISECONDS);
            
            if(socket.isConnected())
            {
                //outgoing query
                this.write(dis, dos, order);

                //incoming response
                if((order.getOrderId() != U2UFSProtocolOrder.ACKW) &&
                        (order.getOrderId() != U2UFSProtocolOrder.QUIT))
                {
                    U2UFSProtocolResponse response =
                        U2UFSProtocolResponse.parseResponseFromByteArray(this.readByteArray(dis,dos));

                    //send to listeners
                    //this.dispacher.invokeListenerMethod(response);
                    invokeListenerMethod(response);
                    //finish because a unexpected close?
                    if(response.getResponseId() == U2UFSProtocolResponse.RQUIT)
                    {
                        protocolState = PROTOCOL_FINISHED;
                    }
                }
            }

            //finish?
            if(order.getOrderId() == U2UFSProtocolOrder.QUIT)
            {
                protocolState = PROTOCOL_FINISHED;
                //send to listeners
                //this.dispacher.invokeListenerMethod(new U2UFSPResponseQuit());
                invokeListenerMethod(new U2UFSPResponseQuit());
            }

            System.out.println(protocolID + " DownloadSide: INCOMING RESPONSE FOR ORDER in = " + (System.currentTimeMillis() - init));
        }
        //--------EOB
    }

    /**
     * run the protocol's Upload Side behavior
     * @param dis DataInputStream
     * @param dos DataOutputStream
     * @throws java.lang.InterruptedException
     * @throws java.io.IOException
     */
    private void runUploadSide(DataInputStream dis, DataOutputStream dos) throws InterruptedException, IOException
    {
        U2UFSProtocolResponse response = null;

        //--------response connection
        {
            String responseNumber = null;
            /*
             * the first statement if 'get from the queue', because the decition come from the U2UFSS in the case
             * the app reaches the maximum number of U2UUM instances or from the U2UUM in the other case
             */
            //get the response from the SynchronousQueue
            response = responsesQueue.take();

            responseNumber = response.getResponseNumber();

            if(!(response instanceof U2UFSPResponseConnection))
            {
                this.protocolState = PROTOCOL_FINISHED;
                //outgoing response
                this.write(dis,dos, U2UFSProtocolResponse.
                        newResponseConnection(U2UFSProtocolResponse.R521, PROTOCOL_VERSION, peerId));

                throw new VerifyError("the response isn't of type U2UFSPResponseConnection");
            }
            else if(U2UFSPResponseConnection.R201.equals(responseNumber))
            {
                this.protocolState = PROTOCOL_RUNNING;
                //outgoing response
                this.write(dis, dos,response);
            }
            //Intermediate positive response
            else if(U2UFSPResponseConnection.R401.equals(responseNumber))
            {
                //outgoing first response
                this.write(dis, dos,response);

                //get the response from the SynchronousQueue
                //response = responsesQueue.take();
                response = responsesQueue.poll(2*socket.getSoTimeout(), TimeUnit.MILLISECONDS);

                if(response == null)
                {
                    //this.dispacher.invokeListenerMethod(new U2UFSPOrderQuit());
                    invokeListenerMethod(new U2UFSPOrderQuit());
                    this.protocolState = PROTOCOL_FINISHED;
                }
                else if(!(response instanceof U2UFSPResponseConnection))
                {
                    this.protocolState = PROTOCOL_FINISHED;
                    //outgoing response
                    this.write(dis,dos, U2UFSProtocolResponse.
                            newResponseConnection(U2UFSProtocolResponse.R521, PROTOCOL_VERSION, peerId));

                    throw new VerifyError("the response isn't of type U2UFSPResponseConnection");
                }
                else
                {
                    if(U2UFSPResponseConnection.R201.equals(response.getResponseNumber()))
                    {
                        this.protocolState = PROTOCOL_RUNNING;
                    }
                    //otherwise, response that starts with 5 and 4, eg, 501, 511, 521, and 401
                    else
                    {
                        this.protocolState = PROTOCOL_FINISHED;
                    }
                    //outgoing second response
                    this.write(dis, dos,response);
                }
            }
            //otherwise, response that starts with 5, eg, 501, 511, 521
            else
            {
                this.protocolState = PROTOCOL_FINISHED;
                //outgoing response
                this.write(dis, dos,response);
            }
        }
        //--------EOB
        
        //--------running(response process and listeners manager)
        //diferent than PROTOCOL_DISABLED and PROTOCOL_FINISHED
        while(protocolState == PROTOCOL_RUNNING)
        {
            long init = System.currentTimeMillis();
            response = null;

            //incoming order
            U2UFSProtocolOrder order = U2UFSProtocolOrder.parseOrderFromByteArray(this.readByteArray(dis,dos));

            //send to listeners
            //this.dispacher.invokeListenerMethod(order);
            invokeListenerMethod(order);

            //get the response from the SynchronousQueue
            if((order.getOrderId() != U2UFSProtocolOrder.ACKW) &&
                    (order.getOrderId() != U2UFSProtocolOrder.QUIT))
            {
                //response = responsesQueue.take();
                response = responsesQueue.poll(2*socket.getSoTimeout(), TimeUnit.MILLISECONDS);

                //outgoing response
                this.write(dis, dos,response);
            }
            
            //finish?
            if(order.getOrderId() == U2UFSProtocolOrder.QUIT)
            {
                protocolState = PROTOCOL_FINISHED;
            }

            System.out.println(protocolID + " UploadSide: OUTGOING RESPONSE FOR ORDER in = " + (System.currentTimeMillis() - init));
        }
        //--------EOB
    }

    private void checkMode(final int mode)
    {
        switch(mode)
        {
            case UPLOAD:
                if(this.mode != UPLOAD) {
                    throw new IllegalAccessError("The Protocol instance's type is UPLOAD, so you can't invoke a method of DOWNLOAD side");
                }
                break;

            case DOWNLOAD:
                if(this.mode != DOWNLOAD) {
                    throw new IllegalAccessError("The Protocol instance's type is DOWNLOAD, so you can't invoke a method of UPLOAD side");
                }
                break;

            default:
                throw new IllegalArgumentException("Incorrect mode");
        }
    }

    private void checkProtocolState(final int protocolStateExpected)
    {
        if (this.protocolState != protocolStateExpected)
        {
            throw new IllegalStateException("Incorrect protocol's state");
        }
    }

    /** 
     * read a Response or an Order in byte array format from a DataInputStream
     *
     * @param dis DataInputStream
     * @param dos DataOutputStream
     */
    private synchronized byte[] readByteArray(DataInputStream dis, DataOutputStream dos)
    {
        //read the length prefix
        // (from the DataInputStream implementation)the next four bytes of this input stream, interpreted as an int
        int length;
        byte[] byteArray = null;
        boolean close = false;
        //if write -1 at the remote peer OutputStream byte ch1 = ((ch1 = dis.readByte()) == -1? dis.readByte() : ch1);
        /*byte ch1 = dis.readByte();
        byte ch2 = dis.readByte();
        byte ch3 = dis.readByte();
        byte ch4 = dis.readByte();
        int c1 = (ch1 < 0? (ch1 + 256) : ch1);
        int c2 = (ch2 < 0? (ch2 + 256) : ch2);
        int c3 = (ch3 < 0? (ch3 + 256) : ch3);
        int c4 = (ch4 < 0? (ch4 + 256) : ch4);*/

        //if the first byte read in int format is -1, then we read again
        int ch1;
        //read return values from 0 to 255
        //length prefix is always > 0, so the ch1 only can get values from 0 to 127
        try
        {
            do
            {
                ch1 = dis.read();
            } while(((ch1 < 0) || (ch1 > 127)) && socket.isConnected());

            if(socket.isConnected())
            {
                int ch2 = dis.read();
                int ch3 = dis.read();
                int ch4 = dis.read();

                if ((ch1 | ch2 | ch3 | ch4) < 0)
                {
                    throw new VerifyError("character in bad state");
                }

                length = ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));

                //read the complete order or response
                //<length prefix><response id><response number><payload> = response
                //<length prefix><order id><payload> = order

                System.out.println("length " + length);
                byteArray = new byte[4 + length];

                //length prefix
                byteArray[0] = (byte) ch1;
                byteArray[1] = (byte) ch2;
                byteArray[2] = (byte) ch3;
                byteArray[3] = (byte) ch4;

                //for read the pieces of the message if the byte array is large that the remote peer's sendBufferSize
                int toRead = length;
                int readBytes = 0;
               //int read = 0;
                int from = 4;
                int piece = 0;
                //while((toRead > 0) && (read >= 0))
                while(toRead > 0)
                {
                    //waiting for received piece
                    int read = dis.read(byteArray, from, toRead);
                    System.out.println(protocolID + " piece " + piece +" read (length) = " + read);

                    from+=read;
                    toRead-=read;

                    readBytes+=read;

                    //sending ACK
                    dos.write(65);//A = 0x41 = 65
                    dos.flush();
                    System.out.println(protocolID + " sending ACK " + piece);

                    piece++;
                }

                System.out.println(MessageFormat.format("bytes read = <{0}>, expected = <{1}>",
                        readBytes,
                        length));
            }
            else
            {
                close = true;
            }
        }
        catch (IOException ex)
        {
            Logger.getLogger(U2UFileSharingProtocol.class.getName()).log(Level.SEVERE, null, ex);
            close = true;
        }

        if(close || !socket.isConnected())
        {
            System.out.println(protocolID + " Making the Quit order or Response, because a socket close");
            if(this.mode == DOWNLOAD)
            {
                byteArray = (new U2UFSPResponseQuit()).getByteArrayRepresentation();
            }
            else if(this.mode == UPLOAD)
            {
                byteArray = (new U2UFSPOrderQuit()).getByteArrayRepresentation();
            }
        }

        return byteArray;
    }

    /**
     * write an byte array representation of an order or a response
     *
     * @param dis DataInputStream
     * @param dos DataOutputStream
     * @param obj Object of type Order or Response
     * 
     * @throws java.io.IOException
     */
    private synchronized void write(DataInputStream dis, DataOutputStream dos, Object obj)
    {
        byte[] completeArray = null;

        if(obj instanceof U2UFSProtocolOrder)
        {
            completeArray = ((U2UFSProtocolOrder)obj).getByteArrayRepresentation();
        }
        else if(obj instanceof U2UFSProtocolResponse)
        {
            completeArray = ((U2UFSProtocolResponse)obj).getByteArrayRepresentation();
        }

        try
        {

            if(completeArray != null)
            {
                int lengthArray = completeArray.length;
                int npieces = (lengthArray / sendBufferSize) + 1;
                //if completeArray length > Send buffer size

                for(int i = 0; i < npieces; i++)
                {
                    int lengthPiece = (i < (npieces-1) ? sendBufferSize : (lengthArray - sendBufferSize*(npieces-1)));
                    //send maximum the sendBufferSize
                    dos.write(completeArray, i*sendBufferSize, lengthPiece);
                    dos.flush();

                    System.out.println(protocolID + " piece " + i + ", write (length) = " + lengthPiece);

                    //waiting for received the ACK
                    int ack;
                    do
                    {
                        ack = dis.read();
                    } while((ack != 65) && socket.isConnected());//ACK = A = 0x41" = 65

                    if(!socket.isConnected())
                    {
                        i = npieces;
                    }

                    System.out.println(protocolID + " received ACK " + i + " = " + ack);

                }
            }

            dos.writeByte(-1);
            dos.flush();
        }
        catch (IOException ex)
        {
            Logger.getLogger(U2UFileSharingProtocol.class.getName()).log(Level.SEVERE, null, ex);
            try {
                //reducing the closing time
                socket.setSoTimeout(1);
                System.out.println(protocolID + " closing the socket, soTimeOut = " + socket.getSoTimeout());

                socket.close();
            } catch (IOException ex1) {
                Logger.getLogger(U2UFileSharingProtocol.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
    }

    /**
     * Sends the event to the listeners of the protocol
     * @param obj Response or Order to send to the listeners
     */
    private void invokeListenerMethod(Object obj) {

        // are there any registered protocol listeners,
        // generate the event and callback.
        long t0 = System.currentTimeMillis();

        Object[] allListeners = this.protocolListeners.toArray(new Object[0]);
        U2UFileSharingProtocolEvent event = null;
        String objId = null;

        if(obj instanceof U2UFSProtocolOrder)
        {
            U2UFSProtocolOrder order = (U2UFSProtocolOrder) obj;
            event = U2UFileSharingProtocolEvent.newOrderEvent(
                    order,
                    protocolID);

            objId = "order" + order.getOrderId();
        }
        else if(obj instanceof U2UFSProtocolResponse)
        {
            U2UFSProtocolResponse response = (U2UFSProtocolResponse) obj;
            event = U2UFileSharingProtocolEvent.newResponseEvent(
                    response,
                    protocolID);

            objId = "response" + response.getResponseId();
        }

        for (Object allListener : allListeners) {
            
            ((U2UFileSharingProtocolListener) allListener).protocolEvent(event);
        }

        System.out.println(protocolID + " Called all listenters to query " + objId +
                " in : " + (System.currentTimeMillis() - t0));
    }
    
    //static
    /**
     * Factory method that build an download instance of the Protocol with low security level.
     *
     * if any of the parameters are null or in unappropriated state the Protocol instance can't be build.
     * @param socket Socket instance
     * @param adv the content that is required
     * @param peerId Peer Id in String format
     * @return an instance of the protocol in download side
     */
    public static U2UFileSharingProtocol newDownloadInstance(JxtaSocket socket, U2UContentAdvertisementImpl adv, String peerId)
    {
        return newInstance(DOWNLOAD, socket, adv, peerId);
    }

    /**
     * Factory method that build an upload instance of the Protocol with low security level.
     *
     * if any of the parameters are null or in unappropriated state the Protocol instance can't be build.
     * @param socket Socket instance
     * @param peerId Peer Id in String format
     * @return an instance of the protocol in download side
     */
    public static U2UFileSharingProtocol newUploadInstance(JxtaSocket socket, String peerId)
    {
        return newInstance(UPLOAD, socket, null, peerId);
    }

    /**
     * Factory method that build a secure download instance of the Protocol.
     *
     * if any of the parameters are null or in unappropriated state the Protocol instance can't be build.
     * @param socket Socket instance
     * @param adv the content that is required
     * @return a secure instance of the protocol in upload side
     */
    public static U2UFileSharingProtocol newSecureDownloadInstance(JxtaSocket socket, U2UContentAdvertisementImpl adv)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Factory method that build an instance of the Protocol, it can be only of one type (Download or Upload)
     * not both, the mode can be get from the protocol class U2UFileSharingProtocol.DOWNLOAD or U2UFileSharingProtocol.UPLOAD,
     * the JxtaSocket is necesary by the protocol to talk with the remote peers's protocol, the U2UContentAdvertisementImpl
     * is necesary by the remote peer to find the U2UUploadingManager that server the file's chunks
     *
     * if any of the parameters are null or in unappropriated state the Protocol instance can't be build.
     *
     * @param mode UPLOAD or DOWNLOAD
     * @param sk Socket instance
     * @param adv the content that is required
     * @param peerId Peer Id in String format
     * @return an instance of the protocol
     */
    private static U2UFileSharingProtocol newInstance(int mode, JxtaSocket socket, U2UContentAdvertisementImpl adv, String peerId)
    {
        if( (mode != DOWNLOAD) && (mode != UPLOAD) )
        {
            throw new IllegalArgumentException("The mode isn't correct");
        }
        else if( socket == null )
        {
            throw new NullPointerException("The socket is null");
        }
        else if( socket.isClosed() )
        {
            throw new IllegalArgumentException("The socket is close");
        }
        else if( (adv == null) && (mode == DOWNLOAD) )
        {
            throw new NullPointerException("The adv is null");
        }
        //checking peerId
        else if(peerId == null)
        {
            throw new NullPointerException("Peer's Id can't be null");
        }
        //JXTA's ID have 80 bytes, with urn:jxta:uuid-
        else if(peerId.length() != 80)
        {
            throw new IllegalArgumentException("Peer's Id haven't the correct number of bytes");
        }

        return (new U2UFileSharingProtocol(mode, socket, adv, peerId));
    }

}