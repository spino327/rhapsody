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
 *  <code>GeneralAdvertisementListener</code>:
 *
 * <p/><b>Example 1:</b>
 * <pre>
 * GeneralAdvertisementListener myListener = new GeneralAdvertisementListener() {
 *   public void generalAdvertisementEvent(U2UShellDiscoveryEvent e) {
 *     Enumeration msg = e.getResponse();
 *     if (myQueryID == e.getQueryID()) {
 *      
 *     }
 *   }
 *   shell.addGeneralAdvertisementListener(myListener);
 *   
 *   shell.shell.executeCmd("search");
 * </pre>
 *
 * <p/><b>Example 2:</b>
 * <pre>
 * public class AppDemo implements GeneralAdvertisementListener {
 *          ..
 *          ..
 *     public void generalAdvertisementEvent(U2UShellDiscoveryEvent e) {
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
public interface GeneralAdvertisementListener {

    /**
     * Called to handle an event from the Shell relationship with the discovery of General Advertisements.
     *
     * @param event the U2UShellDiscovery event
     */
    void generalAdvertisementEvent(U2UShellDiscoveryEvent event);
}
