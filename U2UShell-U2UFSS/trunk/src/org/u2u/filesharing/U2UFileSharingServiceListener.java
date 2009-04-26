/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.u2u.filesharing;

/**
 *  The listener interface for receiving {@link U2UFileSharingServiceEvent}s from the
 *  U2UFileSharingService.
 *
 *  The following 2 examples illustrate how to implement a
 *  <code>U2UFileSharingProtocolListener</code>:
 *
 * <p/><b>Example 1:</b>
 * <pre>
 * U2UFileSharingServiceListener myListener = new U2UFileSharingServiceListener() {
 *   public void serviceEvent(U2UFileSharingServiceEvent event) {
 *     ...
 *   }
 * }
 *
 * protocol.addProtocolListener(myListener);
 *
 * </pre>
 *
 * <p/><b>Example 2:</b>
 * <pre>
 * public class AppDemo implements U2UFileSharingServiceListener {
 *          ..
 *          ..
 *     public void serviceEvent(U2UFileSharingServiceEvent event) {
 *      ..
 *      ..
 *
 *     }
 * }
 * </pre>
 *
 * @author irene & sergio
 */


public interface U2UFileSharingServiceListener {

    /**
     * Called to handle an event from the U2UFileSharingService
     * relationship with task placed to U2UFileSharingService through of u2ufss command.
     *
     * @param event U2UFileSharingServiceEvent
     */
    void serviceEvent(U2UFileSharingServiceEvent event);
}
