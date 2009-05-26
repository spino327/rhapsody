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
import java.util.Timer;
import java.awt.event.ActionListener;
import java.util.TimerTask;
import org.u2u.app.U2UFXApp;

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
var timer:Timer;

var queryTask:ProgressTask = new ProgressTask();

    init {

        imgBackground = Image{
            url:"{__DIR__}resources/content2.png";
        };

        model = U2UDownloadListModel{};

        listNodes = U2UList{};

        listNodes.setModel(this.model);


        timer = new Timer("Progress Query Task");


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

       // shcedule the timer to it does a progress query to the U2UShell every
       // five seconds
       timer.schedule(queryTask,5000);
       // return if it could insert the download file in the model (donwload files' list)
       return model.insertFileIntoModel(selAdv);
       
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

/**
* Class that represents the task to query the progress level of hte downloads
*/
class ProgressTask extends TimerTask{

   override function run(){

       //if(U2UFXApp.APP.getStatusServiceU2UFSS()==U2UFXApp.U2UFSS_INIT)
        {
            /*Se ejecuta el comando u2ufss conActiveDown la opción progress
            *para averiguar el estado de las descargas (este comando genera
            * un evento U2UFileSharingServiceEvent de tipo PROGRESS)*/
            U2UFXApp.APP.shell.executeCmd("u2ufss -progress");

        }
   }

}


