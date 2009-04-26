/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.u2u.filesharing.fsprotocol;

/**
 * This class handle an order of the QUIT message, that sends finish message to remote peer
 * <length prefix>  <order id>   <no payload>  <br>
 * |   4 bytes    |  1 byte    |   0 bytes    |
 * |              |            |              |
 * @author Irene & Sergio
 */
public class U2UFSPOrderQuit extends U2UFSProtocolOrder{

    /**
     * number of payload's bytes
     */
    private static final int minPLength = 0;

    /**
     * Create a new instance of the order
     */
    protected U2UFSPOrderQuit()
    {
        super(QUIT, new byte[0]);

        checkOrderState(minPLength, QUIT);
    }

    /**
     * Create a new instance of the order from an byte array representation of a whole order, use it
     * when you read an order from an InputStream, because you only have a byte array and you want
     * to use the specialise methods of the subclass
     * @param orderBytes a byte array representation of the order
     * @param onlyForCanUseTheOtherConstructor this parameter doesn't have any kind of importance
     * hack, the second parameter doesn't have any kind of importance, you can pass true or false
     */
    protected U2UFSPOrderQuit(byte[] orderBytes, boolean onlyForCanUseTheOtherConstructor)
    {
        super(orderBytes);

        checkOrderState(minPLength, QUIT);

        if(this.getPayload().length > 0)
        {
            throw new VerifyError("order in bad state");
        }
    }
}
