/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.u2u.filesharing;

import org.u2u.filesharing.uploadpeer.U2UContentManagerImpl;
import org.u2u.common.db.SharedFiles;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.PipeID;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.platform.NetworkManager;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.share.FileContent;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.u2u.common.db.ConnectTo;
import org.u2u.common.db.Table;
import static org.junit.Assert.*;

/**
 *
 * @author sergio
 */
public class U2UContentManagerTest {
    private static NetworkManager manager;
    private static PeerGroup group;
    private static U2UFileSharingService fss;

    public U2UContentManagerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {

        Runtime.getRuntime().exec("rm -r .jxta");

        manager = new NetworkManager(NetworkManager.ConfigMode.EDGE, "U2UFSTest");
        
        NetworkConfigurator conf = manager.getConfigurator();
        conf.setTcpEnabled(true);
        conf.setName("irene");
        conf.setTcpInterfaceAddress("192.168.0.3");
        
        group = manager.startNetwork();

        fss = new U2UFileSharingService(group, false, false);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        manager.stopNetwork();
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }
    
    @Test
    public void testU2UContentIdImpl() throws FileNotFoundException
    {
        //
        File f = new File("test/org/u2u/filesharing/hola.rtf");
        String knowSha1 = "sha1:c236c0095c6348b2aec8a544574363f89470ff44";
        FileInputStream in = new FileInputStream(f);
        
        System.out.println("---test 1");
        
        U2UContentIdImpl cadv = new U2UContentIdImpl(in);
        
        assertEquals(knowSha1, cadv.toString());

        byte[] array = cadv.getByteRepresentation();

        for(int i = 0; i < array.length; i++)
        {
            int v = array[i] + 0x80;
            System.out.println("v"+i+" = "+v);
            System.out.println("byte("+array[i]+") = " + Character.toString((char) (array[i] + 0x80)));
        }
        //
        f = new File("test/org/u2u/filesharing/img.png");
        knowSha1 = "sha1:697ba96ac6cdb7801e68083532dc2de491ea6084";
        in = new FileInputStream(f);
        
        System.out.println("---test 2");
        
        cadv = new U2UContentIdImpl(in);
        
        assertEquals(knowSha1, cadv.toString());
    }
    
