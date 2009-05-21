/*
 * U2USearchScene.fx
 *
 * Created on 20-may-2009, 12:10:51
 */

package org.u2u.gui.scene;

import org.u2u.filesharing.U2UContentAdvertisementImpl;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.Group;


/**
 * @author sergio
 */

public class U2USearchScene extends U2UAbstractMain{

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
    /**
    *@return the index of the node selected in the scene
    */
    public function getAdvSelected():U2UContentAdvertisementImpl{
        var adv:U2UContentAdvertisementImpl = null;
        return adv;
    }

    override function updateButtons() {
        imgSearchView.opacity = 0.5;
        imgShareView.opacity = 1;
        imgDownView.opacity = 1;
    }
}
