/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package RMS;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import javax.microedition.rms.RecordFilter;

/**
 *
 * @author Swoko
 */
public class Filter implements RecordFilter{

    private String searchedString = null;

    public Filter(String string){
        this.searchedString = string.toLowerCase();
    }

    public boolean matches(byte[] candidate) {
        String compareString;
        ByteArrayInputStream bais;
        DataInputStream dis;
        try {
            bais = new ByteArrayInputStream(candidate);
            dis  = new DataInputStream(bais);
            compareString = dis.readUTF().toLowerCase();
        } catch (IOException ex) {
            return false;
        }
        if( (compareString!=null) && 
                (compareString.indexOf(searchedString))!= -1 ){
            return true;
        } else {
            return false;
        }
    }

}
