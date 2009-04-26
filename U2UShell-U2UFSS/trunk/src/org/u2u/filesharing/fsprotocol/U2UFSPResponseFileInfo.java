/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.u2u.filesharing.fsprotocol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This class handle a response of the INFO message, that request file's info with SHA-1
 * @author Irene & Sergio
 */
public class U2UFSPResponseFileInfo extends U2UFSProtocolResponse {

    /**
     * list of remote peer's chunks( T(1 4 5...) or
     * F(2 3 6...) only the most small in bytes),
     *
     * T ( 1 4 5 )           2 bytes for short codificaction
     * | | | | | |
     * 1 1 2 2 2 1
     */
    public static final byte PEER_CHUNKS = 0x30;//0
    /**
     * sha-1 list of file's chunks( (1, xxxxxxxxx; 2, xxxxxxxx....))
     *
     * H ( 1XXXX 2XXXX 3XXXX )         22 bytes: 2  for the short
     * | |   |     |     |   |                   20 for the sha1
     * 1 1   22    22    22  1
     *
     */
    public static final byte CHUNKS_SHA1 = 0x31;//1

    /**
     * Represents the response "file's info complete, the peer have all the chunks"
     * 2 End affirmative response
     */
    public static final String R221 = "221";
    /**
     * Represents the response "file's info incomplete, the peer haven't all the chunks"
     * 2 End affirmative response
     */
    public static final String R222 = "222";
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
     * Represents the response "info request not available"
     * 4 Intermediate positive response
     */
    public static final String R431 = "431";

    protected static final String[] RESPONSES = new String[] {R221, R222, R411, R421, R431};
    /**
     * number of payload's bytes
     */
    private static final int minPLength = 1;

    /**
     * Create a new instance of the response
     * @param resNum a String
     * @param payload a byte array representation of the payload
     */
    protected U2UFSPResponseFileInfo(String resNum, byte[] payload)
    {
        super(RINFO, resNum, payload);

        //the payload have reason only for response 2XX
        if(resNum.equals(R221) || resNum.equals(R222))
        {
            checkResponseState(minPLength, RINFO);
        }
    }

    /**
     * Create a new instance of the response
     * @param responseBytes
     */
    protected U2UFSPResponseFileInfo(byte[] responseBytes)
    {
        super(responseBytes);

        //the payload have reason only for response 2XX
        String resNum = this.getResponseNumber();
        if(resNum.equals(R221) || resNum.equals(R222))
        {
            checkResponseState(minPLength, RINFO);
        }
    }

    /**
     * return the query type (PEER_CHUNKS or CHUNKS_SHA1)
     * @return a byte representation of the query type
     */
    public byte getQueryType()
    {
        checkResponseState(minPLength, RINFO);
        return this.getPayload()[0];
    }

