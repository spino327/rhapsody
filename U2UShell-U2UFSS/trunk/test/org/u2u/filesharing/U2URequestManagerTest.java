/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.u2u.filesharing;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.u2u.filesharing.downloadpeer.U2UContentDiscoveryEvent;
import org.u2u.filesharing.downloadpeer.U2USearchListener;
import java.io.File;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.peergroup.PeerGroup;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.platform.NetworkManager;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.u2u.filesharing.downloadpeer.U2URequestManagerImpl;
import static org.junit.Assert.*;


/**
 *
 * @author sergio
 */
public class U2URequestManagerTest {

    private static NetworkManager manager;
    private static PeerGroup group;
    private boolean status;

    public U2URequestManagerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception
    {
        manager = new NetworkManager(NetworkManager.ConfigMode.EDGE, "U2UFSTest");
        
        NetworkConfigurator conf = manager.getConfigurator();
        
        conf.setName("RequestManagerTest");
        conf.setTcpEnabled(true);
        conf.setTcpInterfaceAddress("192.168.0.3");
        
        conf.setHome(new File("jxtaRequest"));
        conf.setTcpPort(2525);
        
        Set<String> set = new HashSet<String>();
        //set.add("tcp://u2u.homeunix.net:9701");
        set.add("tcp://192.168.0.3:8080");
        //configurator.setRelaySeedURIs(new ArrayList<String>(set));
        conf.setRendezvousSeeds(set);
        
        group = manager.startNetwork();
     
        manager.waitForRendezvousConnection(25000);
        
        Thread.sleep(10000);
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
    public void searchTest() throws InterruptedException {
   
        U2UFileSharingService fss = new U2UFileSharingService(group, false, false);
        U2URequestManagerImpl rm = new U2URequestManagerImpl(fss);

        DiscoveryService discovery = group.getDiscoveryService();
        try {

            Enumeration en = discovery.getLocalAdvertisements(DiscoveryService.ADV, null, null);

            while(en.hasMoreElements())
                discovery.flushAdvertisement((Advertisement)en.nextElement());
            
        } catch (IOException ex) {
            Logger.getLogger(U2URequestManagerTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        String content = ".png";

        status = false;

        U2USearchListener listener = new U2USearchListener() {

           public void contentAdvertisementEvent(U2UContentDiscoveryEvent event)
           {
               Enumeration en = event.getResponseAdv();

               while(en.hasMoreElements())
                   System.out.println(en.nextElement());

               assertTrue(true);
               status = true;
           }
        };

        rm.addSearchListener(listener);
        assertTrue(rm.searchContent("name", content));
        Thread.sleep(30000);

        //if the event was received
        assertTrue(status);

        //remove the listener
        assertTrue(rm.removeSearchListener(listener));
    }

}
