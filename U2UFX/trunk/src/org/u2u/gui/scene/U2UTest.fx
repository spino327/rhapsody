/*
 * U2UTest.fx
 *
 * Created on 17-may-2009, 21:01:59
 */

package org.u2u.gui.scene;

import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;

/**
 * @author sergio
 */

public class U2UTest extends U2UAbstractMain {

    init {
        this.contentPane = Rectangle {
            width: 100;
            height: 100;
            fill: Color.CHOCOLATE;
        }

    }

}
