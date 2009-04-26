/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.u2u.common.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handle the table Chunks in the DB
 * @author Sergio e Irene
 */
public class Chunks_Add extends Table{

    /**
     * Create a new record in the db
     *
     * @param id_add pipeId_add's id_add in the table Address
     * @param sha1_chunk sha1 of the SharedFile's chunks
     * @return number of new chunks
     */
    public static int create(int id_add, String[] sha1_chunk) {

        /*try {
            door.acquire();

            if(conDB == null) {
                return 0;
            }

        } catch (InterruptedException ex) {
            Logger.getLogger(Chunks_Add.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            door.release();
        }*/
        if(!isConnected)
        {
            return 0;
        }

        //checking the sha1_sf and the sha1_chunk
        if((sha1_chunk != null) && (sha1_chunk.length > 0))
        {
            for(String s : sha1_chunk)
            {
                if(!checkSHA1(s))
                {
                    return 0;
                }
            }
        }
        else
        {
            return 0;
        }
        
        //cheching id_add
        if(id_add < 0)
        {
            return 0;
        }


        ResultSet rs = null;
        //making the query
        //only inset whose we haven't
        ConnectTo conDB = null;
        try
        {
            //door.acquire();
            conDB = conDBQueue.take();

            StringBuffer query = new StringBuffer("INSERT INTO U2U.CHUNKS_ADD(ID_ADD, SHA1_CHUNK) VALUES");
            boolean needToInsert = false;
            for(String sha1 : sha1_chunk)
            {
                sha1 = sha1.substring(5);//sha1: = 5
                //we haven't it?
                rs = conDB.sQLQuery("SELECT COUNT(SHA1_CHUNK) FROM U2U.CHUNKS_ADD " +
                        "WHERE ID_ADD = " + id_add +
                        " AND SHA1_CHUNK = '" + sha1 + "'");
                rs.first();

                if(rs.getInt(1) == 0)
                {
                    needToInsert = true;
                    query.append("(");
                    query.append(id_add);
                    query.append(",'");
                    query.append(sha1);
                    query.append("'),");
                }
            }

            if(needToInsert)
            {
                query.replace(query.lastIndexOf(","), query.length(), "");
                return conDB.sQLExecuteUpdate(query.toString());
            }
                
            
        } catch (InterruptedException ex) {
            Logger.getLogger(Chunks_Add.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
                Logger.getLogger(Chunks_Add.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if(rs != null)
                {
                    rs.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(Chunks_Add.class.getName()).log(Level.SEVERE, null, ex);
            }
            //door.release();
            if(conDB != null)
            {
                conDBQueue.offer(conDB);
            }
        }

        return 0;
    }


    /**
     * Delete the records relationship with the sha1_sf's chunks
     *
     * @param sha1_sf SharedFile's sha1
     * @return
     */
    public static boolean delete(String sha1_sf)
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

            //checking the sha1
            if(!checkSHA1(sha1_sf))
            {
                return false;
            }

            sha1_sf = sha1_sf.substring(5);//sha1: = 5

            conDB = conDBQueue.take();

            return conDB.sQLExecute("DELETE FROM U2U.CHUNKS_ADD " +
                    "WHERE SHA1_CHUNK IN (SELECT SHA1_CHUNK FROM U2U.CHUNKS " +
                    "WHERE SHA1_SF = '" + sha1_sf + "')");

        } catch (InterruptedException ex) {
            Logger.getLogger(Chunks_Add.class.getName()).log(Level.SEVERE, null, ex);
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
     * Delete all the records from the db
     * @return
     */
    public static boolean deleteAll()
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

            conDB = conDBQueue.take();

            return conDB.sQLExecute("DELETE FROM U2U.CHUNKS_ADD");

        } catch (InterruptedException ex) {
            Logger.getLogger(Chunks_Add.class.getName()).log(Level.SEVERE, null, ex);
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
     * check if the sha1_chunk already exist in the db
     * @param sha1_chunk sha1 of the SharedFile's chunks
     * @return true if exist
     */
    /*public static boolean exist(String sha1_chunk) {

        if(conDB == null) {
            return false;
        }

        int pos = sha1_chunk.indexOf(':');
        if (pos == -1) {
            System.out.println("Missing content id type: " + sha1_chunk);
            return false;
        }
        String type = sha1_chunk.substring(0, pos);
        if (!type.equalsIgnoreCase("sha1")) {
            System.out.println("Invalid content id type: " + sha1_chunk);
            return false;
        }
        if (sha1_chunk.length() - pos - 1 != 40) {
            System.out.println( "Invalid content id hash length: " + sha1_chunk);
            return false;
        }

        boolean status = false;
        sha1_chunk = sha1_chunk.substring(5);//sha1: = 5
        ResultSet rs = conDB.sQLQuery("SELECT COUNT(*) FROM U2U.CHUNKS_ADD WHERE SHA1_CHUNK = '" + sha1_chunk + "'");

        try {
            rs.first();

            if(rs.getInt(1) > 0)
            {
                status = true;
            }

            rs.close();

        } catch (SQLException ex) {
            Logger.getLogger(SharedFiles.class.getName()).log(Level.SEVERE, null, ex);
        }

        return status;

    }*/

    /**
     * get the number of chunks of the SHA1_SHAREDFILE
     * @param sha1_sf
     * @return
     */
    /*public static int numberOfChunks(String sha1_sf)
    {
        if(conDB == null) {
            return 0;
        }

        //checking the sha1_sf and the sha1_chunk
        int pos = sha1_sf.indexOf(':');
        if (pos == -1) {
            System.out.println("Missing content id type: " + sha1_sf);
            return 0;
        }
        String type = sha1_sf.substring(0, pos);
        if (!type.equalsIgnoreCase("sha1")) {
            System.out.println("Invalid content id type: " + sha1_sf);
            return 0;
        }
        if (sha1_sf.length() - pos - 1 != 40) {
            System.out.println( "Invalid content id hash length: " + sha1_sf);
            return 0;
        }

        int count = 0;

        sha1_sf = sha1_sf.substring(5);//sha1: = 5
        ResultSet rs = conDB.sQLQuery("SELECT COUNT(*) FROM U2U.CHUNKS_ADD WHERE SHA1_SF = '" + sha1_sf + "'");

        try {
            rs.first();

            count = rs.getInt(1);
            
            rs.close();

        } catch (SQLException ex) {
            Logger.getLogger(SharedFiles.class.getName()).log(Level.SEVERE, null, ex);
        }

        return count;
    }*/

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
}
