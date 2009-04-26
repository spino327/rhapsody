/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package launch;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.text.MessageFormat;
import java.util.Enumeration;
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

import net.jxta.util.SimpleSelectable;
import net.jxta.util.SimpleSelector;
import org.u2u.filesharing.U2UContentAdvertisementImpl;
import org.u2u.filesharing.U2UContentIdImpl;
import org.u2u.filesharing.U2UFileSharingService;
import org.u2u.filesharing.fsprotocol.U2UFSPOrderConnection;
import org.u2u.filesharing.fsprotocol.U2UFSProtocolOrder;
import org.u2u.filesharing.fsprotocol.U2UFSProtocolResponse;
import org.u2u.filesharing.uploadpeer.U2UContentManagerImpl;
import org.u2u.filesharing.uploadpeer.U2UUploadingManager;

/**
 *
 * @author sergio
 */
public class UploadingManagerAux {
    
    private static NetworkManager manager;
    private static PeerGroup group;
    private static U2UContentManagerImpl cm;

    private U2UContentAdvertisementImpl adv;
    private static U2UFileSharingService fss;
    private U2UUploadingManager uploadingManager;

    public UploadingManagerAux(U2UContentAdvertisementImpl n)
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
                System.out.println("Waiting for connections, Server Socket soTimeOut = " + serverSocket.getSoTimeout());
                Socket socket = serverSocket.accept();
                System.out.println("socket soTimeOut = " + socket.getSoTimeout());
                socket.setSoTimeout(10*60000);
                System.out.println("socket soTimeOut = " + socket.getSoTimeout());

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

     private class ConnectionHandler implements Runnable  {
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
                        "> & getReceiveBufferSize = <" + socket.getReceiveBufferSize() + ">" +
                        " & getRetryTimeout = <" + socket.getRetryTimeout() + ">");

                String thread = "("+count+")";
                //long start = System.currentTimeMillis();
                // get the socket output stream
                OutputStream out = socket.getOutputStream();
                DataOutputStream dos = new DataOutputStream(out);
                // get the socket input stream
                InputStream in = socket.getInputStream();
                DataInputStream dis = new DataInputStream(in);

                //ArrayList<Byte> array = new ArrayList<Byte>();
                //System.out.println(thread+"read from input "+in.read(buf));
            
                //CONN

                //incoming query
               // array = read(dis, 5);

                /*byte[] byteArray = new byte[array.size()];

                for(int i = 0; i < array.size(); i++)
                {
                	byteArray[i] = array.get(i).byteValue();
                }*/

                byte[] byteArray = readByteArray(dis, dos);

                //System.out.println(thread+"read from input "+array.size());
                System.out.println(thread+"read from input "+byteArray.length);

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

                //EO CONN

                //using the UploadingManager
                if(uploadingManager == null)
                {
                    uploadingManager = new U2UUploadingManager(fss, cid);
                    (new Thread(uploadingManager, uploadingManager.getUploadingID())).start();
                }

                Thread.sleep(1000);
                System.out.println(thread + " attending the incoming CONN request");
                uploadingManager.attendConnRequest(socket, order);

                System.out.println(thread + "---"+order.getOrderId()+" ---"+sha1+"---");

            } catch (Exception ie) {
                ie.printStackTrace();
            }
        }

        /*private ArrayList<Byte> read(DataInputStream dis, int ensureCapacity) throws IOException
        {
            ArrayList<Byte> array = new ArrayList<Byte>();
            array.ensureCapacity(ensureCapacity);

            byte inByte;
            while ((inByte = (byte) dis.readByte()) != -1)
            {
                array.add(inByte);
            }

            return array;
        }*/

        private synchronized byte[] readByteArray(DataInputStream dis, DataOutputStream dos) throws IOException
        {
            //read the length prefix
            // (from the DataInputStream implementation)the next four bytes of this input stream, interpreted as an int
            int length;
            byte[] byteArray = null;
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
                    System.out.println(" piece " + piece +" read (length) = " + read);

                    from+=read;
                    toRead-=read;

                    readBytes+=read;

                    //sending ACK
                    dos.write(65);
                    dos.flush();
                    System.out.println(" sending ACK " + piece);

                    piece++;
                }

                System.out.println(MessageFormat.format("bytes read = <{0}>, expected = <{1}>",
                        readBytes,
                        length));
            }

            return byteArray;
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

                conf.setName(JOptionPane.showInputDialog("Peers Name?")+InetAddress.getLocalHost().getHostName());
                //conf.setTcpInterfaceAddress("192.168.0.3");

                /*Iterator it = IPUtils.getAllLocalAddresses();
                Vector<String> vector = new Vector<String>();*/
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
            Logger.getLogger(UploadingManagerAux.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex)
        {
            Logger.getLogger(UploadingManagerAux.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {

            fss = new U2UFileSharingService(group, false, false);
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

                System.out.println("the SHA-1 digest is = "+ adv.getContentId().toString());

                UploadingManagerAux socEx = new UploadingManagerAux(adv);
                socEx.run();
            }

            

        } catch (Throwable e) {
            System.err.println("Failed : " + e);
            e.printStackTrace(System.err);
            System.exit(-1);
        }
    }


}