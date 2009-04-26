/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.u2u.common.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handle the table Address in the DB
 * @author Sergio e Irene
 */
public class Address extends Table {
    
    /**
     * Create a new record in the db
     *
     * @param pipeId pipeId in String format
     * @return
     */
    public static boolean create(String pipeId) {
        
        /*try {
            door.acquire();

            if(conDB == null) {
                return false;
            }

        } catch (InterruptedException ex) {
            Logger.getLogger(Address.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            door.release();
        }*/

        if(!isConnected)
        {
            return false;
        }

        //checking the pipeId
        if(!checkPipeId(pipeId))
        {
            return false;
        }

        ConnectTo conDB = null;
        try {

            //door.acquire();
            conDB = conDBQueue.take();

            PreparedStatement pstmt = conDB.createPreparedStatement("INSERT INTO U2U.ADDRESS(PIPEID_ADD) VALUES (?)");

            pstmt.setString(1, pipeId.substring(14));//urn:jxta:uuid- = 14

            return (pstmt.executeUpdate() > 0 ? true : false);

        } catch (InterruptedException ex) {
            Logger.getLogger(Address.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Address.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
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
     * @param pipeId pipeId in String format
     * @return
     */
    public static boolean delete(String pipeId)
    {
        /*try {
            door.acquire();

            if(conDB == null) {
                return false;
            }

        } catch (InterruptedException ex) {
            Logger.getLogger(Address.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            door.release();
        }*/
        if(!isConnected)
        {
            return false;
        }

        //checking the pipeId
        if(!checkPipeId(pipeId))
        {
            return false;
        }

        pipeId = pipeId.substring(14);//urn:jxta:uuid- = 14

        ConnectTo conDB = null;
        try {
            //door.acquire();
            conDB =  conDBQueue.take();

            return conDB.sQLExecute("DELETE FROM U2U.ADDRESS WHERE PIPEID_ADD = '" + pipeId + "'");

        } catch (InterruptedException ex) {
            Logger.getLogger(Address.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            //door.release();
            if(conDB != null)
            {
                conDBQueue.offer(conDB);
            }
        }

        return false;
    }

    /**
     * Delete all the records from the db
     * @return
     */
    public static boolean deleteAll()
    {
        ConnectTo conDB = null;
        try {
            //door.acquire();
            if(!isConnected)
            {
                return false;
            }

            /*if(conDB == null) {
                return false;
            }*/

            conDB = conDBQueue.take();

            return conDB.sQLExecute("DELETE FROM U2U.ADDRESS");

        } catch (InterruptedException ex) {
            Logger.getLogger(Address.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            //door.release();
            if(conDB != null)
            {
                conDBQueue.offer(conDB);
            }
        }

        return false;
    }

    /**
     * check if the pipeId already exist in the db
     * @param pipeId pipeId in String format
     * @return true if exist
     */
    public static boolean exist(String pipeId) {

        /*try {
            door.acquire();

            if(conDB == null) {
                return false;
            }

        } catch (InterruptedException ex) {
            Logger.getLogger(Address.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            door.release();
        }*/
        if(!isConnected)
        {
            return false;
        }

        //checking the pipeId
        if(!checkPipeId(pipeId))
        {
            return false;
        }

        boolean status = false;
        pipeId = pipeId.substring(14);//urn:jxta:uuid- = 14
        ResultSet rs = null;

        ConnectTo conDB = null;

        try {
            //door.acquire();
            conDB = conDBQueue.take();

            rs = conDB.sQLQuery("SELECT COUNT(*) FROM U2U.ADDRESS WHERE PIPEID_ADD = '" + pipeId + "'");
            rs.first();

            if(rs.getInt(1) > 0)
            {
                status = true;
            }
            
        } catch (InterruptedException ex) {
            Logger.getLogger(Address.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Address.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if(rs != null)
                {
                    rs.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(Address.class.getName()).log(Level.SEVERE, null, ex);
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
     * return the id_add that represents the pipeId in the table Address
     * @param pipeId pipeId in String format
     * @return the id_add > 0, -1 if any error happend
     */
    public static int pipeIdToIDADD(String pipeId)
    {
        /*try {
            door.acquire();

            if(conDB == null) {
                return -1;
            }

        } catch (InterruptedException ex) {
            Logger.getLogger(Address.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            door.release();
        }*/
        if(!isConnected)
        {
            return -1;
        }

        //checking the pipeId
        if(!checkPipeId(pipeId))
        {
            return -1;
        }

        int id_add = -1;
        pipeId = pipeId.substring(14);//urn:jxta:uuid- = 14
        ResultSet rs = null;

        ConnectTo conDB = null;

        try {
            //door.acquire();
            conDB = conDBQueue.take();

            rs = conDB.sQLQuery("SELECT ID_ADD FROM U2U.ADDRESS WHERE PIPEID_ADD = '" + pipeId + "'");
            rs.first();

            id_add = rs.getInt(1);
            
            
        } catch (InterruptedException ex) {
            Logger.getLogger(Address.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Address.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if(rs != null)
                {
                    rs.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(Address.class.getName()).log(Level.SEVERE, null, ex);
            }
            //door.release();
            if(conDB != null)
            {
                conDBQueue.offer(conDB);
            }
        }

        return id_add;
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
}
