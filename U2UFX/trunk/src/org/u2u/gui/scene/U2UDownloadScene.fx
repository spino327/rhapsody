/*
 * U2UDownloadScene.fx
 *
 * Created on 18-may-2009, 10:51:53
 */

package org.u2u.gui.scene;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.Group;
import javafx.scene.layout.VBox;
import org.u2u.gui.U2UList;

/**
 * @author sergio
 */

public class U2UDownloadScene extends U2UAbstractMain{

var imgBackground:Image;
var imgBackView:ImageView;
var listNodes:U2UList;
var vbox:VBox;


    init {

        imgBackground = Image{
            url:"{__DIR__}content.png";
        };
        listNodes = U2UList{};

        this.contentPane = Group{
            content: [
                ImageView{
                image:imgBackground;
                translateX:210;
                translateY:25;

               },
               listNodes
           ];
        }

    }

    override function updateButtons() {
        imgDownView.opacity = 0.5;

        imgSearchView.opacity = 1;
        imgShareView.opacity = 1;
    }

}
