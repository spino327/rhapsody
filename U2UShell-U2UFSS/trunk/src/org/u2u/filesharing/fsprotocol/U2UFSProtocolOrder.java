/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.u2u.filesharing.fsprotocol;

import java.util.ArrayList;

/**
 * This class represents an protocol's order of any type and its payload
 *  <length prefix> <order id> <payload>
 * |   4 bytes     |  1 byte  | message |
 * |               |          |  depent |
 * @author Irene & Sergio
 */
public class U2UFSProtocolOrder {

    /**
     * CONN type represents the order, DP request connection for the file with SHA-1.
     */
    public static final byte CONN = 0x49;//I

    /**
     * INFO type represents the order, DP request file's info with SHA-1.
     */
    public static final byte INFO = 0x4C;//L

    /**
     * GETC type represents the order, DP request chunk with SHA-1.
     */
    public static final byte GETC = 0x59;//Y

    /**
     * ACKW type represents the order, DP sends Acknowledge message.
     */
    public static final byte ACKW = 0x53;//S

    /**
     * GETC type represents the order, DP sends finish message.
     */
    public static final byte QUIT = 0x41;//A

    //
    private final int lengthPrefix;
    private final byte orderId;
    private final byte[] payload;

    //
    /**
     * Build an order from the given parameters
     * @param order a byte representation of an protocol's order, eg. 'CONN' or 'INFO'
     * @param payload a byte array representation of the order's payload
     */
    protected U2UFSProtocolOrder(byte order, byte[] payload)
    {
        //verify order id
        //FIXME spino327@gmail.com missing orders
        if( (order == CONN) || (order == INFO) || (order == GETC) || (order == QUIT))
           this.orderId = order;
        else
            throw new IllegalArgumentException("The order id is not correct");

        //verify payload
        if(payload == null)
        {
            throw new NullPointerException("payload can't be null");
        }
        /*else
        {
           //FIXME spino327@gmail.com ?
           for(byte a : payload)
           {
               if((a < -128) || (a > 127))
               {
                   throw new IllegalArgumentException("the payload has incorrect values");
               }
           }
        }*/
        this.payload = payload.clone();

        //generating length prefix
        this.lengthPrefix = /*1 byte for the order id*/1 + payload.length;
    }

    /**
     * Builds an order from a byte array representation
     * @param orderBytes
     */
    protected U2UFSProtocolOrder(byte[] orderBytes)
    {
        //verificating
        if(orderBytes == null)
        {
            throw new NullPointerException("orderBytes can't be null");
        }
        else if(orderBytes.length < (4 + 1))
        {
            throw new IllegalArgumentException("orderBytes can't have less than five bytes");
        }
        
        this.lengthPrefix = extractLengthPrefix(orderBytes);
        this.orderId = orderBytes[4];
        this.payload = extractPayload(orderBytes);
    }

    /**
     * Builds an order from an ArrayList representation
     * @param array 
     */
    protected U2UFSProtocolOrder(ArrayList<Byte> array)
    {
        //verificating
        if(array == null)
        {
            throw new NullPointerException("array list can't be null");
        }
        else if(array.size() < (4 + 1))
        {
            throw new IllegalArgumentException("array list can't have less than five Bytes");
        }
        
    	byte[] orderBytes = new byte[array.size()];

    	for(int i = 0; i < orderBytes.length; i++)
    	{
    		orderBytes[i] = (Byte) array.get(i);
		}

        this.lengthPrefix = extractLengthPrefix(orderBytes);
        this.orderId = orderBytes[4];
        this.payload = extractPayload(orderBytes);
    }

    //

    /**
     * returns the byte representation of the order
     * @return a byte
     */
    public byte getOrderId()
    {
        return orderId;
    }

    /**
     * returns the byte array representation of the order's payload
     * @return a byte array
     */
    public byte[] getPayload()
    {
        return payload;
    }

    /**
     * returns the int representation of the order's length
     * @return an int
     */
    public int getLengthPrefix()
    {
        return lengthPrefix;
    }

    /**
     * Returns a byte array representation of the U2UFSProtocolOrder
     * <length prefix> <order id> <payload>
     * |   4 bytes    |  1 byte  | message
     * |              |          |  depent
     * @return a byte array of the complete order
     */
    public byte[] getByteArrayRepresentation()
    {
        int i;
        byte[] array = new byte[4 + 1 + payload.length];
        
        //length prefix
        array[0] = (byte) ((lengthPrefix >>> 24) & 0xFF);
        array[1] = (byte) ((lengthPrefix >>> 16) & 0xFF);
        array[2] = (byte) ((lengthPrefix >>>  8) & 0xFF);
        array[3] = (byte) ((lengthPrefix >>>  0) & 0xFF);

        //order id
        array[4] = orderId;

        //payload
        i = 5;
        for(byte a : payload)
        {
            array[i] = a;
            i++;
        }

        return array;
    }

