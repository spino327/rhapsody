/*
 * ContentStage.fx
 *
 * Created on 16-may-2009, 21:44:51
 */

package org.u2u.gui;

import javafx.stage.Stage;
import javafx.scene.Scene;


/**
 * @author Irene
 */

public class ContentStage extends Stage{


    var shareScene: ShareScene;
    var animScene: AnimationScene = AnimationScene{width:650 height:500};
    var currentScene: Scene = animScene;

    override var scene = bind currentScene;

    public function setCurrentScene(scene:Scene):Void
    {
        this.currentScene = scene;
    }

}
