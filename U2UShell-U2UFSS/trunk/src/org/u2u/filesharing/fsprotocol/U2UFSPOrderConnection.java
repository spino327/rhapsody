/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.u2u.filesharing.fsprotocol;

/**
 * This class handle an order of the CONN message, that request connection for the file
 * with SHA-1
 * <length prefix>  <order id>    <Pv>    <requested file's SHA1> <PeerID> <(optional)Certificate>
 * |   4 bytes    |  1 byte    | PVersion|       20 bytes        |33 bytes|        n bytes        |
 * |              |            |  1 byte |                       |        |                       |
 * @author Irene & Sergio
 */
public class U2UFSPOrderConnection extends U2UFSProtocolOrder{

    /**
     * number of payload's bytes
     */
    private static final int minPLength = 54;
    /**
     * Create a new instance of the order
     * @param payload a byte array representation of the payload
     */
    protected U2UFSPOrderConnection(byte[] payload)
    {
        super(CONN, payload);
        //FIXME spino327@gmail.com add support for converting a portion of payload in the certificate
        checkOrderState(minPLength, CONN);
    }

    /**
     * Create a new instance of the order from an byte array representation of a whole order, use it
     * when you read an order from an InputStream, because you only have a byte array and you want
     * to use the specialise methods of the subclass
     * @param orderBytes a byte array representation of the order
     * @param onlyForCanUseTheOtherConstructor this parameter doesn't have any kind of importance
     * hack, the second parameter doesn't have any kind of importance, you can pass true or false
     */
    protected U2UFSPOrderConnection(byte[] orderBytes, boolean onlyForCanUseTheOtherConstructor)
    {
        super(orderBytes);

        checkOrderState(minPLength, CONN);

    }

    /**
     * return the protocol version
     * @return a byte representation of the version
     */
    public byte getProtocolVersion()
    {
        checkOrderState(minPLength, CONN);
        return this.getPayload()[0];
    }

    /**
     * return a byte array representation of the SHA-1
     * @return byte array
     */
    public byte[] getSha1File()
    {
        checkOrderState(minPLength, CONN);

        byte[] sha1 = new byte[20];
        byte[] payload = this.getPayload();

        for(int i = 1; i < 21; i++)
        {
            sha1[i-1] = payload[i];
        }

        return sha1;
    }

    /**
     * return a String representation of the PeerID, 
     * @return String representation, 80 characters, with 'urn:jxta:uuid-'
     */
    public String getPeerId()
    {
        checkOrderState(minPLength, CONN);

        byte[] peerId = new byte[33];
        byte[] payload = this.getPayload();

        int i = 21;
        for(byte a : peerId)
        {
            a = payload[i];
            i++;
        }

        return U2UFSProtocolOrder.idToString(payload);
    }

}
