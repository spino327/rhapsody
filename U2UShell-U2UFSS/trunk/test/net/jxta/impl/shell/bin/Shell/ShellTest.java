/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jxta.impl.shell.bin.Shell;

import java.io.IOException;
import java.net.InetAddress;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import net.jxta.exception.PeerGroupException;
import net.jxta.impl.shell.ConsoleShellConsole;
import net.jxta.peergroup.PeerGroup;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.platform.NetworkManager;

import net.jxta.protocol.PeerAdvertisement;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;



/**
 *
 * @author sergio
 */
public class ShellTest {
    
    private static PeerGroup netPG;
    private static Shell shell;
    private static NetworkManager manager;
    
    private boolean status;
    private boolean status2;
    private long longTime;
    
    public ShellTest() {
        longTime = 10000;
    }

    @BeforeClass
    public static void setUpClass() throws Exception
    {
        //conectandonos a la red JXTA
        try
        {            
            manager = new NetworkManager(NetworkManager.ConfigMode.EDGE, "Receiver");
            //manager.setInfrastructureID(peerGroupID);
            //performing tunning configurations
            NetworkConfigurator configurator = manager.getConfigurator();
            configurator.setTcpInterfaceAddress(InetAddress.getLocalHost().getHostAddress());
            
            Set<String> set = new HashSet<String>();
            //set.add("tcp://u2u.homeunix.net:9701");
            set.add("tcp://192.168.0.3:8080");
            //configurator.setRelaySeedURIs(new ArrayList<String>(set));
            configurator.setRendezvousSeeds(set);
            
            //starting JXTA
            netPG = manager.startNetwork();
            
            manager.waitForRendezvousConnection(25000);
       
            shell = new Shell(netPG);
        }
        catch(IOException e)
        {
            System.out.println("No se pudo recuperar la direccion IP - Fallo"); 
            e.printStackTrace(); 
            System.exit(1);
        }
        catch(PeerGroupException e)
        {
            System.out.println("No se pudo crear el PeerGroup - Fallo"); 
            e.printStackTrace(); 
            System.exit(1);
        }
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

    /**
     * Test of isEmbedded method, of class Shell.
     */
    @Test
    public void testIsShellEmbedded()
    {
        System.out.println("isShellEmbedded");
        boolean expResult = true;
        boolean result = (shell.getConsole() instanceof ConsoleShellConsole ? true : false);
        assertEquals(expResult, result);
    }
    
    /**
     * 
     */
    @Test
    public void testExecuteCmd()
    {
        System.out.println("Test executeCmd");
        
        assertTrue(shell.executeCmd("peers -r"));
        assertTrue(shell.executeCmd("peers"));
        assertTrue(shell.executeCmd("mygroup = newpgrp -n irene"));
        assertTrue(shell.executeCmd("groups -r"));
        assertTrue(shell.executeCmd("groups"));
        assertTrue(shell.executeCmd("env"));      
    }
    
    /**
     * Test of getShellObjectEnv method, which return a object inside of ShellObject. 
     * ShellObject is getting from the ShellEnv  using a specific key.
     */
    @Test
    public void testGetShellObjectEnv() 
    {
        System.out.println("Test getShellObjectEnv");
        
        Object obj = null;
        
        obj = shell.getShellObjectEnv("peers");
        assertNotNull(obj);
        
        obj = shell.getShellObjectEnv("peer0");
        assertNotNull(obj);
        
        assertTrue(obj instanceof PeerAdvertisement);
        
        obj = shell.getShellObjectEnv("peer1bbbbbb");
        assertNull(obj);
        
        obj = shell.getShellObjectEnv("peer0");
        if(obj!=null && obj instanceof PeerAdvertisement)
        {


            PeerAdvertisement adv = (PeerAdvertisement)obj;
            adv.setName("TestJUnit");
            
            assertNotSame(adv.getName(), ((PeerAdvertisement)shell.getShellObjectEnv("peer0")).getName());
        }
        else
            fail();
    }
    
    /**
     * Test of the PeerAdvertisementsListener
     */
    @Test
    public void testPeerAdvertisementListener() throws InterruptedException
    {
        System.out.println("\n\n\nTest PeerAdvertisementsListener");
        
        status = false;
        
        PeerAdvertisementListener pAL = new PeerAdvertisementListener(){
            
            public void peerAdvertisementEvent(U2UShellDiscoveryEvent event)
            {
                System.out.println("inside peerAdvertisementEvent ");
                Enumeration en = event.getResponse();
                while (en.hasMoreElements()) {
                    System.out.println(en.nextElement());
                }
                assertTrue(true);
                status = true;
            }
        };
     
        shell.addPeerAdvertisementListener(pAL);
        
        shell.executeCmd("peers -r");
        
        Thread.sleep(longTime);
        
        shell.removePeerAdvertisementListener(pAL);
        if(!status)
            fail();
    }
    
    /**
     * Test of the PeerGroupAdvertisementsListener
     */
    @Test
    public void testPeerGroupAdvertisementListener() throws InterruptedException
    {
        System.out.println("\n\n\nTest PeerGroupAdvertisementsListener");
        
        status = false;
        
        PeerGroupAdvertisementListener pAL = new PeerGroupAdvertisementListener(){
            
            public void peerGroupAdvertisementEvent(U2UShellDiscoveryEvent event)
            {
                System.out.println("inside peerGroupAdvertisementEvent");
                Enumeration en = event.getResponse();
                while (en.hasMoreElements()) {
                    System.out.println(en.nextElement());
                }
                assertTrue(true);
                status = true;
            }
        };
     
        shell.addPeerGroupAdvertisementListener(pAL);
        
        shell.executeCmd("groups -r");
        
        Thread.sleep(longTime);
        
        shell.removePeerGroupAdvertisementListener(pAL);
        if(!status)
            fail();
    }
    
    /**
     * Test of the GeneralAdvertisementsListener
     */
    @Test
    public void testGeneralAdvertisementListener() throws InterruptedException
    {
        System.out.println("\n\n\nTest GeneralAdvertisementsListener");
        
        status = false;
        
        GeneralAdvertisementListener pAL = new GeneralAdvertisementListener(){
            
            public void generalAdvertisementEvent(U2UShellDiscoveryEvent event)
            {
                System.out.println("inside generalAdvertisementEvent");
                Enumeration en = event.getResponse();
                while (en.hasMoreElements()) {
                    System.out.println(en.nextElement());
                }
                assertTrue(true);
                status = true;
            }
        };
     
        shell.addGeneralAdvertisementListener(pAL);
        
        shell.executeCmd("search -r");
        
        Thread.sleep(longTime);
        
        shell.removeGeneralAdvertisementListener(pAL);
        if(!status)
            fail();
    }
    
    /**
     * Test multiple listeners, add and remove
     */
    @Test
    public void testMultipleListenersAddRemove() throws InterruptedException
    {
        
        System.out.println("\n\n\nTest GeneralAdvertisementsListener");
        
        //peerAdvertisementListener
        {
            System.out.println("\nMultiple peerAdversitementListener");

            status = status2 = false;

            TestPeerAdv p1 = new TestPeerAdv("p1") {

                @Override
                public void peerAdvertisementEvent(U2UShellDiscoveryEvent event) {
                    System.out.println(whoami + " true");
                    status = true;
                    st = false;
                }
            };    

            TestPeerAdv p2 = new TestPeerAdv("p2") {

                @Override
                public void peerAdvertisementEvent(U2UShellDiscoveryEvent event) {
                    System.out.println(whoami + " true");
                    status2 = true;
                    st = false;
                }
            }; 

            shell.addPeerAdvertisementListener(p1);
            shell.addPeerAdvertisementListener(p2);

            shell.executeCmd("peers -r");
            Thread.sleep(longTime);
            assertTrue(status&status2);

            status = status2 = false;
            shell.removePeerAdvertisementListener(p1);
            shell.removePeerAdvertisementListener(p2);
            shell.executeCmd("peers -r");
            Thread.sleep(longTime);
            assertFalse(status&status2);

            p1 = null;
            p2 = null;
        
        }
        
        
        //peerGroupAdvertisementListener
        {
            System.out.println("\nMultiple peerGroupAdversitementListener");
            
            status = status2 = false;
        
            TestPeerGroupAdv pg1 = new TestPeerGroupAdv("pg1") {

                @Override
                public void peerGroupAdvertisementEvent(U2UShellDiscoveryEvent event) {
                    System.out.println(whoami + " true");
                    status = true;
                    st = false;
                }
            };  

            TestPeerGroupAdv pg2 = new TestPeerGroupAdv("pg2") {

                @Override
                public void peerGroupAdvertisementEvent(U2UShellDiscoveryEvent event) {
                    System.out.println(whoami + " true");
                    status2 = true;
                    st = false;
                }
            };

            shell.addPeerGroupAdvertisementListener(pg1);
            shell.addPeerGroupAdvertisementListener(pg2);

            shell.executeCmd("groups -r");
            Thread.sleep(longTime);
            assertTrue(status&status2);

            status = status2 = false;
            shell.removePeerGroupAdvertisementListener(pg1);
            shell.removePeerGroupAdvertisementListener(pg2);
            shell.executeCmd("groups -r");
            Thread.sleep(longTime);
            assertFalse(status&status2);

            pg1 = null;
            pg2 = null;
        }
        
        //generalAdvertisementListener
        {
            System.out.println("\nMultiple generalAdversitementListener");
            
            status = status2 = false;
        
            TestGeneralAdv g1 = new TestGeneralAdv("g1") {

                @Override
                public void generalAdvertisementEvent(U2UShellDiscoveryEvent event) {
                    System.out.println(whoami + " true");
                    status = true;
                    st = false;
                }
            };

            TestGeneralAdv g2 = new TestGeneralAdv("g2") {

                @Override
                public void generalAdvertisementEvent(U2UShellDiscoveryEvent event) {
                    System.out.println(whoami + " true");
                    status2 = true;
                    st = false;
                }
            };

            shell.addGeneralAdvertisementListener(g1);
            shell.addGeneralAdvertisementListener(g2);

            shell.executeCmd("search -r");
            Thread.sleep(longTime);
            assertTrue(status&status2);

            status = status2 = false;
            shell.removeGeneralAdvertisementListener(g1);
            shell.removeGeneralAdvertisementListener(g2);
            shell.executeCmd("search -r");
            Thread.sleep(longTime);
            assertFalse(status&status2);

            g1 = null;
            g2 = null;
        }
        
    }
}

abstract class TestPeerAdv implements PeerAdvertisementListener, Runnable
{

