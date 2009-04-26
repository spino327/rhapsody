/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package launch;

import java.util.HashSet;
import java.util.Set;
import net.jxta.impl.shell.bin.Shell.PeerAdvertisementListener;

/**
 *
 * @author sergio
 */
public class test {

    public static void main(String[] args)
    {
        Set<PeerAdvertisementListener> peerAdvListeners = new HashSet<PeerAdvertisementListener>();
        
        System.out.println(peerAdvListeners.getClass().getName());
    }
}
