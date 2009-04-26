/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package launch;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.exception.PeerGroupException;
import net.jxta.peergroup.PeerGroup;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.platform.NetworkManager;
import org.u2u.filesharing.U2UContentAdvertisementImpl;
import org.u2u.filesharing.U2UFileSharingService;
import org.u2u.filesharing.downloadpeer.U2UContentDiscoveryEvent;
import org.u2u.filesharing.downloadpeer.U2UDownloadingManager;
import org.u2u.filesharing.downloadpeer.U2URequestManagerImpl;
import org.u2u.filesharing.downloadpeer.U2USearchListener;

/**
 *
 * @author sergio
 */
public class Downloading {
    private static NetworkManager manager;
    private static NetworkConfigurator conf;
    private static PeerGroup group;
    private static U2UContentAdvertisementImpl adv;

    public static void main(String[] args)
    {
        try
        {
            String localHost = InetAddress.getLocalHost().getHostAddress();
//            Runtime.getRuntime().exec("rm -r jxtaUploading");
//            Runtime.getRuntime().exec("rm -r .jxta");
            manager = new NetworkManager(NetworkManager.ConfigMode.EDGE, "UploadingInstance"+localHost);
            conf = manager.getConfigurator();
            conf.setName("DownloadingPeer"+localHost);
            conf.setHome(new File("jxtaUploading"));
            //conf.setTcpInterfaceAddress("192.168.0.3");
            
            System.out.println("---"+localHost);
            conf.setTcpInterfaceAddress(localHost);
            conf.setTcpPort(2525);

            Set<String> set = new HashSet<String>();
            //set.add("tcp://u2u.homeunix.net:9701");
            //set.add("tcp://190.240.10.222:9701");
            //set.add("tcp://" + localHost + ":8080");
            set.add("tcp://192.168.0.3:8080");
            //conf.setRelaySeedURIs(new ArrayList<String>(set));
            conf.setRendezvousSeeds(set);
            //starting JXTA
            group = manager.startNetwork();
            manager.waitForRendezvousConnection(10000);

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

            U2UFileSharingService fss = new U2UFileSharingService(group, false, false);
            U2URequestManagerImpl rm = new U2URequestManagerImpl(fss);
            adv = null;

            U2USearchListener listener = new U2USearchListener() {

                public void contentAdvertisementEvent(U2UContentDiscoveryEvent event)
                {
                    Enumeration en = event.getResponseAdv();

                    adv = (U2UContentAdvertisementImpl)en.nextElement();
                    System.out.println("---arrived a Adv");
                }
            };

            rm.addSearchListener(listener);
            rm.searchContent("name", "Move to the city");

            Thread.sleep(15000);

            System.out.println("---Downloading a SharedContent");
            /*if(adv !=null)
            {
               U2UDownloadingManager dm = new U2UDownloadingManager(fss);
               //return true if the downloading start successful
               System.out.println(dm.download(adv));
            }*/

        } catch (InterruptedException ex)
        {
            Logger.getLogger(Downloading.class.getName()).log(Level.SEVERE, null, ex);
        } catch (PeerGroupException ex)
        {
            Logger.getLogger(Downloading.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex)
        {
            Logger.getLogger(Downloading.class.getName()).log(Level.SEVERE, null, ex);
        }

        manager.stopNetwork();

    }
}
