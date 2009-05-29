/*
 * Test.fx
 *
 * Created on 27-may-2009, 11:30:09
 */

package org.u2u.data;


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
import javafx.scene.image.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.LinearGradient;
import javafx.scene.effect.InnerShadow;

import org.u2u.data.U2UAbstractNode;
import org.u2u.data.U2UDownloadNode;
import javafx.stage.Stage;
import javafx.scene.Scene;
/**
 * @author sergioPino
 */

var nodeView:Node;
var textDesc:Text;
var rec:Rectangle;
var recProgress:Rectangle;
var textProgress:Text;
def SIZE_FONT:Integer =12;
var width: Integer = 370;
var height: Integer = 90;
var gro:Node = null;
var desc = "111112222233333111112222233333111112222233333111112222233333";
var name: String = null;//"111112222233333111112222233333111112222233333111112222233333.mp3";
var level = 50;
var status = "download";
Stage {
    height: 200;
    width: 400;
    scene: Scene {
        content: bind [gro];
    }

}


gro = Group {
                cache: true;
                cursor: Cursor.HAND;

                content: [

                    rec = Rectangle {
                        width: width;
                        height: height;
                        arcHeight: 30.0;
                        arcWidth: 30.0;
                        fill: Color.GREY;

                    },
                    Text {
                        translateX:20;
                        translateY:20;
                        content: "Name: {if(name.length() > 17) then ("{name.substring(0, 17)}...{name.substring(name.lastIndexOf("."))}") else (name)}";
                        textAlignment: TextAlignment.JUSTIFY;
                        font: Font.font("Verdana",FontWeight.BOLD,12);
                        fill: Color.WHITESMOKE;
                        textOrigin: TextOrigin.BASELINE;

                    },
                    Text{
                        translateX:20;
                        translateY:40;
                        content: "Size: 192222222 KB";
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
                        image: this.getTypeFile("zip");
                        translateX:305;
                        translateY:10;
                        effect: DropShadow { offsetY: 3 color: Color.color(0.4, 0.4, 0.4) };

                    },
                    ImageView {
                        image: getImageStatus(status);
                        translateX:325;
                        translateY:60;
                        //
                        
                    },
                    recProgress = Rectangle {
                        x: 20
                        y: 65
                        width: bind level;
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
                        content: bind "{(level/2) as Integer} %";
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

/** return the image to show*/
    function getTypeFile(type:String): Image {

        var imgType:Image = TypeFile.getImageTypeFile(type);

        return imgType;
    }

    function getImageStatus(status:String):Image{

         if(status.equals("download"))
         {
             return Image {url:"{__DIR__}resources/download.png"};
         }else
         {
             return Image {url:"{__DIR__}resources/pause.png"};
         }

    }
