package launch;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import org.u2u.filesharing.U2UContentIdImpl;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Irene & Sergio
 */
public class ChunksGenerator {

    public static void main(String args[])
    {
        JFileChooser fc = new JFileChooser();

        if(JFileChooser.APPROVE_OPTION == fc.showOpenDialog(null))
        {

            FileInputStream fis = null;
            try {
                File file = fc.getSelectedFile();

                fis = new FileInputStream(file);
                U2UContentIdImpl cid = new U2UContentIdImpl(fis);

                String prefix = cid.toString().substring(5) + "-";//sha1: = 5
                //making the chunks
                FileChannel fch = fis.getChannel();

                long lengthFile = file.length();
                int nchunks = (int) (lengthFile / (64 * 1024)) + 1;//number of chunks

                //byte's length equals to the chunk's size
                ByteBuffer buffer = null;
                long init = System.currentTimeMillis();
                System.out.println("init" + init);
                for(int i = 0; i < nchunks; i++)
                {
                    //from file

                    int lengthChunk = (i < (nchunks-1) ? 64*1024 : (int)(lengthFile - (64*1024)*(nchunks-1)));

                    buffer = fch.map(FileChannel.MapMode.READ_ONLY, (long)i*(64*1024), lengthChunk);

                    byte[] buf = new byte[lengthChunk];
                    buffer.get(buf);

                    //to the file
                    File chunk = new File("/chunks/" + prefix + i);
                    FileOutputStream fos = new FileOutputStream(chunk);

                    fos.write(buf);

                    fos.close();

                }
                System.out.println("----Finish"+ (System.currentTimeMillis() - init) );

            } catch (FileNotFoundException ex) {
                Logger.getLogger(ChunksGenerator.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(ChunksGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }finally {
                try {
                    fis.close();
                } catch (IOException ex) {
                    Logger.getLogger(ChunksGenerator.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

}
