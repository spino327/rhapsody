/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.u2u.common.db;

import java.io.FileNotFoundException;
import org.u2u.filesharing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.u2u.common.io.U2UFileInputStream;

/**
 * Handle the table SharedFiles in the db
 * @author sergio
 */


public class SharedFiles extends Table{

    public static boolean createIncomplete(U2UFileContentImpl fci) {

        if (fci == null) 
        {
            return false;
        }

        U2UFileInputStream fis = null;
        //FileInputStream fin = null;
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
            
            U2UContentAdvertisementImpl cadv = (U2UContentAdvertisementImpl) fci.getContentAdvertisement();
            U2UContentIdImpl cid = (U2UContentIdImpl)cadv.getContentId();
            File file = fci.getFile();

            PreparedStatement pstmt = conDB.createPreparedStatement("INSERT INTO U2U.SharedFiles VALUES(?,?,?,?,?,?)");

            //making a sub string of the ContentID without the sha1: substring
            pstmt.setString(1, cid.toString().substring(5));

            //path
            String filePath = file.toURI().toString();
            String name = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length());
            String path = filePath.substring(0, filePath.lastIndexOf("/") + 1);

            pstmt.setString(2, name);
            pstmt.setString(3, path);

            //
            fis = cadv.getInputStream();
            pstmt.setBinaryStream(4, fis, fis.getLength());

            //pstmt.setInt(5, U2UContentManagerImpl.generateChunkSize(file));
            pstmt.setInt(5, cadv.getChunksize());

            //file is incomplete
            pstmt.setString(6,"F");
            //pstmt.setBytes(6, new byte[]{Byte.parseByte("1")});

