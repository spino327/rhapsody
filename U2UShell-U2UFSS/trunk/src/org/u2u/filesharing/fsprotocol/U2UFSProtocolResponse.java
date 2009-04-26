/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.u2u.filesharing.fsprotocol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * This class represents a response from a remote peer (not specialise, use the subclasses)
 *  <length prefix> <response id> <response number> <payload>
 * |   4 bytes     |  1 byte     |     2 bytes     | message |
 * |               |             |      short      | depent  |
 * 
 * @author Irene & Sergio
 */
public class U2UFSProtocolResponse {

    //response common number
    /**
     * Represents the response "Syntax error, order unknown"
     * 5 End permanent response
     */
    public static final String R501 = "501";
    /**
     * Represents the response "Syntax error, incorrect parameters"
     * 5 End permanent response
     */
    public static final String R511 = "511";
    /**
     * Represents the response "order request not executed, peer not available"
     * 5 End permanent response
     */
    public static final String R521 = "521";
    /**
     * Represents the response "Transaction Failed"
     * 5 End permanent response
     */
    public static final String R541 = "541";

    /**
     * Common RESPONSES of every Response Class
     */
    private static final String[] COMMON_RESPONSES = new String[]{R501, R511, R521, R541};

    //RESPONSES type
    /**
     * RCONN type represents the decition of the remote peer.
     */
    public static final byte RCONN = 0x49;//I

    /**
     * RINFO type represents the info about the chunks that the remote peer have, or
     * the SHA-1 of all chunks in the file.
     */
    public static final byte RINFO = 0x4C;//L

    /**
     * RGETC type represents the chunk data that was requested.
     */
    public static final byte RGETC = 0x59;//Y

    /**
     * RQUIT type represents only the sends of the QUIT order.
     */
    public static final byte RQUIT = 0x41;//A

    private final int lengthPrefix;
    private final byte responseId;//represents the type of the response for the class
    private final String responseNumber;//represents the number of the response, eg, 201, 401
    private final byte[] resPayload;//represents the payload of the response
    //
    /**
     * Build an response from the given parameters
     * @param type a byte representation of an protocol's response, eg. 'RCONN' or 'RINFO'
     * @param resNum a short that represents the response number, eg. 201, 401, this response have a fixed size(3)
     * @param payload a byte array representation of the response's payload
     */
    protected U2UFSProtocolResponse(byte type, String resNum, byte[] payload)
    {
        //verify response id
        if( (type == RCONN) || (type == RINFO) || (type == RGETC) || (type == RQUIT) )
           responseId = type;
        else
            throw new IllegalArgumentException("The type is not correct");

        //verify response number
        if(resNum.length() != 3)
        {
            throw new IllegalArgumentException("The response fixed size isn't correct");
        }
        else
        {
            boolean isCorrect = true;
            for(int i = 0; i < 3; i++)
            {
                if(!Character.isDigit(resNum.charAt(i)))
                {
                    isCorrect = false;
                }
            }

            if(isCorrect)
            {
                this.responseNumber = new String(resNum);
            }
            else
            {
                throw new IllegalArgumentException("The response only can contain digits");
            }
        }       

        //verify payload
        if(payload == null)
        {
            throw new NullPointerException("payload can't be null");
        }
        /*else
        {
           for(byte a : payload)
           {
               if((a < -128) || (a > 127))
               {
                   throw new IllegalArgumentException("the payload has incorrect values");
               }
           }
        }*/
        this.resPayload = payload.clone();

        //generating length prefix
        this.lengthPrefix = 1 + 2 + payload.length;
    }

    /**
     * Builds a response from a byte array representation
     * @param responseBytes
     */
    protected U2UFSProtocolResponse(byte[] responseBytes)
    {
        //checking
        if(responseBytes == null)
        {
            throw new NullPointerException("responseBytes can't be null");
        }
        else if(responseBytes.length < (4 + 1 + 2))
        {
            throw new IllegalArgumentException("responseBytes can't have less than seven bytes");
        }

        this.lengthPrefix = extractLengthPrefix(responseBytes);
        this.responseId = responseBytes[4];
        this.responseNumber = extractResponseNumber(responseBytes);
        this.resPayload = extractPayload(responseBytes);
    }

