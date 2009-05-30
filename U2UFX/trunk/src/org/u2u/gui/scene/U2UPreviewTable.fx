/*
 * U2UPreviewTable.fx
 *
 * Created on 29-may-2009, 14:30:32
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

/**
 * @author sergio
 */

/*
 * ShareFilesTable.fx
 *
 * Created on 21-may-2009, 21:18:42
 */

import javafx.scene.CustomNode;
import javafx.scene.Node;
import javafx.scene.Group;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.effect.Glow;
import javax.swing.JFileChooser;
import javafx.ext.swing.SwingComponent;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.*;
import javafx.scene.paint.*;

import java.io.File;
import javafx.scene.effect.DropShadow;
import org.jfxtras.stage.JFXDialog;
import javafx.scene.Scene;
import javafx.ext.swing.SwingTextField;
import javafx.ext.swing.SwingButton;
import javax.swing.JOptionPane;
import org.u2u.app.U2UFXApp;
import org.u2u.filesharing.U2UFileSharingServiceListener;
import org.u2u.filesharing.U2UFileSharingServiceEvent;
import java.util.ArrayList;

import javafx.scene.media.*;

import org.u2u.data.TypeFile;

import org.u2u.gui.scene.extra.SimpleMediaPlayer;

/**
 * @author Irene
 */
public class U2UPreviewTable extends CustomNode, U2UFileSharingServiceListener {

    public var selec: Integer = -1 on replace {
        println("selection change {selec}")
    };
    public var shared:SharedFile[] = null on replace {
    
        println("share change, now have {sizeof shared} elements");
        if((shared != null) and (table != null)) {
            
            table.rows = for(s in shared) {
                        SwingTable.TableRow{
                            cells: [
                                SwingTable.TableCell{
                                    text: s.name;
                                },
                                SwingTable.TableCell{
                                    text: s.path;
                                },
                                 SwingTable.TableCell{
                                    text: s.type;
                                },
                            ]
                        }
                    };
        }
        
    
    };
    public var inputDialog:JFXDialog;
    //public var sourceMedia:String;
    var group:Group;
    var player:SimpleMediaPlayer;
    var table: SwingTable;

    override function create():Node{
        group = Group{
            content: [
                Text{
                    translateX:250;
                    translateY:70;
                    content:"PREVIEW DOWNLOADED FILES";
                    font: Font.font("Arial",FontWeight.BOLD,20);
                },
                table = SwingTable{
                    width:380;
                    height:200;
                    translateX:230;
                    translateY:110;
                    columns: [
                        SwingTable.TableColumn{
                            text : "Name File";
                            },
                        SwingTable.TableColumn{
                            text : "Path";
                            },
                        SwingTable.TableColumn{
                            text : "Type";
                            }
                    ];

//                    rows: bind  for(s in shared) {
//                        SwingTable.TableRow{
//                            cells: [
//                                SwingTable.TableCell{
//                                    text: s.name;
//                                },
//                                SwingTable.TableCell{
//                                    text: s.path;
//                                },
//                                 SwingTable.TableCell{
//                                    text: s.type;
//                                },
//                            ]
//                        }
//                    };
                    selection : bind selec with inverse;

                },

                ImageView{
                    translateX: 360;
                    translateY: 390;
                    image: Image{url:"{__DIR__}resources/frame.png"}
                },
                ImageView{

                    translateX: 410;
                    translateY: 410;
                    image: Image{url:"{__DIR__}resources/preview2.png"}
                    onMouseClicked:function(me:MouseEvent):Void{
                        
                        previewFile();
                    }
                    onMouseMoved:function(me:MouseEvent):Void{
                        me.node.effect = Glow{level:0.5}
                    }
                    onMouseExited:function(me:MouseEvent):Void{
                        me.node.effect = null;
                    }
                }

            ]
        }
    }

    function previewFile():Void{
        
        println("Preview the file {selec} llamado: {shared[selec].name}");

        if(this.selec >= 0) {
            var typeFile:String = TypeFile.getTypeFile(shared[selec].name);

            if(typeFile.equals(TypeFile.MUSIC) or typeFile.equals(TypeFile.VIDEO) )
            {
                if(typeFile.equals(TypeFile.MUSIC)){

                    //delete group.content[2];

                    println("tipo de archivo: {TypeFile.getTypeFile(shared[selec].name)}");

                    if(this.player != null) {
                        this.player.player.stop();
                        this.player.source = null;
                        delete this.player from group.content;
                    }

                    this.player = SimpleMediaPlayer {
                      translateX:240;
                      translateY:330;
                      source : ((new File("Shared/{shared[selec].name}")).toURI()).toString();
                      width: 350
                      height: bind 100
                    }

                    insert player into group.content;
                    
                }

            }else{

                JOptionPane.showMessageDialog(null,"Sorry, only preview for audio and video files!");
            }
        }


    }

    /**
    * This function verifies if the file to insert is already inserted
    */
    function isFileShared(name:String):Boolean{

        var files:U2UPreviewTable.SharedFile[] = shared;
        for(x in [0..sizeof files])
        {
            if(files[x].name.equals(name))
            {
                return true;
            }
        }
        return false;
    }

    override function serviceEvent(event:U2UFileSharingServiceEvent ):Void {

        if(event.getType()== U2UFileSharingServiceEvent.SHARED)
        {
            var obj:Object[]  = event.getInformation();
            var nameF:ArrayList  = obj[0] as ArrayList;
            var local:ArrayList  = obj[1] as ArrayList ;
            var name:String ;
            var ext:String ;
            var tmpShared: SharedFile[] = [];

            for(i in [0 .. < nameF.size()])
            {
                name = nameF.get(i) as String;
                ext = name.substring(name.indexOf('.')+1);

                var newShared: SharedFile =
                    SharedFile {
                        name: name;
                        path: local.get(i) as String;
                        type: name;
                    };

               insert newShared into tmpShared;
            }

            this.shared = tmpShared;

            this.selec = -1;
        }
     }

}

protected class SharedFile{

    public-read var name: String;
    public-read var path: String;
    public-read var type:String;
}


