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
import org.u2u.gui.Animation;
import javafx.scene.Group;
import javafx.scene.text.*;
import javafx.animation.transition.Transition;
import javafx.animation.Timeline;

/**
 * @author sergio
 */

var imgStar: Image;
var imgView1: ImageView;
var imgView2 : ImageView;
var textIntro: Text;
var anim: Animation = Animation {};



Stage {
    title: "U2U FX"
    width: 400
    height: 400
    style: StageStyle.TRANSPARENT;

    scene: Scene {
        fill: Color.TRANSPARENT;
        content: [

            Group {
                
                content: [
                    imgView1 = ImageView{

                        image: bind imgStar
                        scaleX: bind anim.scal;
                        scaleY: bind anim.scal;

                        effect: GaussianBlur{
                           radius:bind anim.blur
                            }
                    },
                    imgView2 = ImageView{
                        image: bind imgStar
                        scaleX: bind anim.scal;
                        scaleY: bind anim.scal;
                        x:200
                        y:70
                     },

                     textIntro = Text{
                        opacity: bind anim.opacity;
                        x:150
                        y:250
                        font: Font.font("Verdana",50)
                        textAlignment: TextAlignment.CENTER
                        content:"Download\nFiles\nShare\nKnowledge"
                        fill: Color.BLACK

                        effect: PerspectiveTransform {
                            ulx:  80 uly: 30
                            urx: 320 ury: 30
                            lrx: 340 lry: 380
                            llx:  40 lly: 380
                        }

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

    anim.playAnimRotate(1s,imgView1,0,180,5,true);
    anim.playScalAnim(0.3,0.15,0.5,0s,800ms,3,Timeline.INDEFINITE );
    anim.playOpacityAnimation(0.1, 0.1,0.5);
    anim.playTranslateAnimation(textIntro,-800, 9s);

}
