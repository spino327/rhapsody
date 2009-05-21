/*
 * U2UShareScene.fx
 *
 * Created on 18-may-2009, 10:00:26
 */

package org.u2u.gui.scene;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.Group;
import javafx.scene.effect.Lighting;
import javafx.scene.effect.light.DistantLight;
import javafx.scene.effect.Glow;

/**
 * @author sergio
 */

public class U2UShareScene extends U2UAbstractMain{

    var imgBackground:Image;
    var imgBackView:ImageView;


    init {

        imgBackground = Image{
            url:"{__DIR__}resources/config-button.png";
        }

        this.contentPane = Group{
            content: [

                ImageView {
                    effect: Glow{level:0.8}
                    translateX:210;
                    translateY:25;
                    image:imgBackground;
                }
            ]
        };

    }

    override function updateButtons() {

        butShare.aplyPressed = Glow{level:0.7};
        butDown.aplyPressed =  null;
        butSearch.aplyPressed =  null;
      
    }

}
