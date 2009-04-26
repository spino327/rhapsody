/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jxta.impl.shell.bin.Shell;

/**
 *  The listener interface for receiving {@link U2UShellDiscoveryEvent}s from the
 *  Shell.
 *
 *  The following 2 examples illustrate how to implement a 
 *  <code>PeerGroupAdvertisementListener</code>:
 *
 * <p/><b>Example 1:</b>
 * <pre>
 * PeerGroupAdvertisementListener myListener = new PeerGroupAdvertisementListener() {
 *   public void peerGroupAdvertisementEvent(U2UShellDiscoveryEvent e) {
 *     Enumeration msg = e.getResponse();
 *     if (myQueryID == e.getQueryID()) {
 *      
 *     }
 *   }
 *   shell.addPeerGroupAdvertisementListener(myListener);
 *   
 *   shell.shell.executeCmd("groups -r");
 * </pre>
 *
 * <p/><b>Example 2:</b>
 * <pre>
 * public class AppDemo implements PeerGroupAdvertisementListener {
 *          ..
 *          ..
 *     public void peerGroupAdvertisementEvent(U2UShellDiscoveryEvent e) {
 *
 *      Enumeration msg = e.getResponse();
 *      ..
 *      ..
 *     
 *     }
 * }
 * </pre>
 *
 **/
public interface PeerGroupAdvertisementListener {

    /**
     * Called to handle an event from the Shell relationship with the discovery of Peer Group Advertisements.
     *
     * @param event the U2UShellDiscovery event
     */
    void peerGroupAdvertisementEvent(U2UShellDiscoveryEvent event);
}