    /**
     * Builds a response from an ArrayList representation
     * @param array
     */
    protected U2UFSProtocolResponse(ArrayList<Byte> array)
    {
        //verificating
        if(array == null)
        {
            throw new NullPointerException("array list can't be null");
        }
        else if(array.size() < (4 + 1 + 2))
        {
            throw new IllegalArgumentException("array list can't have less than seven Bytes");
        }

    	byte[] responseBytes = new byte[array.size()];

        long ini = System.currentTimeMillis();
    	for(int i = 0; i < responseBytes.length; i++)
    	{
    		responseBytes[i] = (Byte) array.get(i);
		}
        System.out.println("passing ArrayList to byte[] = "+(System.currentTimeMillis()-ini)+" ms");

        array.clear();

        this.lengthPrefix = extractLengthPrefix(responseBytes);
        this.responseId = responseBytes[4];
        this.responseNumber = extractResponseNumber(responseBytes);
        this.resPayload = extractPayload(responseBytes);
    }

    //
    /**
     * returns the type of the response, it can be RCONN, RINFO, RGETC
     * @return byte that represents the type
     */
    public byte getResponseId()
    {
        return responseId;
    }

    /**
     * returns the response number, eg 201, 401
     * @return a String that represents the response number
     */
    public String getResponseNumber()
    {
        return responseNumber;
    }

    /**
     * returns a Byte Array representation of the response, the major of cases is the better option
     * @return byte array representation of response
     */
    public byte[] getPayload()
    {
        return resPayload;
    }

    /**
     * returns a int representation of the response, only if it's logical
     * @return int representation of response
     */
    //public abstract int getIntValue();

    /**
     * returns a String representation of the response
     * @return String representation of response
     */
    //public abstract String getValue();
    

