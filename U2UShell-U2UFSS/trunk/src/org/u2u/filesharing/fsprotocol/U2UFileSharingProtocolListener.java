/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.u2u.filesharing.fsprotocol;

/**
 *  The listener interface for receiving {@link U2UFileSharingProtocolEvent}s from the
 *  U2UFileSharingProtocol.
 *
 *  The following 2 examples illustrate how to implement a
 *  <code>U2UFileSharingProtocolListener</code>:
 *
 * <p/><b>Example 1:</b>
 * <pre>
 * U2UFileSharingProtocolListener myListener = new U2UFileSharingProtocolListener() {
 *   public void protocolEvent(U2UFileSharingProtocolEvent event) {
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
 * public class AppDemo implements U2UFileSharingProtocolListener {
 *          ..
 *          ..
 *     public void protocolEvent(U2UFileSharingProtocolEvent event) {
 *      ..
 *      ..
 *
 *     }
 * }
 * </pre>
 *
 * @author Irene & Sergio
 */
public interface U2UFileSharingProtocolListener {

    /**
     * Called to handle an event from the U2UFileSharingProtocol of the U2UFileSharingService
     * relationship with the incoming of request or querys from a remote peer.
     *
     * @param event U2UFileSharingProtocolEvent
     */
    void protocolEvent(U2UFileSharingProtocolEvent event);
}
