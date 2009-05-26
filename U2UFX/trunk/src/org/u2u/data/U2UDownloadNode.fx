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
import javafx.scene.effect.InnerShadow;

/**
 * @author sergio
 */

public class U2UDownloadNode extends U2UAbstractNode {

    var nodeView:Node;
    var textDesc:Text;
    var rec:Rectangle;
    var recProgress:Rectangle;
    var textProgress:Text;
    def SIZE_FONT:Integer =12;
    /** level of download of this node [max:200- min:0] */
    protected var level:Float on replace {


    };


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
                        translateX:20;
                        translateY:25;
                        content: "Name: {this.name}";
                        textAlignment: TextAlignment.JUSTIFY;
                        font: Font.font("Verdana",FontWeight.BOLD,SIZE_FONT);
                        fill: Color.WHITESMOKE;
                        textOrigin: TextOrigin.BASELINE;

                    },
                    Text{
                        translateX:20;
                        translateY:40;
                        content: "Size: {this.length.toString()} KB";
                        textAlignment: TextAlignment.JUSTIFY;
                        font: Font.font("Verdana",FontWeight.BOLD,SIZE_FONT);
                        fill: Color.BLACK;
                    },
                    textDesc = Text{
                        translateX:20;
                        translateY:55;
                        content: "Description: {this.description.toString()}";
                        textAlignment: TextAlignment.JUSTIFY;
                        font: Font.font("Verdana",FontWeight.BOLD,SIZE_FONT);
                        fill: Color.BLACK;
                    },
                    ImageView{
                        image:this.getTypeFile();
                        translateX:305;
                        translateY:15;
                        effect: DropShadow { offsetY: 3 color: Color.color(0.4, 0.4, 0.4) };

                    },
                    recProgress = Rectangle {
                        x: 20
                        y: 70
                        width:bind this.level;
                        height: 15;
                        fill: LinearGradient {
                                startX: 0.0, startY: 0.0, endX: 0.0, endY: 1.0
                                stops: [ Stop { offset: 0.0 color: Color.LIGHTYELLOW },
                                         Stop { offset: 1.0 color: Color.DARKGREEN } ]
                        };

                    },
                    
                    textProgress = Text{
                    
                        x: bind recProgress.width + 24;
                        y: 78;
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
    public function updateLevel(lev:Float):Void {
        
        if(lev>=0 and lev<=200 )
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

