/*
 * U2UAbstractMain.fx
 *
 * Created on 17-may-2009, 17:10:37
 */

package org.u2u.gui.scene;

import javafx.scene.Group;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


/**
 * This class has the propuse of provide a common option pane for his subclasses.
 *
 * @author sergio
 */
public class U2UAbstractMain extends U2UAbstractScene {

    //var optionPane: U2ULeftPaneUI;
    var imgLP:Image;
    var imgLeftPane:ImageView;
    var img:Image;
    var imgV:ImageView;
    protected var contentPane: Node;

    override var content = bind [
        updateContentPane()
    ];

    init {

        imgLP = Image{
            url: "{__DIR__}leftpane.png";
        };
        img = Image {
            url:"{__DIR__}Earth-Horizon.png";
        }
    }

    bound function updateContentPane():Node {

        Group {
            content:[
                imgV = ImageView{
                    image:bind img;

                },

                imgLeftPane = ImageView{
                    image:bind imgLP;
                    translateX:15;
                    translateY:25;
                    opacity: 0.7;
                },

                 VBox {
                    content: [
                        contentPane
                    ];
                 }


            ];
        }
    }

}


