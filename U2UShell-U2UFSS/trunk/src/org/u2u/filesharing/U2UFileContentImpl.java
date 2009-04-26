/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.u2u.filesharing;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import net.jxta.share.ContentAdvertisement;
import net.jxta.share.ContentId;
import net.jxta.share.FileContent;

/**
 * FileContent implementation it represents a Shared File, adv+file information
 */
public class U2UFileContentImpl implements FileContent {

    //instance's variables
    private File file;
    private long mtime;                      //last modified time
    private U2UContentAdvertisementImpl adv;

     
    
    /**
     * 
     * @param file
     * @param name
     * @param type
     * @param desc
     * @throws IOException 
     */
    public U2UFileContentImpl(File file, String name, String type, String desc) throws IOException
    {
        // normalize path name
        this.file = new File(file.getAbsolutePath());
        InputStream is = new FileInputStream(file);
        mtime = file.lastModified();
        long length = file.length();
        ContentId id = new U2UContentIdImpl(is);
        is.close();
        
        if (name == null) {
            name = file.getName();
        }
        
        adv = new U2UContentAdvertisementImpl(name, id, length, type, desc);
    }

    public U2UFileContentImpl(File file, U2UContentAdvertisementImpl adv)
    {
        this.file = new File(file.getAbsolutePath());
        mtime = file.lastModified();
        this.adv = adv;
    }
    
    //methods
    public File getFile() {    
        return this.file;
    }

    public ContentAdvertisement getContentAdvertisement() {
       return this.adv;
    }

    public InputStream getInputStream() throws IOException {
        return (file != null ? (new FileInputStream(file)) : null);
    }

}
