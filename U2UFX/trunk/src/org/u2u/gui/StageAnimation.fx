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

/**
 * @author sergio
 */

var imgStar: Image;
var imgView1: ImageView;
//var animRotate: Animation;
var node:Rectangle;

var rotTransition = RotateTransition {
     duration: 4s node: node//imgView1
     byAngle: 180 
     repeatCount:Timeline.INDEFINITE
     autoReverse: true

    }

//animRotate.node = imgView1;

Stage {
    title: "U2U FX"
    width: 250
    height: 250
    //style: StageStyle.TRANSPARENT;

    scene: Scene {
        //fill: Color.TRANSPARENT;
        content: [

            /*Rectangle{
                fill: Color.RED;
                width:250
                height:250
                },*/
            imgView1 = ImageView{

                image: bind imgStar
                //effect: Bloom{}

                onMouseClicked:function(me:MouseEvent):Void{
                    rotTransition.playFromStart();
                    }
                },
            ImageView{
                image: bind imgStar
                scaleX: 0.2
                scaleY: 0.2
                x:100
                y:50
                },

               node = Rectangle {
                x: 100 y: 40
                height: 100 width:  100
                arcHeight: 50 arcWidth: 50
                fill: Color.VIOLET

                onMouseClicked:function(me:MouseEvent):Void{
                    rotTransition.playFromStart();
                    }
            }


        ]
    }
}

initComponents();
rotTransition.play();
/*
    This function initializes the UI components
*/
function initComponents():Void{

    imgStar = Image{
        url:"{__DIR__}star1.png"
        };

    }

function initAnimation():Void{

    //animRotate.playAnimRotate();


     rotTransition.play();


    }