    /**
     * 1) return a ArrayList representation of the list of remote peer's chunks( T(1 4 5...) or
     * F(2 3 6...) only the most small in bytes)<br>
     * ArrayList<br>
     *  _________<br>
     * |_________|------> 0 Charater with values 'T' or 'F'<br>
     * |_________|------> 1 short[] with the list of remote peer's chunks 1 2 3<br>
     *
     * or
     *<br>
     * 2) sha-1 list of file's chunks( (1, xxxxxxxxx; 2, xxxxxxxx....))<br>
     * H ( 1XXXX 2XXXX 3XXXX )         22 bytes: 2  for the short<br>
     * | |   |     |     |   |                   20 for the sha1<br>
     * 1 1   22    22    22  1<br>
     * HashMap<Short, byte[]> representation<br>
     * __________________<br>
     * |___1____|__XXXX__|<br>
     * |___2____|__XXXX__|<br>
     * |_  ... _|_  ... _|<br>
     * |___n____|__XXXX__|<br>
     *
     *
     * or null if the response have a bad state
     *
     * @return Object, you need to uses (obj instanceOf ArrayList) or (obj instanceOf HashMap)
     * for uses the result object
     * @throws IllegalArgumentException
     * @throws VerifyError 
     */
    @SuppressWarnings("unchecked")
    public Object getList()// throws IllegalArgumentException, VerifyError
    {
        checkResponseState(minPLength, RINFO);

        Object obj= null;

        String resNum = this.getResponseNumber();
        if(resNum.equals(R221) || resNum.equals(R222))
        {
            byte[] payload = this.getPayload();

            char head = (char) payload[1];//head 'T' or 'F' or 'H'
            char first = (char) payload[2];//firts '('
            char last = (char) payload[payload.length - 1];//last ')'

            if((head == 'T') ||
                    (head == 'F') ||
                    (head == 'H'))
            {
                if(/*first*/((first == '(')) &&
                    /*last*/(last == ')'))
                {
                    if(head == 'H')
                    {
                        if(this.getQueryType() == CHUNKS_SHA1)
                        {
                            //FIXME spino327@gmail.com the logic was changed
                            /*payload = XH(1XXXX 2XXXX) => start = 3 toRead = length - start - 1
                             *
                             * X H ( 1XXXX 2XXXX )
                             * | | |   |     |   |
                             * 1 1 1   22    22  1
                             * HashMap representation<br>
                             *  _________________
                             * |___1____|__XXXX__|
                             * |___2____|__XXXX__|
                             * |_  ... _|_  ... _|
                             * |___n____|__XXXX__|
                             */
                            int toRead = (payload.length - 3 - 1)/22;

                            if( ((payload.length - 3 - 1)/22.0) - toRead != 0)
                            {
                                throw new IllegalArgumentException("the payload is in bad format!");
                            }

                            Map<Short, byte[]> temp = new HashMap<Short, byte[]>();

                            for(int i = 0; i < (toRead); i++)
                            {
                                short posChunk;
                                byte[] hash;
                                int firstByte;
                                int secondByte;
                                int lastByte;

                                //Pos chunk
                                firstByte = 3 + 22*i;
                                secondByte = firstByte + 1;
                                //parsing the short
                                // (based from the DataInputStream implementation Sun Microsystems) the next two bytes of this input stream, interpreted as an short
                                int ch1 = (payload[firstByte] < 0? (payload[firstByte] + 256) : payload[firstByte]);
                                int ch2 = (payload[secondByte] < 0? (payload[secondByte] + 256) : payload[secondByte]);
                                if ((ch1 | ch2) < 0)
                                {
                                    throw new VerifyError("character in bad state");
                                }

                                posChunk = (short)((ch1 << 8) + (ch2 << 0));

                                //hash
                                firstByte = secondByte + 1;
                                lastByte = firstByte + 19;//19 because firstByte + 19 = 20

                                hash = new byte[20];
                                
                                for(int j = firstByte, k = 0; j <= lastByte; j++)
                                {
                                    hash[k++] = payload[j];
                                }

                                //put
                                temp.put(posChunk, hash);

                            }

                            //
                            obj = temp;

                            temp = null;
                        }
                    }
                    else if(this.getQueryType() == PEER_CHUNKS)
                    {
                        //                 _________
                        //ArrayList ->  0 |_________|------> Charater with values 'T' or 'F'
                        //              1 |_________|------> short[] with the list of remote peer's chunks 1 2 3
                        ArrayList temp = new ArrayList();
                        int toRead;
                        short[] list;

                        //TRUE o FALSE
                        temp.add(head);

                        //list
                        // X T ( 1 4 5 )           2 bytes for short codificaction
                        // | | | | | | |              toRead = length - 3 - 1
                        // 1 1 1 2 2 2 1                       to read 6 bytes, for 1 4 and 5

                        toRead = (payload.length - 3 - 1)/2;

                        if( ((payload.length - 3 - 1)/2.0) - toRead != 0)
                        {
                            throw new IllegalArgumentException("the payload is in bad format!");
                        }

                        list = new short[toRead];


                        for(int i = 0; i < (toRead); i++)
                        {
                            int firstByte = 3 + 2*i;
                            int secondByte = firstByte + 1;
                            //parsing the short
                            // (based from the DataInputStream implementation Sun Microsystems) the next two bytes of this input stream, interpreted as an short
                            int ch1 = (payload[firstByte] < 0? (payload[firstByte] + 256) : payload[firstByte]);
                            int ch2 = (payload[secondByte] < 0? (payload[secondByte] + 256) : payload[secondByte]);
                            if ((ch1 | ch2) < 0)
                            {
                                throw new VerifyError("character in bad state");
                            }

                            list[i] = (short)((ch1 << 8) + (ch2 << 0));
                        }

                        //add the list of shorts
                        temp.add(list);

                        obj = temp;

                        temp = null;
                    }
                }
            }
        }
        
        return obj;
    }

