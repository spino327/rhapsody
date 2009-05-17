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
import javafx.scene.effect.light.*;
import javafx.scene.Group;
import javafx.scene.text.*;
import javafx.animation.Timeline;
import javafx.scene.shape.Rectangle;
import javafx.scene.input.KeyCode.*;

import org.u2u.gui.MainScene;
import org.u2u.gui.Animation;
import javafx.scene.input.KeyEvent;


/**
 * @author irene
 */

public var stageAnim: Stage;
public var scene:Scene;

var imgStar: Image;
var imgLogo: Image;
var imgBac : Image;
var imgView1: ImageView;
var imgView2 : ImageView;
var imgViewLogo: ImageView;
var imgViewBac: ImageView;
var textIntro: Text;
var anim: Animation = Animation {};

var fromx: Number;
var fromy: Number;

function run(args:String[]):Void{

    stageAnim = Stage {
        title: "U2U FX"
        width: 400
        height: 400
        style: StageStyle.TRANSPARENT;

        scene: bind scene = Scene {
            fill: Color.TRANSPARENT;
            content: bind [

                Group {

                    content: [

                        imgViewBac = ImageView{
                            translateX:bind (scene.width - imgViewBac.layoutBounds.width)/2
                            translateY: bind (scene.height - imgViewBac.layoutBounds.height)/2
                            image: bind imgBac;
                            clip: Rectangle{//arcHeight:30 arcWidth:30
                                            width:400//imgViewBac.layoutBounds.width
                                            height:300//imgViewBac.layoutBounds.height}*/
                                            arcHeight:30
                                            arcWidth:30
                                            }

                            scaleX: bind anim.scal2
                            scaleY: bind anim.scal2
                            opacity: bind anim.opacity
                            /*scaleX: bind anim.scal3
                            scaleY: bind anim.scal3
                            opacity: bind anim.opacityWithScal2*/
                            onKeyPressed:function(ke:KeyEvent):Void{

                    
                            }
                            
                        },
                        imgViewLogo = ImageView{
                           
                            translateX:bind (scene.width - imgViewLogo.layoutBounds.width)/2
                            translateY: bind (scene.height - imgViewLogo.layoutBounds.height)/2
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
                            opacity: bind anim.opacityWithScal;

                            font: Font.font("Verdana",50)
                            textAlignment: TextAlignment.CENTER
                            content:"Download\nFiles\nShare\nKnowledge"
                            fill: Color.BLACK
                            effect: PerspectiveTransform {
                                ulx:  120 uly: 30
                                urx: 280 ury: 30
                                lrx: 340 lry: 280
                                llx:  40 lly: 280
                            }
                         },
                         
                     ];
                }
            ]
        }
    }

    initComponents();
}



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
    };

    imgBac = Image{
            url: "{__DIR__}Earth-Horizon.png";
     };

    fromx = (scene.width-textIntro.layoutBounds.width)/2;
    fromy = (scene.height-textIntro.layoutBounds.height)/2;
    //rotate animation: stars
    anim.playAnimRotate(3s,imgView1,0,180,20,true);
    //scal animation with blur effect: stars
    anim.playScalWithBlurAnim(0.2,0.15,0.45,1,6,0.5s,1s,1,Timeline.INDEFINITE);
     //translate animation: text with opacity effect
    anim.playTranslateAnimation(textIntro,fromx,380,fromx,-500, 10s,2);
    //scal animation with opacity effect: logoU2U
    anim.playScalWithOpacityAnim(0.0, 0.1, 1, 0.1, 1, 9.5s, 15s,0.0 , 1);
    //scal and opcaity the background of the application
    //anim.playScalWithOpacityAnim(14s, 19s);
    anim.playScalWithOpacityAnim(0.0, 0.1, 1, 0.1, 1, 14s, 18s,0.0 , 1);



     //anim.playChangeAnim(19s, 21s);
     //scene = MainScene.sceneContent;

}
