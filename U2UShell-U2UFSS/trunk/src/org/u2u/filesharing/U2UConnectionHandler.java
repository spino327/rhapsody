/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.u2u.filesharing;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jxta.socket.JxtaSocket;
import org.u2u.filesharing.fsprotocol.U2UFSPOrderConnection;
import org.u2u.filesharing.fsprotocol.U2UFSPResponseConnection;
import org.u2u.filesharing.fsprotocol.U2UFSProtocolOrder;
import org.u2u.filesharing.fsprotocol.U2UFileSharingProtocol;
import org.u2u.filesharing.uploadpeer.U2UUploadingManager;

/**
 *
 * @author Irene & Sergio
 */
public class U2UConnectionHandler implements Runnable{

    private final JxtaSocket socket;
    private U2UFileSharingService fss;

    private static final ExecutorService SENDING_NEGATIVE_POOL = Executors.newSingleThreadExecutor();

    /**
     * Create a handler of a specific socket connection
     *
     * @param s
     * @param service
     */
    protected U2UConnectionHandler(JxtaSocket s, U2UFileSharingService service)
    {
        this.socket = s;
        this.fss = service;
    }

    public void run()
    {
        try {

            System.out.println("------------\nFSS U2UConnectionHandler try to accept order CONN\n------------");
            //getting the output of the socket
            OutputStream out = socket.getOutputStream();
            DataOutputStream dos = new DataOutputStream(out);

            //getting the input of the socket
            InputStream in = socket.getInputStream();
            DataInputStream dis = new DataInputStream(in);

            byte[] byteArray = readByteArray(dis, dos);

            //System.out.println(thread+"read from input "+array.size());
            System.out.println("read from input "+byteArray.length);

            U2UFSProtocolOrder order = U2UFSProtocolOrder.parseOrderFromByteArray(byteArray);
            
            //checking order type
            if(order instanceof U2UFSPOrderConnection)
            {
                U2UFSPOrderConnection conOrder = (U2UFSPOrderConnection) order;
                System.out.println("order length = " + conOrder.getLengthPrefix() +
                		"\norder protocol version = " + conOrder.getProtocolVersion() +
                		"\norder sha1 = " + U2UContentIdImpl.hashToString(conOrder.getSha1File()) +
                		"\norder peerId = " + conOrder.getPeerId());

                //init the parsing
                String sha1 = U2UContentIdImpl.hashToString(conOrder.getSha1File());

                if(sha1 != null)
                {
                    U2UUploadingManager upload = fss.getUploadingManager(sha1);

                    //we have a Upload!
                    if(upload != null)
                    {
                        System.out.println("------------\nFSS we have a Upload! try attend the request\n------------");
                        upload.attendConnRequest(socket, conOrder);
                    }
                    //can't attend the request, sending 521
                    else
                    {
                        System.out.println("------------\nFSS can't attend the request, sending 521\n------------");
                        //sending 521
                        U2UFileSharingProtocol protocol =
                                U2UFileSharingProtocol.newUploadInstance(socket, fss.getGroup().getPeerID().toString());

                        socket.setSoTimeout(30*1000);//limit the soTimeOut
                        SENDING_NEGATIVE_POOL.submit(protocol);
                        
                        try {
                            protocol.responseInit(U2UFSPResponseConnection.R521);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(U2UConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                }
                else
                {
                    System.out.println("------------\nFSS incorrept parameters, sending 511\n------------");
                    //sending 511
                    U2UFileSharingProtocol protocol =
                            U2UFileSharingProtocol.newUploadInstance(socket, fss.getGroup().getPeerID().toString());

                    socket.setSoTimeout(30*1000);//limit the soTimeOut
                    SENDING_NEGATIVE_POOL.submit(protocol);
                    
                    try {
                        protocol.responseInit(U2UFSPResponseConnection.R511);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(U2UConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            }
            //closing the socket
            else
            {
                System.out.println("------------\nFSS fatal error, closing the socket, not order conn find\n------------");
                SENDING_NEGATIVE_POOL.submit(new Runnable() {
                    public void run()
                    {
                        try {

                            socket.close();
                        } catch (IOException ex) {
                            Logger.getLogger(U2UConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });  
            }

        } catch (IOException ex) {
            Logger.getLogger(U2UConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private synchronized byte[] readByteArray(DataInputStream dis, DataOutputStream dos) throws IOException
    {
            //read the length prefix
            // (from the DataInputStream implementation)the next four bytes of this input stream, interpreted as an int
            int length;
            byte[] byteArray = null;
            //if write -1 at the remote peer OutputStream byte ch1 = ((ch1 = dis.readByte()) == -1? dis.readByte() : ch1);
            /*byte ch1 = dis.readByte();
            byte ch2 = dis.readByte();
            byte ch3 = dis.readByte();
            byte ch4 = dis.readByte();
            int c1 = (ch1 < 0? (ch1 + 256) : ch1);
            int c2 = (ch2 < 0? (ch2 + 256) : ch2);
            int c3 = (ch3 < 0? (ch3 + 256) : ch3);
            int c4 = (ch4 < 0? (ch4 + 256) : ch4);*/

            //if the first byte read in int format is -1, then we read again
            int ch1;
            //read return values from 0 to 255
            //length prefix is always > 0, so the ch1 only can get values from 0 to 127
            do
            {
                ch1 = dis.read();
            } while(((ch1 < 0) || (ch1 > 127)) && socket.isConnected());

            if(socket.isConnected())
            {
                int ch2 = dis.read();
                int ch3 = dis.read();
                int ch4 = dis.read();

                if ((ch1 | ch2 | ch3 | ch4) < 0)
                {
                    throw new VerifyError("character in bad state");
                }

                length = ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));

                //read the complete order or response
                //<length prefix><response id><response number><payload> = response
                //<length prefix><order id><payload> = order

                System.out.println("length " + length);
                byteArray = new byte[4 + length];

                //length prefix
                byteArray[0] = (byte) ch1;
                byteArray[1] = (byte) ch2;
                byteArray[2] = (byte) ch3;
                byteArray[3] = (byte) ch4;

                //for read the pieces of the message if the byte array is large that the remote peer's sendBufferSize
                int toRead = length;
                int readBytes = 0;
               //int read = 0;
                int from = 4;
                int piece = 0;
                //while((toRead > 0) && (read >= 0))
                while(toRead > 0)
                {
                    //waiting for received piece
                    int read = dis.read(byteArray, from, toRead);
                    System.out.println(" piece " + piece +" read (length) = " + read);

                    from+=read;
                    toRead-=read;

                    readBytes+=read;

                    //sending ACK
                    dos.write(65);
                    dos.flush();
                    System.out.println(" sending ACK " + piece);

                    piece++;
                }

                System.out.println(MessageFormat.format("bytes read = <{0}>, expected = <{1}>",
                        readBytes,
                        length));
            }

            return byteArray;
    }


}
