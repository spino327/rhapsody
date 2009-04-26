/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


package org.u2u.filesharing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.peergroup.PeerGroup;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.platform.NetworkManager;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.socket.JxtaSocket;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.u2u.filesharing.downloadpeer.U2UContentDiscoveryEvent;
import org.u2u.filesharing.downloadpeer.U2URequestManagerImpl;
import org.u2u.filesharing.downloadpeer.U2USearchListener;
import org.u2u.filesharing.fsprotocol.U2UFSPResponseGetChunk;
import org.u2u.filesharing.fsprotocol.U2UFSPResponseConnection;
import org.u2u.filesharing.fsprotocol.U2UFSPResponseFileInfo;
import org.u2u.filesharing.fsprotocol.U2UFileSharingProtocol;
import org.u2u.filesharing.fsprotocol.U2UFileSharingProtocolEvent;
import org.u2u.filesharing.fsprotocol.U2UFileSharingProtocolListener;
import org.u2u.filesharing.fsprotocol.U2UFSProtocolResponse;
import static org.junit.Assert.*;


/**
 *
 * @author Sergio e Irene
 */
public class U2UFileSharingProtocolDownloadTest {
    private static NetworkManager manager;
    private static PeerGroup group;
    private static NetworkConfigurator conf;
    private U2UContentAdvertisementImpl adv;
    private HashMap<Short, byte[]> chunksHash;

    public U2UFileSharingProtocolDownloadTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception
    {
//        Runtime.getRuntime().exec("rm -r jxtaClientSide");
//        Runtime.getRuntime().exec("rm -r .jxta");

        manager = new NetworkManager(NetworkManager.ConfigMode.EDGE, "ProtocolClientSide");
        conf = manager.getConfigurator();

        conf.setName("ProtocolClientSide");
        conf.setHome(new File("jxtaClientSide"));
        conf.setTcpInterfaceAddress("192.168.0.3");
        //String localHost = InetAddress.getLocalHost().getHostAddress();
        //conf.setTcpInterfaceAddress(localHost);
        conf.setTcpPort(2525);

        Set<String> set = new HashSet<String>();
        //set.add("tcp://u2u.homeunix.net:9701");
        set.add("tcp://192.168.0.3:8080");
        //set.add("tcp://"+localHost+":8080");
        //configurator.setRelaySeedURIs(new ArrayList<String>(set));
        conf.setRendezvousSeeds(set);

        //starting JXTA
        group = manager.startNetwork();

        manager.waitForRendezvousConnection(10000);

    }

    @AfterClass
    public static void tearDownClass() throws Exception
    {
        manager.stopNetwork();
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void clientSideTest() throws IOException, InterruptedException
    {
        adv = null;

        U2UFileSharingService fss = new U2UFileSharingService(group, false, false);
        //erasing the cache
        Enumeration en = group.getDiscoveryService().getLocalAdvertisements(DiscoveryService.ADV,
                null, null);

        while(en.hasMoreElements())
        {
            group.getDiscoveryService().flushAdvertisement((Advertisement)en.nextElement());
        }

        en = null;
        //
        System.out.println("---Requesting Content Information");

        U2URequestManagerImpl rm = new U2URequestManagerImpl(fss);
        adv = null;

        U2UListenerImpl listener = new U2UListenerImpl(adv);
        new Thread(listener, "listenerU2USearchListener").start();

        rm.addSearchListener(listener);
        rm.searchContent("name", "Move to the city");

        Thread.sleep(5000);
        adv = listener.getAdv();
        //
        System.out.println("---Connecting with the UploadPeer");
        //getting the PipeAdv

        assertTrue(adv != null);

        PipeAdvertisement pipeAdv = adv.getSocketAdv();

        JxtaSocket socket = new JxtaSocket(group,
                //no specific peerid
                null,
                pipeAdv,
                //connection timeout: 5 seconds
                5000,
                // reliable connection
                true);

        System.out.println("window size = <"+socket.getWindowSize()+"> & getSendBufferSize = <" + socket.getSendBufferSize() +
                "> & getReceiveBufferSize = <" + socket.getReceiveBufferSize() + ">");

        assertTrue(socket != null);

        //client side
        U2UFileSharingProtocol protocol = U2UFileSharingProtocol.newDownloadInstance(socket, adv, group.getPeerID().toString());
        new Thread(protocol, "protocol").start();

        assertTrue(protocol != null);

        //register the ProtocolListener
        U2UFSPListenerImpl plistener = new U2UFSPListenerImpl();
        new Thread(plistener, "protocolListener").start();

        protocol.addProtocolListener(plistener);

        //1)init the metting, this method is invoke in both sides.
        //is equivalent in the client side to sends the first type(CONN) of msg of the Protocol,
        //DownloadPeer requests connection for the file with SHA-1
        assertTrue(protocol.init());

        Thread.sleep(10000);

        //first response, msg 2 of the protocol
        System.out.println("response = "+plistener.response);
        assertEquals("201", plistener.response);


        //2) remote peer's chunks
        assertTrue(protocol.fileInfoQueryPeerChunks());

        Thread.sleep(10000);

        ArrayList cList = plistener.chunksList;

        assertTrue(cList != null);

        assertTrue(cList.size() == 2);

        char ch = ((Character) cList.get(0)).charValue();
        assertTrue((ch == 'T') || (ch == 'F'));

        short[] expectedPos = new short[] {1,3,6};
        short[] incomingPos = (short[]) cList.get(1);
        for(int i = 0; i < expectedPos.length; i++)
        {
            assertEquals(expectedPos[i], incomingPos[i]);
        }

        assertTrue(cList.get(1).getClass() == short[].class);

        //2.1) list of chunks' hash
        assertTrue(protocol.fileInfoQueryChunksSha1());

        Thread.sleep(20000);

        chunksHash = plistener.cHash;

        assertTrue(chunksHash != null);

        assertEquals(6, chunksHash.size());

        String[] expectedSha1 = new String[] {
            "6406ad9f06ea994500905be16adbf6716a4edb1a",
            "380b59083cde305f0bb050b243e7ec910429f750",
            "c4d359b4dea834f932626c73561176bcda0c67ec",
            "12e312c0707c57b8d3d24d15cc10f2aa0aabb967",
            "3cd7bdc0ae0f1537cd01330777e1ec48d90e84b4",
            "77682623f6263d37dd219ee8d7c02a96b305073c"
        };
        
        for(Map.Entry<Short, byte[]> entry : chunksHash.entrySet())
        {
            //hash to String
            short pos = entry.getKey();
            U2UContentIdImpl cid = new U2UContentIdImpl(entry.getValue());
            System.out.println("chunk pos = " + pos + " value = " + cid.toString());
            assertEquals(expectedSha1[pos-1], cid.toString().substring(5));
        }

        //3) requests chunk with SHA-1

        int chunk = expectedPos[2];

        assertTrue(protocol.requestChunk(chunk));

        Thread.sleep(10000);

        FileInputStream fis = new FileInputStream("/moveChunk.mp3");

        U2UContentIdImpl cid = new U2UContentIdImpl(fis);
        assertEquals(expectedSha1[chunk-1], cid.toString().substring(5));

        //4) QUIT
        assertTrue(protocol.finish());

        Thread.sleep(10000);
        assertTrue(socket.isClosed());


    }

}
class U2UListenerImpl implements U2USearchListener, Runnable
{
    private U2UContentAdvertisementImpl adv;

