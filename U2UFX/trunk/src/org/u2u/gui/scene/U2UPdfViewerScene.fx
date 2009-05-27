/*
 * U2UStagePdfViewer.fx
 *
 * Created on 26-may-2009, 22:58:19
 */

package org.u2u.gui.scene;

import javafx.stage.Stage;
import javafx.scene.*;

import java.io.File;
import java.io.RandomAccessFile;
import java.io.FileNotFoundException;
import java.nio.channels.FileChannel;
import java.nio.ByteBuffer;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;

import java.io.IOException;
import javafx.ext.swing.SwingButton;
import javafx.ext.swing.SwingIcon;
import javafx.scene.image.Image;

import org.u2u.gui.scene.extra.*;


/**
 * @author sergio
 */

public class U2UPdfViewerScene extends U2UAbstractScene{

    
    var userGuide:File  = new File("conf/u2uguide.pdf");
    //println("The file is :{userGuide.getName()}");
    var raf:RandomAccessFile ;
    var current:Integer;
    var numPages:Integer;

    //variables to show help
    var pdfFile:PDFFile;
    var page:PDFPage;
    var pdfPagePanel:U2UPagePanel;
    var mainPanel:U2UJPanel;

    var butNext:SwingButton;
    var butPrev:SwingButton;

    init{
      
        this.content = [

            mainPanel = U2UJPanel{

                width: 650;
                height: 500;

                top :[

                    butPrev = SwingButton{

                        icon: SwingIcon{
                            image:Image{
                                url: "{__DIR__}resources/previous.png";
                            }
                        }
                        action: function():Void{
                            showPrevius();
                        };
                    },

                    butNext = SwingButton{

                        icon: SwingIcon{
                            image:Image{
                                url: "{__DIR__}resources/next.png";
                            }
                        }
                        action: function():Void{
                            showNext();
                        };
                    },

//                    SwingButton{
//
//                        text:"Zoom +";
//                        action:function():Void{
//                            showZoom();
//                        }
//                    }
                ];

                center: pdfPagePanel = U2UPagePanel {};

                bottom: [
                    SwingButton {

                        icon: SwingIcon{
                            image:Image{
                                url: "{__DIR__}resources/back.png";
                            }
                        }
                        action: function():Void{
                            exitHelp();
                        };
                    }
                ];
            }
       ];

       initComponents();

    }

    function initComponents():Void{

    try {
            println("existe el guide ? {userGuide.exists()}");
            raf = new RandomAccessFile(userGuide, "r");
            println("the file is : {userGuide.getName()}");
            var channel:FileChannel  = raf.getChannel();
            var buf: ByteBuffer  = channel.map(FileChannel.MapMode.READ_ONLY,
                0, channel.size());
            pdfFile = new PDFFile(buf);

            // show the first page
            page = pdfFile.getPage(0);
            pdfPagePanel.showPagePdf(page);
//            jB_pre.setEnabled(false);
            current = 1;
            numPages = pdfFile.getNumPages();
            


        } catch ( ex:FileNotFoundException) {
            println("FileNotFoundException in showHelp into U2UContentStage {ex.getCause()}");
        } catch ( ex:IOException) {
            println("IOEception in showHelp into U2UContentStage {ex.getCause()}");

        }
    }

    /**
    * Shows the next page in the pdf
    */
    function showNext():Void{

        if(current >= 1 and current!= numPages)
        {
            butPrev.enabled = true;
            current++;
            page = pdfFile.getPage(current);
            //pagePDFPanel.showPage(page);
            pdfPagePanel.showPagePdf(page);

        }else{

            butNext.enabled = false;
        }
    }

    /**
    * Shows the previous page in the pdf
    */
    function showPrevius():Void{

        if(current <=1 and current <= numPages)
        {
            butPrev.enabled = false;
        }
        else
        {
            butNext.enabled = true;
            current--;
            page = pdfFile.getPage(current);
            pdfPagePanel.showPagePdf(page);
        }
    }


    /**
    * Exit from the Help Scene
    */

    function exitHelp():Void{
        current = 1 ;
        this.contentStage.showSearch();
    }

}
