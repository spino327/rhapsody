/*
 * U2U_Stage1.fx
 *
 * Created on 05-may-2009, 13:33:07
 */

package org.u2u.gui;

import javafx.animation.transition.*;
import javafx.scene.shape.*;
import javafx.animation.Interpolator;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.paint.*;
import javafx.scene.text.*;
import javafx.scene.image.*;
import javafx.scene.effect.*;
import javafx.scene.effect.light.*;
import javafx.scene.input.*;
import javafx.ext.swing.*;

var text1: Text;
var text2: Text;
var text3: Text;
var text4: Text;
var textU: Text;
var texto:Text;

var button1:SwingButton;
var img =  Image {
url:"{__DIR__}u2ulogo.png"
};
var img2 =  Image {
url:"{__DIR__}uislogo.jpeg"
};
var imgView: ImageView;
var imgV2: ImageView;

var path = Path {
    elements: [
        MoveTo{
            x:0
            y:150
        },
        HLineTo{
            x:450
           }
    ]
};
var path2 = Path {
    elements: [
        MoveTo{
            x:170
            y:280
        },
        CubicCurveTo {
            controlX1: 260  controlY1: 360
            controlX2: 260  controlY2: 370
                    x: 430  y:  310
        }
    ]
};

var anim = PathTransition{
    duration:1.5s
    node: bind imgView
    path: AnimationPath.createFromPath(path)
    orientation: OrientationType.ORTHOGONAL_TO_TANGENT
    interpolate: Interpolator.EASEBOTH
    autoReverse: true
    repeatCount: 0.62//1.39//Timeline.INDEFINITE
}
var anim2 = PathTransition{
    duration:1.5s
    node: bind  imgV2
    path: AnimationPath.createFromPath(path2)
    orientation: OrientationType.ORTHOGONAL_TO_TANGENT
    interpolate: Interpolator.EASEBOTH
    autoReverse: true
    repeatCount: 1.5//Timeline.INDEFINITE
}
var sceneU2U:Scene;

Stage {
    title: "U2U FX";
    width: 580
    height: 420
    resizable:false;

    scene:sceneU2U =  Scene {
        content:
        [
            Rectangle
            {
                width:580;
                height:420;
                fill: LinearGradient {
                    startX:0.0
                    startY:0.0
                    endX:0.0
                    endY:1.0
                    stops: [
                        Stop { offset: 0.0 color: Color.DARKGRAY },
                        Stop { offset: 1.0 color: Color.DIMGREY }
                    ]
                }
            },
            text1 = Text
            {
                translateX:15
                translateY: 280
                font: Font.font("Verdana",25)
                fill: Color.WHEAT
                content: "Share"
                opacity:0.8
                rotate:30
                effect: MotionBlur { radius: 5 angle: -10 }
            },
            text2 = Text
            {
                translateX:55
                translateY: 150
                font: Font.font("Verdana",25)
                fill: Color.GOLD
                content: "Download"
                opacity:0.5
                rotate:6
                effect: MotionBlur { radius: 5 angle: -10 }
            },
             text3 =Text
            {
                translateX:425
                translateY: 100
                font: Font.font("Verdana",25)
                fill: Color.BLUEVIOLET
                content: "knowledge"
                opacity:0.8
                rotate:-10
                effect: MotionBlur { radius: 5 angle: -10 }
            },
             text4 =Text
            {
                translateX:460
                translateY: 300
                font: Font.font("Verdana",25)
                fill: Color.BLUE
                content: "Files"
                opacity:0.8
                rotate:285
                effect: MotionBlur { radius: 5 angle: -10 }
            },
             textU =Text
            {
                fill:Color.WHITE
                font : Font.font("Verdana",FontWeight.BOLD,12.5,)
                x: 10, y: 30
                translateX:150
                translateY:250
                textAlignment: TextAlignment.CENTER
                content: "Universidad Industrial de Santander\n"
                "Bucaramanga, Colombia\n"
                "2009"
                onMouseClicked:function(me:MouseEvent):Void
                {
                    
                    }
            },
            texto = Text{
                translateX:240
                translateY: 210
                font: Font.font("Verdana",FontWeight.BOLD,20)
                fill: Color.WHITE
                content: "Press any key for continue"
                opacity:0.2
                onKeyPressed:function(ke:KeyEvent):Void
                {
                    clearStage();
                    putAllElements();
                }
            },
            imgView = ImageView
            {
                image: bind img
                translateY:150
                effect: DropShadow { offsetY: 3 color: Color.color(0.4, 0.4, 0.4) };
                effect: InnerShadow { offsetX: 4 offsetY: 4 }
            },
            imgV2 = ImageView
            {
                image: bind img2
                scaleX:0.5
                scaleY:0.5
                translateX:90
                translateY:250
                visible:false
            },
        ]
    }
}
var stage2: Stage;

function startAnim():Void
{
    anim2.playFromStart();
    anim.pause();
    anim.playFromStart();

}

function clearStage():Void
{
    text1.visible=false;
    text2.visible=false;
    text3.visible=false;
    text4.visible=false;
    textU.visible=false;
    texto.visible=false;
    imgView.visible=false;
    imgV2.visible=false;
}

function putAllElements():Void
{
    insert button1 = SwingButton
            {
                font: Font.font("Verdana", FontWeight.BOLD,13)
                foreground: Color.BLACK

                text: "Share a File"
                visible:true

                onMouseMoved:function(me:MouseEvent):Void
                {
                    button1.effect=  Lighting {
                        light: DistantLight { azimuth: -15 elevation: 40
                                            color:Color.HONEYDEW}
                        //surfaceScale: 5
                    }
                }
                onMouseExited:function(me:MouseEvent):Void
                {
                    button1.effect=  Lighting {
                        light: DistantLight { azimuth: 5 elevation: 55 }
                        //surfaceScale: 5
                    }
                }
             }
     into sceneU2U.content;
}

imgV2.visible = true;
startAnim();
