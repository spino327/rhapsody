/*
 * ContentStage.fx
 *
 * Created on 16-may-2009, 21:44:51
 */

package org.u2u.gui;

import javafx.stage.Stage;
import javafx.scene.Scene;
import org.u2u.gui.scene.*;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;


/**
 * @author Irene
 */

public class ContentStage extends Stage {


    var shareScene: U2UTest;
    var animScene: U2UIntroAnimation;
    var currentScene: U2UAbstractScene;
    override var scene = bind currentScene;

    init {

        //Show intro 
        this.showIntro();
    }


    function showIntro():Void {
        
        animScene = U2UAbstractScene.getU2UIntroAnimation(this);

        currentScene = animScene;

    }

    public function showShare():Void {

        shareScene = U2UTest {
            
        };

        currentScene = shareScene;
    }


}