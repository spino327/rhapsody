/*
 * ContentStage.fx
 *
 * Created on 16-may-2009, 21:44:51
 */

package org.u2u.gui;

import javafx.stage.Stage;

import org.u2u.gui.scene.*;
import org.u2u.app.U2UFXApp;
/**
 * @author Irene
 */

public class U2UContentStage extends Stage {


    //var shareScene: U2UTest;
    var shareScene: U2UShareScene = U2UAbstractScene.getU2UShareScene(this);
    var searchScene: U2USearchScene = U2UAbstractScene.getU2USearchScene(this);
    var downScene:U2UDownloadScene = U2UAbstractScene.getU2UDownloadScene(this);
    var animScene: U2UIntroAnimation = null;
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
        //Show intro
        this.showIntro();
    }

    postinit {
        this.showShare();
    }



    function showIntro():Void {

        currentScene = animScene;

    }

    public function showShare():Void {

        if(currentScene != shareScene) {
            
            currentScene = shareScene;
            (currentScene as U2UAbstractMain).updateButtons();
        }
        
    }

    public function showDownload():Void{
        if(currentScene != downScene ) {

            currentScene = downScene;
            (currentScene as U2UAbstractMain).updateButtons();
        }
    }

    public function showSearch():Void{
        if(currentScene != searchScene){

            currentScene = searchScene;
            (currentScene as U2UAbstractMain).updateButtons();
        }
    }

    function resizeStage(w:Number,h:Number):Void{

        this.width = w;
        this.height = h;
    }

     /**
     * Inicia la descarga de un archivo seleccionado en el panel de busquedas e
     * Inserta una nueva fila de descarga en el panel de descargas
     */
    function downloadFile():Void
    {
       //var advDown:U2UContentAdvertisementImpl  = this.search.getAdvSelected();

       /*if(advDown != null)
       {
//           if(download.executeDownload(advDown))
//           {
//               String name = generateVariableEnv();
//               //Se guarda la referencia de la variable de entorno y el anuncio
//               download.saveVarReference(name, advDown);
//               U2U4UApp.shell.createVarEnvU2UAdvertisement(advDown,name);
//               //se inicia la descarga del archivo por medio del shell U2U
//               U2U4UApp.shell.executeCmd("u2ufss -download "+name);
//               JOptionPane.showMessageDialog(this, "Inicia busqueda de fuentes para descarga...");
//           }
       }
       else
       {}*/
         
    }

    public function registerSearchListener():Void{
        
         //It creates a new enviroment variable that corresponds to the listener
         U2UFXApp.APP.shell.createVarEnvSearchListener(searchScene.getSearchListener());
         //runs the command to register the search listener
         U2UFXApp.APP.shell.executeCmd("u2ufss -register");
         println("SE REGISTRO EL LISTENER PARA BUSQUEDAS");
    }

}