    /**
     * check if the payload of the order have at least the minimun length,
     * and if the order was cast correctly
     * @param minPayloadLength
     * @param expecterOrder byte representation of the expected order
     * @throws java.lang.VerifyError
     */
    protected void checkOrderState(int minPayloadLength, byte expecterOrder) throws VerifyError
    {
        if (payload.length < minPayloadLength)
        {
            throw new VerifyError("order in bad state");
        }
        else if(this.orderId != expecterOrder)
        {
            throw new IllegalAccessError("this object(order) isn't of the subclass type");
        }
    }

    private byte[] extractPayload(byte[] orderBytes)
    {
        //-1 for the order id byte
        byte[] subset = new byte[lengthPrefix - 1];
        for (int i = 0; i < subset.length; i++)
        {
            subset[i] = orderBytes[i + 5];
        }
        return subset;
    }

    private int extractLengthPrefix (byte[] orderBytes)
    {
        // (based from the DataInputStream implementation Sun Microsystems)the next four bytes of this input stream, interpreted as an int
        int ch1 = (orderBytes[0] < 0? (orderBytes[0] + 256) : orderBytes[0]);
        int ch2 = (orderBytes[1] < 0? (orderBytes[1] + 256) : orderBytes[1]);
        int ch3 = (orderBytes[2] < 0? (orderBytes[2] + 256) : orderBytes[2]);
        int ch4 = (orderBytes[3] < 0? (orderBytes[3] + 256) : orderBytes[3]);
        if ((ch1 | ch2 | ch3 | ch4) < 0)
        {
            throw new VerifyError("character in bad state");
        }

        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
    }

    //factory methods

    /**
     * Return a new Instance of a subclass of U2UFSProtocolOrder that result of the parse of the byte array.
     * You need to cast the resuting object, for using the specialise methods
     * @param orderBytes order's byte array representation
     * @return an Object of a subclass of U2UFSProtocolOrder
     */
    public static U2UFSProtocolOrder parseOrderFromByteArray(byte[] orderBytes)
    {
        U2UFSProtocolOrder order = null;

        //checking
        if(orderBytes == null)
        {
            throw new NullPointerException("orderBytes can't be null");
        }
        else if(orderBytes.length < (4 + 1))
        {
            throw new IllegalArgumentException("orderBytes can't have less than five bytes");
        }

        //order id
        byte orderId = orderBytes[4];

        //FIXME spino327@gmail.com this is very very important!s
        switch(orderId)
        {
            case CONN:
                //using a hacked constructor
                order = new U2UFSPOrderConnection(orderBytes, true);
                break;

            case INFO:
                //using a hacked constructor
                order = new U2UFSPOrderFileInfo(orderBytes, true);
                break;

            case GETC:
                //using a hacked constructor
                order = new U2UFSPOrderGetChunk(orderBytes, true);
                break;
                
            default:
                //using a hacked constructor
                order = new U2UFSPOrderQuit(orderBytes, true);
                break;
        }

        return order;
    }

    /**
     * Return a new Instance of the specialise class U2UFSPOrderConnection
     * @param protVer protocol version
     * @param sha1File byte array that represents the file's SHA1 hash
     * @param peerId JXTA's id of this peer
     * @return U2UFSPOrderConnection
     */
    public static U2UFSPOrderConnection newOrderConnection(byte protVer, byte[] sha1File, String peerId)
    {
        //checking protVer
        if(protVer == 0x00)
        {
            throw new IllegalArgumentException("Protocol Version can't be 0x00");
        }

        //checking sha1File
        if(sha1File == null)
        {
            throw new NullPointerException("sha1File can't be null");
        }
        else if(sha1File.length != 20)
        {
            throw new IllegalArgumentException("the sha1File most have 20 bytes");
        }
        /*else
        {
           //FIXME spino327@gmail.com ?
           for(byte a : sha1File)
           {
               if((a < -128) || (a > 127))
               {
                   throw new IllegalArgumentException("the sha1File has incorrect values");
               }
           }
        }*/

        //checking peerId
        if(peerId == null)
        {
            throw new NullPointerException("Peer's Id can't be null");
        }
        //JXTA's ID have 80 bytes, with urn:jxta:uuid-
        else if(peerId.length() != 80)
        {
            throw new IllegalArgumentException("Peer's Id haven't the correct number of bytes");
        }

        //creating the payload, <protocolVersion> <sha1File> <peerId> <(o)certificate>
        //                     |      1 byte     | 20 bytes |33 bytes|    n bytes
        int i;
        byte[] payload = new byte[1 + 20 + 33];

        //protocol version
        payload[0] = protVer;

        //sha1File
        i = 1;
        for(byte a : sha1File)
        {
            payload[i] = a;
            i++;
        }

        //PeerId
        //i = 21;
        byte[] id = parseIDAsString(peerId);
        
        for(byte a : id)
        {
            payload[i] = a;
            i++;
        }

        return new U2UFSPOrderConnection(payload);
    }

