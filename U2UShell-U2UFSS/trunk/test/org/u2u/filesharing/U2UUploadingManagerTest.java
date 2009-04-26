package org.u2u.filesharing;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jxta.peergroup.PeerGroup;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.platform.NetworkManager;
import net.jxta.share.FileContent;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.u2u.common.db.Chunks;
import org.u2u.common.db.SharedFiles;
import org.u2u.filesharing.uploadpeer.U2UContentManagerImpl;
import static org.junit.Assert.*;

/**
 *
 * @author sergio
 */
public class U2UUploadingManagerTest {
    private static NetworkManager manager;
    private static NetworkConfigurator conf;
    private static PeerGroup group;

    public U2UUploadingManagerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception
    {
        Runtime.getRuntime().exec("rm -r SocketServer");
        Runtime.getRuntime().exec("rm -r .jxta");

        manager = new NetworkManager(NetworkManager.ConfigMode.EDGE, "responseModule");
        conf = manager.getConfigurator();

        conf.setName("SocketServer");
        conf.setHome(new File("SocketServer"));
        //conf.setTcpInterfaceAddress("192.168.0.3");

        /*Iterator it = IPUtils.getAllLocalAddresses();
        Vector<String> vector = new Vector<String>();*/
        String localHost = "192.168.0.2";//InetAddress.getLocalHost().getHostAddress();

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
        conf.setUseMulticast(false);

        Set<String> set = new HashSet<String>();
        //set.add("tcp://u2u.homeunix.net:9701");
        //192.168.1.122 UIS BIBLIOTK
        //set.add("tcp://192.168.1.122:8080");
        //set.add("tcp://190.240.10.222:9701");//rdv/relay eMacSergio
        //set.add("tcp://"+localHost+":8080");
        set.add("tcp://192.168.0.3:8080");
        //conf.setRelaySeedURIs(new ArrayList<String>(set));
        conf.setRendezvousSeeds(set);

        //starting JXTA
        group = manager.startNetwork();

        manager.waitForRendezvousConnection(25000);

        
    }

    @AfterClass
    public static void tearDownClass() throws Exception
    {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
     @Test
     public void uploadingChunckTest() throws IOException {

        U2UFileSharingService fss = new U2UFileSharingService(group, false, false);
        //File f = new File("/img.png");
        File f = new File("/home/sergio/Move to the city.mp3");

        U2UContentManagerImpl cm = new U2UContentManagerImpl(fss);

        //register the file f in the db of shared files, verifier if the return is not null
        FileContent fc = cm.share(f);

        U2UContentAdvertisementImpl adv = (U2UContentAdvertisementImpl) fc.getContentAdvertisement();

        group.getDiscoveryService().remotePublish(adv);

     }

     @Test
     public void sendResponseGetChunkTaskTest()
     {
            long init = System.currentTimeMillis();
            //query the DB
            String sha1_sh = "842bd1e4fd077eb8b95dd202df815c0bb48b839a";
            U2UContentIdImpl mainCid = new U2UContentIdImpl("sha1:" + sha1_sh);
            sha1_sh = mainCid.toString();
            boolean status = false;
            //U2UFileSharingProtocol protocol = fspActiveReferences.get(protocolId);

            short index = 5;

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
                    //have all the chunks, we read the sharedFile from the shared file
                    if((downloadRelation == 1.0f) &&
                            (SharedFiles.getCompleteSFValue(mainCid)))
                    {
                        File sharedFile;
                        try {
                            sharedFile = new File(new URI(SharedFiles.getPathOfTheSharedFile(mainCid)));

                            if(sharedFile.exists())
                            {
                                FileInputStream fis = new FileInputStream(sharedFile);
                                FileChannel fch = fis.getChannel();

                                int chunkSize = SharedFiles.getChunkSizeOfTheSharedFile(mainCid);
                                short nchunks = (short) Chunks.numberOfChunks(sha1_sh);
                                int lengthChunk = (index < (nchunks-1) ? chunkSize*1024 : (int)(sharedFile.length() - (chunkSize*1024)*(nchunks-1)));

                                ByteBuffer buffer = fch.map(FileChannel.MapMode.READ_ONLY, (long)index*(chunkSize*1024), lengthChunk);

                                array = new byte[lengthChunk];
                                buffer.get(array);
                                System.out.println("bytes read <" + array.length + "> from chunk");
                            }

                        } catch (URISyntaxException ex) {
                            Logger.getLogger(U2UUploadingManagerTest.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (FileNotFoundException ex) {
                            Logger.getLogger(U2UUploadingManagerTest.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IOException ex) {
                            Logger.getLogger(U2UUploadingManagerTest.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    //haven't all the chunks, we read the sharedFile form the .chunks folder, where
                    //the chunks are save with the name, fileName =  "sha1_sf" + "-" + index
                    else if(downloadRelation > 0.0f)
                    {
                        File chunk = new File("/conf/chunks/" + sha1_sh.substring(5) + "-" + index);

                        if(chunk.exists() &&
                                (chunk.length() <= SharedFiles.getChunkSizeOfTheSharedFile(mainCid)*1024))
                        {
                            array = new byte[(int) chunk.length()];

                            try {
                                FileInputStream fis = new FileInputStream(chunk);

                                System.out.println("bytes read <" + fis.read(array) + "> from chunk");

                            } catch (FileNotFoundException ex) {
                                Logger.getLogger(U2UUploadingManagerTest.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (IOException ex) {
                                Logger.getLogger(U2UUploadingManagerTest.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }

                    //send RGET 211
                    if(array != null)
                    {
                        //status = protocol.responseRequestChunk(U2UFSPResponseGetChunk.R211, array);
                        System.out.println("sending protocol.responseRequestChunk(U2UFSPResponseGetChunk.R211, array);");
                    }
                    //send RGET 212
                    else
                    {
                        //status = protocol.responseRequestChunk(U2UFSPResponseGetChunk.R212, null);
                        System.out.println("sending protocol.responseRequestChunk(U2UFSPResponseGetChunk.R212, null);");
                    }
                }
                //send RGET 212
                else
                {
                    //status = protocol.responseRequestChunk(U2UFSPResponseGetChunk.R212, null);
                    System.out.println("sending protocol.responseRequestChunk(U2UFSPResponseGetChunk.R212, null);");
                }
            }
            //send RGET 511
            else
            {
                //status = protocol.responseRequestChunk(U2UFSPResponseGetChunk.R511, null);
                System.out.println("sending protocol.responseRequestChunk(U2UFSPResponseGetChunk.R511, null);");
            }
            System.out.println("--------------------RGET CHUNK in = " + (System.currentTimeMillis() - init));

     }

}