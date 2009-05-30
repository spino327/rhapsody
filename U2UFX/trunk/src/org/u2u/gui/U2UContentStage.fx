/*
 * ContentStage.fx
 *
 * Created on 16-may-2009, 21:44:51
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
package org.u2u.gui;

import javafx.stage.Stage;

import org.u2u.gui.scene.*;
import org.u2u.app.U2UFXApp;
import org.u2u.filesharing.U2UContentAdvertisementImpl;
import org.u2u.filesharing.U2UFileSharingServiceListener;

import javafx.scene.media.*;
import javafx.scene.Scene;
import javafx.scene.text.Text;
import java.io.File;

/**
 * @author Irene
 */

public class U2UContentStage extends Stage {

    //var shareScene: U2UTest;
    var shareScene: U2UShareScene = U2UAbstractScene.getU2UShareScene(this);
    var searchScene: U2USearchScene = U2UAbstractScene.getU2USearchScene(this);
    var downScene:U2UDownloadScene = U2UAbstractScene.getU2UDownloadScene(this);
    var helpScene:U2UHelpScene = U2UAbstractScene.getU2UHelpScene(this);
    var preferScene: U2UPreferencesScene = U2UAbstractScene.getU2UPreferencesScene(this);
    var previewScene: U2UPreviewScene = U2UAbstractScene.getU2UPreviewScene(this);
    var conDown:Integer = 0;

    var currentScene: U2UAbstractScene = null on replace {
        println("cambio la scene: {currentScene.getClass().toString()}");
        this.scene = currentScene;
    };

    override var onClose = function() {
        println("Closing the application");
        U2UFXApp.APP.quit();
    };

    init {
        this.title = "U2U FX";
        this.fullScreen = false;
        this.resizable = false;



    }
    /**
    * Shows the share scene in the stage
    */
    public function showShare():Void {
        if(currentScene != shareScene) {
             currentScene = shareScene;
            (currentScene as U2UAbstractMain).updateButtons();
            U2UFXApp.APP.shell.executeCmd("u2ufss -showsf");
        }
    }

    /**
    * Shows the download scene in the stage
    */
    public function showDownload():Void{
        if(currentScene != downScene ) {
            currentScene = downScene;
            (currentScene as U2UAbstractMain).updateButtons();
        }
    }

    /**
    * Shows the search scene in the stage
    */
    public function showSearch():Void{
        if(currentScene != searchScene){
            currentScene = searchScene;
            (currentScene as U2UAbstractMain).updateButtons();

        }
    }

    /**
    * Shows the help scene in the stage
    */
    public function showHelp():Void{
        //show the U2UStagePdfViewer
        currentScene = helpScene;
       
    }

    /**
    * Shows the preferences scene in the stage
    */
    public function showPreferences():Void{
        currentScene = preferScene;
    }


    /**
    * Shows the help scene in the stage
    */
    public function showPreview():Void{
        //show the U2UStagePdfViewer
        currentScene = previewScene;
        U2UFXApp.APP.shell.executeCmd("u2ufss -showsf");

    }

    

    /**
    * Change the state of the scene: disable or enable
    */
    public function changeStateScene(value:Boolean):Void{

        (currentScene as U2UAbstractMain).active  = value;
    }

    /**
    * Registers the search listener in the U2UShell
    */
    public function registerListeners():Void{

        //	We get the listener to the scene downloads
        var lis:U2UFileSharingServiceListener  = downScene.getDownloadListener();
        var lisShare:U2UFileSharingServiceListener  = shareScene.getShareListener();
        var lisSharePreview:U2UFileSharingServiceListener  = previewScene.getShareListener();

        //We create the environment variable containing the reference objects listener
        U2UFXApp.APP.shell.createVarEnvServiceListener("downlistener", lis);
        //We recorded the dowload listener with the command u2ufss
        U2UFXApp.APP.shell.executeCmd("u2ufss -addlistener downlistener");
        
        //We create the environment variable containing the reference objects listener
        U2UFXApp.APP.shell.createVarEnvServiceListener("sharedlistener", lisShare);
        //We recorded the shared listener with the command u2ufss
        U2UFXApp.APP.shell.executeCmd("u2ufss -addlistener sharedlistener");

        //We create the environment variable containing the reference objects listener
        U2UFXApp.APP.shell.createVarEnvServiceListener("previewlistener", lisSharePreview);
        //We recorded the shared listener with the command u2ufss
        U2UFXApp.APP.shell.executeCmd("u2ufss -addlistener previewlistener");

         //We create the environment variable containing the reference objects listener
        U2UFXApp.APP.shell.createVarEnvSearchListener(searchScene.getSearchListener());
        //runs the command to register the search listener
        U2UFXApp.APP.shell.executeCmd("u2ufss -register");
        println("\nregister for search listener ready");
    }


   public function downloadAFile(adv:U2UContentAdvertisementImpl):Void{
   
        var name:String = generateVariableEnv();
        U2UFXApp.APP.shell.createVarEnvU2UAdvertisement(adv,name);
        println("Download a file: {adv.getName()}");

        var res:Boolean = downScene.runDownloadFile(adv, name);
        
        if(res){
            U2UFXApp.APP.shell.executeCmd("u2ufss -download {name}");
            downScene.updateListNodes();
            this.showDownload();
        }
   }

    /**
     * Gnerates a new variable's name
     * @return a name for a enviroment's variable
     */
    function generateVariableEnv():String
    {
        conDown++;
        var nameVar:String  = "Down0{String.valueOf(conDown)}";
        return nameVar;
    }


}
