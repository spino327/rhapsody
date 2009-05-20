/*
 * U2UDownloadNode.fx
 *
 * Created on 19-may-2009, 16:27:40
 */

package org.u2u.data;

import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.Group;
import javafx.scene.shape.Rectangle;
import org.u2u.data.U2UAbstractNode;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.*;
import javafx.scene.effect.Reflection;
import javafx.scene.effect.Lighting;

/**
 * @author sergio
 */

public class U2UDownloadNode extends U2UAbstractNode {

    var nodeView:Node;

    //instance methods
    override function getNodeView():Node {   

        if(nodeView == null)
        {
            nodeView = Group{

                content: [
                    Rectangle {
                        width: this.width;
                        height: this.height;
                        arcHeight: 30.0;
                        arcWidth: 30.0;
                        fill: Color.GREY;
                    },
                    Text {
                        content: "Name:{this.name}";
                        textAlignment: TextAlignment.JUSTIFY;
                        font: Font.font("Verdana",FontWeight.BOLD,15);
                        fill: Color.BLUE;
                    },
                    Text{
                        translateY:20;
                        content: "Size:{this.length.toString()}";
                        textAlignment: TextAlignment.JUSTIFY;
                        font: Font.font("Verdana",FontWeight.BOLD,15);
                        fill: Color.BLUE;
                    }
                    

                ];
            };
        }

        return nodeView;
    };

}

