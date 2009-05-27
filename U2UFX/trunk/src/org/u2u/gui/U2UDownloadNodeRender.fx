/*
 * U2UDownloadNodeRender.fx
 *
 * Created on 26-may-2009, 18:40:03
 */

package org.u2u.gui;

import javafx.scene.Node;
import javafx.scene.Group;
import javafx.scene.Cursor;
import javafx.scene.text.Text;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.*;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextOrigin;
import javafx.scene.image.ImageView;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.LinearGradient;
import javafx.scene.effect.InnerShadow;

import org.u2u.data.U2UAbstractNode;
import org.u2u.data.U2UDownloadNode;

/**
 * Implementation for DownloadNOdes
 *
 * @author sergio
 */
public class U2UDownloadNodeRender extends U2UAbstractNodeRender {

    override function getNodeView(dataNode: U2UAbstractNode):Node {

        var nodeView:Node;
        var textDesc:Text;
        var rec:Rectangle;
        var recProgress:Rectangle;
        var textProgress:Text;
        var name: String = dataNode.getName();
        var desc: String = dataNode.getDescription();

        var node: U2UDownloadNode = dataNode as U2UDownloadNode;

        nodeView = Group {
                cache: true;
                cursor: Cursor.HAND;

                content: [

                    rec = Rectangle {
                        width: this.width;
                        height: this.height;
                        arcHeight: 30.0;
                        arcWidth: 30.0;
                        fill: Color.GREY;

                    },
                    Text {
                        translateX:20;
                        translateY:20;
                        content: "Name: {if(name.length() > (20 + 4)) then ("{name.substring(0, 20)}...{name.substring(name.lastIndexOf("."))}") else (name)}";
                        textAlignment: TextAlignment.JUSTIFY;
                        font: Font.font("Verdana",FontWeight.BOLD,12);
                        fill: Color.WHITESMOKE;
                        textOrigin: TextOrigin.BASELINE;

                    },
                    Text{
                        translateX:20;
                        translateY:40;
                        content: "Size: {dataNode.getLength()/1024} KB";
                        textAlignment: TextAlignment.JUSTIFY;
                        font: Font.font("Verdana",FontWeight.REGULAR,11);
                        fill: Color.BLACK;
                    },
                    textDesc = Text{
                        translateX:20;
                        translateY:55;
                        content: "Desc: {if(desc.length() > 30) then ("{desc.substring(0, 30)}...") else (desc)}";
                        textAlignment: TextAlignment.JUSTIFY;
                        font: Font.font("Verdana",FontWeight.REGULAR,11);
                        fill: Color.BLACK;
                    },
                    ImageView {
                        image: this.getTypeFile(dataNode);
                        translateX:305;
                        translateY:10;
                        effect: DropShadow { offsetY: 3 color: Color.color(0.4, 0.4, 0.4) };
                    },
                    recProgress = Rectangle {
                        x: 20
                        y: 65
                        width: bind (node.level);
                        height: 15;
                        fill: LinearGradient {
                                startX: 0.0, startY: 0.0, endX: 0.0, endY: 1.0
                                stops: [ Stop { offset: 0.0 color: Color.LIGHTYELLOW },
                                         Stop { offset: 1.0 color: Color.DARKGREEN }]
                        };

                    },

                    textProgress = Text{

                        x: bind recProgress.width + 24;
                        y: 76;
                        content: bind "{(node.level/2) as Integer} %";
                        font: Font.font("Verdana",FontWeight.BOLD, 12);
                        fill: Color.YELLOWGREEN;
                        effect: InnerShadow {
                            choke: 0.5
                            offsetX: 5
                            offsetY: 5
                            radius: 5
                            color: Color.BLACK
                        }

                    }
                ];
        };

        return nodeView;
    };
}
