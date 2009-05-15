/*
 * MainScene.fx
 *
 * Created on 13-may-2009, 13:32:08
 */

package org.u2u.gui;


import javafx.stage.*;
import javafx.scene.Scene;
import javafx.scene.shape.Rectangle;
import javafx.scene.image.*;
import org.u2u.gui.StageAnimation;
import javafx.scene.effect.Reflection;

/**
 * @author Irene
 */

var stage: Stage;

/*stage = StageAnimation.stage;
stage.visible = false;*/
var imgViewBac:ImageView;
var imgViewCon: ImageView;
var imgBac:Image;
var imgCon:Image;

stage = Stage   {

    style:StageStyle.TRANSPARENT;
    width:650;
    height:500;

    scene: Scene {
           content: [

                imgViewBac = ImageView{
                    image: bind imgBac;
                    scaleY:1.6

                },
                imgViewCon = ImageView{
                    translateX:210
                    translateY:30
                    image: bind imgCon;
                },

           ]
        }
    }

    imgBac = Image{
            url: "{__DIR__}Earth-Horizon.png";
        }
     imgCon = Image{
            url: "{__DIR__}content.png";
        }
