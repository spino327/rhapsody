/*
 * U2UShareScene.fx
 *
 * Created on 18-may-2009, 10:00:26
 */

package org.u2u.gui.scene;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.u2u.gui.U2UList;
import org.u2u.data.U2UDownloadListModel;

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

        var list: U2UList = U2UList{};
        list.setModel(U2UDownloadListModel{});

        this.contentPane = list;
        list.updateUI();

    }


}