    /**
     * Return a new Instance of the specialise class U2UFSPOrderFileInfo
     * @param queryType PEER_CHUNKS or CHUNKS_SHA1
     * @param sha1File byte array that represents the file's SHA1 hash
     * @return U2UFSPOrderFileInfo
     */
    public static U2UFSPOrderFileInfo newOrderFileInfo(byte queryType, byte[] sha1File)
    {
        //checking queryType
        if((queryType != U2UFSPOrderFileInfo.PEER_CHUNKS) && (queryType != U2UFSPOrderFileInfo.CHUNKS_SHA1))
        {
            throw new IllegalArgumentException("invalid queryType");
        }

        //checking sha1File
        if(sha1File == null)
        {
            throw new NullPointerException("sha1File can't be null");
        }
        else if(sha1File.length != 20)
        {
            throw new IllegalArgumentException("the sha1File most have 20 bytes");
        }
        /*else
        {
         * //FIXME spino327@gmail.com ?
           for(byte a : sha1File)
           {
               if((a < -128) || (a > 127))
               {
                   throw new IllegalArgumentException("the sha1File has incorrect values");
               }
           }
        }*/

        //creating the payload,  <query type> <file's SHA1>
        //                      |   1 byte   |  20 bytes   |
        int i;
        byte[] payload = new byte[1 + 20];

        //query type
        payload[0] = queryType;

        //sha1File
        i = 1;
        for(byte a : sha1File)
        {
            payload[i] = a;
            i++;
        }

        return new U2UFSPOrderFileInfo(payload);
    }

    /**
     * Return a new Instance of the specialise class U2UFSPOrderGetChunk
     * @param index int specifying the zero-based chunk index [0-37767]
     * @return U2UFSPOrderGetChunk
     */
    public static U2UFSPOrderGetChunk newOrderGetChunk(int index)
    {
        //checking index (2^15-1)
        if((index < 0) || (index > 32767))
        {
            throw new IllegalArgumentException("incorrect chunk's index");
        }

        //creating the payload,   <index>
        //                      |  2 byte |
        byte[] payload = new byte[2];

        payload[0] = (byte) ((index >>>  8) & 0xFF);
        payload[1] = (byte) ((index >>>  0) & 0xFF);

        return new U2UFSPOrderGetChunk(payload);
    }

    /**
     * Return a new Instance of the specialise class U2UFSPOrderQuit
     * @return U2UFSPOrderQuit
     */
    public static U2UFSPOrderQuit newOrderQuit()
    {
        return new U2UFSPOrderQuit();
    }

    /**
     * Parse a byte array from a JXTA ID in String format
     * @param t the id in String format
     * @return byte array representation, with 33 bytes
     */
    protected static byte[] parseIDAsString(String t) {

        byte[] b = new byte[33];

        int pos = t.indexOf('-');
        if (pos == -1) {
            throw new IllegalArgumentException("Missing id type: " + t);
        }
        String type = t.substring(0, pos);
        if (!type.equalsIgnoreCase("urn:jxta:uuid")) {
            throw new IllegalArgumentException("Invalid id type: " + t);
        }
        if (t.length() - pos - 1 != 66) {
            throw new IllegalArgumentException( "Invalid id length: " + t);
        }
        t = t.substring(pos + 1);
        for (int i = 0, j = 0; i < 33; i++) {
            int hi = Character.digit(t.charAt(j++), 16);
            int lo = Character.digit(t.charAt(j++), 16);
            if (hi == -1 || lo == -1) {
                throw new IllegalArgumentException( "Invalid id hash: " + t);
            }
            b[i] = (byte) ((hi << 4) | lo);
        }
        return b;
    }

    /**
     * converts an byte array in a String representation of a JXTA ID
     * @param hash byte array
     * @return String representation, 80 characters, with 'urn:jxta:uuid-'
     */
    protected static String idToString(byte[] hash) {

        StringBuffer d = new StringBuffer();

        d.append("urn:jxta:uuid");
        d.append('-');
        for(int i=0; i<hash.length; i++)
        {
            int v = hash[i] & 0xFF;
            if(v < 16)
                d.append("0");

            d.append(Integer.toString(v, 16).toUpperCase());
        }

        return d.toString();
    }

}
