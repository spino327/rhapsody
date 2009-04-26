/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package launch;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.jxta.exception.PeerGroupException;
import net.jxta.peergroup.PeerGroup;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.platform.NetworkManager;
import net.jxta.share.Content;
import net.jxta.share.FileContent;
import net.jxta.socket.JxtaServerSocket;

import org.u2u.filesharing.U2UContentAdvertisementImpl;
import org.u2u.filesharing.U2UContentIdImpl;
import org.u2u.filesharing.U2UFileContentImpl;
import org.u2u.filesharing.U2UFileSharingService;
import org.u2u.filesharing.uploadpeer.U2UContentManagerImpl;

/**
 *
 * @author sergio
 */
public class UploadingAux {
    private static NetworkManager manager;
    private static PeerGroup group;
    private static U2UContentManagerImpl cm;

    public U2UContentAdvertisementImpl adv;

    public UploadingAux(U2UContentAdvertisementImpl n)
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

     private class ConnectionHandler implements Runnable {
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
        private void sendAndReceiveData(Socket socket) {
            try {

                String thread = "("+count+")";
                //long start = System.currentTimeMillis();
                // get the socket output stream
                OutputStream out = socket.getOutputStream();
                DataOutputStream dos = new DataOutputStream(out);
                // get the socket input stream
                InputStream in = socket.getInputStream();

                byte[] buf = new byte[45];
                System.out.println(thread+"read from input "+in.read(buf));
                String sha1 = new String(buf);
                System.out.println(thread+"---"+sha1+"---");
                buf = null;

                Content[] content = cm.getContent(new U2UContentIdImpl(sha1));

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
                System.out.println(thread+"Connection closed");
            } catch (Exception ie) {
                ie.printStackTrace();
            }
        }

        public void run() {
            sendAndReceiveData(socket);
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
            Runtime.getRuntime().exec("rm -r SocketServer");
            Runtime.getRuntime().exec("rm -r .jxta");

            manager = new NetworkManager(NetworkManager.ConfigMode.EDGE, "responseModule");
            NetworkConfigurator conf = manager.getConfigurator();

            conf.setName("SocketServer");
            conf.setHome(new File("SocketServer"));
            //conf.setTcpInterfaceAddress("192.168.0.3");

            /*Iterator it = IPUtils.getAllLocalAddresses();
            Vector<String> vector = new Vector<String>();*/
            String localHost = InetAddress.getLocalHost().getHostAddress();

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

            conf.setTcpPort(2526);

            Set<String> set = new HashSet<String>();
            //set.add("tcp://u2u.homeunix.net:9701");
            //192.168.1.122 UIS BIBLIOTK
            //set.add("tcp://192.168.1.122:8080");
            //set.add("tcp://190.240.10.222:9701");//rdv/relay eMacSergio
            set.add("tcp://"+localHost+":8080");
            //conf.setRelaySeedURIs(new ArrayList<String>(set));
            conf.setRendezvousSeeds(set);

            //starting JXTA
            group = manager.startNetwork();

            manager.waitForRendezvousConnection(25000);

        } catch (PeerGroupException ex)
        {
            Logger.getLogger(UploadingAux.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex)
        {
            Logger.getLogger(UploadingAux.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {

            U2UFileSharingService fss = new U2UFileSharingService(group, false, false);
            //File f = new File("/img.png");
            File f = new File("/Move to the city.mp3");

            cm = new U2UContentManagerImpl(fss);

            //register the file f in the db of shared files, verifier if the return is not null
            FileContent fc = cm.share(f);

            U2UContentAdvertisementImpl adv = (U2UContentAdvertisementImpl) fc.getContentAdvertisement();

            group.getDiscoveryService().remotePublish(adv);

            UploadingAux socEx = new UploadingAux(adv);
            socEx.run();

        } catch (Throwable e) {
            System.err.println("Failed : " + e);
            e.printStackTrace(System.err);
            System.exit(-1);
        }
    }
}