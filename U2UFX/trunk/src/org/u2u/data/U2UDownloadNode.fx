/*
 * U2UDownloadNode.fx
 *
 * Created on 19-may-2009, 16:27:40
 */

package org.u2u.data;

import javafx.scene.Node;
import javafx.scene.image.Image;
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
import org.u2u.data.TypeFile;
import javafx.scene.effect.DropShadow;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;

/**
 * @author sergio
 */

public class U2UDownloadNode extends U2UAbstractNode {

    var nodeView:Node;
    var textDesc:Text;
    var rec:Rectangle;
    def SIZE_FONT:Integer =12;
    /** level of download of this node [max:200- min:0] */
    protected var level:Integer;


    //instance methods
    override function getNodeView():Node {   

        if(nodeView == null)
        {
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
                        translateX:10;
                        translateY:20;
                        content: "Name:{this.name}";
                        textAlignment: TextAlignment.JUSTIFY;
                        font: Font.font("Verdana",FontWeight.BOLD,SIZE_FONT);
                        fill: Color.WHITESMOKE;
                        textOrigin: TextOrigin.BASELINE;

                    },
                    Text{
                        translateX:10;
                        translateY:33;
                        content: "Size:{this.length.toString()}";
                        textAlignment: TextAlignment.JUSTIFY;
                        font: Font.font("Verdana",FontWeight.BOLD,SIZE_FONT);
                        fill: Color.BLACK;
                    },
                    textDesc = Text{
                        translateX:10;
                        translateY:46;
                        content: "Description:{this.description.toString()}";
                        textAlignment: TextAlignment.JUSTIFY;
                        font: Font.font("Verdana",FontWeight.BOLD,SIZE_FONT);
                        fill: Color.BLACK;
                    },
                    ImageView{
                        image:this.getTypeFile();
                        translateX:285;
                        translateY:4;
                        effect: DropShadow { offsetY: 3 color: Color.color(0.4, 0.4, 0.4) };


                    },
                    Rectangle {
                        x: 10
                        y: 75
                        width:bind this.level;
                        height: 20;
                        fill: LinearGradient {
                                startX: 0.0, startY: 0.0, endX: 0.0, endY: 1.0
                                stops: [ Stop { offset: 0.0 color: Color.LIGHTBLUE },
                                         Stop { offset: 1.0 color: Color.DARKBLUE } ]
                        };

                    },
                   
                ];

                /*onMousePressed:function(me:MouseEvent):Void{
                    nodeView.opacity = 0.7;
                    rec.fill = Color.GOLDENROD;
                };*/
                /*
                onMouseClicked:function(me:MouseEvent):Void{
                   
                };

                onMouseExited:function(me:MouseEvent):Void{
                    rec.fill = Color.GRAY;
                    nodeView.opacity =1.0;
                }
                onMouseMoved:function(me:MouseEvent):Void{
                    rec.fill = Color.THISTLE;
                    nodeView.opacity = 0.7;
                };*/
                /*
                onMouseReleased:function(me:MouseEvent):Void{
                    nodeView.opacity =1.0;
                    rec.fill = Color.GREY;
                }*/
            };

        }
        else {
            nodeView.scaleY = 1.0;
            nodeView.translateX = 0;
            nodeView.translateY = 0;
        }


        return nodeView;
    };

    /**
    * Change the level of download of the node
    */
    public function updateLevel(lev:Integer):Void {
        
        if(lev<=200 and lev>=0)
        {
            this.level = lev;
        }
    }

    function getTypeFile():Image{

        var nam:String = this.name as String;
        var type:String= TypeFile.getTypeFile(nam.substring(nam.indexOf('.')+1));

        var imgType:Image = TypeFile.getImageTypeFile(type);

        return imgType;
    }



}

