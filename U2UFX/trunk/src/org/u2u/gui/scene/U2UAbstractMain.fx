/*
 * U2UAbstractMain.fx
 *
 * Created on 17-may-2009, 17:10:37
 */

package org.u2u.gui.scene;

import javafx.scene.Group;
import javafx.scene.layout.VBox;
import javafx.scene.Node;


/**
 * This class has the propuse of provide a common option pane for his subclasses.
 *
 * @author sergio
 */
public class U2UAbstractMain extends U2UAbstractScene {

    var optionPane: U2ULeftPaneUI;
    protected var contentPane: Node;

    override var content = bind [
        updateContentPane()
    ];

    init {

        optionPane = U2ULeftPaneUI{

        };
    }

    bound function updateContentPane():Node {

         VBox {
            content: [
                optionPane,
                contentPane
            ];
         }

    }


}


