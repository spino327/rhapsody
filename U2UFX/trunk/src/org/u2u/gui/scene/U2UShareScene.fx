/*
 * U2UShareScene.fx
 *
 * Created on 18-may-2009, 10:00:26
 */

package org.u2u.gui.scene;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.Group;
import javafx.scene.effect.Glow;
import javafx.ext.swing.SwingButton;
import javax.swing.JFileChooser;
import javafx.scene.input.MouseEvent;

import javax.swing.*;
import javafx.ext.swing.SwingComponent;
import javafx.scene.text.*;
import javafx.scene.paint.Color;
/**
 * @author sergio
 */

public class U2UShareScene extends U2UAbstractMain{

    var imgBackground:Image;
    var imgBackView:ImageView;
    var fileChooser:JFileChooser;
    var selectedFile:String;
    var swing:SwingComponent;



    init {

        imgBackground = Image{
            url:"{__DIR__}resources/content.png";
        }

        this.contentPane = Group{
            content: [

                ImageView {
                    effect: Glow{level:0.8}
                    translateX:210;
                    translateY:25;
                    image:imgBackground;
                },
                ImageView{
                    translateX: 550;
                    translateY: 35;
                    image: Image{url:"{__DIR__}resources/share.png"}
                    onMouseClicked:function(me:MouseEvent):Void{

                        fileChooser = new JFileChooser();
                        if ( fileChooser.showOpenDialog(swing.getRootJComponent()) == JFileChooser.APPROVE_OPTION){
                            selectedFile = fileChooser.getSelectedFile().getName();
                        }
                    }
                    onMouseMoved:function(me:MouseEvent):Void{
                        me.node.effect = Glow{level:0.5}
                    }
                    onMouseExited:function(me:MouseEvent):Void{
                        me.node.effect = null;
                    }
                },
                Text{
                    translateX: 230;
                    translateY: 50;
                    content:bind selectedFile;
                    font: Font.font("Verdana",FontWeight.BOLD,15);
                    fill: Color.SNOW;
                }

            ]
        };

    }

    override function updateButtons() {

        butShare.aplyPressed = Glow{level:0.7};
        butDown.aplyPressed =  null;
        butSearch.aplyPressed =  null;
      
    }

}
