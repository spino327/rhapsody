/*
 * U2U_Stage.fx
 *
 * Created on 18-abr-2009, 12:50:08
 */

package org.u2u.gui;

import javafx.animation.KeyFrame;
import javafx.animation.Interpolator;
import javafx.animation.Timeline;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.paint.*;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.shape.Rectangle;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;

/**
 * @author Irene
 */

var startXVal:Number = 100;
var img =  Image {
            url:"{__DIR__}u2u.png"
          };
var anim = Timeline {
    autoReverse: true
    keyFrames: [
        KeyFrame {
            time: 0s
            values: startXVal => 100
        },
        KeyFrame {
            time: 1s
            values: startXVal => 300 tween Interpolator.LINEAR
        }
    ]

repeatCount: Timeline.INDEFINITE
};

Stage {
    title: "U2U FX";
    width: 600
    height: 400
    resizable:false;

    scene: Scene {
        content:
        [
            Rectangle
            {
                width:600;
                height:400;
                fill: LinearGradient {
                    startX:0.0
                    startY:0.0
                    endX:0.0
                    endY:1.0
                    stops: [
                        Stop { offset: 0.0 color: Color.LIGHTBLUE },
                        Stop { offset: 1.0 color: Color.DARKBLUE }
                    ]
                }
            },
            Text
            {
                font : Font {
                size : 16
                }
                x: 10, y: 30
                content: "hola"
            },
            ImageView
            {
                image: bind img
                translateX:30
                translateY:50
                x: bind startXVal
                onMousePressed:function(me:MouseEvent):Void
                {
                    anim.playFromStart();
                    }
            }

        ]
    }
}