    /**
     * PEER_CHUNKS
     * making the payload
     * @param res the response number
     * @param have true if the peer have the chunksList or false if the peer haven't the chunksList
     * @param list list of chunks position, if the peer have all the chunks then the short is new short[0] not null
     * @return byte[]
     * @throws IllegalArgumentException 
     */
    public static byte[] makingPayloadForPeerChunks(String res, boolean have, short[] list)// throws IllegalArgumentException
    {
        byte[] pay;
        //list
        // T ( 1 4 5 )           2 bytes for short codificaction
        // | | | | | |
        // 1 1 2 2 2 1
        
        if(res.equals(R221))
        {
            pay = new byte[1 + 3];

            //query type
            pay[0] = U2UFSPResponseFileInfo.PEER_CHUNKS;

            //list
            //TRUE
            pay[1] = (byte) 'T';

            //'('
            pay[2] = (byte) '(';

            //')'
            pay[3] = (byte) ')';
        }
        else if(res.equals(R222))
        {
            //1(query type) + 1(T or F or H) + 1('(') + 2*(number of chunks) + 1(')s')
            pay = new byte[1 + 1 + 1 + 2*(list.length) + 1];

            //query type
            pay[0] = U2UFSPResponseFileInfo.PEER_CHUNKS;

            //list
            //TRUE or FALSE
            pay[1] = (byte) (have? 'T' : 'F');

            //'('
            pay[2] = (byte) '(';

            //shorts
            int i = 3;
            for(short a : list)
            {
                //little endian

                //firts byte
                pay[i] =  (byte) ((a >>>  8) & 0xFF);
                //second byte
                pay[i = i + 1] = (byte) ((a >>>  0) & 0xFF);
                i++;
            }

            //')'
            if(i != (pay.length - 1))
            {
                throw new IllegalArgumentException("the codification was wrong");
            }
            pay[i] = ')';
        }
        else
        {
            //nothing
            pay = new byte[0];
        }
        
        return pay;
    }

    /**
     * CHUNKS_SHA1
     * making the payload
     * @param res the response number
     * @param hashList the HashMap that represents the hash of all the file's chunks
     * @return byte[]
     * @throws IllegalArgumentException
     *
     * HashMap representation<br>
     *  _________________
     * |___1____|__XXXX__|
     * |___2____|__XXXX__|
     * |_  ... _|_  ... _|
     * |___n____|__XXXX__|
     */
    public static byte[] makingPayloadForChunksSha1(String res, HashMap<Short, String> hashList)// throws IllegalArgumentException
    {
        byte[] pay;

        /*payload = XH(1XXXX 2XXXX) => start = 3 toRead = length - start - 1
         *
         * X H ( 1XXXX 2XXXX )
         * | | |   |     |   |
         * 1 1 1   22    22  1
         * HashMap representation<br>
         *  _________________
         * |___1____|__XXXX__|
         * |___2____|__XXXX__|
         * |_  ... _|_  ... _|
         * |___n____|__XXXX__|
         */
        if(res.equals(R221))
        {
            //1(query type) + 1(T or F or H) + 1('(') + 22*(number of chunks) + 1(')s')
            pay = new byte[1 + 1 + 1 + 22*(hashList.size()) + 1];

            //query type
            pay[0] = U2UFSPResponseFileInfo.CHUNKS_SHA1;

            //list
            //H
            pay[1] = (byte) 'H';

            //'('
            pay[2] = (byte) '(';

            //shorts
            int i = 3;

            //loop the HashMap
            for(Map.Entry<Short, String> entry : hashList.entrySet())
            {
                //pos as short, 2 bytes little endian
                //first
                short pos = entry.getKey();
                pay[i] =  (byte) ((pos >>>  8) & 0xFF);
                pay[i = i + 1] = (byte) ((pos >>>  0) & 0xFF);

                //hash as byte[], 20 bytes
                //the sha1 in String format to byte[] format
                String sha1 = entry.getValue();
                byte[] hash = sha1AsStringToByteArray(sha1);

                for(byte a  : hash)
                {
                    pay[i = i + 1] = a;
                }

                //
                i++;
            }

            //')'
            if(i != (pay.length - 1))
            {
                throw new IllegalArgumentException("the codification was wrong");
            }
            pay[i] = ')';
        }
        else
        {
            //nothing
            pay = new byte[0];
        }


        return pay;
    }

    /**
     * parse a SHA-1 hash in String format(40 characters) to a byte array (20 bytes)
     * @param sha1 a 40 characters String
     * @return a byte array of 20 bytes representing the SHA-1
     */
    private static byte[] sha1AsStringToByteArray(String sha1)// throws IllegalArgumentException
    {
        if(sha1.length() != 40)
        {
            throw new IllegalArgumentException("the sha1 is in bad format, the correct size is 40 Characters");
        }

        byte[] b = new byte[20];
        
        for (int i = 0, j = 0; i < 20; i++) {
            int hi = Character.digit(sha1.charAt(j++), 16);
            int lo = Character.digit(sha1.charAt(j++), 16);
            if (hi == -1 || lo == -1) {
                throw new IllegalArgumentException( "Invalid content id hash: " + sha1);
            }
            b[i] = (byte) ((hi << 4) | lo);
        }

        return b;
    }
}
