/*
 * ShareScene.fx
 *
 * Created on 16-may-2009, 23:18:19
 */

package org.u2u.gui;

import javafx.scene.Scene;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.*;

/**
 * @author Irene
 */

public class ShareScene extends Scene{

 override var content = [
    Rectangle{
        width:600
        height:500
        fill: Color.RED;
    }
 ];

}
