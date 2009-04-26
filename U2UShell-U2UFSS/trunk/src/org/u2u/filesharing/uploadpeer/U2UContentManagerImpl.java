/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.u2u.filesharing.uploadpeer;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import org.u2u.common.db.SharedFiles;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.u2u.filesharing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import net.jxta.discovery.DiscoveryService;
import net.jxta.share.Content;
import net.jxta.share.ContentAdvertisement;
import net.jxta.share.ContentFilter;
import net.jxta.share.ContentId;
import net.jxta.share.ContentManager;
import net.jxta.share.FileContent;
import net.jxta.share.metadata.ContentMetadata;
import org.u2u.common.db.Address;
import org.u2u.common.db.Chunks;
import org.u2u.common.db.Chunks_Add;
import org.u2u.common.db.ConnectTo;
import org.u2u.common.db.Table;

/**
 * ContentManager implementation for managing local shared content within
 * a peer, it share, unshare and find shared files.
 */
public class U2UContentManagerImpl extends ContentManager {

    //instance's variables
    //private ConnectTo conDB;
    private U2UFileSharingService fss;
    private Object[] sharedFiles; //sharedFiles in the DB
       
    //constructors
    
    /**
     * creates a U2UContentManagerImpl, who init the access to the embebed database,  
     * (creates the database from the backup if the db not getU2UContentAdvertisementImpl, commonly at the first run)
     * @param service 
     */
    public U2UContentManagerImpl(U2UFileSharingService service)
    {
        
        /*conDB = new ConnectTo("org.apache.derby.jdbc.EmbeddedDriver");
        //check if the db's folder already getU2UContentAdvertisementImpl
        if (!conDB.getConnection("jdbc:derby:U2UClient", "U2U", "")) {
            //if debug mode then put 'createFrom=conf/.U2UClient'
            //for build the distribution then put 'createFrom=conf/.U2UClient'
            conDB.getConnection("jdbc:derby:U2UClient;createFrom=conf/.U2UClient", "U2U", "");
        }

        //passign the connection
        SharedFiles.setConnection(conDB);
        Chunks.setConnection(conDB);
        Address.setConnection(conDB);
        Chunks_Add.setConnection(conDB);*/

        Table.connect();

        //clean the tables
        boolean status = true;
        status = status & Address.deleteAll();
        status = status & Chunks_Add.deleteAll();
        System.out.println("U2UContentManagerImpl cleaning the tables " + status);

        this.fss = service;

    }
    
    //methods
    
    public FileContent share(File file, String name, String type, String desc) throws IOException {
       
        if(name==null)
            name=file.getName();
        
        FileContent fc = new U2UFileContentImpl(file, name, type, desc);
        //add chunk size
        ((U2UContentAdvertisementImpl)fc.getContentAdvertisement()).setChunksize(generateChunkSize(file));
        
        return (this.share(fc) == true ? fc : null);
    }

    @Override
    public FileContent share(File file, String name, String type, ContentMetadata[] metadata) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
     //Check to see if the SharedFiles' records stored into the DB still points to an uncorrupt file.
    private void checkSharedFiles() {

        //HashMap resMap = SharedFiles.getAllSharedFiles();
        /*
        Iterator<Content> eachShare = shares.iterator();
        
        while ( eachShare.hasNext() ) {
            Content content = eachShare.next();
            boolean isCorrupted = false;
            
            if (content instanceof FileContent) {
                FileContent sharedFileContent = (FileContent) content;
                File sharedFile = sharedFileContent.getFile();
                
                if (! sharedFile.exists()) {
                    //System.err.println ("File: " +content.getContentAdvertisement().getName()+ "Does not getU2UContentAdvertisementImpl anymore, Unsharing it ...");
                    isCorrupted=true;
                } else if (! sharedFile.canRead()) {
                    //System.err.println ("File: " +content.getContentAdvertisement().getName()+ "can't be read anymore, Unsharing it ...");
                    isCorrupted=true;
                } else if (sharedFile.length()!=content.getContentAdvertisement().getLength()) {
                    //System.err.println ("File size for: " +content.getContentAdvertisement().getName()+ "has changed: Was "+content.getContentAdvertisement().getLength()+" bytes and now is: "+sharedFile.length()+" bytes. Unsharing it ...");
                    isCorrupted = true;
                }
                
                if (isCorrupted) {
                    eachShare.remove();
                }
            }
        }*/
    }
    
