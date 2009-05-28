/*
 * ShareFilesTable.fx
 *
 * Created on 21-may-2009, 21:18:42
 */

package org.u2u.gui.scene;

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


/**
 * @author Irene
 */
public class U2UShareFilesTable extends CustomNode, U2UFileSharingServiceListener {
    public var selection: Integer;
    public var shared:SharedFile[];
    public var inputDialog:JFXDialog;

    override function create():Node{
        Group{
            content: [
                Text{
                    translateX:350;
                    translateY:70;
                    content:"SHARE FILES";
                    font: Font.font("Arial",FontWeight.BOLD,20);
                },
                SwingTable{
                    width:380;
                    height:260;
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
                    
                    translateX: 385;
                    translateY: 410;
                    image: Image{url:"{__DIR__}resources/add.png"}
                    onMouseClicked:function(me:MouseEvent):Void{
                        //insert file into the fileshares
                        insertFile();
                    }
                    onMouseMoved:function(me:MouseEvent):Void{
                        me.node.effect = Glow{level:0.5}
                    }
                    onMouseExited:function(me:MouseEvent):Void{
                        me.node.effect = null;
                    }
                },

                ImageView{
                    var fileChooser:JFileChooser;
                    var swing:SwingComponent;
                    var selectedFile:File;
                    translateX: 430;
                    translateY: 420;
                    image: Image{url:"{__DIR__}resources/remove.png"}
                    onMouseClicked:function(me:MouseEvent):Void{

                       delete shared[selection];

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

    /**
     * This function does the ckeckups necesary for insert the file as shared file
    */
    function insertFile():Void{

        var fileChooser:JFileChooser;
        var swing:SwingComponent;
        var selectedFile:File;
        var desc:String;

        fileChooser = new JFileChooser();

        if ( fileChooser.showOpenDialog(swing.getRootJComponent()) == JFileChooser.APPROVE_OPTION){
            selectedFile = fileChooser.getSelectedFile();

            var resInput :String = JOptionPane.showInputDialog(null,
                "Insert a description to a shared file:", "Description",
                    JOptionPane.INFORMATION_MESSAGE);

            var res:Boolean = this.isFileShared(selectedFile.getName());

            if(not res){
                var sharedFile:SharedFile =
                SharedFile{
                           name:selectedFile.getName();
                           path: selectedFile.getPath();
                           type: selectedFile.getName();
                }
                //inform to the U2UFSS that it must share the file
                if(desc.equals("") or desc == null or desc.equals(" ") or desc.startsWith(" ")) {
                    U2UFXApp.APP.shell.executeCmd("u2ufss -share -p {  selectedFile.toURI().getPath()}");
                }
                else {
                    U2UFXApp.APP.shell.executeCmd("u2ufss -share -p { selectedFile.toURI().getPath() } -d { desc }");
                }
                //Insert the file to the list of shared files
                insert sharedFile into shared;
             }
            else{
                //show a dialog that says that the file is already sahred
                JOptionPane.showMessageDialog(null, "The file is already being shared in the P2P network!");
            }
        }
    }

    /**
    * This function verifies if the file to insert is already inserted
    */
    function isFileShared(name:String):Boolean{

        var files:U2UShareFilesTable.SharedFile[] = shared;
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

