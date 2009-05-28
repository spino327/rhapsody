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
import org.u2u.filesharing.U2UFileSharingServiceListener;

import org.memefx.popupmenu.*;
import javafx.scene.text.Font;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import org.u2u.data.U2UDownloadNode;
import org.u2u.gui.U2UDownloadNodeRender;


/**
 * @author sergio
 */

public class U2UDownloadScene extends U2UAbstractMain{

    var imgBackground:Image;
    var imgBackView:ImageView;
    var listNodes:U2UList;
    var model:U2UDownloadListModel;
    var vbox:VBox;

    var popupMenu:PopupMenu;


    init {

        imgBackground = Image{
            url:"{__DIR__}resources/content2.png";
        };

        model = U2UDownloadListModel{};

        listNodes = U2UList{
            render: U2UDownloadNodeRender {};

        };

        listNodes.setModel(this.model);

        popupMenu = PopupMenu{

                    font: Font { size: 12, name: "Verdana" };
                    fill: Color.LIGHTGRAY
                    stroke: Color.BLACK
                    opacity: 0.9
                    shadowX: 5, shadowY: 5
                    verticalSpacing: 5
                    highlight: Color.LIGHTGREEN
                    highlightStroke:Color.BLACK

                    content: [
                        MenuItem { text: "Pause Download", callNode: pauseDownload },
                         MenuItem { text: "Restart Download", callNode: restartDownload },
                          MenuItem { text: "Delete Download", callNode: stopDownload }
                    ];
               };

        //bind the popupMenu to the imagBackView
        popupMenu.to(imgBackView);

        this.contentPane = Group{
           content: [
               imgBackView = ImageView{
                    image:imgBackground;
                    translateX:210;
                    translateY:25;
               },
               Group{
                content: bind listNodes
               },

              popupMenu.activateMenus()
           ];
           
        }

        
    }

    function pauseDownload(node:Node):Void{

        var nodeSel:U2UDownloadNode = node as U2UDownloadNode;

        println("pause node");

    }

    function restartDownload(node:Node):Void{

        var nodeSel:U2UDownloadNode = node as U2UDownloadNode;

        println("restart node");

    }

    function stopDownload(node:Node):Void{

        var nodeSel:U2UDownloadNode = node as U2UDownloadNode;

        println("stop node");

    }

    override function updateButtons() {

        butDown.aplyPressed = Glow{level:0.3
        input:DropShadow{offsetX:3 color: Color.BLACK}};
        butShare.aplyPressed = null;
        butSearch.aplyPressed = null;
    }

    /**
    * Store in the list of download files the advertisement of the selected file
    * shellEnv represents the shellenv in the U2UShell context
    */
    public function runDownloadFile(selAdv:U2UContentAdvertisementImpl, shellEnv: String):Boolean{

       // return if it could insert the download file in the model (donwload files' list)
       return model.insertFileIntoModel(selAdv, shellEnv);
    }

    /**
    * Forces to update the list of nodes in the download scene
    */
    public function updateListNodes():Void{

        listNodes.updateUI();
    }

    /**
    * Stops the download of the file
    */
    public function stopDownloadFile():Void{
    }

    /**
    * Return the download listener for receives the events from U2UShell
    */
    public function getDownloadListener():U2UFileSharingServiceListener{

        return this.model;
    }


    
}