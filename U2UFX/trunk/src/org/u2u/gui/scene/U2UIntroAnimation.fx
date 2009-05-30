/*
 * U2UIntroAnimation.fx
 *
 * Created on 17-may-2009, 16:32:31
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
package org.u2u.gui.scene;

import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.image.*;
import javafx.scene.text.*;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.KeyEvent;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.PerspectiveTransform;
import javafx.scene.Group;
import javafx.stage.*;

import org.u2u.gui.*;
import java.lang.System;

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
    var textKey:Text;
    var fromx: Number;
    var fromy: Number;
    public-init var initStage: Stage;

    init {

        //contentStage.style = StageStyle.TRANSPARENT;
        anim = Animation{};
        fill = Color.TRANSPARENT;
        
        content = [
            Group{
                cache: true;
                content: [

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

                },
                textKey = Text{
                    content:"Press any key to continue..."
                    translateX:bind ((this.width/2)-100);
                    translateY: bind (this.height - imgViewBac.layoutBounds.height/2);
                    font: Font.font("Verdana",FontWeight.BOLD,15);
                    fill: Color.SNOW;
                   
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

                onKeyPressed:function(ke:KeyEvent)
                {
                    //force the change of the stage
                    println("Estamos en onKeyPressed");
                    this.initStage.visible = false;
                    this.contentStage.showShare();
                    this.contentStage.visible = true;

                    System.gc();
                }
            }
        ];

        }

    /*public function initComponents():Void{*/

    postinit {

        imgStar = Image{
            url:"{__DIR__}resources/star1.png"
            };
        imgLogo = Image
        {
            url:"{__DIR__}resources/u2ulogo.png";
        };

        imgBac = Image{
                url: "{__DIR__}resources/Earth-Horizon.png";
         };

        fromx = (this.width-textIntro.layoutBounds.width)/2;
        fromy = (this.height-textIntro.layoutBounds.height)/2;

        //rotate animation: stars
        anim.playAnimRotate(3s,imgView1,0,180,20,true);
        //scal animation with blur effect: stars
        anim.playScalWithBlurAnim(0.2,0.10,0.2,1,6,1s,2.5s,1,10);

         //translate animation: text with opacity effect
        anim.playTranslateAnimation(textIntro,fromx,380,fromx,-500,7s,1);
        //scal animation with opacity effect: logoU2U
        anim.playScalWithOpacityAnim(0.5, 0.5, 1, 0.4, 1, 5s, 6.5s,0.0 , 1);
        //scal and opcaity the background of the application
        //anim.playScalWithOpacityAnim(14s, 19s);
        anim.playScalWithOpacityAnim(0.0, 0.1, 1, 0.1, 1, 6.5s, 7s,0.0, 1);

    }
}
