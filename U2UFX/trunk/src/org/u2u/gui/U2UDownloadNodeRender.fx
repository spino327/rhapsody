/*
 * U2UDownloadNodeRender.fx
 *
 * Created on 26-may-2009, 18:40:03
 */
/**
 * Copyright (c) 2009, Sergio Pino and Irene Manotas
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of Sergio Pino and Irene Manotas. nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * @author: Sergio Pino and Irene Manotas
 * Website: http://osum.sun.com/profile/sergiopino, http://osum.sun.com/profile/IreneLizeth
 * emails  : spino327@gmail.com - irenelizeth@gmail.com
 * Date   : March, 2009
 * This license is based on the BSD license adopted by the Apache Foundation.
 *
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
import javafx.scene.image.Image;

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
                        id: "background";

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
                    ImageView {
                        image: bind changeStatus(node.status);
                        translateX:325;
                        translateY:60;
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

    bound function changeStatus(status: String): Image {
        return Image{
            url:"{__DIR__}resources/{status}.png";
        };

    }


}
