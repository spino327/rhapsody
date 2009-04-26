/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.u2u.filesharing.downloadpeer;

/**
 *  The listener interface for receiving {@link U2UContentDiscoveryEvent}s from the
 *  U2UFileSharingService.
 *
 *  The following 2 examples illustrate how to implement a 
 *  <code>U2USearchListener</code>:
 *
 * <p/><b>Example 1:</b>
 * <pre>
 * U2USearchListener myListener = new U2USearchListener() {
 *   public void contentAdvertisementEvent(U2UContentDiscoveryEvent e) {
 *     Enumeration msg = e.getResponse();
 *     if (myQueryID == e.getQueryID()) {
 *      
 *     }
 *   }
 *   fss.addSearchListener(myListener);
 *   
 * </pre>
 *
 * <p/><b>Example 2:</b>
 * <pre>
 * public class AppDemo implements U2USearchListener {
 *          ..
 *          ..
 *     public void contentAdvertisementEvent(U2UContentDiscoveryEvent e) {
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
public interface U2USearchListener {

    /**
     * Called to handle an event from the Request Module of the U2UFileSharingService
     * relationship with the discovery of Content Advertisements.
     *
     * @param event U2UContentDiscoveryEvent
     */
    void contentAdvertisementEvent(U2UContentDiscoveryEvent event);
}
