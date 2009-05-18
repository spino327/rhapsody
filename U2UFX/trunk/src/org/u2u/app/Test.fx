/*
 * Test.fx
 *
 * Created on 17-may-2009, 8:04:45
 */

package org.u2u.app;

import javafx.stage.Stage;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.input.MouseEvent;
import javafx.scene.Scene;
import org.u2u.app.MiScene;

/**
 * @author sergio
 */

function run(args: String[])
{

    MiScene {
        width: 500;
        height: 500;
    }



}


/*var stageScene:MiScene;

var scene1 : MiScene = MiScene {
//    content: [
//        Circle {
//            fill: Color.YELLOW;
//            centerX: 100;
//            centerY: 100;
//            radius: 50;
//
//            onMouseClicked:function(me:MouseEvent) {
//                println("click cir");
//                stageScene = scene2;
//            }
//
//        }
//
//    ];
};

var scene2 : MiScene = MiScene {
//    content: [
//        Rectangle {
//            height: 100;
//            width: 100;
//            x: 50;
//            y: 50;
//            fill: Color.AQUAMARINE;
//
//            onMouseClicked:function(me:MouseEvent) {
//                println("click rec");
//                stageScene = scene1;
//            }
//        }
//
//    ];
};

stageScene = scene1;


Stage {
    title:"Hola nene, te amo";
    scene: bind stageScene;
}*/
