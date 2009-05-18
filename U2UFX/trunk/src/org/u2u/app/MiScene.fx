/*
 * MiScene.fx
 *
 * Created on 17-may-2009, 9:07:09
 */

package org.u2u.app;

import javafx.scene.Scene;
import javafx.scene.shape.*;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseEvent;

/**
 * @author sergio
 */

public class MiScene extends Scene{

    var rec : Rectangle;

    init{

        content = [
            rec = Rectangle
            {
                width: 1;
                height: 200;
                fill: Color.BLUE;

                onMouseEntered:function(me:MouseEvent)
                {
                    rec.width = 50;
                }

                onMouseExited:function(me:MouseEvent)
                {
                    rec.width = 1;
                }

            }

        ];
    };
}


var s = MiScene{

};