/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package RMS;

import agenda.Contact;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import javax.microedition.rms.*;

/**
 *
 * @author Swoko
 */
public class DataRMS {

    private RecordStore rs;

    public DataRMS(String name){
        openRecordStore(name);
    }

    public void openRecordStore(String name){
        try{
            rs = RecordStore.openRecordStore(name, true);
        }
        catch(RecordStoreException e) {
            System.out.println("RecordStore not opened");
        }
    }

    public void closeRecordStore(){
         try{
            if(rs.getNumRecords() == 0){
               String name= rs.getName();
               rs.closeRecordStore();
               RecordStore.deleteRecordStore(name);
            }
            else
            {
                rs.closeRecordStore();
            }
         }

        catch(RecordStoreException e)
        {
            e.printStackTrace();
        }
    }
    
    public int newRecord(String name, String phoneNumber, int phoneType,
            String mail, int contactType){
        byte[] record;
        ByteArrayOutputStream baos;
        DataOutputStream dos;

        try{
            baos = new ByteArrayOutputStream();
            dos = new DataOutputStream(baos);
            dos.writeUTF(name);
            dos.writeUTF(phoneNumber);
            dos.writeUTF(mail);
            dos.writeInt(phoneType);
            dos.writeInt(contactType);
            dos.flush();
            record = baos.toByteArray();
            int id = rs.addRecord(record, 0, record.length);
            baos.close(); dos.close();
            return id;
        }
        catch(Exception e){
            System.out.println("Registry insertion failed");
        }
        return(-1);
    }

    public void setRecord(int id, String name, String phoneNumber, String mail,
            int phoneType, int contactType){
        byte[] record;
        ByteArrayOutputStream baos;
        DataOutputStream dos;

        try{
            baos = new ByteArrayOutputStream();
            dos = new DataOutputStream(baos);
            dos.writeUTF(name);
            dos.writeUTF(phoneNumber);
            dos.writeUTF(mail);
            dos.writeInt(phoneType);
            dos.writeInt(contactType);
            dos.flush();
            record = baos.toByteArray();
            rs.setRecord(id, record, 0, record.length);
            baos.close(); dos.close();
        }
        catch(Exception e){
            System.out.println("Registry insertion failed");
        }
    }
    
    public Contact getRecord(int id){
        ByteArrayInputStream bais;
        DataInputStream dis;
        byte[] record = new byte[75];

        try{
            bais = new ByteArrayInputStream(record);
            dis = new DataInputStream(bais);
            rs.getRecord(id, record, 0);
            String name = dis.readUTF();
            String phoneNumber = dis.readUTF();
            String mail = dis.readUTF();
            int phoneType = dis.readInt();
            int contactType = dis.readInt();
            bais.close(); dis.close();
            return(new Contact(name, phoneNumber, phoneType, mail,
                    contactType));
        }
        catch(Exception e){
            System.out.println("registry read error");
        }
        record = null;
        return null;
    }

     public void deleteRecord(int id)
      {
          try
          {
              rs.deleteRecord(id);
          }catch(RecordStoreException e)
          {
              e.printStackTrace();
          }
      }

     public RecordEnumeration getNumRecords() throws RecordStoreNotOpenException
      {
          //Object to compare records
          Comparator comp = new Comparator();
          return(rs.enumerateRecords(null, comp, false));
      }

     public RecordEnumeration searchRecords(String search)
             throws RecordStoreNotOpenException{
         //Object to filter the search
         Filter filter = new Filter(search);
         //Object to compare records
         Comparator comparator = new Comparator();
         return(rs.enumerateRecords(filter, comparator, false));
     }

}
