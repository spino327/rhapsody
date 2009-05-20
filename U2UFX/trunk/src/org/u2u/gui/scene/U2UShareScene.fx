/*
 * U2UShareScene.fx
 *
 * Created on 18-may-2009, 10:00:26
 */

package org.u2u.gui.scene;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.u2u.data.U2UDownloadListModel;
import javafx.scene.Group;


/**
 * @author sergio
 */

public class U2UShareScene extends U2UAbstractMain{

    var imgBackground:Image;
    var imgBackView:ImageView;


    init {

        imgBackground = Image{
            url:"{__DIR__}content.png";
        }

        this.contentPane = Group{
            content: [

                ImageView {
                    translateX:210;
                    translateY:25;
                    image:imgBackground;
                }
            ]
        };

    }



}
