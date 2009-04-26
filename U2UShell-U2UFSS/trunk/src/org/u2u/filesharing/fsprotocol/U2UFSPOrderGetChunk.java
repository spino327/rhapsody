/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.u2u.filesharing.fsprotocol;

/**
 * This class handle an order of the GETC message, that request chunk with index
 *  <length prefix> <order id>  <index>
 * |   4 bytes     |  1 byte  | 2 byte  |
 * |               |          |         |
 * @author Irene & Sergio
 */
public class U2UFSPOrderGetChunk extends U2UFSProtocolOrder {
    
    /**
     * number of payload's bytes
     */
    private static final int minPLength = 2;

    /**
     * Create a new instance of the order
     * @param payload a byte array representation of the payload
     */
    protected U2UFSPOrderGetChunk(byte[] payload)
    {
        super(GETC, payload);

        checkOrderState(minPLength, GETC);
    }

    /**
     * Create a new instance of the order from an byte array representation of a whole order, use it
     * when you read an order from an InputStream, because you only have a byte array and you want
     * to use the specialise methods of the subclass
     * @param orderBytes a byte array representation of the order
     * @param onlyForCanUseTheOtherConstructor this parameter doesn't have any kind of importance
     * hack, the second parameter doesn't have any kind of importance, you can pass true or false
     */
    protected U2UFSPOrderGetChunk(byte[] orderBytes, boolean onlyForCanUseTheOtherConstructor)
    {
        super(orderBytes);

        checkOrderState(minPLength, GETC);
    }

    /**
     * return the requested chunk's index
     * @return a short representation of the requested chunk's index [0 - (2^15-1)], -1 if the short isn't in the interval
     */
    public short getChunkIndex()
    {
        checkOrderState(minPLength, GETC);

        short index;
        byte[] payload = this.getPayload();
        // (based from the DataInputStream implementation Sun Microsystems) the next two bytes of this input stream, interpreted as an short
        int ch1 = (payload[0] < 0? (payload[0] + 256) : payload[0]);
        int ch2 = (payload[1] < 0? (payload[1] + 256) : payload[1]);
        if ((ch1 | ch2) < 0)
        {
            throw new VerifyError("character in bad state");
        }

        index = (short)((ch1 << 8) + (ch2 << 0));
        
        //checking index (2^15-1)
        if((index < 0) || (index > 32767))
        {
            return -1;
        }
        else
        {
            return index;
        }

    }
}