    @Test
    public void testAdv() throws FileNotFoundException
    {
        U2UFileSharingService fss = new U2UFileSharingService(group, false, false);
        try {
            File f = new File("test/org/u2u/filesharing/hola.rtf");
            
            FileInputStream in = new FileInputStream(f);

            System.out.println("---test publish adv");

            U2UContentIdImpl cid = new U2UContentIdImpl(in);
            U2UContentAdvertisementImpl adv = new U2UContentAdvertisementImpl(f.getName(), cid, f.length(), null, null);
            adv.setSocketAdv(fss.getSocketAdv());

            Enumeration en;
            
            DiscoveryService discovery = group.getDiscoveryService();
            try {
                
                en = discovery.getLocalAdvertisements(DiscoveryService.ADV, null, null);
                
                while(en.hasMoreElements())
                    discovery.flushAdvertisement((Advertisement)en.nextElement());
                en = null;

                Thread.sleep(10000);

                discovery.publish(adv);
            } catch (InterruptedException ex)
            {
                Logger.getLogger(U2UContentManagerTest.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(U2UContentManagerTest.class.getName()).log(Level.SEVERE, null, ex);
            }

            en = discovery.getLocalAdvertisements(DiscoveryService.ADV, null, null);
                
            while (en.hasMoreElements())
            {
                Object obj= en.nextElement();
                 
                if(obj instanceof U2UContentAdvertisementImpl)
                {
                    U2UContentAdvertisementImpl u2uAdv = (U2UContentAdvertisementImpl)obj;
                    cid = (U2UContentIdImpl) u2uAdv.getContentId();
                      
                    
                    System.out.println("old:"+adv.getContentId());
                    System.out.println("get:"+u2uAdv.getContentId());
                    assertEquals(adv.getContentId().toString(), cid.toString());
                    assertTrue(cid.equals(adv.getContentId()));

                    PipeAdvertisement pipeadv = null;
                    pipeadv = adv.getSocketAdv();

                    assertTrue(pipeadv != null);
                    assertTrue(pipeadv instanceof PipeAdvertisement);
                    assertTrue(pipeadv.getName().equals("Socket:irene"));
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(U2UContentManagerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    @Test
    public void testSharedFiles() throws IOException, SQLException, ClassNotFoundException
    {
        FileInputStream in = null;
        
        try {
            System.out.println("---Registering the file in the db");

            U2UContentAdvertisementImpl cadv;
            ConnectTo con = new ConnectTo("org.apache.derby.jdbc.EmbeddedDriver");
            assertTrue(con.getConnection("jdbc:derby:U2UClient", "U2U", ""));
            Table.connect();
            
            File f = new File("test/org/u2u/filesharing/hola.rtf");

            in = new FileInputStream(f);
            U2UContentIdImpl cid = new U2UContentIdImpl(in);

            U2UFileContentImpl fci = new U2UFileContentImpl(f, f.getName(), null, null);
            
            System.out.println("---test creting the record");
            if(!SharedFiles.exist(cid))
                assertTrue(SharedFiles.create(fci));
            else
                assertFalse(SharedFiles.create(fci));
            
            System.out.println("---test exist");
            assertTrue(SharedFiles.exist(cid));
            
            System.out.println("---test find all");
            //making more, more
            f = new File("test/org/u2u/filesharing/img.png");
            fci = new U2UFileContentImpl(f, f.getName(), null, null);
            if(!SharedFiles.exist((U2UContentIdImpl) fci.getContentAdvertisement().getContentId()))
                assertTrue(SharedFiles.create(fci));
            
            f = new File("test/org/u2u/filesharing/pipeserver.adv");
            fci = new U2UFileContentImpl(f, f.getName(), null, null);
            if(!SharedFiles.exist((U2UContentIdImpl) fci.getContentAdvertisement().getContentId()))
                assertTrue(SharedFiles.create(fci));
            
            Enumeration en = SharedFiles.find(null);
            
            int count = 0;
            
            if(en.hasMoreElements())
            {
                while(en.hasMoreElements()) {
                    Object obj = en.nextElement();
                    if(obj instanceof U2UFileContentImpl)
                    {
                        assertTrue(true);
                        count++;
                    }
                    else
                        fail();

                }
                assertTrue(count == 3);
            }
            else
                fail();
            
            
            System.out.println("---test find one");
            en = SharedFiles.find((U2UContentIdImpl)fci.getContentAdvertisement().getContentId());
            
            count = 0;
            if(en.hasMoreElements())
            {
                while(en.hasMoreElements()) {
                    Object obj = en.nextElement();
                    if(obj instanceof U2UFileContentImpl)
                    {
                        assertTrue(true);
                        count++;
                    }
                    else
                        fail();

                }
                assertTrue(count == 1);
            }
            else
                fail();
            
            System.out.println("---test delete");
            assertTrue(SharedFiles.delete(cid));
            assertFalse(SharedFiles.exist(cid));
        
        } catch (FileNotFoundException ex) {
            Logger.getLogger(U2UContentManagerTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if(in != null)
                {
                    in.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(U2UContentManagerTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
           
    }
    
    @Test
    public void testShare() throws FileNotFoundException, NoSuchAlgorithmException, IOException
    {
        File f = new File("test/org/u2u/filesharing/hola.rtf");
        
        U2UContentManagerImpl cm = new U2UContentManagerImpl(fss);
        
        //register the file f in the db of shared files, verifier if the return is not null
        System.out.println("---Registering the file in the db");
        FileContent fc = cm.share(f);
        assertNotNull(fc);

        File file = new File("conf/.JxtaSocketID");

        if(file.exists())
        {
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            try
            {
                PipeID pipeIdFile = (PipeID) ois.readObject();
                PipeID pipeIDAdv = (PipeID) ((U2UContentAdvertisementImpl)fc.getContentAdvertisement()).getSocketAdv().getPipeID();
                assertEquals(pipeIdFile, pipeIDAdv);

            } catch (ClassNotFoundException ex)
            {   
                Logger.getLogger(U2UContentManagerTest.class.getName()).log(Level.SEVERE, null, ex);
            } finally
            {
                fis.close();
                ois.close();
            }
        }

        //verifier if the share file is in the DB
        System.out.println("---Finding the file in the db");
        
        U2UContentIdImpl id = new U2UContentIdImpl(new FileInputStream(f));
        
        assertTrue(cm.getContent(id).length > 0);
        
        //verifier if the ContentAdvertisement is publish in the local discoveryService
        System.out.println("---Finding the ContentAdvertisement at the local cache (DiscoveryService)");
        DiscoveryService discovery = group.getDiscoveryService();
        
        Enumeration en = discovery.getLocalAdvertisements(DiscoveryService.ADV, "cid" , id.toString());
        
        while(en.hasMoreElements())
        {
            U2UContentAdvertisementImpl adv = (U2UContentAdvertisementImpl)en.nextElement();
            System.out.println("---adv found::  "+ adv);
            assertTrue(id.equals(adv.getContentId()));
        }
        
    }



}