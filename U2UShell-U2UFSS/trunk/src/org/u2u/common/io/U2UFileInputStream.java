/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.u2u.common.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * This class help to Obtaing a FileInputStream and return his length from the file
 */
public class U2UFileInputStream extends FileInputStream {

    private File file;
    
    public U2UFileInputStream(File file) throws FileNotFoundException
    {
        super(file);
        
        this.file = file;
    }
    
    public int getLength()
    {
        return (int)file.length();
    }
}