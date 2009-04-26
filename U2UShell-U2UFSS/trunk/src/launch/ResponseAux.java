/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package launch;

import org.u2u.filesharing.*;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jxta.exception.PeerGroupException;
import net.jxta.peergroup.PeerGroup;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.platform.NetworkManager;
import net.jxta.share.FileContent;
import org.u2u.filesharing.uploadpeer.U2UContentManagerImpl;

/**
 *
 * @author sergio
 */
public class ResponseAux {
    private static NetworkManager manager;
    private static PeerGroup group;

    public ResponseAux() {
    }

    
    public static void main(String[] args)
    {
        System.out.println("--------starting");
        try
        {
            manager = new NetworkManager(NetworkManager.ConfigMode.EDGE, "responseModule");
            NetworkConfigurator conf = manager.getConfigurator();
            
            conf.setName("ResponseAux");
            conf.setHome(new File("jxtaResponse"));
            conf.setTcpInterfaceAddress("192.168.0.3");
            conf.setTcpPort(2526);
            
            Set<String> set = new HashSet<String>();
            //set.add("tcp://u2u.homeunix.net:9701");
            set.add("tcp://192.168.0.3:8080");
            //configurator.setRelaySeedURIs(new ArrayList<String>(set));
            conf.setRendezvousSeeds(set);
            
            //starting JXTA            
            group = manager.startNetwork();

            manager.waitForRendezvousConnection(25000);
         
        } catch (PeerGroupException ex)
        {
            Logger.getLogger(ResponseAux.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex)
        {
            Logger.getLogger(ResponseAux.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try
        {
            U2UFileSharingService fss = new U2UFileSharingService(group, false, false);
            File f = new File("/img.png");


            U2UContentManagerImpl cm = new U2UContentManagerImpl(fss);

            //register the file f in the db of shared files, verifier if the return is not null
            FileContent fc = cm.share(f);
            
            
        } catch (IOException ex)
        {
            System.out.println(""+ex.getMessage());
            //Logger.getLogger(ResponseServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        System.out.println("------------while");
        while(true)
        {
            
        }
    }
}