            //making the decision
            return (pstmt.executeUpdate() > 0 ? true : false);
            
            
        } catch (SQLException ex) {
            Logger.getLogger(SharedFiles.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(SharedFiles.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try
            {
                if (fis != null)
                {
                    fis.close();
                }

            } catch (IOException ex)
            {
                Logger.getLogger(SharedFiles.class.getName()).log(Level.SEVERE, null, ex);
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
     * Create a new record in the db, the method verifies if the SharedFile is Complete
     * @param fci
     * @return
     */
    public static boolean create(U2UFileContentImpl fci) {
        
        if (fci == null) 
        {
            return false;
        }
        
        U2UFileInputStream fis = null;
        FileInputStream fin = null;
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

            U2UContentAdvertisementImpl cadv = (U2UContentAdvertisementImpl) fci.getContentAdvertisement();
            U2UContentIdImpl cid = (U2UContentIdImpl)cadv.getContentId();
            File file = fci.getFile();

            PreparedStatement pstmt = conDB.createPreparedStatement("INSERT INTO U2U.SharedFiles VALUES(?,?,?,?,?,?)");

            //making a sub string of the ContentID without the sha1: substring
            pstmt.setString(1, cid.toString().substring(5));

            //path
            String filePath = file.toURI().toString();
            String name = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length());
            String path = filePath.substring(0, filePath.lastIndexOf("/") + 1);

            pstmt.setString(2, name);
            pstmt.setString(3, path);

            //
            fis = cadv.getInputStream();
            pstmt.setBinaryStream(4, fis, fis.getLength());

            //pstmt.setInt(5, U2UContentManagerImpl.generateChunkSize(file));
            pstmt.setInt(5, cadv.getChunksize());

            //verificating if is complete
            fin = new FileInputStream(file);
            pstmt.setString(6, (cid.equals(new U2UContentIdImpl(fin))? "T" : "F"));
            //pstmt.setBytes(6, new byte[]{Byte.parseByte("1")});

            //making the decision
            return (pstmt.executeUpdate() > 0 ? true : false);

        } catch (FileNotFoundException ex) {
            Logger.getLogger(SharedFiles.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(SharedFiles.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(SharedFiles.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try
            {
                if (fis != null)
                {
                    fis.close();
                }
                if (fin != null)
                {
                    fin.close();
                }
            } catch (IOException ex)
            {
                Logger.getLogger(SharedFiles.class.getName()).log(Level.SEVERE, null, ex);
            }
            //door.release();
            if(conDB != null)
            {
                conDBQueue.offer(conDB);
            }
        }

        return false;
    }

 

    public static boolean delete(U2UContentIdImpl cid) {
        
        if(cid == null)
        {
            return false;
        }
        
        String sha1 = cid.toString().substring(5);
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
            
            return conDB.sQLExecute("DELETE FROM U2U.SHAREDFILES WHERE SHA1_SF = '" + sha1 + "'");

        } catch (InterruptedException ex) {
            Logger.getLogger(SharedFiles.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            //door.release();
            if(conDB != null)
            {
                conDBQueue.offer(conDB);
            }
        }

        return false;
    }

    public static boolean exist(U2UContentIdImpl cid) {
        
        if(cid == null) 
        {
            return false;
        }
        
        boolean status = false;
        
        if(numberOfRecords(cid) > 0) {
            status = true;
        }
       
        return status;
        
    }

    public static U2UContentAdvertisementImpl getU2UContentAdvertisementImplFromName(String nameFile) {

        if(nameFile == null) 
        {
            return null;
        }
        
        U2UContentAdvertisementImpl adv = null;
        ConnectTo conDB = null;
        ResultSet rs = null;
        int con = 0;
        
        try {
            /*door.acquire();

            if(conDB == null) {
                return null;
            }*/
            if(!isConnected)
            {
                return null;
            }

            conDB = conDBQueue.take();

            //find a file by name in the database
            rs = conDB.sQLQuery("SELECT COUNT(*) FROM U2U.SHAREDFILES WHERE NAME_SF='"+nameFile+"'");
            rs.first();
            con = rs.getShort(1);

            if(con > 0)
            {
                ObjectInputStream ois = null;
                rs = conDB.sQLQuery("SELECT ADV_SF FROM U2U.SHAREDFILES WHERE NAME_SF='" + nameFile + "'");
                rs.first();

                if(rs.getRow()>0)
                {
                     Blob blobAdv = rs.getBlob("ADV_SF");
                    try {
                        ois = new ObjectInputStream(blobAdv.getBinaryStream());
                        adv = (U2UContentAdvertisementImpl) ois.readObject();

                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(SharedFiles.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(SharedFiles.class.getName()).log(Level.SEVERE, null, ex);
                    }finally
                    {
                        try
                        {
                            ois.close();
                        } catch (IOException ex) {
                        Logger.getLogger(SharedFiles.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(SharedFiles.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(SharedFiles.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if(rs != null)
                {
                    rs.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(SharedFiles.class.getName()).log(Level.SEVERE, null, ex);
            }
            //door.release();
            if(conDB != null)
            {
                conDBQueue.offer(conDB);
            }
        }

        return adv;
    }

    public static U2UContentAdvertisementImpl getU2UContentAdvertisementImplFromSHA1(String sha1_sf) {

        if(sha1_sf == null)
        {
            return null;
        }
        
        /*try {
            door.acquire();

            if(conDB == null) {
                return null;
            }

        } catch (InterruptedException ex) {
            Logger.getLogger(SharedFiles.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            door.release();
        }*/
        if(!isConnected)
        {
            return null;
        }

        //checking the sha1
        int pos = sha1_sf.indexOf(':');
        if (pos == -1) {
            System.out.println("Missing content id type: " + sha1_sf);
            return null;
        }
        String type = sha1_sf.substring(0, pos);
        if (!type.equalsIgnoreCase("sha1")) {
            System.out.println("Invalid content id type: " + sha1_sf);
            return null;
        }
        if (sha1_sf.length() - pos - 1 != 40) {
            System.out.println( "Invalid content id hash length: " + sha1_sf);
            return null;
        }

        U2UContentAdvertisementImpl adv = null;

        ResultSet rs = null;
        int count = 0;
        ConnectTo conDB = null;
        
        try
        {
            //door.acquire();
            conDB = conDBQueue.take();

            sha1_sf = sha1_sf.substring(5);//sha1: = 5
            //find a file by name in the database
            rs = conDB.sQLQuery("SELECT COUNT(*) FROM U2U.SHAREDFILES " +
                    "WHERE SHA1_SF = '" + sha1_sf + "'");
            rs.first();
            count = rs.getShort(1);

            if(count > 0)
            {
                ObjectInputStream ois = null;
                rs = conDB.sQLQuery("SELECT ADV_SF FROM U2U.SHAREDFILES WHERE SHA1_SF = '" + sha1_sf + "'");
                rs.first();

                if(rs.getRow() > 0)
                {
                    Blob blobAdv = rs.getBlob("ADV_SF");
                    try {

                        ois = new ObjectInputStream(blobAdv.getBinaryStream());
                        adv = (U2UContentAdvertisementImpl) ois.readObject();

                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(SharedFiles.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(SharedFiles.class.getName()).log(Level.SEVERE, null, ex);
                    }finally
                    {
                        try
                        {
                            if(ois != null)
                            {
                                ois.close();
                            }
                        } catch (IOException ex) {
                        Logger.getLogger(SharedFiles.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
         
            
        } catch (InterruptedException ex) {
            Logger.getLogger(SharedFiles.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(SharedFiles.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if(rs != null)
                {
                    rs.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(SharedFiles.class.getName()).log(Level.SEVERE, null, ex);
            }
            //door.release();
            if(conDB != null)
            {
                conDBQueue.offer(conDB);
            }
        }

        return adv;
    }

    public static String getPathOfTheSharedFile(U2UContentIdImpl cid)
    {
        if (cid == null) {
            return null;
        }

        ResultSet rs = null;
        ConnectTo conDB = null;
        
        try {
            /*door.acquire();

            if(conDB == null) {
                return null;
            }*/
            if(!isConnected)
            {
                return null;
            }
            
            String sha1_sf = cid.toString().substring(5);//sha1: = 5

            conDB = conDBQueue.take();

            rs = conDB.sQLQuery("SELECT PATH_SF || NAME_SF FROM U2U.SHAREDFILES " +
                    "WHERE SHA1_SF = '" + sha1_sf + "'");
            rs.first();

            if(rs.getRow() > 0)
            {
                return rs.getString(1);
            }

        } catch (SQLException ex) {
            Logger.getLogger(SharedFiles.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(SharedFiles.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(SharedFiles.class.getName()).log(Level.SEVERE, null, ex);
            }   
            //door.release();
            if(conDB != null)
            {
                conDBQueue.offer(conDB);
            }
        }
        
        return null;
    }

    public static String getNameOfTheSharedFile(U2UContentIdImpl cid)
    {
        if (cid == null) {
            return null;
        }
        
        ResultSet rs = null;
        ConnectTo conDB = null;
        try {
            /*door.acquire();

            if(conDB == null) {
                return null;
            }*/
            if(!isConnected)
            {
                return null;
            }
            
            String sha1_sf = cid.toString().substring(5);//sha1: = 5

            conDB = conDBQueue.take();

            rs = conDB.sQLQuery("SELECT NAME_SF FROM U2U.SHAREDFILES " +
                    "WHERE SHA1_SF = '" + sha1_sf + "'");
            rs.first();
            String name;
            if(rs.getRow() > 0)
            {
                name = rs.getString(1);
                name = name.replace("%20", " ");
                return name;
            }

        } catch (SQLException ex) {
            Logger.getLogger(SharedFiles.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(SharedFiles.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(SharedFiles.class.getName()).log(Level.SEVERE, null, ex);
            }
            //door.release();
            if(conDB != null)
            {
                conDBQueue.offer(conDB);
            }
        }
        
        return null;
    }

    
    public static int getChunkSizeOfTheSharedFile(U2UContentIdImpl cid)
    {
        if (cid == null) {
            return 0;
        }
        
        ResultSet rs = null;
        ConnectTo conDB = null;
        try {
            /*door.acquire();

            if(conDB == null) {
                return 0;
            }*/
            if(!isConnected)
            {
                return 0;
            }

            conDB = conDBQueue.take();

            String sha1_sf = cid.toString().substring(5);
            rs = conDB.sQLQuery("SELECT CHUNK_SIZE_SF FROM U2U.SHAREDFILES " + "WHERE SHA1_SF = '" + sha1_sf + "'");
            rs.first();

            if(rs.getRow() > 0)
            {
                return rs.getInt(1);
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(SharedFiles.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(SharedFiles.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(SharedFiles.class.getName()).log(Level.SEVERE, null, ex);
            }        
            //door.release();
            if(conDB != null)
            {
                conDBQueue.offer(conDB);
            }
        }

        return 0;
    }

    public static boolean getCompleteSFValue(U2UContentIdImpl cid)
    {
        if (cid == null) {
            return false;
        }

        ResultSet rs = null;
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
            
            String sha1_sf = cid.toString().substring(5);//sha1: = 5

            conDB = conDBQueue.take();

            rs = conDB.sQLQuery("SELECT COMPLETE FROM U2U.SHAREDFILES " +
                    "WHERE SHA1_SF = '" + sha1_sf + "'");
            rs.first();

            if(rs.getRow() > 0)
            {
                String result = rs.getString(1);

                if(result.equals("T"))
                {
                    return true;
                }
                else if(result.equals("F"))
                {
                    return false;
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(SharedFiles.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(SharedFiles.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(SharedFiles.class.getName()).log(Level.SEVERE, null, ex);
            }
            //door.release();
            if(conDB != null)
            {
                conDBQueue.offer(conDB);
            }
        }

        return false;
    }

    public static int numberOfRecords(U2UContentIdImpl cid)
    {
        int count = 0;
        
        String sha1 = null;
        ResultSet rs = null;
        ConnectTo conDB = null;
        try {
            /*door.acquire();

            if(conDB == null) {
                return 0;
            }*/
            if(!isConnected)
            {
                return 0;
            }

            conDB = conDBQueue.take();

            if(cid != null)
            {
                sha1 = cid.toString().substring(5);
                rs = conDB.sQLQuery("SELECT COUNT(*) FROM U2U.SHAREDFILES WHERE SHA1_SF = '" + sha1 + "'");
            }
            else
            {
                rs = conDB.sQLQuery("SELECT COUNT(*) FROM U2U.SHAREDFILES");
            }

            rs.first();

            count = rs.getInt(1);

        } catch (SQLException ex) {
            Logger.getLogger(SharedFiles.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(SharedFiles.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if(rs != null)
                {
                    rs.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(SharedFiles.class.getName()).log(Level.SEVERE, null, ex);
            }
            //door.release();
            conDBQueue.offer(conDB);
        }
 
        return count;
    }

    /**
     * register that the Shared File identify by the SHA1-sf was downloaded, setting
     * the status to 'T'
     *
     * @param cid
     * 
     * @return true if the modification was successful
     */
    public static boolean registerSharedFileDownload(U2UContentIdImpl cid)
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

            //checking the cid
            if(cid != null)
            {
                String sha1_sf = cid.toString().substring(5);//sha1: = 5

                conDB = conDBQueue.take();

                return conDB.sQLExecute("UPDATE U2U.SHAREDFILES SET COMPLETE = 'T' " +
                        "WHERE SHA1_SF = '" + sha1_sf + "'");

            }

        } catch (InterruptedException ex) {
            Logger.getLogger(SharedFiles.class.getName()).log(Level.SEVERE, null, ex);
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
     * find an specific content at the database
     *
     * @param cid find the specific content at the SharedFiles table, if cid is null then returns all the sharedfiles
     * @return an Enumeration"\<"U2UFileContentImpl"\>"
     */
    public static Enumeration<U2UFileContentImpl> find(U2UContentIdImpl cid) {
        
        Enumeration<U2UFileContentImpl> en = null;
        ObjectInputStream in = null;
        ResultSet rs = null;
        ConnectTo conDB = null;

        try {
            /*door.acquire();

            if(conDB == null) {
                return null;
            }*/
            if(!isConnected)
            {
                return null;
            }
            
            String sha1 = null;
            
            final ArrayList<U2UFileContentImpl> array = new ArrayList<U2UFileContentImpl>();

            conDB = conDBQueue.take();
            
            if(cid != null)
            {
                sha1 = cid.toString().substring(5);
                rs = conDB.sQLQuery("SELECT NAME_SF,PATH_SF,ADV_SF FROM U2U.SHAREDFILES WHERE SHA1_SF = '" + sha1 + "'");
            }
            else
            {
                rs = conDB.sQLQuery("SELECT NAME_SF,PATH_SF,ADV_SF FROM U2U.SHAREDFILES");
            }

            rs.first();

            while (!rs.isAfterLast() && (rs.getRow() > 0)) {
                String name = rs.getString(1);
                String path = rs.getString(2);

                in = new ObjectInputStream(rs.getBlob(3).getBinaryStream());
                U2UContentAdvertisementImpl cadv = (U2UContentAdvertisementImpl) in.readObject();
                in.close();

                File file = new File(path + name);
                //U2UFileContentImpl fci = new U2UFileContentImpl(file, name, null, null);
                U2UFileContentImpl fci = new U2UFileContentImpl(file, cadv);

                array.add(fci);

                rs.next();
            }

            en = new Enumeration<U2UFileContentImpl>() {

                private ArrayList<U2UFileContentImpl> arrayFC = array;
                private int count = arrayFC.size();

                public boolean hasMoreElements() {
                    return count > 0 ? true : false;
                }

                public U2UFileContentImpl nextElement() {
                    if (count > 0) {
                        count--;
                        return arrayFC.get(count);
                    } else {
                        return null;
                    }
                }
            };

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(SharedFiles.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SharedFiles.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(SharedFiles.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(SharedFiles.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if(rs != null)
                {
                    rs.close();
                }
                if(in != null)
                {
                    in.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(SharedFiles.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
                Logger.getLogger(SharedFiles.class.getName()).log(Level.SEVERE, null, ex);
            }

            //door.release();
            conDBQueue.offer(conDB);
        }
        
        return en;
    }

    /*public static HashMap getAllSharedFiles() {

        ResultSet res;

        res = conDB.sQLQuery("SELECT SHA1_SF, PATH_SF FROM U2U.SHAREDFILES WHERE COMPLETE_SF='F'");

        return null;

    }*/
}
