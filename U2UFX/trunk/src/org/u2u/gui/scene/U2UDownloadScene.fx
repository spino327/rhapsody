/*
 * U2UDownloadScene.fx
 *
 * Created on 18-may-2009, 10:51:53
 */

package org.u2u.gui.scene;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.ArrayList;

/**
 * @author sergio
 */

public class U2UDownloadScene extends U2UAbstractMain{

var imgBackground:Image;
var imgBackView:ImageView;

var seqDownloads: ArrayList;


    init {

        imgBackground = Image{
            url:"{__DIR__}content.png";
        }

        this.contentPane = ImageView{
                            image:imgBackground;
                            translateX:210;
                            translateY:25;

                           };
    }


}
