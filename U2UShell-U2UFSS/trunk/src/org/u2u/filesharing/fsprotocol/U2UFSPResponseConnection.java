/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.u2u.filesharing.fsprotocol;

/**
 * This class handle a response of the CONN message, that request connection for the file
 * with SHA-1
 * <length prefix> <response id> <response number>    <Pv>    <PeerID>
 * |   4 bytes    |  1 byte     |     2 bytes     |  1 byte  | 33 bytes
 * |              |             |      short      | PVersion |
 * @author Irene & Sergio
 */
public class U2UFSPResponseConnection extends U2UFSProtocolResponse {

    //
    /**
     * Represents the response "transmission request accept"
     * 2 End affirmative response
     */
    public static final String R201 = "201";
    /**
     * Represents the response "transmission service disable"
     * 4 Intermediate positive response
     */
    public static final String R401 = "401";
    
    /**
     * String Array of the RESPONSES of this class
     */
    protected static final String[] RESPONSES = new String[] {R201, R401};

    /**
     * number of payload's bytes
     */
    private static final int minPLength = 34;

    /**
     * Create a new instance of the response
     * @param resNum a String 
     * @param payload a byte array representation of the payload
     */
    protected U2UFSPResponseConnection(String resNum, byte[] payload)
    {
        super(RCONN, resNum, payload);

        //the payload have reason only for response 2XX
        if(resNum.equals(R201))
        {
            checkResponseState(minPLength, RCONN);
        }
    }

    /**
     * Create a new instance of the response
     * @param responseBytes
     */
    protected U2UFSPResponseConnection(byte[] responseBytes)
    {
        super(responseBytes);

        //the payload have reason only for response 2XX
        if(this.getResponseNumber().equals(R201))
        {
            checkResponseState(minPLength, RCONN);
        }
    }

    /**
     * return the protocol version
     * @return a byte representation of the version
     */
    public byte getProtocolVersion()
    {
        checkResponseState(minPLength, RCONN);

        return this.getPayload()[0];
    }

    /**
     * return a String representation of the PeerID,
     * @return String representation, 80 characters, with 'urn:jxta:uuid-'
     */
    public String getPeerId()
    {
        checkResponseState(minPLength, RCONN);

        byte[] peerId = new byte[33];
        byte[] payload = this.getPayload();

        int i = 1;
        for(byte a : peerId)
        {
            a = payload[i];
            i++;
        }

        return U2UFSProtocolResponse.idToString(payload);
    }
}
