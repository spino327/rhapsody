/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.u2u.filesharing.fsprotocol;

/**
 * This class handle an order of the INFO message, that request file's info
 * with SHA-1
 *  <length prefix> <order id> <query type> <file's SHA1>
 * |   4 bytes     |  1 byte  |   1 byte   |  20 bytes   |
 * |               |          |            |             |
 * @author Irene & Sergio
 */
public class U2UFSPOrderFileInfo extends U2UFSProtocolOrder {

    /**
     * list of remote peer's chunks( T(1, 4, 5...) or
     * F(2, 3, 6...) only the most small in bytes)
     */
    public static final byte PEER_CHUNKS = 0x30;//0
    /**
     * sha-1 list of file's chunks( (1, xxxxxxxxx; 2, xxxxxxxx....))
     */
    public static final byte CHUNKS_SHA1 = 0x31;//1
    /**
     * number of payload's bytes
     */
    private static final int minPLength = 21;
    /**
     * Create a new instance of the order
     * @param payload a byte array representation of the payload
     */
    protected U2UFSPOrderFileInfo(byte[] payload)
    {
        super(INFO, payload);
        
        checkOrderState(minPLength, INFO);
    }

    /**
     * Create a new instance of the order from an byte array representation of a whole order, use it
     * when you read an order from an InputStream, because you only have a byte array and you want
     * to use the specialise methods of the subclass
     * @param orderBytes a byte array representation of the order
     * @param onlyForCanUseTheOtherConstructor this parameter doesn't have any kind of importance
     * hack, the second parameter doesn't have any kind of importance, you can pass true or false
     */
    protected U2UFSPOrderFileInfo(byte[] orderBytes, boolean onlyForCanUseTheOtherConstructor)
    {
        super(orderBytes);

        checkOrderState(minPLength, INFO);

    }

    /**
     * return the query type
     * @return a byte representation of the query type
     */
    public byte getQueryType()
    {
        checkOrderState(minPLength, INFO);
        return this.getPayload()[0];
    }

    /**
     * return a byte array representation of the SHA-1
     * @return byte array
     */
    public byte[] getSha1File()
    {
        checkOrderState(minPLength, INFO);

        byte[] sha1 = new byte[20];
        byte[] payload = this.getPayload();

        for(int i = 1; i < 21; i++)
        {
            sha1[i-1] = payload[i];
        }

        return sha1;
    }

}
