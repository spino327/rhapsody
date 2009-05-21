/*
 * HelpButtonNode.fx
 *
 * Created on 21-may-2009, 7:32:37
 */

package org.u2u.gui.scene;

import javafx.scene.CustomNode;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;

import javafx.animation.*;
import javafx.scene.text.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.effect.Glow;
import javafx.scene.paint.Color;
import javafx.scene.effect.InnerShadow;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;

/**
 * @author sergio
 */

public class ButtonNode extends CustomNode {


  /**
  * The text that this buttom shows to the user
  */
  protected var title:String;

  /**
   * The Image for this button
   */
  protected var buttonImage:Image;

  /**
   * The URL of the image on the button
   */
  protected var  imageURL:String on replace {
    buttonImage =
      Image {
        url: imageURL
      };
  }

  /**
   * The percent of the original image size to show when mouse isn't
   * rolling over it.
   */
  protected var  scale:Number = 0.9;

  /**
   * The initial opacity of the button when not in a rollover state
   */
  protected var  opacityValue:Number = 0.9;

  /**
   *The initial scale for the text of the button when not in a rollover state
  */
  protected var textScale:Number =0.0;

  /**
   * Efect aplies to this button
  */
  protected var aplyEffect:Effect;
  /**
   * A Timeline to control fading behavior when mouse enters the button
   */
  protected var  fadeTimeline:Timeline =
    Timeline {
      //toggle: true
      autoReverse:true;
      keyFrames: [
        KeyFrame {
          time: 600ms
          values: [
            scale => 1.0 tween Interpolator.LINEAR,
            opacityValue => 1.0 tween Interpolator.LINEAR,
            textScale => 1.0 tween Interpolator.LINEAR
          ]
        }
      ]
    };

 /**
   * A Timeline to control fading behavior when mouse exits the button
   */
  protected var fadeOutTimeline:Timeline =
        Timeline {

            keyFrames: [
                KeyFrame{
                    time:300ms
                    values:[
                        scale => 0.9 tween Interpolator.LINEAR,
                        opacityValue => 0.9 tween Interpolator.LINEAR,
                        textScale => 0.0 tween Interpolator.LINEAR
                    ]
                }
            ]

    };

  /**
   * The action function  that is executed when the
   * the button is pressed
   */
  protected var  action:function():Void;

  //protected var opacityPressed:Number = 1.0;
  protected var aplyPressed:Effect;
  /**
   * Create the Node
   */
  override public function create():Node {
        Group{
          
              effect: bind aplyPressed;
              var textRef:Text;
              var img:ImageView;
              content: [
                Rectangle {
                  width: bind buttonImage.width;
                  height: bind buttonImage.height;
                  opacity: 0.0;
                },
                img=ImageView {
                  image: buttonImage;
                  opacity: bind opacityValue;
                  scaleX: bind scale;
                  scaleY: bind scale;
                  translateX: bind buttonImage.width / 2 - buttonImage.width * scale / 2
                  
                  onMouseEntered:
                    function(me:MouseEvent):Void {
                      fadeTimeline.playFromStart();
                      img.effect = aplyEffect;
                    }
                  onMouseExited:
                    function(me:MouseEvent):Void {
                      fadeTimeline.stop();
                      fadeOutTimeline.playFromStart();
                      me.node.effect = null
                    }
                  onMousePressed:
                    function(me:MouseEvent):Void {
                      me.node.effect = Glow {
                        level: 0.7
                      };
                      
                    }
                  onMouseReleased:
                    function(me:MouseEvent):Void {
                      me.node.effect = null;
                    }
                  onMouseClicked:
                    function(me:MouseEvent):Void {
                      action();
                    }
                },
                textRef = Text {
                  translateX: bind (buttonImage.width / 2) - 16
                  translateY: bind buttonImage.height+12
                  textOrigin: TextOrigin.BOTTOM
                  content: title
                  fill: Color.SNOW
                  scaleX:bind textScale
                  scaleY: bind textScale
                  font:Font.font("Sans serif", FontWeight.BOLD,16);
                },
              ]
            };
        }

 }




