/*
 * U2UShareScene.fx
 *
 * Created on 18-may-2009, 10:00:26
 */

package org.u2u.gui.scene;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.Group;
import javafx.scene.effect.Glow;
import javafx.ext.swing.SwingButton;
import javax.swing.JFileChooser;
import javafx.scene.input.MouseEvent;

import javax.swing.*;
import javafx.ext.swing.SwingComponent;
import javafx.scene.text.*;
import javafx.scene.paint.Color;
import org.u2u.common.db.SharedFiles;
/**
 * @author sergio
 */

public class U2UShareScene extends U2UAbstractMain{

    var imgBackground:Image;
    var imgBackView:ImageView;
    var fileChooser:JFileChooser;
    var selectedFile:String;
    var swing:SwingComponent;
    //table that shows the files shared for the user
    var table:ShareFilesTable;

init {
    
        imgBackground = Image{
            url:"{__DIR__}resources/content2.png";
        }

        this.contentPane = Group{
            content: [

                ImageView {
                    //effect: Glow{level:0.8}
                    translateX:210;
                    translateY:25;
                    image:imgBackground;
                },

                table = ShareFilesTable{}

//                
            ]
        };

    }

    function isFileShared(name:String):Boolean{

        var files:ShareFilesTable.SharedFile[] = table.shared as ShareFilesTable.SharedFile[];;
        for(x in [0..sizeof files])
        {
            if(files[x].name.equals(name))
            {
                return true;
            }
        }
        return false;
    }

    override function updateButtons() {

        butShare.aplyPressed = Glow{level:0.7};
        butDown.aplyPressed =  null;
        butSearch.aplyPressed =  null;
      
    }

}
