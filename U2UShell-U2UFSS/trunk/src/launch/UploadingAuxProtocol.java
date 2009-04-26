/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package launch;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.exception.PeerGroupException;
import net.jxta.peergroup.PeerGroup;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.platform.NetworkManager;
import net.jxta.share.FileContent;
import net.jxta.socket.JxtaServerSocket;
import net.jxta.socket.JxtaSocket;

import org.u2u.filesharing.U2UContentAdvertisementImpl;
import org.u2u.filesharing.U2UContentIdImpl;
import org.u2u.filesharing.U2UFileSharingService;
import org.u2u.filesharing.fsprotocol.U2UFSPOrderConnection;
import org.u2u.filesharing.fsprotocol.U2UFSPOrderFileInfo;
import org.u2u.filesharing.fsprotocol.U2UFSPOrderGetChunk;
import org.u2u.filesharing.fsprotocol.U2UFSPOrderQuit;
import org.u2u.filesharing.fsprotocol.U2UFSPResponseConnection;
import org.u2u.filesharing.fsprotocol.U2UFSPResponseFileInfo;
import org.u2u.filesharing.fsprotocol.U2UFSPResponseGetChunk;
import org.u2u.filesharing.fsprotocol.U2UFSProtocolOrder;
import org.u2u.filesharing.fsprotocol.U2UFSProtocolResponse;
import org.u2u.filesharing.fsprotocol.U2UFileSharingProtocol;
import org.u2u.filesharing.fsprotocol.U2UFileSharingProtocolEvent;
import org.u2u.filesharing.fsprotocol.U2UFileSharingProtocolListener;
import org.u2u.filesharing.uploadpeer.U2UContentManagerImpl;

/**
 *
 * @author sergio
 */
public class UploadingAuxProtocol implements Runnable{

    private static NetworkManager manager;
    private static PeerGroup group;
    private static U2UContentManagerImpl cm;

    public U2UContentAdvertisementImpl adv;
    public U2UFileSharingProtocol uploadProtocol;

    public UploadingAuxProtocol(U2UContentAdvertisementImpl n)
    {
        adv = n;
    }