    @Override
    public void unshare(Content c) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void unshare(ContentAdvertisement cAdv) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Content[] getContent() {

        ContentId id = null;
        return this.getContent(id);
    }

    //FIXME Create a new method that acept String
    @Override
    public Content[] getContent(ContentId id) {

        Content[] fcon = null;
        int count = SharedFiles.numberOfRecords((U2UContentIdImpl) id);
        
        if(count>0)
        {
            fcon = new Content[count];

            Enumeration en = SharedFiles.find((U2UContentIdImpl) id);

            while(en.hasMoreElements())
            {
                count--;
                fcon[count] = (Content) en.nextElement();
            }
        }

        return fcon;  
    }

    @Override
    public Content[] getContent(ContentFilter filter) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    //fixme
    @Override
    public String getMimeType(File file) {
        //return TypeFile.getTypeFile(file);
        String ext = file.getName().toLowerCase();
        return ext.substring(ext.lastIndexOf('.')+1);
    }

    public boolean unshare(String nameFile) {
        boolean res = false;
        U2UContentAdvertisementImpl adv;

         //if (fss != null && conDB != null)
         if (fss != null)
         {
            if( (adv = SharedFiles.getU2UContentAdvertisementImplFromName(nameFile)) != null)
            {
                DiscoveryService ds = fss.getGroup().getDiscoveryService();
                try {
                    ds.flushAdvertisement(adv);
                    res = SharedFiles.delete((U2UContentIdImpl) adv.getContentId());
                                       
                } catch (IOException ex) {

                    System.out.println("Can't flush the advertisement that represents the file: "+nameFile);
                    Logger.getLogger(U2UContentManagerImpl.class.getName()).log(Level.SEVERE, null, ex);
                    return false;
                }
            }
         }

        return res;
    }

   

    /**
     * Close the connection to the database
     * @throws java.lang.Throwable
     */
    @Override
    protected void finalize() throws Throwable {
        Table.disconnect();
        super.finalize();
        
    }

    /**
     * to the db and publish
     * @param fc
     * @return
     */
    private boolean share(FileContent fc)
    {
        synchronized (this)
        {
            boolean status = false;

            //if (fss != null && conDB != null)
            if (fss != null)
            {
                boolean canPublish = false;

                U2UContentAdvertisementImpl adv = (U2UContentAdvertisementImpl) fc.getContentAdvertisement();
                //Saves in the DB the shared file and the chunks information
                if(!SharedFiles.exist((U2UContentIdImpl) adv.getContentId()))
                {
                    if(SharedFiles.create((U2UFileContentImpl) fc))
                    {
                        canPublish = true;

                        //remove all the chunks relatinship with the ContentId
                        //Chunks.deleteAllChunks(adv.getContentId().toString());

                        try {
                            //calculating the chunks
                            FileInputStream fis = new FileInputStream(fc.getFile());
                            FileChannel fch = fis.getChannel();

                            String sha1_sf = adv.getContentId().toString();
                            int chunk_size = adv.getChunksize()*1024;
                            long lengthFile = adv.getLength();
                            int nchunks = (int) (lengthFile / chunk_size) + 1;//number of chunks

                            //creating the chunks
                            //byte's length equals to the chunk's size
                            ByteBuffer buffer = null;
                            byte[] buf = null;
                            long init = System.currentTimeMillis();
                            System.out.println("init the calculating of the chunks' sha1 of the shared content " + init);
                            for(int i = 0; i < nchunks; i++)
                            {
                                //length of chunk
                                int lengthChunk = (i < (nchunks-1) ? chunk_size : (int)(lengthFile - chunk_size*(nchunks-1)));

                                //reading from the File System
                                buffer = fch.map(FileChannel.MapMode.READ_ONLY, (long)i*chunk_size, lengthChunk);
                                buf = new byte[lengthChunk];
                                buffer.get(buf);

                                //calculating the SHA-1
                                String sha1_chunk = U2UContentIdImpl.getChunkSHA1(buf);
                                //to the Database
                                canPublish = canPublish & Chunks.create(sha1_sf, sha1_chunk, (short)i, true);
                            }
                            
                            System.out.println("calculating the chunks' sha1 of the shared content " + (System.currentTimeMillis() - init));

                        } catch (FileNotFoundException ex) {
                            Logger.getLogger(U2UContentManagerImpl.class.getName()).log(Level.SEVERE, null, ex);

                            canPublish = false;
                        }catch (IOException ex) {
                            Logger.getLogger(U2UContentManagerImpl.class.getName()).log(Level.SEVERE, null, ex);
                        } 
                        
                    }
                }
                else
                {
                    canPublish = true;
                }


                // Index the new shared content, Publishing the ContentAdvertisement
                if(canPublish)
                {
                    fss.publishContent(fc);
                    status = true;
                }

            }

            return status;
        }

    }

