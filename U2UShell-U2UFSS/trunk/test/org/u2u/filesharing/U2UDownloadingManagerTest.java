/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.u2u.filesharing;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.peergroup.PeerGroup;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.platform.NetworkManager;
import org.bouncycastle.jce.provider.JDKMessageDigest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.u2u.common.db.ConnectTo;
import org.u2u.filesharing.downloadpeer.U2UContentDiscoveryEvent;
import org.u2u.filesharing.downloadpeer.U2UDownloadingManager;
import org.u2u.filesharing.downloadpeer.U2URequestManagerImpl;
import org.u2u.filesharing.downloadpeer.U2USearchListener;
import static org.junit.Assert.*;

/**
 *
 * @author sergio
 */
public class U2UDownloadingManagerTest {
    private static NetworkManager manager;
    private static PeerGroup group;
    private U2UContentAdvertisementImpl adv;

    public U2UDownloadingManagerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception
    {
        //Runtime.getRuntime().exec("rm -r jxtaUploading");
        //Runtime.getRuntime().exec("rm -r .jxta");

        manager = new NetworkManager(NetworkManager.ConfigMode.EDGE, "downloadingManager");

        if(!(new File(".jxta")).exists())
        {
            NetworkConfigurator conf = manager.getConfigurator();

            //conf.setTcpInterfaceAddress("192.168.0.3");

            /*Iterator it = IPUtils.getAllLocalAddresses();
            Vector<String> vector = new Vector<String>();*/
            String localHost = "192.168.0.3";//*/InetAddress.getLocalHost().getHostAddress();

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

            //conf.setTcpPort(2526);
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
        }

        //starting JXTA
        group = manager.startNetwork();

        manager.waitForRendezvousConnection(10000);
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
    public void downloadingChunkTest() throws InterruptedException, SQLException, IOException {

        //fss
        U2UFileSharingService fss = new U2UFileSharingService(group, false, false);
        Thread.sleep(1000);
        
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
        rm.searchContent("name", "Move");
        
        Thread.sleep(15000);

        this.adv = listener.getAdv();
        System.out.println("---Downloading a SharedContent(start)");
        if(adv !=null)
        {
           U2UDownloadingManager dm = new U2UDownloadingManager(fss, adv);
           (new Thread(dm, "downloadManager@"+adv.getContentId().toString())).start();
           //return true if the downloading start successful
           Thread.sleep(200000);
           //assertTrue(dm.start());
           //assertTrue(dm.download(adv));

           Thread.sleep(10000);

           assertTrue(dm.pause());

           Thread.sleep(5000);

           //assertTrue(dm.start());
        }
        else
        {
            fail();
        }

        Thread.sleep(10000);

        //sha1 file
        System.out.println("---Verificating the sha-1 of the downloaded file with the adv's cid");
        File file = new File("/Shared/img.png");
        byte[] hash;
        String sha1File = "";

        try {

            FileInputStream in = new FileInputStream(file);
            MessageDigest md = JDKMessageDigest.getInstance("SHA-1");
            int ch;
            while ((ch = in.read()) != -1) {
                md.update((byte) ch);
            }
            hash = md.digest();

            StringBuffer d = new StringBuffer();

            for(int i=0; i<hash.length; i++)
            {
                int v = hash[i] & 0xFF;
                if(v < 16)
                    d.append("0");

                d.append(Integer.toString(v, 16));
            }

            sha1File = d.toString();
            assertEquals(sha1File, adv.getContentId().toString().substring(5));

        } catch (IOException ex) {
            Logger.getLogger(U2UDownloadingManagerTest.class.getName()).log(Level.SEVERE, null, ex);
            fail();
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(U2UDownloadingManagerTest.class.getName()).log(Level.SEVERE, null, ex);
            fail();
        }

        //in the db?
        System.out.println("---Verificating the downloaded file is in the data base");

        ConnectTo con = new ConnectTo("org.apache.derby.jdbc.EmbeddedDriver");
        assertTrue(con.getConnection("jdbc:derby:U2UClient", "U2U", ""));

        ResultSet res = con.sQLQuery("SELECT COMPLETE FROM SHAREDFILES WHERE SHA1_SF='"+sha1File+"'");
        res.first();

        assertTrue(res.getString("COMPLETE").equals("T"));

        res.close();
        con.closeConnection();

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