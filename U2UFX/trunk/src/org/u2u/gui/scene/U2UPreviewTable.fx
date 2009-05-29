/*
 * U2UPreviewTable.fx
 *
 * Created on 29-may-2009, 14:30:32
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

    public var selection: Integer;
    public var shared:SharedFile[];
    public var inputDialog:JFXDialog;
    //public var sourceMedia:String;
    var mediaAudio:MediaPlayer;
    var group:Group;
    var mediaPlayer:SimpleMediaPlayer;
    var src:String;

    override function create():Node{
        group = Group{
            content: [
                Text{
                    translateX:250;
                    translateY:70;
                    content:"PREVIEW DOWNLOADED FILES";
                    font: Font.font("Arial",FontWeight.BOLD,20);
                },
                SwingTable{
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

                    rows: bind  for(s in shared)
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
                    selection: bind selection with inverse
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
        
        println("Preview the file {selection} llamado: {shared[selection].name}");
        var typeFile:String = TypeFile.getTypeFile(shared[selection].name);

        if(typeFile.equals(TypeFile.MUSIC) or typeFile.equals(TypeFile.VIDEO) )
        {
            if(typeFile.equals(TypeFile.MUSIC)){

                //delete group.content[2];

                println("tipo de archivo: {TypeFile.getTypeFile(shared[selection].name)}");

                var player:SimpleMediaPlayer = SimpleMediaPlayer {
                  translateX:240;
                  translateY:330;
                  source: ((new File("Shared/{shared[selection].name}")).toURI()).toString();
                  width: 350
                  height: bind 100
                }

                insert player before group.content[3];
            }

        }else{
           
            JOptionPane.showMessageDialog(null,"Sorry, only preview for audio and video files!");
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

               insert newShared into shared;
            }
        }
     }

}

protected class SharedFile{

    public-read var name: String;
    public-read var path: String;
    public-read var type:String;
}


