/*
 * Test.fx
 *
 * Created on 17-may-2009, 8:04:45
 */

package org.u2u.app;

import org.u2u.data.U2UDownloadNode;
import javafx.scene.Scene;
import javafx.scene.image.*;
import javafx.scene.effect.PerspectiveTransform;

/**
 * @author sergio
 */

function run(args: String[])
{

    var tmp: U2UDownloadNode = U2UDownloadNode {
        
    };

    Scene {
        content: [

            ImageView {
                image: Image {
                    url: "{__DIR__}video.png";
                }

                effect: PerspectiveTransform {
                    llx: 13.4, lly: 210.0
                    lrx: 186.6, lry: 190.0
                    ulx: 117.4, uly: -10.0
                    urx: 156.6, ury: 10.0
                }


            }

        ];
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
