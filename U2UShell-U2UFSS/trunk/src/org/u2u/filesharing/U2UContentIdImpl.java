/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.u2u.filesharing;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jxta.share.ContentId;
import org.bouncycastle.jce.provider.JDKMessageDigest;

/**
 * A ContentId represents a unique content identifier. ContentId implementation based on SHA-1 digest algorithm.
 */
public class U2UContentIdImpl implements ContentId {
    
    private byte[] hash;
    private static final String ID_TYPE = "sha1";
    
    /**
     * Creates a new content id for the bytes contained in the specified
     * input stream.
     * @param in an InputStream, it can be a FileInputStream
     */
    public U2UContentIdImpl(InputStream in)
    {
        try {
            
            MessageDigest md = JDKMessageDigest.getInstance("SHA-1");
            
            int len;
            long init = System.currentTimeMillis();
            byte[] b = new byte[64*1024];
            
            //System.out.println("init the computing of the hash "+init);

            while ((len = in.read(b, 0, b.length)) != -1) {
                md.update(b, 0, len);
            }
            
            hash = md.digest();
            System.out.println("end the computing of the hash "+(System.currentTimeMillis()-init));
            
        } catch (IOException ex) {
            Logger.getLogger(U2UContentIdImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(U2UContentIdImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Creates a new content id for the bytes contained in the specified
     * String.
     * @param t The String represents the id, the hash Sha-1
     */
    public U2UContentIdImpl(String t) {
        hash = parseString(t);
    }

    /**
     * Creates a new content id with the bytes passed
     * @param hash The String represents the id, the hash Sha-1
     */
    public U2UContentIdImpl(byte[] hash)
    {
        //verify
        if(hash == null)
        {
            throw new NullPointerException("hash can't be null");
        }
        else if(hash.length != 20)
        {
            throw new IllegalArgumentException("the hash most have 20 bytes");
        }
        /*else
        {
           //FIXME spino327@gmail.com ?
           for(byte a : hash)
           {
               if((a < -128) || (a > 127))
               {
                   throw new IllegalArgumentException("the hash has incorrect values");
               }
           }
        }*/

        //
        this.hash = hash.clone();
    }
    
    /**
     * Returns a string representation of this content id. start with sha1:...
     */
    @Override
    public String toString() {
        
        StringBuffer d = new StringBuffer();
        
        d.append(ID_TYPE);
        d.append(':');
        for(int i=0; i<hash.length; i++)
        {
            int v = hash[i] & 0xFF;
            if(v < 16) 
                d.append("0");
            
            d.append(Integer.toString(v, 16));
        }
        
        return d.toString();
    }

    /**
     * Returns a byte array representation of this content id.
     * @return a byte array clone of the hash
     */
    public byte[] getByteRepresentation()
    {
        return hash.clone();
    }

    /**
     * Returns true if this U2UContentId is equal to the specified object.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof U2UContentIdImpl)) {
            return false;
        }
        U2UContentIdImpl id = (U2UContentIdImpl) obj;
        byte[] b1 = hash;
        byte[] b2 = id.hash;
        if (b1.length != b2.length) {
            return false;
        }
        for (int i = 0; i < b1.length; i++) {
            if (b1[i] != b2[i]) {
                return false;
            }
        }
        return true;
    }

    // Parse content id string
    private byte[] parseString(String t) {

        int pos = t.indexOf(':');
        if (pos == -1) {
            throw new IllegalArgumentException("Missing content id type: " + t);
        }
        String type = t.substring(0, pos);
        if (!type.equalsIgnoreCase(ID_TYPE)) {
            throw new IllegalArgumentException("Invalid content id type: " + t);
        }
        if (t.length() - pos - 1 != 40) {
            throw new IllegalArgumentException( "Invalid content id hash length: " + t);
        }
        t = t.substring(pos + 1);
        byte[] b = new byte[20];
        for (int i = 0, j = 0; i < 20; i++) {
            int hi = Character.digit(t.charAt(j++), 16);
            int lo = Character.digit(t.charAt(j++), 16);
            if (hi == -1 || lo == -1) {
                throw new IllegalArgumentException( "Invalid content id hash: " + t);
            }
            b[i] = (byte) ((hi << 4) | lo);
        }
        return b;
    }

    //static
    /**
     * this utility get the SHA1 from the byte[] representation of a chunk
     * @param chunkBytes chunk's byteArray represetation
     * @return a String -> "sha1:XXXXXXXXXX..."
     */
    public static String getChunkSHA1(byte[] chunkBytes)
    {
        if(chunkBytes == null)
        {
            throw new IllegalArgumentException("chunkBytes can't be null");
        }
        else if(chunkBytes.length <= 0)
        {
            throw new IllegalArgumentException("chunkBytes need to have at least 1 byte");
        }

        byte[] hashChunk = null;

        try {

            MessageDigest md = JDKMessageDigest.getInstance("SHA-1");

            long init = System.currentTimeMillis();

            md.update(chunkBytes);

            hashChunk = md.digest();
            
            System.out.println("end the computing of the hash "+(System.currentTimeMillis()-init));

            StringBuffer d = new StringBuffer();

            d.append(ID_TYPE);
            d.append(':');
            for(int i=0; i<hashChunk.length; i++)
            {
                int v = hashChunk[i] & 0xFF;
                if(v < 16)
                    d.append("0");

                d.append(Integer.toString(v, 16));
            }

            return d.toString();

        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(U2UContentIdImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    /**
     * Try to parse a byte array and get a SHA-1 String representation
     * @return String 'sha1:XXX...'
     */
    public static String hashToString(byte[] hash) {

        if(hash.length == 20)
        {
            StringBuffer d = new StringBuffer();

            d.append(ID_TYPE);
            d.append(':');
            for(int i=0; i<hash.length; i++)
            {
                int v = hash[i] & 0xFF;
                if(v < 16)
                    d.append("0");

                d.append(Integer.toString(v, 16));
            }

            return d.toString();
        }
        else
        {
            return null;
        }
        
    }
    
}
