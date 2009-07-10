/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package RMS;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import javax.microedition.rms.RecordComparator;

/**
 *
 * @author Swoko
 */
public class Comparator implements RecordComparator{

    public int compare(byte[] rec1, byte[] rec2) {
        
        ByteArrayInputStream bais;
        DataInputStream dis;
        String cad1, cad2;
        try{
            bais = new ByteArrayInputStream(rec1);
            dis = new DataInputStream(bais);
            cad1 = dis.readUTF().toLowerCase();
            bais = new ByteArrayInputStream(rec2);
            dis = new DataInputStream(bais);
            cad2 = dis.readUTF().toLowerCase();
            int resultado = cad1.compareTo(cad2);
            if (resultado == 0)
                return RecordComparator.EQUIVALENT;
            else if (resultado < 0)
                return RecordComparator.PRECEDES;
            else
                return RecordComparator.FOLLOWS;
            }
            catch (Exception e){
                return RecordComparator.EQUIVALENT;
        }

    }

}
