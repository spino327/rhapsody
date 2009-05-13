/*
 * Animation.fx
 *
 * Created on 11-may-2009, 15:32:45
 */

package org.u2u.gui;

import javafx.animation.transition.RotateTransition;
import javafx.scene.Node;
/**
 * @author sergio
 */

 public class Animation {

     public var node:Node;
     public var repeat:Number;


     var rotTransition = RotateTransition {
         duration: 3s
         node: bind node
         fromAngle:0
         byAngle: 180
         repeatCount:bind repeat
         autoReverse: true
        }

     public function playAnimRotate():Void{
         println("hola");
         rotTransition.play();

         }
 }