    /**
     * wait for connections
     */
     public void run()
     {
        System.out.println("Starting ServerSocket");
        JxtaServerSocket serverSocket = null;
        try {
            //serverSocket = new JxtaServerSocket(group, createSocketAdvertisement());
            serverSocket = new JxtaServerSocket(group, this.adv.getSocketAdv());
            serverSocket.setSoTimeout(0);
        } catch (IOException e) {
            System.out.println("failed to create a server socket");
            e.printStackTrace();
            System.exit(-1);
        }
        while (true)
        {
            try {
                System.out.println("Waiting for connections");
                Socket socket = serverSocket.accept();
                if (socket != null) {
                    System.out.println("New socket connection accepted");
                    Thread thread = new Thread(new ConnectionHandler(socket),
                                                     "Connection Handler Thread");
                    thread.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

     }

     private class ConnectionHandler implements Runnable, U2UFileSharingProtocolListener  {
        Socket socket = null;
        int count = 0;

        ConnectionHandler(Socket socket) {
            this.socket = socket;
            this.count++;
        }
        /**
         * Sends data over socket
         *
         * @param socket the socket
         */
        private void sendAndReceiveData(JxtaSocket socket) {
            try {
                System.out.println("window size = <"+socket.getWindowSize()+"> getSendBufferSize = <" + socket.getSendBufferSize() +
                        "> & getReceiveBufferSize = <" + socket.getReceiveBufferSize() + ">");

                String thread = "("+count+")";
                //long start = System.currentTimeMillis();
                // get the socket output stream
                OutputStream out = socket.getOutputStream();
                DataOutputStream dos = new DataOutputStream(out);
                // get the socket input stream
                InputStream in = socket.getInputStream();
                DataInputStream dis = new DataInputStream(in);

                ArrayList<Byte> array = new ArrayList<Byte>();

                byte[] buf = new byte[45];
                //System.out.println(thread+"read from input "+in.read(buf));

                //CONN

                //incoming query
                array = read(dis, 5);

                byte[] byteArray = new byte[array.size()];

                for(int i = 0; i < array.size(); i++)
                {
                	byteArray[i] = array.get(i).byteValue();
                }

                System.out.println(thread+"read from input "+array.size());

                U2UFSPOrderConnection order = (U2UFSPOrderConnection) U2UFSProtocolOrder.parseOrderFromByteArray(byteArray);

                U2UContentIdImpl cid = new U2UContentIdImpl(order.getSha1File());
                String sha1 = new String(cid.toString());

                System.out.println("order id = " +
                		(order.getOrderId() == U2UFSProtocolOrder.CONN ? "CONN" : "ERROR") +
                		"\norder length = " + order.getLengthPrefix() +
                		"\norder protocol version = " + order.getProtocolVersion() +
                		"\norder sha1 = " + sha1 +
                		"\norder peerId = " + order.getPeerId());
                //

                //making the protocol for response to the order
                uploadProtocol = U2UFileSharingProtocol.newUploadInstance(socket, group.getPeerID().toString());
                System.out.println("remote peer id getting from the socket " + uploadProtocol.getProtocolID());
                new Thread(uploadProtocol, "protocol").start();

                uploadProtocol.addProtocolListener(this);

                //response through the protocol
                uploadProtocol.responseInit(U2UFSPResponseConnection.R401);

                uploadProtocol.responseInit(U2UFSPResponseConnection.R201);
                //EO CONN

                //FILEINFO

                //the protocol have the responsability


                //EO FILEINFO


                System.out.println(thread+"---"+order.getOrderId()+" ---"+sha1+"---");


                //Thread.sleep(50000);
                //socket.close();

                //Th
                /*Content[] content = cm.getContent(cid);

                if(content != null)
                {
                    System.out.println(thread+"---inside if(content != null)");
                    U2UFileContentImpl fileCon = (U2UFileContentImpl) content[0];
                    File file = fileCon.getFile();
                    FileInputStream fis = new FileInputStream(file);
                    FileChannel fch = fis.getChannel();

                    long lengthFile = file.length();
                    int nchunks = (int) (lengthFile / (64 * 1024)) + 1;//number of chunks

                    //byte's length equals to the chunk's size
                    ByteBuffer buffer = null;
                    System.out.println(thread+"----Init transfer"+new Date(System.currentTimeMillis()));
                    for(int i = 0; i < nchunks; i++)
                    {
                    	System.out.println(thread+"loop "+i);
                        //from file

                    	int lengthChunk = (i < (nchunks-1) ? 64*1024 : (int)(lengthFile - (64*1024)*(nchunks-1)));

                        buffer = fch.map(FileChannel.MapMode.READ_ONLY, (long)i*(64*1024), lengthChunk);

                        buf = new byte[lengthChunk];
                        buffer.get(buf);
                        dos.write(buf);
                        dos.flush();

                        byte[] resB = new byte[1];
                        in.read(resB);
                        String res = new String(resB);
                        System.out.println("response "+res);

                    }
                    System.out.println(thread+"----Finish transfer"+new Date(System.currentTimeMillis()));
                }

                out.close();
                in.close();
                socket.close();
                System.out.println(thread+"Connection closed");*/
            } catch (Exception ie) {
                ie.printStackTrace();
            }
        }

        public void protocolEvent(U2UFileSharingProtocolEvent event) {

    		//para servidor es getOrder, si es cliente y hace getOrder se devuelve null
            U2UFSProtocolOrder ord = event.getOrder();

            switch(ord.getOrderId())
            {
                case U2UFSProtocolOrder.CONN:
                    U2UFSPOrderConnection oC = (U2UFSPOrderConnection)ord;

                    break;

                case U2UFSProtocolOrder.INFO:
                    U2UFSPOrderFileInfo fI = (U2UFSPOrderFileInfo)ord;
                    if(fI.getQueryType() == U2UFSPOrderFileInfo.PEER_CHUNKS)
                    {
                    	try {
    						uploadProtocol.responseFileInfoQueryPeerChunks(
    								U2UFSPResponseFileInfo.R222, true, new short[] {1,3,6});
    					} catch (InterruptedException e) {
    						// TODO Auto-generated catch block
    						e.printStackTrace();
    					}
                    }
                    else if(fI.getQueryType() == U2UFSPOrderFileInfo.CHUNKS_SHA1)
                    {
                        HashMap<Short, String> list = new HashMap<Short, String>();

                        list.put((short)1, "6406ad9f06ea994500905be16adbf6716a4edb1a");
                        list.put((short)2, "380b59083cde305f0bb050b243e7ec910429f750");
                        list.put((short)3, "c4d359b4dea834f932626c73561176bcda0c67ec");
                        list.put((short)4, "12e312c0707c57b8d3d24d15cc10f2aa0aabb967");
                        list.put((short)5, "3cd7bdc0ae0f1537cd01330777e1ec48d90e84b4");
                        list.put((short)6, "77682623f6263d37dd219ee8d7c02a96b305073c");
                        try
                        {
                            uploadProtocol.responseFileInfoQueryChunksSha1(U2UFSPResponseFileInfo.R221, list);
                        } catch (InterruptedException ex)
                        {
                            Logger.getLogger(UploadingAuxProtocol.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    break;

                case U2UFSProtocolOrder.GETC:
                    U2UFSPOrderGetChunk gC = (U2UFSPOrderGetChunk)ord;
                    File file = new File("/home/sergio/Desktop/Move/Move to the city.mp3-c"+gC.getChunkIndex());
                    //File file = new File("/home/sergio/Desktop/Move/Move to the city.mp3");
                    if(file.exists())
                    {
                        byte[] array = new byte[(int) file.length()];

                        try {
                            FileInputStream fis = new FileInputStream(file);

                            System.out.println("bytes read <" + fis.read(array) + ">");

                            uploadProtocol.responseRequestChunk(U2UFSPResponseGetChunk.R211, array);

                        } catch (InterruptedException ex) {
                            Logger.getLogger(UploadingAuxProtocol.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (FileNotFoundException ex) {
                            Logger.getLogger(UploadingAuxProtocol.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IOException ex) {
                            Logger.getLogger(UploadingAuxProtocol.class.getName()).log(Level.SEVERE, null, ex);
                        }


                    }
                    break;

                case U2UFSProtocolOrder.QUIT:
                    U2UFSPOrderQuit q = (U2UFSPOrderQuit)ord;
                    JOptionPane.showMessageDialog(null, "Quit message incoming");
                    break;
            }
    	}

        private ArrayList<Byte> read(DataInputStream dis, int ensureCapacity) throws IOException
        {
            ArrayList<Byte> array = new ArrayList<Byte>();
            array.ensureCapacity(ensureCapacity);

            byte inByte;
            while ((inByte = (byte) dis.readByte()) != -1)
            {
                array.add(inByte);
            }

            return array;
        }

        private void write(DataOutputStream dos, U2UFSProtocolResponse response) throws IOException
        {
            dos.write(response.getByteArrayRepresentation());
            dos.writeByte(-1);
            dos.flush();
        }

        public void run() {
            sendAndReceiveData((JxtaSocket) socket);
        }
    }

     /**
     * main
     *
     * @param args command line args
     */
    public static void main(String args[]) {

        try
        {
            //Runtime.getRuntime().exec("rm -r SocketServer");
            //Runtime.getRuntime().exec("rm -r .jxta");

            manager = new NetworkManager(NetworkManager.ConfigMode.EDGE, "responseModule");

            if(!(new File(".jxta")).exists())
            {
                NetworkConfigurator conf = manager.getConfigurator();

                conf.setName(JOptionPane.showInputDialog("Peers Name?"));
                //conf.setTcpInterfaceAddress("192.168.0.3");

                /*Iterator it = IPUtils.getAllLocalAddresses();
                Vector<String> vector = new Vector<String>();*/
                //String localHost = "192.168.109.79";///InetAddress.getLocalHost().getHostAddress();

                String localHost = JOptionPane.showInputDialog("Ingrese la ip de la maquina");//*/InetAddress.getLocalHost().getHostAddress();


                JOptionPane.showMessageDialog(null, localHost);
                /*while(it.hasNext())
                {
                    String st = it.next().toString().substring(1);
                    if(st.equals(localHost))
                        vector.add(0, st);
                    else
                        vector.add(st);
                }

                for(int i = 0; i < vector.size(); i++)
                {
                    System.out.println(vector.get(i));
                }*/

                conf.setTcpInterfaceAddress(localHost);
    
                //conf.setTcpPort(0);
                conf.setUseMulticast(false);

                Set<String> set = new HashSet<String>();
                //set.add("tcp://u2u.homeunix.net:9701");
                //192.168.1.122 UIS BIBLIOTK
                //set.add("tcp://192.168.1.122:8080");
                //set.add("tcp://190.240.10.222:9701");//rdv/relay eMacSergio
                //set.add("tcp://192.168.109.78:8080");
                //set.add("tcp://192.168.0.3:8080");
                //set.add("tcp://"+localHost+":8080");
                //set.add("tcp://192.168.0.3:8080");
                set.add("tcp://"+JOptionPane.showInputDialog("Ingrese la ip del rendezvous tcp://"));
                //conf.setRelaySeedURIs(new ArrayList<String>(set));
                conf.setRendezvousSeeds(set);
            }


            //starting JXTA
            group = manager.startNetwork();

            manager.waitForRendezvousConnection(25000);

        } catch (PeerGroupException ex)
        {
            Logger.getLogger(UploadingAuxProtocol.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex)
        {
            Logger.getLogger(UploadingAuxProtocol.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {

            U2UFileSharingService fss = new U2UFileSharingService(group, false, false);
            //File f = new File("/img.png");
            //File f = new File("/home/sergio/Move to the city.mp3");
            JFileChooser chosser = new JFileChooser();

            if(chosser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
            {
                File f = chosser.getSelectedFile();

                cm = new U2UContentManagerImpl(fss);

                DiscoveryService dis = group.getDiscoveryService();
                Enumeration en = dis.getLocalAdvertisements(DiscoveryService.ADV, null, null);
                while(en.hasMoreElements())
                {
                    dis.flushAdvertisement((Advertisement) en.nextElement());
                }
                en = dis.getLocalAdvertisements(DiscoveryService.PEER, null, null);
                while(en.hasMoreElements())
                {
                    dis.flushAdvertisement((Advertisement) en.nextElement());
                }
                //register the file f in the db of shared files, verifier if the return is not null
                FileContent fc = cm.share(f);

                U2UContentAdvertisementImpl adv = (U2UContentAdvertisementImpl) fc.getContentAdvertisement();





                UploadingAuxProtocol socEx = new UploadingAuxProtocol(adv);
                new Thread(socEx, "UploadingAuxProtocol").start();
            }

            

        } catch (Throwable e) {
            System.err.println("Failed : " + e);
            e.printStackTrace(System.err);
            System.exit(-1);
        }
    }


}