/*
 * MainScene.fx
 *
 * Created on 13-may-2009, 13:32:08
 */

package org.u2u.gui;

import javafx.stage.Stage;
import javafx.stage.*;
import javafx.scene.Scene;
import javafx.scene.shape.Rectangle;
import javafx.scene.image.*;
import org.u2u.gui.StageAnimation;
/**
 * @author Irene
 */

var stage: Stage;

stage = StageAnimation.stage;
stage.visible = false;
var imgView:ImageView;
var img:Image;

    Stage   {

    style:StageStyle.TRANSPARENT;
    width:650;
    height:500;

    scene: Scene {
           content: [

                imgView = ImageView{
                    image:bind img;
                },
                Rectangle
                {
                    clip: bind imgView;
                    width:500
                    height:600
                    smooth: true
                }
           ]
        }
    }

    img = Image{
            url: "{__DIR__}metal3.png";
        }