    public U2UListenerImpl(U2UContentAdvertisementImpl adv)
    {
        this.adv = adv;
    }

    public void contentAdvertisementEvent(U2UContentDiscoveryEvent event)
    {
        Enumeration en = event.getResponseAdv();

        adv = (U2UContentAdvertisementImpl)en.nextElement();
        System.out.println("---arrived a Adv");
    }

    public void run()
    {
        while(true)
        {

        }
    }

    public U2UContentAdvertisementImpl getAdv()
    {
        return adv;
    }
}

class U2UFSPListenerImpl implements U2UFileSharingProtocolListener, Runnable
{
    String response;
    String rFileInfo;
    ArrayList chunksList;
    HashMap<Short, byte[]> cHash;

    public void protocolEvent(U2UFileSharingProtocolEvent event)
    {
        //para servidor es getOrder, si es cliente y hace getOrder se devuelve null
        U2UFSProtocolResponse res = event.getResponse();

        switch(res.getResponseId())
        {
            case U2UFSProtocolResponse.RCONN:
                U2UFSPResponseConnection rC = (U2UFSPResponseConnection)res;
                this.response = rC.getResponseNumber();
                break;

            case U2UFSProtocolResponse.RINFO:
                U2UFSPResponseFileInfo rFI = (U2UFSPResponseFileInfo)res;
                System.out.println("response File Info " + rFI.getResponseNumber());

                if(rFI.getQueryType() == U2UFSPResponseFileInfo.PEER_CHUNKS)
                {
                    ArrayList array = (ArrayList) rFI.getList();

                    System.out.println("array(0) = " + array.get(0));
                    short[] chunks = (short[]) array.get(1);
                    System.out.println("array(1) = " + chunks.length + " chunk(s)");
                    for(short n : chunks)
                    {
                        System.out.println(n);
                    }
                    //rFileInfo = rFI.getValue();
                    chunksList = array;
                }
                else if(rFI.getQueryType() == U2UFSPResponseFileInfo.CHUNKS_SHA1)
                {
                    cHash = (HashMap<Short, byte[]>) rFI.getList();
                }

                break;

            case U2UFSProtocolResponse.RGETC:
                U2UFSPResponseGetChunk rGC = (U2UFSPResponseGetChunk)res;

                File file = new File("/moveChunk.mp3");
                try {
                    FileOutputStream fos = new FileOutputStream(file);

                    fos.write(rGC.getChunkBytes());

                } catch (FileNotFoundException ex) {
                    Logger.getLogger(U2UFSPListenerImpl.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(U2UFSPListenerImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                break;
        }
    }

    public void run()
    {
        while(true)
        {

        }
    }

}