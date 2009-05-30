/*
 * Animation.fx
 *
 * Created on 11-may-2009, 15:32:45
 */
 /**
 * Copyright (c) 2009, Sergio Pino and Irene Manotas
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of Sergio Pino and Irene Manotas. nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * @author: Sergio Pino and Irene Manotas
 * Website: http://osum.sun.com/profile/sergiopino, http://osum.sun.com/profile/IreneLizeth
 * emails  : spino327@gmail.com - irenelizeth@gmail.com
 * Date   : March, 2009
 * This license is based on the BSD license adopted by the Apache Foundation.
 *
 */

package org.u2u.gui;

import javafx.animation.transition.RotateTransition;
import javafx.animation.transition.TranslateTransition;
import javafx.scene.Node;
import javafx.animation.*;
import javafx.scene.effect.light.*;
import javafx.scene.Scene;

//import org.u2u.gui.MainScene;
/**
 * @author irene
 */

 public class Animation {

    var rotTran = RotateTransition {};
    var timLinScal = Timeline{};
    var timLin = Timeline{};
    var timLinScalOpc = Timeline{};
    var timLinScalOpc2 = Timeline{};
    var transTran = TranslateTransition{};

    public var scal: Float;
    public var scal2: Float;
    public var scal3: Float;
    public var blur: Float;
    public var opacityWithScal: Float;
    public var opacityWithScal2:Float;

    public var opacity: Float;
    public var sceneChanged:Scene;
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

        var dur2=dur;
        var dur3 = dur2 + 5s;
        var dur4= dur3+1s;
        var dur5 = dur4 + 2s;
       //this.playOpacityAnimation(0.0, 0.1, 1, 10s, 15s);
       this.playOpacityAnimation(0.0, 0.5, 1, dur,dur3 ,dur4,dur5);
    }

    /*
        This animation does that a node has a opacity effect
    */
    public function playOpacityAnimation(opac:Float, minOpac:Float, maxOpac:Float,
        time1:Duration, time2:Duration, time3:Duration, time4:Duration):Void
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
                },
            KeyFrame{
                time: time3
                values: this.opacityWithScal => maxOpac
                },
             KeyFrame{
                time: time4
                values: this.opacityWithScal => minOpac
            }
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

public function playScalWithOpacityAnim(time1:Duration, time2:Duration):Void{
        this.scal3 = 0.0;
        this.opacityWithScal2 = 0.0;

        timLinScalOpc2.repeatCount = 1;
        timLinScalOpc2.autoReverse = true;
        timLinScalOpc2.keyFrames = [

            KeyFrame {
                time:time1;
                values: [this.scal3 =>0.1,
                        this.opacityWithScal2 => 0.1
                        ]
            },
            KeyFrame{
                time:time2
                values: [this.scal2 =>1 tween Interpolator.EASEBOTH,
                        this.opacity => 1
                       ]
                }
        ];
        timLinScalOpc2.playFromStart();

    }

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

    public function stopAllAnimations():Void
    {
        this.rotTran.stop();
        this.transTran.stop();
        this.timLin.stop();
        this.timLinScal.stop();
        this.timLinScalOpc.stop();
        this.timLinScalOpc2.stop();
    }

//    public function playChangeAnim(time1:Duration, time2:Duration):Void
//    {
//
//
//        timLinScalOpc2.repeatCount = 1;
//        timLinScalOpc2.autoReverse = true;
//        timLinScalOpc2.keyFrames = [
//
//            KeyFrame {
//                time:time1;
//                values: [this.scal3 =>0.1,
//                        ]
//            },
//            KeyFrame{
//                time:time2
//                values: [this.scal3 =>1 tween Interpolator.EASEBOTH,
//                         this.sceneChanged => MainScene.sceneContent
//                       ]
//                }
//        ];
//        timLinScalOpc2.playFromStart();
//    }


 }



