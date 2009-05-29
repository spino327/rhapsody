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
import org.u2u.data.TypeFile;
import org.u2u.gui.U2UDownloadNodeRender;
import org.u2u.app.U2UFXApp;
import javax.swing.JOptionPane;
import java.io.File;


/**
 * @author sergio
 */

public class U2UDownloadScene extends U2UAbstractMain{

    var imgBackground:Image;
    var imgBackView:ImageView;
    /**
    * list of nodes that represents the downloading files
    */
    var listNodes:U2UList;
    var model:U2UDownloadListModel;
    var vbox:VBox;

    var popupMenu:PopupMenu;


    init {

        imgBackground = Image{
            url:"{__DIR__}resources/content2.png";
        };

        imgBackView = ImageView{
                    image:imgBackground;
                    translateX:210;
                    translateY:25;
        };

        model = U2UDownloadListModel{};

        listNodes = U2UList{
            render: U2UDownloadNodeRender {};

        };

        listNodes.setModel(this.model);

        popupMenu = PopupMenu{

            corner:20;
            padding: 8;
            borderWidth: 4;
            opacity: 0.9;
            animate: true
            font: Font { size: 12, name: "Verdana" };
            fill: Color.LIGHTGRAY
            stroke: Color.BLACK
            opacity: 0.9
            shadowX: 5, shadowY: 5
            verticalSpacing: 5
            highlight: Color.LIGHTGREEN
            highlightStroke:Color.BLACK
            translateX:210;
            translateY:15;
            content: [
                MenuItem { text: "Pause Download", call: pauseDownload },
                 MenuItem { text: "Restart Download", call: restartDownload },
                  MenuItem { text: "Delete Download", call: stopDownload },
                   MenuItem { text:"Find Sources", call:findSourcesDownload}

            ];
       };

       //bind the popupMenu to the imagBackView
       popupMenu.to(listNodes);

          

        this.contentPane = Group{

           content: [
                imgBackView ,
                Group{
                    content: bind listNodes
                },

                popupMenu.activateMenus()
           ];
           
        };
    }


    function pauseDownload():Void{

        var selIndex:Integer = listNodes.getSelectedIndex();
        var nodeSel:U2UDownloadNode=  model.getNodeAt(selIndex) as U2UDownloadNode;
        var nameVarEnv:String;

        if(not (nodeSel.equals(null))){
            println("pause node {nodeSel.getName()}");
            nameVarEnv = nodeSel.getShellEnv();
            println("enviroment variable is {nodeSel.getShellEnv()}");

            if(nameVarEnv != null)
            {
                if(nodeSel.status.equals(U2UDownloadNode.DOWNLOAD)){

                    //it must pause download for this file
                    U2UFXApp.APP.shell.executeCmd("u2ufss -pausedownload {nameVarEnv}");
                    //Cambiar el estado de la fila de la descarga pausada
                    nodeSel.setStatus(U2UDownloadNode.PAUSE);
                }
            }
        }
    }

    function restartDownload():Void{


        var selIndex:Integer = listNodes.getSelectedIndex();
        var nodeSel:U2UDownloadNode=  model.getNodeAt(selIndex) as U2UDownloadNode;
        var nameVarEnv:String;

        if(not (nodeSel.equals(null))){
            println("restart node {nodeSel.getName()}");
            nameVarEnv = nodeSel.getShellEnv();
            println("enviroment variable is {nodeSel.getShellEnv()}");

            if(nameVarEnv != null)
            {
                if(nodeSel.status.equals(U2UDownloadNode.DOWNLOAD)){

                    //it must pause download for this file
                    pauseDownload();
                    //it must restart download for this file
                    U2UFXApp.APP.shell.executeCmd("u2ufss -restartdownload {nameVarEnv}");
                    //Cambiar el estado de la fila de la descarga pausada
                    nodeSel.setStatus(U2UDownloadNode.DOWNLOAD);

                }else if(nodeSel.status.equals(U2UDownloadNode.PAUSE)){

                    //it must pause download for this file
                    U2UFXApp.APP.shell.executeCmd("u2ufss -restartdownload {nameVarEnv}");
                    //Cambiar el estado de la fila de la descarga pausada
                    nodeSel.setStatus(U2UDownloadNode.DOWNLOAD);
                }
            }
        }

        println("restart node");

    }

    function stopDownload():Void{

        this.pauseDownload();

        var selIndex:Integer = listNodes.getSelectedIndex();
        var nodeSel:U2UDownloadNode=  model.getNodeAt(selIndex) as U2UDownloadNode;
        var nameVarEnv:String;

        var res:Integer = JOptionPane.showConfirmDialog(null, "Do you want to delete this file?","Delete download file",JOptionPane.OK_CANCEL_OPTION);
        if(res==JOptionPane.OK_OPTION)
        {

            if(not (nodeSel.equals(null))){
                println("delete node {nodeSel.getName()}");
                nameVarEnv= nodeSel.getShellEnv();
                println("enviroment variable is {nodeSel.getShellEnv()}");

                if(nameVarEnv != null)
                {
                    //it must delete download for this file
                    U2UFXApp.APP.shell.executeCmd("u2ufss -stopdownload {nameVarEnv}");
                    println("stop and delete node");
                    model.deleteFileOfModel(selIndex);
                    listNodes.updateUI();
                }
            }
        }
        else
        {
            restartDownload();
        }


    }

    function findSourcesDownload():Void{

        //u2ufss -findsources
        var selIndex:Integer = listNodes.getSelectedIndex();
        var nodeSel:U2UDownloadNode=  model.getNodeAt(selIndex) as U2UDownloadNode;
        var nameVarEnv:String;

        if(not (nodeSel.equals(null))){
            println("find more sources to node {nodeSel.getName()}");
            nameVarEnv = nodeSel.getShellEnv();
            println("enviroment variable is {nodeSel.getShellEnv()}");

            if(nameVarEnv != null)
            {
                if(nodeSel.status.equals(U2UDownloadNode.PAUSE)){

                    //it must restart download for this file
                    U2UFXApp.APP.shell.executeCmd("u2ufss -restartdownload {nameVarEnv}");
                    //Cambiar el estado de la fila de la descarga pausada
                    nodeSel.setStatus(U2UDownloadNode.DOWNLOAD);
                    //it must find more sources for this file
                    U2UFXApp.APP.shell.executeCmd("u2ufss -findsources {nameVarEnv}");

                }else if(nodeSel.status.equals(U2UDownloadNode.DOWNLOAD)){

                    //it must find more sources for this file
                    U2UFXApp.APP.shell.executeCmd("u2ufss -findsources {nameVarEnv}");
                }
            }
        }

        println("find more surces for this node");
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