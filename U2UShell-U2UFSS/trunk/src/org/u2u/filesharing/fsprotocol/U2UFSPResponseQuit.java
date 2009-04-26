/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.u2u.filesharing.fsprotocol;

/**
 * This class handle a response of the QUIT message, that sends quit message
 * @author Irene & Sergio
 */
public class U2UFSPResponseQuit extends U2UFSProtocolResponse {

    //
    /**
     * Represents the response "finish successful"
     */
    public static final String R260 = "260";

    /**
     * Create a new instance of the response, this never is send from the remote peer to the local peer, only
     * for tell to the listeners about the sends of the QUIT message
     */
    protected U2UFSPResponseQuit()
    {
        super(RQUIT, R260, new byte[0]);
    }

}
