/*
 * U2UStagePdfViewer.fx
 *
 * Created on 26-may-2009, 22:58:19
 */
/**
 * Copyright (c) 2009, Sergio Pino and Irene Manotas
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of Sergio Pino and Irene Manotas. nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * @author: Sergio Pino and Irene Manotas
 * Website: http://osum.sun.com/profile/sergiopino, http://osum.sun.com/profile/IreneLizeth
 * emails  : spino327@gmail.com - irenelizeth@gmail.com
 * Date   : March, 2009
 * This license is based on the BSD license adopted by the Apache Foundation.
 *
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

public class U2UHelpScene extends U2UAbstractScene{

    
    var userGuide:File  = new File("conf/u2ufxguide.pdf");
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
            page = pdfFile.getPage(1);
            pdfPagePanel.showPagePdf(page);

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
        this.contentStage.showShare();
       
        
    }

}