    /**
     * share the incomplete file
     *
     * this method is "special" and support the  "almost truths" logic.
     *
     * 1) if the advertisment isn't at the database in the SharedFiles table, register it. This for operation purpose of the system.
     * or
     * 2) if the advertisment is at the database in the SharedFiles table, publish it. Because the system already have at least one chunk.
     *
     * @param adv
     *
     * @return true if the system do the task
     */
    public boolean shareIncompleteData(U2UContentAdvertisementImpl adv)
    {
        synchronized (this)
        {
            boolean status = false;

            //if (fss != null && conDB != null)
            if (fss != null)
            {

                //if the advertisment isn't at the database in the SharedFiles table
                //Saves in the DB the shared file and the chunks information
                if(!SharedFiles.exist((U2UContentIdImpl) adv.getContentId()))
                {
                    System.out.println("U2UContentManagerImpl shareIncompleteData, register the adv at the db");
                    //register it
                    File file = new File("Shared/" + adv.getName());
                    adv.setSocketAdv(null);

                    U2UFileContentImpl fc = new U2UFileContentImpl(file, adv);

                    status = SharedFiles.createIncomplete(fc);
                    
                }
                //already exists
                //if the advertisment is at the database in the SharedFiles table
                else
                {
                    try
                    {
                        //if we have at least one chunk, so publish it.
                        if(0.0f < Chunks.haveAllTheChunks(adv.getContentId().toString()))
                        {
                            System.out.println("U2UContentManagerImpl shareIncompleteData, publis the adv at the p2p network, 'almost truths' logic");
                            //publish it
                            String uRI = SharedFiles.getPathOfTheSharedFile((U2UContentIdImpl) adv.getContentId());
                            File file = new File(new URI(uRI));

                            U2UFileContentImpl fc = new U2UFileContentImpl(file, adv);

                            fss.publishContent(fc);
                        }
                        //if we haven't at least one chunk, don't publish
                        else
                        {
                            System.out.println("U2UContentManagerImpl shareIncompleteData, the system haven't the minimum required information for the 'almost truths' logic");
                        }

                        status = true;

                    } catch (URISyntaxException ex) {
                        Logger.getLogger(U2UContentManagerImpl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            }

            return status;
        }
    }

    /**
     * share all the shared files at the database
     * @return
     */
    public boolean shareAllTheSharedFilesAtDB()
    {
        synchronized (this)
        {
            boolean status = false;

            //if (fss != null && conDB != null)
            if (fss != null)
            {
                status = true;
                Enumeration<U2UFileContentImpl> en = SharedFiles.find(null);
                ArrayList<String> names = new ArrayList<String>();
                ArrayList<String> local = new ArrayList<String>();
                String loc;
                while(en.hasMoreElements())
                {
                    U2UFileContentImpl fc = en.nextElement();
                    U2UContentIdImpl cid = (U2UContentIdImpl) fc.getContentAdvertisement().getContentId();
                    //complete?
                    if(SharedFiles.getCompleteSFValue(cid))
                    {
                        names.add(SharedFiles.getNameOfTheSharedFile(cid));
                        loc = SharedFiles.getPathOfTheSharedFile(cid);
                        loc = loc.replace("%20", " ");
                        local.add(loc);
                        fss.publishContent(fc);
                    }
                    //incomplete
                    else
                    {
                        this.shareIncompleteData((U2UContentAdvertisementImpl) fc.getContentAdvertisement());
                    }

                    if(fc != null)
                    {
                        
                    }
                }
                sharedFiles = new Object[] {names, local};
                 //show the shared files in the GUI throught a service event
            }

            return status;
        }
    }

   /*
    * @return a Object[] that contains the information about complete shared files in teh data base
    */
    public Object[] getCompleteSharedFilesDB()
    {
        return sharedFiles;
    }
   
    //static
    private static short generateChunkSize(File file) {
        return 256;
    }

    /**
     * Find in the database incomplete files and generate a event that
     * inform to GUI the files for download
     */
    private void findFilesIncomplete() {



    }

   
   
}
