/*
 * U2UFXApp.fx
 *
 * Created on 16-may-2009, 22:22:50
 */

package org.u2u.app;

import org.u2u.gui.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Scene;
import javafx.animation.Timeline;
/**
 * @author Irene
 */
var animScene:AnimationScene;
public var shareScene:ShareScene;
public var stage:ContentStage;

function run(args:String[])
{
    shareScene = ShareScene{width:650 height:500};
    /*var share: ShareScene = ShareScene{width:400 height:500}
    animScene = AnimationScene{width:600 height:550};
     // var anim:Animation=animScene.anim;
    Stage {
        style:StageStyle.TRANSPARENT;
          
            scene: bind animScene;
    }*/
   stage = ContentStage{style:StageStyle.TRANSPARENT;};
   
}

public function changeMainScene()
{
    stage.setCurrentScene(shareScene);
}
