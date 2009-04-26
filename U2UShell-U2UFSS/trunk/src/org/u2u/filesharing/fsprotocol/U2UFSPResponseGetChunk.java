/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.u2u.filesharing.fsprotocol;

/**
 * This class handle a response of the GETC message, that request chunk with index
 *  <length prefix> <order id>  <index>
 * |   4 bytes     |  1 byte  | 2 byte  |
 * |               |          |         |
 * @author Irene & Sergio
 */
public class U2UFSPResponseGetChunk extends U2UFSProtocolResponse {

    /**
     * Represents the response "requested chunk available"
     * 2 End affirmative response
     */
    public static final String R211 = "211";
    /**
     * Represents the response "requested chunk not available"
     * 2 End affirmative response
     */
    public static final String R212 = "212";
    /**
     * Represents the response "request wasn't executed, busy peer"
     * 4 Intermediate positive response
     */
    public static final String R411 = "411";
    /**
     * Represents the response "request was canceled, local error"
     * 4 Intermediate positive response
     */
    public static final String R421 = "421";
    /**
     * Represents the response "requested transfer order canceled"
     * 5 End permanent response
     */
    public static final String R531 = "531";

    protected static final String[] RESPONSES = new String[] {R211, R212, R411, R421, R531};
    /**
     * number of payload's bytes
     */
    private static final int minPLength = 3;
    
    /**
     * Create a new instance of the response
     * @param resNum a String
     * @param payload a byte array representation of the payload
     */
    protected U2UFSPResponseGetChunk(String resNum, byte[] payload)
    {
        super(RGETC, resNum, payload);

        //the payload have reason only for response 211
        if(resNum.equals(R211))
        {
            checkResponseState(minPLength, RGETC);
        }
    }

    /**
     * Create a new instance of the response
     * @param responseBytes
     */
    protected U2UFSPResponseGetChunk(byte[] responseBytes)
    {
        super(responseBytes);

        //the payload have reason only for response 211
        String resNum = this.getResponseNumber();
        if(resNum.equals(R211))
        {
            checkResponseState(minPLength, RGETC);
        }
    }

    /**
     * returns the byte array representation of the chunk's data
     * @return byte array representation of the incoming
     */
    public byte[] getChunkBytes()
    {
        checkResponseState(minPLength, RGETC);
        
        byte[] cBytes = null;

        if(this.getResponseNumber().equals(R211))
        {
            cBytes = this.getPayload();
        }

        return cBytes;
    }

    //static

    /**
     * making the payload
     * @param res the response number
     * @param rawChunkBytes chunk's raw byte array
     * @return byte[] the payload for the response, with code
     * @throws IllegalArgumentException
     */
    public static byte[] makingPayloadChunkBytes(String res, byte[] rawChunkBytes)
    {
        byte[] payload = null;

        //checking rawChunkBytes
        if(rawChunkBytes == null)
        {
            throw new NullPointerException("rawChunkBytes can't be null");
        }

        //checking the response number
        if(res.equals(R211))
        {
            //FIXME spino327@gmail.com add coded
            payload = rawChunkBytes;
        }
        else
        {
            //nothing
            payload = new byte[0];
        }

        return payload;
    }
}
