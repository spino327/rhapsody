/*
 * StageAnimation.fx
 *
 * Created on 11-may-2009, 14:20:24
 */

package org.u2u.gui;

import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Scene;
import javafx.scene.paint.*;
import javafx.scene.image.*;
import javafx.scene.effect.*;
import javafx.scene.shape.*;
import javafx.scene.input.*;


import javafx.animation.*;
import javafx.animation.transition.*;
import org.u2u.gui.Animation;
import javafx.scene.Group;

/**
 * @author sergio
 */

var imgStar: Image;
var imgView1: ImageView;
var animRotate: Animation = Animation {
                                node: bind imgView1;
                                repeat: 4.0;
                            };

Stage {
    title: "U2U FX"
    width: 250
    height: 250
    style: StageStyle.TRANSPARENT;

    scene: Scene {
        fill: Color.TRANSPARENT;
        content: [

            Group {
                effect:DropShadow {
                            offsetX: 10
                            offsetY: 10
                            color: Color.BLACK
                            radius: 10
                        }
                content: [
                    imgView1 = ImageView{

                        image: bind imgStar

                    },
                    ImageView{
                        image: bind imgStar
                        scaleX: 0.2
                        scaleY: 0.2
                        x:100
                        y:50

                        },
                    Rectangle {
                        height: 250;
                        width: 400;
                        fill: Color.BLACK;
                        opacity: 0.1;
                        arcHeight: 20;
                        arcWidth: 20;

                    }
                ];

            }

            
        ]
    }
}

initComponents();

/*
    This function initializes the UI components
*/
function initComponents():Void{

    imgStar = Image{
        url:"{__DIR__}star1.png"
        };
    animRotate.playAnimRotate();
}
