/*
 * U2UDownloadScene.fx
 *
 * Created on 18-may-2009, 10:51:53
 */

package org.u2u.gui.scene;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.Group;
import javafx.scene.layout.VBox;
import org.u2u.gui.U2UList;
import javafx.scene.paint.Color;
import javafx.scene.effect.Glow;
import javafx.scene.effect.DropShadow;
import org.u2u.data.U2UDownloadListModel;
import org.u2u.filesharing.U2UContentAdvertisementImpl;

/**
 * @author sergio
 */

public class U2UDownloadScene extends U2UAbstractMain{

var imgBackground:Image;
var imgBackView:ImageView;
var listNodes:U2UList;
var model:U2UDownloadListModel;
var vbox:VBox;
var conDown:Integer = 0;

    init {

        imgBackground = Image{
            url:"{__DIR__}resources/content2.png";
        };

        model = U2UDownloadListModel{};

        listNodes = U2UList{};

        listNodes.setModel(this.model);

        this.contentPane = Group{
            content: [
                ImageView{
                image:imgBackground;
                translateX:210;
                translateY:25;

               },
               Group{
                content: bind listNodes
               }
           ];
        }
    }

    override function updateButtons() {
        butDown.aplyPressed = Glow{level:0.3
        input:DropShadow{offsetX:3 color: Color.BLACK}};
        butShare.aplyPressed = null;
        butSearch.aplyPressed = null;
    }

    /**
    * Store in the list of download files the advertisement of the selected file
    */
    public function runDownloadFile(selAdv:U2UContentAdvertisementImpl):Boolean{

       return model.insertFileIntoModel(selAdv);
    }


    /**
    *
    */
    public function updateListNodes():Void{

        listNodes.updateUI();
    }

    /**
    * Stops the download of the file
    */
    public function stopDownloadFile():Void{
    }

    
}


