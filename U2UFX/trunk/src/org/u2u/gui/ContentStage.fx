/*
 * ContentStage.fx
 *
 * Created on 16-may-2009, 21:44:51
 */

package org.u2u.gui;

import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;

import org.u2u.gui.scene.*;
import org.u2u.filesharing.U2UContentAdvertisementImpl;
/**
 * @author Irene
 */

public class ContentStage extends Stage {


    //var shareScene: U2UTest;
    var shareScene: U2UShareScene;
    var search: U2USearchScene;
    var downScene:U2UDownloadScene;
    var animScene: U2UIntroAnimation;
    var currentScene: U2UAbstractScene;
    override var scene = bind currentScene;

    init {

        this.resizeStage(650, 500);
        //Show intro
        //this.showIntro();
        this.showShare();
    }


    function showIntro():Void {
        
        animScene = U2UAbstractScene.getU2UIntroAnimation(this);
        
        currentScene = animScene;

    }

    public function showShare():Void {

        /*shareScene = U2UTest {
            
        };*/
        shareScene = U2UShareScene {};
        currentScene = shareScene;
    }

    public function showDownload():Void
    {
        //downScene = U2UDownloadScene{};
        //currentScene = downScene;
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
       var advDown:U2UContentAdvertisementImpl  = this.search.getAdvSelected();

       if(advDown != null)
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
       {}
         
    }

}