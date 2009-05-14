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
import javafx.animation.Timeline;

/**
 * @author irene
 */

var imgStar: Image;
var imgLogo: Image;
var imgView1: ImageView;
var imgView2 : ImageView;
var imgViewLogo: ImageView;
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
                    imgViewLogo = ImageView{
                        x:150
                        y:100
                        image: bind imgLogo
                        scaleX: bind anim.scal2
                        scaleY: bind anim.scal2
                        opacity: bind anim.opacity
                     },
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
                            lrx: 340 lry: 350
                            llx:  40 lly: 350
                        }
                     },
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
    imgLogo = Image
    {
        url:"{__DIR__}u2ulogo.png";
    }

   
    //rotate animation: stars
    anim.playAnimRotate(3s,imgView1,0,180,20,true);
    //scal animation with blur effect: stars
    anim.playScalWithBlurAnim(0.2,0.15,0.45,1,8,2s,8s,1,Timeline.INDEFINITE);
     //translate animation: text
    anim.playTranslateAnimation(textIntro,50,380,50,-500, 14s,2);
    //scal animation with opacity effect: logoU2U
    anim.playScalWithOpacityAnim(0.1,0.1,1.1,0.1,1,10s,15s,0.0,1);
}