    /**
     * Returns a byte array representation of the U2UFSProtocolResponse
     * <length prefix> <response id> <response number> <payload>
     * |   4 bytes    |  1 byte     |     2 bytes     | message
     * |              |             |      short      | depent
     * @return a byte array of the complete response
     */
    public byte[] getByteArrayRepresentation()
    {
        int i;
        
        byte[] array = new byte[4 + 1 + 2 + resPayload.length];

        /*
         * length prefix
         * (based from the DataOutputStream implementation Sun Microsystems) Writes an int to the underlying output stream as four
         * bytes, high byte first.
         */
        array[0] = (byte) ((lengthPrefix >>> 24) & 0xFF);
        array[1] = (byte) ((lengthPrefix >>> 16) & 0xFF);
        array[2] = (byte) ((lengthPrefix >>>  8) & 0xFF);
        array[3] = (byte) ((lengthPrefix >>>  0) & 0xFF);

        //response id
        array[4] = this.responseId;

        //response number
        int rn = Integer.parseInt(responseNumber);
        array[5] = (byte) ((rn >>>  8) & 0xFF);
        array[6] = (byte) ((rn >>>  0) & 0xFF);

        //payload
        i = 7;
        for(byte a : resPayload)
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
     * @param expecterResponse byte representation of the expected response
     * @throws java.lang.VerifyError
     */
    protected void checkResponseState(int minPayloadLength, byte expecterResponse) throws VerifyError
    {
        if (resPayload.length < minPayloadLength)
        {
            throw new VerifyError("order in bad state");
        }
        else if(this.responseId != expecterResponse)
        {
            throw new IllegalAccessError("this object(order) isn't of the subclass type");
        }
    }

    private byte[] extractPayload(byte[] responseBytes)
    {
        //-1 for the byte of response id and -2 for the short of response number
        byte[] subset = new byte[lengthPrefix - 1 - 2];
        for (int i = 0; i < subset.length; i++)
        {
            //+7 for (length prefix + response id + response number)
            subset[i] = responseBytes[i + 7];
        }
        return subset;
    }

    private String extractResponseNumber(byte[] responseBytes)
    {
        // (based from the DataInputStream implementation Sun Microsystems) the next two bytes of this input stream, interpreted as an short
        int ch1 = (responseBytes[5] < 0? (responseBytes[5] + 256) : responseBytes[5]);
        int ch2 = (responseBytes[6] < 0? (responseBytes[6] + 256) : responseBytes[6]);
        if ((ch1 | ch2) < 0)
        {
            throw new VerifyError("character in bad state");
        }
        
        return Short.toString((short)((ch1 << 8) + (ch2 << 0)));
    }

    private int extractLengthPrefix (byte[] responseBytes)
    {
        // (based from the DataInputStream implementation Sun Microsystems)the next four bytes of this input stream, interpreted as an int
        int ch1 = (responseBytes[0] < 0? (responseBytes[0] + 256) : responseBytes[0]);
        int ch2 = (responseBytes[1] < 0? (responseBytes[1] + 256) : responseBytes[1]);
        int ch3 = (responseBytes[2] < 0? (responseBytes[2] + 256) : responseBytes[2]);
        int ch4 = (responseBytes[3] < 0? (responseBytes[3] + 256) : responseBytes[3]);
        if ((ch1 | ch2 | ch3 | ch4) < 0)
        {
            throw new VerifyError("character in bad state");
        }

        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
    }

    //factory methods

    /**
     * Return a new Instance of a subclass of U2UFSProtocolResponse that result of the parse of the byte array.
     * You need to cast the resuting object, for using the specialise methods
     * @param responseBytes response's byte array representation
     * @return an Object of a subclass of U2UFSProtocolResponse
     */
    public static U2UFSProtocolResponse parseResponseFromByteArray(byte[] responseBytes)
    {
        U2UFSProtocolResponse response = null;

        //checking
        if(responseBytes == null)
        {
            throw new NullPointerException("responseBytes can't be null");
        }
        else if(responseBytes.length < (4 + 1 + 2))
        {
            throw new IllegalArgumentException("responseBytes can't have less than seven bytes");
        }

        //response id
        byte resId = responseBytes[4];

        //FIXME spino327@gmail.com this is very very important!
        switch(resId)
        {
            case RCONN:
                response = new U2UFSPResponseConnection(responseBytes);
                break;

            case RINFO:
                response = new U2UFSPResponseFileInfo(responseBytes);
                break;

            case RGETC:
                response = new U2UFSPResponseGetChunk(responseBytes);
                break;

            default:
                response = new U2UFSPResponseQuit();
                break;
        }

        return response;
    }
    
    /**
     * Return a new Instance of the specialise class U2UFSPResponseConnection
     * @param resNum response number, must be a valid response, uses the static constanst of the class
     * @param protVer protocol version
     * @param peerId JXTA's id of this peer
     * @return U2UFSPResponseConnection
     */
    public static U2UFSPResponseConnection newResponseConnection(String resNum, byte protVer, String peerId)
    {
        byte[] payload;
        //checking resNum
        checkResponseNumber(resNum, U2UFSPResponseConnection.RESPONSES);

        //checking if the resNum is 2XX
        if(resNum.startsWith("2"))
        {
            //checking protVer
            if(protVer == 0x00)
            {
                throw new IllegalArgumentException("Protocol Version can't be 0x00");
            }

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

            //creating the payload, <protocolVersion> <PeerId>
            //                      |     1 byte     |33 bytes|
            payload = new byte[1 + 33];

            //protocol version
            payload[0] = protVer;

            //PeerId
            byte[] id = parseString(peerId);
            int i = 1;
            for(byte a : id)
            {
                payload[i] = a;
                i++;
            }
        }
        else
        {
            //nothing
            payload = new byte[0];
        }
        

        return new U2UFSPResponseConnection(resNum, payload);
    }

    /**
     * PEER_CHUNKS
     * Return a new Instance of the specialise class U2UFSPResponseFileInfo
     * @param resNum response number, must be a valid response, uses the static constanst of the class
     * 221 i have all, 222 i have some, 411 request wasn't executed, 421 request was canceled, or 5XX - no
     * @param haveIt true if the peer have the chunksList or false if the peer haven't the chunksList
     * @param chunksList list of chunks position, if the peer have all the chunks then the short is new short[0] not null
     * @return U2UFSPResponseConnection
     *
     * eg. if the peer have the chunks 1, 2 and 5 then haveIt = true anf chunksList = short[] {1,2,5} and
     */
    public static U2UFSPResponseFileInfo newResponseFileInfoPeerChunks(String resNum, boolean haveIt, short[] chunksList)
    {
        byte[] payload;
        //checking resNum
        checkResponseNumber(resNum, U2UFSPResponseFileInfo.RESPONSES);

        //checking if the resNum is 2XX
        if(resNum.startsWith("2"))
        {
            //checking chunksList
            if(chunksList == null)
            {
                throw new NullPointerException("chunksList can't be null");
            }

            //creating the payload,  <query type> <list>      (1 short = 2 bytes)
            //                      |   1 byte   |n bytes|

            payload = U2UFSPResponseFileInfo.makingPayloadForPeerChunks(resNum, haveIt, chunksList);
        }
        //4XX and 5XX
        else
        {
            //1(query type)
            payload = new byte[1];

            //query type
            payload[0] = U2UFSPResponseFileInfo.PEER_CHUNKS;
        }

        return new U2UFSPResponseFileInfo(resNum, payload);
    }

    /**
     * CHUNKS_SHA1
     * Return a new Instance of the specialise class U2UFSPResponseFileInfo
     * @param resNum response number, must be a valid response, uses the static constanst of the class
     * 221 have the list completety, 222 impossible, 411 request wasn't executed, 421 request was canceled, or 5XX - no
     * @param hashList the HashMap that represents the hash of all the file's chunks
     * @return U2UFSPResponseConnection
     *
     * eg. HashMap representation<br>
     * posChunks|sha1<br>
     * Short    |String<br>
     *  _________________<br>
     * |___1____|__XXXX__|<br>
     * |___2____|__XXXX__|<br>
     * |_  ... _|_  ... _|<br>
     * |___n____|__XXXX__|
     */
    public static U2UFSPResponseFileInfo newResponseFileInfoChunksSha1(String resNum, HashMap<Short, String> hashList)
    {
        byte[] payload;
        //checking resNum
        checkResponseNumber(resNum, U2UFSPResponseFileInfo.RESPONSES);

        //checking if the resNum is 221
        if(resNum.equals(U2UFSPResponseFileInfo.R221))
        {
            //checking hashList
            if(hashList == null)
            {
                throw new NullPointerException("hashList can't be null");
            }

            //creating the payload,  <query type> <list>      (1 short = 2 bytes) (hash = 20 bytes)
            //                      |   1 byte   |n bytes|

            payload = U2UFSPResponseFileInfo.makingPayloadForChunksSha1(resNum, hashList);
        }
        //4XX and 5XX
        else
        {
            //1(query type)
            payload = new byte[1];

            //query type
            payload[0] = U2UFSPResponseFileInfo.CHUNKS_SHA1;
        }

        return new U2UFSPResponseFileInfo(resNum, payload);
    }

    /**
     * Return a new Instance of the specialise class U2UFSPResponseGetChunk
     * @param resNum response number, must be a valid response, uses the static constanst of the class
     * 211 available, 212 not available, 411 request wasn't executed, 421 request was canceled, or 5XX - no
     * @param chunkBytes byte array representation of the chunk's data
     * @return U2UFSPResponseGetChunk
     *
     */
    public static U2UFSPResponseGetChunk newResponseGetChunk(String resNum, byte[] chunkBytes)
    {
        byte[] payload;
        //checking resNum
        checkResponseNumber(resNum, U2UFSPResponseGetChunk.RESPONSES);

        //checking if the resNum is 211
        if(resNum.equals(U2UFSPResponseGetChunk.R211))
        {
            //checking hashList
            if(chunkBytes == null)
            {
                throw new NullPointerException("chunkBytes can't be null");
            }

            //creating the payload,  <chunk's bytes> 
            //                      |     n byte    |

            payload = U2UFSPResponseGetChunk.makingPayloadChunkBytes(resNum, chunkBytes);
        }
        else
        {
            //nothing
            payload = new byte[0];
        }

        return new U2UFSPResponseGetChunk(resNum, payload);
    }

    /**
     * Parse a byte array from a JXTA ID in String format
     * @param t the id in String format
     * @return byte array representation, with 33 bytes
     */
    protected static byte[] parseString(String t) {

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

    /**
     * checking the response number
     * @param resNum
     * @param classResponses the responses from the specific class
     * @throws java.lang.IllegalArgumentException
     * @throws java.lang.NullPointerException
     */
    private static void checkResponseNumber(String resNum, String[] classResponses) throws IllegalArgumentException, NullPointerException
    {
        //checking resNum
        if (resNum == null)
        {
            throw new NullPointerException("Response number can't be null");
        } else
        {
            boolean resNumValid = false;
            Arrays.sort(classResponses);
            resNumValid = (Arrays.binarySearch(classResponses, resNum) >= 0 ? true : false);
            if (!resNumValid)
            {
                resNumValid = (Arrays.binarySearch(COMMON_RESPONSES, resNum) >= 0 ? true : false);
            }
            if (!resNumValid)
            {
                throw new IllegalArgumentException("Response number invalid for this kind of response");
            }
        }
    }

}
