
package animation;

import javafx.stage.*;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.animation.transition.FadeTransition;
import javafx.animation.transition.RotateTransition;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.Interpolator;
import javafx.scene.input.KeyEvent;
import javafx.scene.Node;
import javafx.scene.Group;

/**
 * @author sergio
 */
public class IntroAnimation extends AbstractScene {

    
    var campus:Image;
    var mainGroup:Node;
    var introText:Text;
    var tittle:Text;
    var image:ImageView;
    var scale:Float;
    var transition:FadeTransition;
    var transitionCreators:FadeTransition;
    var timeline:Timeline;

    var dur:Duration = bind transition.time on replace{
        println("time changed to :{dur}");
        if(dur == transition.duration)
        {
            //transitionCreators.play();
            timeline.play();
        }
    }

    init {
            this.fill = Color.TRANSPARENT;

            campus = Image{
                url:"{__DIR__}resources/campusCol.jpg"
            };
            transition = FadeTransition {
                duration: 1000ms node: bind introText
                fromValue: 0.0 toValue: 1.0
                repeatCount:1 autoReverse: true

            };
            transitionCreators = FadeTransition{
                duration: 1000ms node: bind tittle
                fromValue: 0.0 toValue: 1.0
                repeatCount:3 autoReverse: true
            }

            timeline  = Timeline{
                autoReverse:true;
                repeatCount:2;
                keyFrames: [
                    KeyFrame{
                        time:1s;
                        values:[ scale=>1.5 ]
                    },
                    KeyFrame{
                        time:10s;
                        values:[ scale=>1.0 tween Interpolator.LINEAR ]
                        action:function() {
                            this.playStage.next();
                        }

                    },
                ]
            }

            content = Group {

                translateX: 100;
                translateY: 100;

                content: [

                    image = ImageView{
                        image:  campus;
                        clip: Circle{
                                centerX:162.5;
                                centerY:141;
                                radius:135


                              }
                        effect: DropShadow { offsetY: 10 color: Color.BLACK};

                    },
                    introText = Text{
                        content:"Presents";
                        font: Font.font("Verdana",FontWeight.BOLD,20);
                        translateX:140;
                        translateY:185;

                        onMouseClicked:function(me:MouseEvent){
                           //
                        }
                    },

                    tittle = Text{

                        content: "The book of the Blue tag\n\n"
                                 "By Hoover Rueda"
                        fill: Color.BLUE;
                        font: Font.font("Verdana",14)
                        textAlignment: TextAlignment.CENTER;
                        translateX:70;
                        translateY:210;
                        scaleX: bind scale;
                        scaleY: bind scale;
                    },

//                    Text{
//
//                        content: "Press any key to continue"
//                        fill: Color.BLUEVIOLET;
//                        font: Font.font("Verdana",12)
//                        textAlignment: TextAlignment.CENTER;
//                        translateX:70;
//                        translateY:250;
//                    }
                ];

//                onKeyPressed:function(ke:KeyEvent)
//                {
//                    //force the change of the stage
//                    println("onKeyPressed executed");
//
//                    this.playStage.next();
//                }

                
            }//End Group
       }//End init

//       postinit{
//
//
//
//       }

       override function start() {
           transition.play();
       }


}
