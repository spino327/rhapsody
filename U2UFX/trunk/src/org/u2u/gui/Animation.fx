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

 var i = bind node on replace {
     println("Cambio a : {i}")
     }

 var rotTransition = RotateTransition {
     duration: 1m node: node
     byAngle: 180 repeatCount:4 autoReverse: true
    }

 public function playAnimRotate():Void{
     println("hola");
     rotTransition.play();

     }
 }






