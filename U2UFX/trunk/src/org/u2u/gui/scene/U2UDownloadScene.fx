/*
 * U2UDownloadScene.fx
 *
 * Created on 18-may-2009, 10:51:53
 */

package org.u2u.gui.scene;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.ArrayList;
import org.u2u.data.U2UDataDownload;
import javafx.scene.Group;
import javafx.scene.layout.VBox;

/**
 * @author sergio
 */

public class U2UDownloadScene extends U2UAbstractMain{

var imgBackground:Image;
var imgDown:Image;
var imgBackView:ImageView;
var model:U2UDataDownload;
var vbox:VBox;

var seqDownloads: ArrayList;


    init {

        imgBackground = Image{
            url:"{__DIR__}content.png";
        };

        imgDown = Image{
            url:"{__DIR__}piece.png";
        };

        model = U2UDataDownload{};
        model.initDataDonwload(this);

        this.contentPane = Group{
                                content: [
                                    ImageView{
                                    image:imgBackground;
                                    translateX:210;
                                    translateY:25;

                                   },
                                   this.vbox = VBox
                                   {
                                       translateX:225;
                                       translateY:40;
                                       content:[
                                            ImageView
                                            {
                                                image:bind imgDown;
                                            }
                                       ]
                                   }
                               ];
                           }

    }


}
