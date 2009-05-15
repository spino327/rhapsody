/*
 * Animation.fx
 *
 * Created on 11-may-2009, 15:32:45
 */

package org.u2u.gui;

import javafx.animation.transition.RotateTransition;
import javafx.animation.transition.TranslateTransition;
import javafx.scene.Node;
import javafx.animation.*;
import javafx.scene.effect.light.*;

import org.u2u.gui.StageAnimation;
/**
 * @author irene
 */

 public class Animation {

    var rotTran = RotateTransition {};
    var timLinScal = Timeline{};
    var timLin = Timeline{};
    var timLinScalOpc = Timeline{};
    var transTran = TranslateTransition{};

    public var scal: Float;
    public var scal2: Float;
    public var blur: Float;
    public var opacityWithScal: Float;
    public var opacity: Float;
    /*
        This function animates a node and to make that node rotates
    */
    public function playAnimRotate(dur:Duration, node:Node, fromAng:Float,
        byAng:Float,repC:Float, rev: Boolean ):Void{
             
             rotTran.duration = dur;
             rotTran.node = node;
             rotTran.fromAngle = fromAng;
             rotTran.byAngle= byAng;
             rotTran.repeatCount = repC;
             rotTran.autoReverse = rev;
             //init the transition
             rotTran.play();
    }

    /*
        This animation does that a node scales its dimentions through binding to scal variable
        with a blur effect
    **/
    public function playScalWithBlurAnim(scalNode:Float,minScal:Float, maxScal:Float,
        minBlur:Number,maxBlur:Number,time1:Duration, time2:Duration,blurNode:Float,repC: Integer):Void
    {
        this.scal = scalNode;
        this.blur = blurNode;
        
        timLinScal.repeatCount = repC;
        timLinScal.autoReverse = true;
        timLinScal.keyFrames = [

            KeyFrame {
                time:time1;
                values: [this.scal =>minScal,
                        this.blur => minBlur
                        ]
            },
            KeyFrame{
                time:time2
                values: [this.scal =>maxScal tween Interpolator.LINEAR,
                        this.blur => maxBlur
                       ]
                },
        ];

        timLinScal.playFromStart();
    }

 /*
        This animation does a translate transition
    */
    public function playTranslateAnimation(node: Node, fromX:Float, fromY: Float,
        toX:Float, toY:Float, dur:Duration, repC:Integer):Void
    {
        transTran.node = node;
        transTran.fromX= fromX;
        transTran.fromY = fromY;
        transTran.toX = toX;
        transTran.toY = toY;

        transTran.duration = dur;
        transTran.repeatCount = repC;
        transTran.interpolate = Interpolator.LINEAR;

        transTran.playFromStart();

       //this.playOpacityAnimation(0.0, 0.1, 1, 10s, 15s);
    }


/*
        This animation does that a node has a opacity effect
    */
    public function playOpacityAnimation(opac:Float, minOpac:Float, maxOpac:Float,
        time1:Duration, time2:Duration):Void
    {
        this.opacityWithScal = opac;

        var keys:KeyFrame[] = [
            KeyFrame{
                time: time1
                values: this.opacityWithScal => minOpac
                },
            KeyFrame{
                time: time2
                values: this.opacityWithScal => maxOpac
                }/*,
            KeyFrame{
                time: time3
                values: this.opacityWithScal => maxOpac
                },
             KeyFrame{
                time: time4
                values: this.opacityWithScal => minOp
            },*/
        ];

        timLin.keyFrames = keys;
        timLin.repeatCount =1;
        timLin.autoReverse = true;
        timLin.playFromStart();

    }
    /*
        This animation does that a node scales its dimentions through binding to scal variable
        with opacity effect
    **/
    public function playScalWithOpacityAnim(scalNode:Float,minScal:Float, maxScal:Float,
        minOpac:Number,maxOpac:Number,time1:Duration, time2:Duration,initOpac:Float,repC: Integer):Void
    {
        this.scal2 = scalNode;
        this.opacity = initOpac;

        timLinScalOpc.repeatCount = repC;
        timLinScalOpc.autoReverse = true;
        timLinScalOpc.keyFrames = [

            KeyFrame {
                time:time1;
                values: [this.scal2 =>minScal,
                        this.opacity => minOpac
                        ]
            },
            KeyFrame{
                time:time2
                values: [this.scal2 =>maxScal tween Interpolator.EASEBOTH,
                        this.opacity => maxOpac
                       ]
                }
        ];
        timLinScalOpc.playFromStart();

        /*this.scal2 = scalNode;
        this.opacityWithScal = initOpac;

        timLinScal.repeatCount = repC;
        timLinScal.autoReverse = true;
        timLinScal.keyFrames = [

            KeyFrame {
                time:time1;
                values: [this.scal2 =>minScal,
                        this.opacityWithScal => minOpac
                        ]
            },
            KeyFrame{
                time:time2
                values: [this.scal2 =>maxScal tween Interpolator.EASEBOTH,
                        this.opacityWithScal => maxOpac
                       ]
                }
        ];
        timLinScal.play();*/
    }


    
   
    /*
        Stops the animation with scale and blur effects
    */
    /*public function stopScalWithOpacity():Void
    {
        if(timLinScal.currentRate==10)
            timLinScal.stop();
    }
    */
    /*
    public function stopTranslateAnimation():Void
    {
        transTran.stop();
        var i = bind transTran.currentRate on replace
        {   println("El nuevo valor de la transiscion es: {i}");
            if(transTran.currentRate==3)
            {transTran.stop();}
        };
    }
    */

    /*
    This function animates a node: scale x and y axis of node
    */
     public function playScalAnim(scalNode:Float,minScal:Float, maxScal:Float,
        time1:Duration, time2:Duration,repC: Integer):Void
    {
        this.scal2 = scalNode;
        //this.scal = scalNode;

        timLinScal.repeatCount = repC;
        timLinScal.autoReverse = true;
        timLinScal.keyFrames = [

            KeyFrame {
                time:time1;
                values: [this.scal2 =>minScal]
            },
            KeyFrame{
                time:time2
                values: [this.scal2 =>maxScal tween Interpolator.EASEBOTH ]
                }
        ];
        timLinScal.play();
    }




 }



