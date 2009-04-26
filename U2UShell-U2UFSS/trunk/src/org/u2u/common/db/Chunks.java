/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.u2u.common.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math.random.RandomData;
import org.apache.commons.math.random.RandomDataImpl;

/**
 * Handle the table Chunks in the DB
 * @author Sergio e Irene
 */
public class Chunks extends Table{
   
    /**
     * Create a new record in the db
     *
     * @param sha1_sf  SharedFile's sha1
     * @param sha1_chunk sha1 of the SharedFile's chunks
     * @param pos_chunk chunk's position at the file
     * @param status true if we have the chunk
     * @return
     */
    public static boolean create(String sha1_sf, String sha1_chunk, short pos_chunk, boolean status) {

        /*try {
            door.acquire();

            if(conDB == null) {
                return false;
            }

        } catch (InterruptedException ex) {
            Logger.getLogger(Chunks.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            door.release();
        }*/
        if(!isConnected)
        {
            return false;
        }

        //checking the sha1_sf and the sha1_chunk
        if(!checkSHA1(sha1_sf))
        {
            return false;
        }

        if(!checkSHA1(sha1_chunk))
        {
            return false;
        }

        //checking the pos_chunk
        if((pos_chunk < 0) || (pos_chunk > (32767)))
        {
            System.out.println("posChunk [0 - 32,767]");
            return false;
        }

        PreparedStatement pstmt = null;
        ConnectTo conDB = null;

        try {
            //door.acquire();
            conDB = conDBQueue.take();

            pstmt = conDB.createPreparedStatement("INSERT INTO U2U.CHUNKS VALUES(?,?,?,?)");

            //making a sub string of the ContentID without the sha1: substring
            pstmt.setString(1, sha1_chunk.substring(5));
            pstmt.setString(2, sha1_sf.substring(5));
            pstmt.setShort(3, pos_chunk);
            pstmt.setString(4, (status ? "T" : "F"));

            return (pstmt.executeUpdate() > 0 ? true : false);

        } catch (InterruptedException ex) {
            Logger.getLogger(Chunks.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Chunks.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if(pstmt != null)
                {
                    pstmt.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(Chunks.class.getName()).log(Level.SEVERE, null, ex);
            }
            //door.release();
            if(conDB != null)
            {
                conDBQueue.offer(conDB);
            }
        }
        

        return false;
    }

    /**
     * Delete a record from the db
     * @param sha1_chunk sha1 of the SharedFile's chunks
     * @return
     */
    public static boolean delete(String sha1_chunk)
    {
        ConnectTo conDB = null;
        try {
            //door.acquire();

            /*if(conDB == null) {
                return false;
            }*/
            if(!isConnected)
            {
                return false;
            }


            //checking sha1
            if(!checkSHA1(sha1_chunk))
            {
                return false;
            }

            sha1_chunk = sha1_chunk.substring(5);//sha1: = 5

            conDB = conDBQueue.take();

            return conDB.sQLExecute("DELETE FROM U2U.CHUNKS WHERE SHA1_CHUNK = '" + sha1_chunk + "'");

        } catch (InterruptedException ex) {
            Logger.getLogger(Chunks.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } finally {
            //door.release();
            if(conDB != null)
            {
                conDBQueue.offer(conDB);
            }
        }

        
    }

     /**
     * Delete a record from the db
     * @param sha1_sf  SharedFile's sha1
     * @return
     */
    public static boolean deleteAllChunks(String sha1_sf)
    {
        ConnectTo conDB = null;
        
        try {
            /*door.acquire();

            if(conDB == null) {
                return false;
            }*/
            if(!isConnected)
            {
                return false;
            }

            //checking sha1
            if(!checkSHA1(sha1_sf))
            {
                return false;
            }

            sha1_sf = sha1_sf.substring(5);//sha1: = 5

            conDB = conDBQueue.take();

            return conDB.sQLExecute("DELETE FROM U2U.CHUNKS WHERE SHA1_SF = '" + sha1_sf + "'");

        } catch (InterruptedException ex) {
            Logger.getLogger(Chunks.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } finally {
            //door.release();
            if(conDB != null)
            {
                conDBQueue.offer(conDB);
            }
        }
        
    }

    /**
     * check if the sha1_chunk exist in the db
     * @param sha1_sf 
     * @param sha1_chunk sha1 of the SharedFile's chunks
     * @return true if exist
     */
    public static boolean exist(String sha1_sf, String sha1_chunk) {

        /*try {
            door.acquire();

            if(conDB == null) {
                return false;
            }

        } catch (InterruptedException ex) {
            Logger.getLogger(Chunks.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            door.release();
        }*/

        if(!isConnected)
        {
            return false;
        }

        //checking sha1
        if(!checkSHA1(sha1_chunk))
        {
            return false;
        }
        
        if (!checkSHA1(sha1_sf)) 
        {
            return false;
        }


        boolean status = false;
        sha1_sf = sha1_sf.substring(5);//sha1: = 5
        sha1_chunk = sha1_chunk.substring(5);//sha1: = 5
        
        ResultSet rs = null;

        ConnectTo conDB = null;

        try {
            //door.acquire();
            conDB = conDBQueue.take();

            rs = conDB.sQLQuery("SELECT COUNT(*) FROM U2U.CHUNKS " +
                "WHERE SHA1_SF = '" + sha1_sf + "' AND " +
                "SHA1_CHUNK = '" + sha1_chunk + "'");
            rs.first();

            if(rs.getInt(1) > 0)
            {
                status = true;
            }

            
        } catch (InterruptedException ex) {
            Logger.getLogger(Chunks.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Chunks.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if(rs != null)
                {
                    rs.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(Chunks.class.getName()).log(Level.SEVERE, null, ex);
            }
            //door.release();
            if(conDB != null)
            {
                conDBQueue.offer(conDB);
            }
        }

        return status;

    }

    /**
     * get the number of chunks of the SHA1_SHAREDFILE
     * @param sha1_sf
     * @return
     */
    public static int numberOfChunks(String sha1_sf)
    {
        /*try {
            door.acquire();

            if(conDB == null) {
                return 0;
            }

        } catch (InterruptedException ex) {
            Logger.getLogger(Chunks.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            door.release();
        }*/
        if(!isConnected)
        {
            return 0;
        }

        //checking the sha1_sf and the sha1_chunk
        if(!checkSHA1(sha1_sf))
        {
            return 0;
        }

        int count = 0;

        sha1_sf = sha1_sf.substring(5);//sha1: = 5
        ResultSet rs = null;
        ConnectTo conDB = null;

        try {
            //door.acquire();
            conDB = conDBQueue.take();

            rs = conDB.sQLQuery("SELECT COUNT(*) FROM U2U.CHUNKS WHERE SHA1_SF = '" + sha1_sf + "'");
            rs.first();

            count = rs.getInt(1);

            
        } catch (InterruptedException ex) {
            Logger.getLogger(Chunks.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Chunks.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if(rs != null)
                {
                    rs.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(Chunks.class.getName()).log(Level.SEVERE, null, ex);
            }
            //door.release();
            if(conDB != null)
            {
                conDBQueue.offer(conDB);
            }
        }

        return count;
    }

    //EOCommon

    //Downloading

    /**
     * get the reverse chunks pos, is like a negation of and intersection with the whole chunks
     * and the provided chunks list
     *
     * @param sha1_sf
     * @param pos
     * @return the reverse chunks position list
     */
    public static short[] getReverseChunksPositions(String sha1_sf, short[] pos)
    {
        /*try {
            door.acquire();

            if(conDB == null) {
                return null;
            }

        } catch (InterruptedException ex) {
            Logger.getLogger(Chunks.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            door.release();
        }*/
        if(!isConnected)
        {
            return null;
        }

        //checking the sha1_sf
        if(!checkSHA1(sha1_sf))
        {
            return null;
        }
        sha1_sf = sha1_sf.substring(5);//sha1: = 5

        ResultSet rs = null;
        ConnectTo conDB = null;
        
        try {
            //door.acquire();
            conDB = conDBQueue.take();

            short maxPos;
            StringBuffer query;
            short[] reversePos;

            //getting the maximun chunk position for the sha1_sf
            rs = conDB.sQLQuery("SELECT MAX(POS_CHUNK) FROM U2U.CHUNKS " +
                    "WHERE SHA1_SF = '" + sha1_sf + "'");
            rs.first();

            if(rs.getRow() > 0)
            {
                maxPos = rs.getShort(1);//maxPos

                //checking the list of chunks
                Arrays.sort(pos);
                if((pos[0] < 0) || (pos[pos.length -1] > maxPos))
                {
                    return null;
                }

                //reverse
                query = new StringBuffer("SELECT POS_CHUNK FROM U2U.CHUNKS WHERE SHA1_SF = '");
                query.append(sha1_sf);
                query.append("' ");
                query.append("AND POS_CHUNK NOT IN (");

                for(short p : pos)
                {
                    query.append(p);
                    query.append(",");
                }

                query.replace(query.lastIndexOf(","), query.length(), ")");

                rs = conDB.sQLQuery(query.toString());
                rs.first();

                if(rs.getRow() > 0)
                {
                    //making the reverse pos
                    reversePos = new short[maxPos-(pos.length-1)];

                    for(int i = 0; i < reversePos.length; i++)
                    {
                        reversePos[i] = rs.getShort(1);
                        rs.next();
                    }

                    return reversePos;
                }
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(Chunks.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Chunks.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if(rs != null)
                {
                    rs.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(Chunks.class.getName()).log(Level.SEVERE, null, ex);
            }
            //door.release();
            if(conDB != null)
            {
                conDBQueue.offer(conDB);
            }
        }

        return null;
    }

    /**
     * return the SHA-1 of the chunk with position = pos
     * @param sha1_sf
     * @param pos
     * @param off - the start offset in the data.
     * @param len - the number of bytes to write.
     * @return
     */
    public static String[] chunkPosToChunkSha1(String sha1_sf, short[] pos, int off, int len)
    {
        /*try {
            door.acquire();

            if(conDB == null) {
                return null;
            }

        } catch (InterruptedException ex) {
            Logger.getLogger(Chunks.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            door.release();
        }*/
        if(!isConnected)
        {
            return null;
        }

        //checking the sha1_sf and the sha1_chunk
        if(!checkSHA1(sha1_sf))
        {
            return null;
        }
        //checking the pso
        if((pos != null) && (pos.length > 0) && (off >= 0) && (len > 0))
        {
            for(int i = off; i < off + len; i++)
            {
                short p = pos[i];
                
                if((p < 0) || (p > (32767)))
                {
                    return null;
                }
            }
        }
        else
        {
            return null;
        }

        sha1_sf = sha1_sf.substring(5);//sha1: = 5

        //making the query
        StringBuffer query = new StringBuffer("SELECT SHA1_CHUNK FROM U2U.CHUNKS");
        query.append(" WHERE SHA1_SF = '");
        query.append(sha1_sf);
        query.append("' AND POS_CHUNK IN (");

        for(int i = off; i < off + len; i++)
        {
            short p = pos[i];
            query.append(p);
            query.append(",");
        }

        query.replace(query.lastIndexOf(","), query.length(), ")");

        ResultSet rs = null;
        ConnectTo conDB = null;

        try {
            //door.acquire();
            conDB = conDBQueue.take();

            rs = conDB.sQLQuery(query.toString());
            rs.first();
            //making the response
            if(rs.getRow() > 0)
            {
                String res[] = new String[len];

                for(int i = 0; i < res.length; i++)
                {
                    res[i] = "sha1:" + rs.getString(1);
                    rs.next();
                }

                return res;
            }

            
        } catch (InterruptedException ex) {
            Logger.getLogger(Chunks.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Chunks.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if(rs != null)
                {
                    rs.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(Chunks.class.getName()).log(Level.SEVERE, null, ex);
            }
            //door.release();
            if(conDB != null)
            {
                conDBQueue.offer(conDB);
            }
        }

        return null;
    }

    /**
     * return the SHA-1 of the chunk with position = pos
     * 
     * @param sha1_sf
     * @param pipeId pipeId in String format
     * @return
     */
    public static short getRandomChunkToDownload(String sha1_sf, String pipeId)
    {
        /*try {
            door.acquire();

            if(conDB == null) {
                return -1;
            }

        } catch (InterruptedException ex) {
            Logger.getLogger(Chunks.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            door.release();
        }*/
        if(!isConnected)
        {
            return -1;
        }

        //checking the sha1_sf
        if(!checkSHA1(sha1_sf))
        {
            return -1;
        }

        //checking the pipeId
        if(!checkPipeId(pipeId))
        {
            return -1;
        }

        pipeId = pipeId.substring(14);//urn:jxta:uuid- = 14
        sha1_sf = sha1_sf.substring(5);//sha1: = 5
        ResultSet rs = null;
        ConnectTo conDB = null;
        
        try {
            //door.acquire();
            conDB = conDBQueue.take();
            
            //how much we can download
            short numAvailableChunks;
            rs = conDB.sQLQuery("SELECT COUNT(SHA1_CHUNK) FROM U2U.CHUNKS_ADD " +
                "WHERE ID_ADD = (SELECT ID_ADD FROM U2U.ADDRESS WHERE PIPEID_ADD = '" + pipeId + "') " +
                "AND SHA1_CHUNK IN (SELECT SHA1_CHUNK FROM U2U.CHUNKS WHERE STATUS = 'F' AND SHA1_SF = '" + sha1_sf + "')");

            rs.first();
            numAvailableChunks = rs.getShort(1);

            if(numAvailableChunks > 0)
            {
                //we choose randomly a chunk for download
                short randomChunk;
                if(numAvailableChunks > 1)
                {
                    RandomData rD = new RandomDataImpl();
                    randomChunk = (short) rD.nextInt(1, numAvailableChunks);
                }
                else
                {
                    randomChunk = 1;
                }

                //get the SHA1_CHUNK of the selected chunk
                rs = conDB.sQLQuery("SELECT SHA1_CHUNK FROM " +
                                        "(SELECT ROW_NUMBER() OVER() AS R, U2U.CHUNKS_ADD.* FROM U2U.CHUNKS_ADD " +
                                        "WHERE ID_ADD = (SELECT ID_ADD FROM U2U.ADDRESS " +
                                            "WHERE PIPEID_ADD = '" + pipeId + "') " +
                                        "AND SHA1_CHUNK IN (SELECT SHA1_CHUNK FROM U2U.CHUNKS " +
                                            "WHERE STATUS = 'F' AND SHA1_SF = '" + sha1_sf + "')) " +
                                     "AS TR WHERE R = " + randomChunk);
                rs.first();

                if(rs.getRow() > 0)
                {
                    //get the pos of the selected chunk
                    rs = conDB.sQLQuery("SELECT POS_CHUNK FROM U2U.CHUNKS " +
                            "WHERE SHA1_CHUNK = '" + rs.getString(1) + "'");
                    rs.first();

                    if(rs.getRow() > 0)
                    {
                        return rs.getShort(1);
                    }
                }
            }

            
        } catch (InterruptedException ex) {
            Logger.getLogger(Chunks.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Chunks.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if(rs != null)
                {
                    rs.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(Chunks.class.getName()).log(Level.SEVERE, null, ex);
            }
            //door.release();
            if(conDB != null)
            {
                conDBQueue.offer(conDB);
            }
        }

        return -1;
    }

    /**
     * return the pos of the chunk with SHA-1
     * @param sha1_sf
     * @param sha1_chunk 
     * @return
     */
    public static short chunkSha1ToChunkPos(String sha1_sf, String sha1_chunk)
    {
        /*try {
            door.acquire();

            if(conDB == null) {
                return -1;
            }

        } catch (InterruptedException ex) {
            Logger.getLogger(Chunks.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            door.release();
        }*/
        if(!isConnected)
        {
            return -1;
        }

        //checking the sha1_sf and the sha1_chunk
        if(!checkSHA1(sha1_sf))
        {
            return -1;
        }
        if(!checkSHA1(sha1_chunk))
        {
            return -1;
        }

        sha1_sf = sha1_sf.substring(5);//sha1: = 5
        sha1_chunk = sha1_chunk.substring(5);//sha1: = 5
        ResultSet rs = null;
        ConnectTo conDB = null;

        try {
            //door.acquire();
            conDB = conDBQueue.take();
            
            rs = conDB.sQLQuery("SELECT POS_CHUNK FROM U2U.CHUNKS " +
                    "WHERE SHA1_SF = '" + sha1_sf + "' AND " +
                    "SHA1_CHUNK = '" + sha1_chunk + "'");
            rs.first();

            if(rs.getRow() > 0)
            {
                return rs.getShort(1);
            }
            
            
        } catch (InterruptedException ex) {
            Logger.getLogger(Chunks.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Chunks.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if(rs != null)
                {
                    rs.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(Chunks.class.getName()).log(Level.SEVERE, null, ex);
            }
            //door.release();
            if(conDB != null)
            {
                conDBQueue.offer(conDB);
            }
        }

        return -1;
    }

    /**
     * register that the chunk identify by the SHA1-chunk was downloaded, setting 
     * the status to 'T'
     * 
     * @param sha1_sf
     * @param sha1_chunk
     * 
     * @return true if the modification was successful
     */
    public static boolean registerChunkDownload(String sha1_sf, String sha1_chunk)
    {
        ConnectTo conDB = null;

        try {
            //door.acquire();

            /*if(conDB == null) {
                return false;
            }*/
            if(!isConnected)
            {
                return false;
            }
            
            //checking the sha1_sf and the sha1_chunk
            if(!checkSHA1(sha1_sf))
            {
                return false;
            }
            if(!checkSHA1(sha1_chunk))
            {
                return false;
            }

            sha1_sf = sha1_sf.substring(5);//sha1: = 5
            sha1_chunk = sha1_chunk.substring(5);//sha1: = 5

            conDB = conDBQueue.take();

            return conDB.sQLExecute("UPDATE U2U.CHUNKS SET STATUS = 'T' " +
                        "WHERE SHA1_SF = '" + sha1_sf + "' AND " +
                        "SHA1_CHUNK = '" + sha1_chunk + "'");

        } catch (InterruptedException ex) {
            Logger.getLogger(Chunks.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } finally {
            //door.release();
            if(conDB != null)
            {
                conDBQueue.offer(conDB);
            }
        }

        
    }
    //EODownloading


    //Uploading

    /**
     * return a list of the chunks that the peer have or a list of chunks that the peer haven't,
     * only who have least positions at the array
     *
     * @param sha1_sf
     * @return solution = Object[] {boolean, short[]}
     */
    public static Object[] getPeerChunksList(String sha1_sf)
    {
        /*try {
            door.acquire();

            if(conDB == null) {
                return null;
            }

        } catch (InterruptedException ex) {
            Logger.getLogger(Chunks.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            door.release();
        }*/
        if(!isConnected)
        {
            return null;
        }

        //checking the sha1_sf and the sha1_chunk
        if(!checkSHA1(sha1_sf))
        {
            return null;
        }

        int numChunks, numDownChunks;
        Object[] solution = null;
        sha1_sf = sha1_sf.substring(5);//sha1: = 5
        ConnectTo conDB = null;
        ResultSet rs = null;

        try {
            //door.acquire();
            conDB = conDBQueue.take();

            short[] pos;
            solution = new Object[2];
            rs = conDB.sQLQuery("SELECT COUNT(*) FROM U2U.CHUNKS WHERE" +
                " SHA1_SF = '" + sha1_sf + "'");
            rs.first();
            numChunks = rs.getInt(1);

            rs = conDB.sQLQuery("SELECT COUNT(*) FROM U2U.CHUNKS WHERE" +
                " SHA1_SF = '" + sha1_sf + "' AND STATUS = 'T'");
            rs.first();
            numDownChunks = rs.getInt(1);

            //have info
            if((numChunks > 0) && (numDownChunks > 0))
            {
                //Whole the chunks
                if(numChunks == numDownChunks)
                {
                    //T
                    solution[0] = true;

                    pos = new short[0];//whole the chunks
                }
                //F      numNonDownChunks      < numDownChunks
                else if((numChunks - numDownChunks) < numDownChunks)
                {
                    //F
                    solution[0] = false;

                    rs = conDB.sQLQuery("SELECT POS_CHUNK FROM U2U.CHUNKS WHERE" +
                            " SHA1_SF = '" + sha1_sf + "' AND STATUS = 'F'");

                    pos = new short[numChunks - numDownChunks];//numNonDownChunks
                }
                //T      numNonDownChunks      > numDownChunks
                else
                {
                    //T
                    solution[0] = true;

                    rs = conDB.sQLQuery("SELECT POS_CHUNK FROM U2U.CHUNKS WHERE" +
                            " SHA1_SF = '" + sha1_sf + "' AND STATUS = 'T'");

                    pos = new short[numDownChunks];//numDownChunks
                }

                //query to short[]
                rs.first();
                if(rs.getRow() > 0)
                {
                    for(int i = 0; i < pos.length; i++)
                    {
                        pos[i] = rs.getShort(1);
                        rs.next();
                    }
                }
                //info not available, F()
                else
                {
                    solution[0] = false;
                    pos = new short[0];
                }

            }
            //info not available, F()
            else
            {
                solution[0] = false;
                pos = new short[0];
            }

            solution[1] = pos;

            return solution;

            
        } catch (InterruptedException ex) {
            Logger.getLogger(Chunks.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Chunks.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if(rs != null)
                {
                    rs.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(Chunks.class.getName()).log(Level.SEVERE, null, ex);
            }
            //door.release();
            if(conDB != null)
            {
                conDBQueue.offer(conDB);
            }
        }
        
        return null;
    }

    /**
     * return a list of the chunks' sha1 of the Shared file
     *
     * @param sha1_sf
     * @return solution = HashMap<Short, String>
     */
    public static HashMap<Short, String> getChunksSHA1List(String sha1_sf)
    {
        /*try {
            door.acquire();

            if(conDB == null) {
                return null;
            }

        } catch (InterruptedException ex) {
            Logger.getLogger(Chunks.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            door.release();
        }*/
        if(!isConnected)
        {
            return null;
        }

        //checking the sha1_sf and the sha1_chunk
        if(!checkSHA1(sha1_sf))
        {
            return null;
        }

        HashMap<Short, String> solution = null;
        sha1_sf = sha1_sf.substring(5);//sha1: = 5
        ConnectTo conDB = null;
        ResultSet rs = null;

        try {
            //door.acquire();
            conDB = conDBQueue.take();

            rs = conDB.sQLQuery("SELECT POS_CHUNK, SHA1_CHUNK FROM U2U.CHUNKS WHERE" +
                " SHA1_SF = '" + sha1_sf + "'");
            //query to HashMap<Short, String>
            rs.first();

            solution = new HashMap<Short, String>();
            while(!rs.isAfterLast() && (rs.getRow() > 0))
            {
                short pos = rs.getShort(1);
                String sha1_chunk = rs.getString(2);
                solution.put(pos, sha1_chunk);

                rs.next();
            }

            return solution;

            
        } catch (InterruptedException ex) {
            Logger.getLogger(Chunks.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Chunks.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if(rs != null)
                {
                    rs.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(Chunks.class.getName()).log(Level.SEVERE, null, ex);
            }
            //door.release();
            if(conDB != null)
            {
                conDBQueue.offer(conDB);
            }
        }

        return null;
    }

    /**
     * Look if the chunk that is representing by the index is already download.
     *
     * @param sha1_sf sha1 digest of the shared file
     * @param index position of the chunk at the shared file
     *
     * @return true if the system have the chunk
     */
    public static boolean haveTheChunk(String sha1_sf, short index)
    {
        /*try {
            door.acquire();

            if(conDB == null) {
                return false;
            }

        } catch (InterruptedException ex) {
            Logger.getLogger(Chunks.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            door.release();
        }*/
        if(!isConnected)
        {
            return false;
        }

        //checking the sha1_sf
        if(!checkSHA1(sha1_sf))
        {
            return false;
        }
        //checking the index
        if((index < 0) ||
                (index > 32767))
        {
            return false;
        }

        sha1_sf = sha1_sf.substring(5);//sha1: = 5
        ConnectTo conDB = null;
        ResultSet rs = null;

        try {
            //door.acquire();
            conDB = conDBQueue.take();

            rs = conDB.sQLQuery("SELECT COUNT(*) FROM U2U.CHUNKS " +
                "WHERE STATUS = 'T' AND SHA1_SF = '" + sha1_sf + "' AND " +
                "POS_CHUNK = " + index);
            rs.first();

            return (rs.getInt(1) > 0 ? true : false);  
            
        } catch (InterruptedException ex) {
            Logger.getLogger(Chunks.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Chunks.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if(rs != null)
                {
                    rs.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(Chunks.class.getName()).log(Level.SEVERE, null, ex);
            }
            //door.release();
            if(conDB != null)
            {
                conDBQueue.offer(conDB);
            }
        }

        return false;
    }

    /**
     * Look if the system have all the chunks.
     *
     * @param sha1_sf sha1 digest of the shared file
     * 
     * @return a fraction that represents the relation between the number of chunks
     * that we have and the total number of file's chunks. eg.. 1.0 = have all the chunks,
     * [0.0 - 1.0) = haven't all the chunks.
     */
    public static float haveAllTheChunks(String sha1_sf)
    {
        /*try {
            door.acquire();

            if(conDB == null) {
                return 0.0f;
            }

        } catch (InterruptedException ex) {
            Logger.getLogger(Chunks.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            door.release();
        }*/
        if(!isConnected)
        {
            return 0.0f;
        }

        //checking the sha1_sf
        if(!checkSHA1(sha1_sf))
        {
            return 0.0f;
        }

        sha1_sf = sha1_sf.substring(5);//sha1: = 5
        ConnectTo conDB = null;
        ResultSet rs = null;

        try {
            //door.acquire();
            conDB = conDBQueue.take();

            float allChunks;
            float downloadChunks = 0;
            float nondownloadChunks = 0;

            //how many chunks have the sha1_sf?
            rs = conDB.sQLQuery("SELECT COUNT(*) FROM U2U.CHUNKS " +
                    "WHERE SHA1_SF = '" + sha1_sf + "'");
            rs.first();

            allChunks = rs.getFloat(1);

            if(allChunks > 0)
            {
                rs = conDB.sQLQuery("SELECT STATUS, COUNT(*) FROM U2U.CHUNKS " +
                    "WHERE SHA1_SF = '" + sha1_sf + "' GROUP BY STATUS");
                    rs.first();

                    if(rs.getRow() > 0)
                    {
                        while(!rs.isAfterLast())
                        {
                            if(rs.getString(1).equals("T"))
                            {
                                downloadChunks = rs.getFloat(2);
                            }
                            //F
                            else
                            {
                                nondownloadChunks = rs.getFloat(2);
                            }

                            rs.next();
                        }

                        if(allChunks == (nondownloadChunks + downloadChunks))
                        {
                            return (downloadChunks / allChunks);
                        }
                    }
            }
     
            
        } catch (InterruptedException ex) {
            Logger.getLogger(Chunks.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Chunks.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if(rs != null)
                {
                    rs.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(Chunks.class.getName()).log(Level.SEVERE, null, ex);
            }
            //door.release();
            if(conDB != null)
            {
                conDBQueue.offer(conDB);
            }
        }

        return 0.0f;
    }

    //EOUploading

    //TOOLS

    private static boolean checkSHA1(String sha1)
    {
        int pos = sha1.indexOf(':');
        if (pos == -1) {
            System.out.println("Missing content id type: " + sha1);
            return false;
        }
        String type = sha1.substring(0, pos);
        if (!type.equalsIgnoreCase("sha1")) {
            System.out.println("Invalid content id type: " + sha1);
            return false;
        }
        if (sha1.length() - pos - 1 != 40) {
            System.out.println( "Invalid content id hash length: " + sha1);
            return false;
        }

        return true;
    }

    /**
     * check the pipeId
     * @param pipeId pipeId in String format
     * @return
     */
    private static boolean checkPipeId(String pipeId)
    {
        //checking the pipeId
        int pos = pipeId.indexOf("urn:jxta:uuid-");
        if (pos == -1) {
            System.out.println("Missing pipe id space: " + pipeId);
            return false;
        }
//        if (pipeId.length() - 14 != 66) {
//            System.out.println( "Invalid pipe id length: " + pipeId);
//            return false;
//        }

        return true;
    }

    //EOFTools
}
