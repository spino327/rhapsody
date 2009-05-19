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
/**
 * @author Irene
 */

public class ContentStage extends Stage {


    //var shareScene: U2UTest;
    var shareScene: U2UShareScene;
    var downScene:U2UDownloadScene;
    var animScene: U2UIntroAnimation;
    var currentScene: U2UAbstractScene;
    override var scene = bind currentScene;

    init {

        this.resizeStage(650, 500);
        //Show intro
        this.showIntro();
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
        downScene = U2UDownloadScene{};   
        currentScene = downScene;
    }

    function resizeStage(w:Number,h:Number):Void{

        this.width = w;
        this.height = h;
    }


}