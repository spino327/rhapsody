/*
 * ContentStage.fx
 *
 * Created on 16-may-2009, 21:44:51
 */

package org.u2u.gui;

import javafx.stage.Stage;

import org.u2u.gui.scene.*;
import org.u2u.app.U2UFXApp;
import org.u2u.filesharing.U2UContentAdvertisementImpl;
import org.u2u.filesharing.U2UFileSharingServiceListener;

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

        //We create the environment variable containing the reference objects listener
        U2UFXApp.APP.shell.createVarEnvServiceListener("downlistener", lis);
        //We recorded the dowload listener with the command u2ufss
        U2UFXApp.APP.shell.executeCmd("u2ufss -addlistener downlistener");
        
         //We create the environment variable containing the reference objects listener
        U2UFXApp.APP.shell.createVarEnvServiceListener("sharedlistener", lisShare);
        //We recorded the shared listener with the command u2ufss
        U2UFXApp.APP.shell.executeCmd("u2ufss -addlistener sharedlistener");

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
