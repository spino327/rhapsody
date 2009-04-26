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
 *  <code>PeerAdvertisementListener</code>:
 *
 * <p/><b>Example 1:</b>
 * <pre>
 * PeerAdvertisementListener myListener = new PeerAdvertisementListener() {
 *   public void peerAdvertisementEvent(U2UShellDiscoveryEvent e) {
 *     Enumeration msg = e.getResponse();
 *     if (myQueryID == e.getQueryID()) {
 *      
 *     }
 *   }
 *   shell.addPeerAdvertisementListener(myListener);
 *   
 *   shell.shell.executeCmd("peers -r");
 * </pre>
 *
 * <p/><b>Example 2:</b>
 * <pre>
 * public class AppDemo implements PeerAdvertisementListener {
 *          ..
 *          ..
 *     public void peerAdvertisementEvent(U2UShellDiscoveryEvent e) {
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
public interface PeerAdvertisementListener {

    /**
     * Called to handle an event from the Shell relationship with the discovery of Peer Advertisements.
     *
     * @param event the U2UShellDiscovery event
     */
    void peerAdvertisementEvent(U2UShellDiscoveryEvent event);
}
