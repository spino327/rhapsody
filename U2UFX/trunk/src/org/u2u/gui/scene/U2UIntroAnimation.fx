/*
 * U2UIntroAnimation.fx
 *
 * Created on 17-may-2009, 16:32:31
 */

package org.u2u.gui.scene;

import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseEvent;
import org.u2u.gui.*;
import javafx.scene.image.*;
import javafx.scene.text.*;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.KeyEvent;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.PerspectiveTransform;

/**
 * @author sergio
 */
public class U2UIntroAnimation extends U2UAbstractScene {

    var anim:Animation;
    var imgBac:Image;
    var imgLogo:Image;
    var imgStar:Image;
    var imgViewBac:ImageView;
    var imgViewLogo:ImageView;
    var imgView1:ImageView;
    var imgView2:ImageView;
    var textIntro:Text;
    var fromx: Number;
    var fromy: Number;

    init {

        anim = Animation{};
        fill = Color.TRANSPARENT;

        content = [
            imgViewBac = ImageView {
                translateX:bind (this.width - imgViewBac.layoutBounds.width)/2
                translateY: bind (this.height - imgViewBac.layoutBounds.height)/2
                image: bind imgBac
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
                effect: InnerShadow { offsetX: 3 offsetY: 3 color: Color.WHITE }

                onKeyPressed:function(ke:KeyEvent)
                {
                    //force the change of the stage
                    println("Estamos en onKeyPressed");
                    this.contentStage.showShare();
                }
            },
            imgViewLogo = ImageView{

                translateX:bind (this.width - imgViewLogo.layoutBounds.width)/2
                translateY: bind (this.height - imgViewLogo.layoutBounds.height)/2
                image: bind imgLogo
                scaleX: bind anim.scal2
                scaleY: bind anim.scal2
                opacity: bind anim.opacity
            },
            imgView1 = ImageView{

                image: bind imgStar;
                scaleX: bind anim.scal;
                scaleY: bind anim.scal;

                effect: GaussianBlur{
                   radius:bind anim.blur;
                };
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

                font: Font.font("Verdana",50);
                textAlignment: TextAlignment.CENTER;
                content:"Download\nFiles\nShare\nKnowledge"
                fill: Color.BLACK
                effect: PerspectiveTransform {
                    ulx:  120 uly: 30
                    urx: 280 ury: 30
                    lrx: 340 lry: 280
                    llx:  40 lly: 280
                }
             }

        ];

        }

    /*public function initComponents():Void{*/

    postinit {

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

        fromx = (this.width-textIntro.layoutBounds.width)/2;
        fromy = (this.height-textIntro.layoutBounds.height)/2;

        //rotate animation: stars
        anim.playAnimRotate(3s,imgView1,0,180,20,true);
        //scal animation with blur effect: stars
        anim.playScalWithBlurAnim(0.2,0.10,0.2,1,6,1s,2.5s,1,10);

         //translate animation: text with opacity effect
        anim.playTranslateAnimation(textIntro,fromx,380,fromx,-500, 6s,1);
        //scal animation with opacity effect: logoU2U
        anim.playScalWithOpacityAnim(0.0, 0.3, 1, 0.3, 1, 7s, 12s,0.0 , 1);
        //scal and opcaity the background of the application
        //anim.playScalWithOpacityAnim(14s, 19s);
        anim.playScalWithOpacityAnim(0.0, 0.1, 1, 0.1, 1, 13s, 14.5s,0.0, 1);

    }
}