    protected String whoami;
    protected boolean st = true;
    
    public TestPeerAdv(String n)
    {
        whoami = n;
        Thread t = new Thread(this);
        t.start();
    }
    
    public abstract void peerAdvertisementEvent(U2UShellDiscoveryEvent event);

    public void run() {
        System.out.println("starting the thread for " + whoami);
        while(st)
        {
            
        }
        System.out.println("stopping the thread for " + whoami);
    }
    
}

abstract class TestPeerGroupAdv implements PeerGroupAdvertisementListener, Runnable
{

    protected String whoami;
    protected boolean st = true;
    
    public TestPeerGroupAdv(String n)
    {
        whoami = n;
        Thread t = new Thread(this);
        t.start();
    }
    
    public abstract void peerGroupAdvertisementEvent(U2UShellDiscoveryEvent event);

    public void run() {
        System.out.println("starting the thread for " + whoami);
        while(st)
        {
            
        }
        System.out.println("stopping the thread for " + whoami);
    }
    
}

abstract class TestGeneralAdv implements Runnable, GeneralAdvertisementListener
{

    protected String whoami;
    protected boolean st = true;
    
    public TestGeneralAdv(String n)
    {
        whoami = n;
        Thread t = new Thread(this);
        t.start();
    }
    
    public abstract void generalAdvertisementEvent(U2UShellDiscoveryEvent event);

    public void run() {
        System.out.println("starting the thread for " + whoami);
        while(st)
        {
            
        }
        System.out.println("stopping the thread for " + whoami);
    }

}
