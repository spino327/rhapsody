/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jxta.impl.shell.bin.u2ufs;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import net.jxta.credential.Credential;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocument;
import net.jxta.exception.PeerGroupException;
import net.jxta.id.ID;
import net.jxta.impl.membership.pse.PSECredential;
import net.jxta.impl.membership.pse.PSEMembershipService;
import net.jxta.impl.shell.bin.Shell.Shell;
import net.jxta.membership.MembershipService;
import net.jxta.peergroup.PeerGroup;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.platform.NetworkManager;
import net.jxta.protocol.ModuleImplAdvertisement;
import net.jxta.service.Service;
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
public class MembershipServiceTest {
    private static NetworkManager manager;
    private static Shell shell;
    private static PeerGroup grupo;

    public MembershipServiceTest() {
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
            grupo = manager.startNetwork();

            manager.waitForRendezvousConnection(25000);

            shell = new Shell(grupo);

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

    @Test
    public void activatePSE_MS()
    {
        MembershipService membershipSrv = grupo.getMembershipService();
        PSEMembershipService pseMembershipSrv = null;
        ModuleImplAdvertisement implAdv = (ModuleImplAdvertisement) membershipSrv.getImplAdvertisement();

        if((null != implAdv)
                &&PSEMembershipService.pseMembershipSpecID.equals(implAdv.getModuleSpecID())
                && (membershipSrv instanceof PSEMembershipService))
        {
            pseMembershipSrv = (PSEMembershipService) membershipSrv;
            assertTrue(true);
        }
    }

    @Test
    public void CredentialPSE_MS()
    {

        //PSECredential credential = new PSECredential(root);

    }
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}

}