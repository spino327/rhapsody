/*
 * U2UPreferencesScene.fx
 *
 * Created on 27-may-2009, 11:56:54
 */

package org.u2u.gui.scene;

import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.*;
import javafx.ext.swing.SwingTextField;
import javafx.ext.swing.SwingLabel;
import javafx.ext.swing.SwingComboBox;
import javafx.ext.swing.SwingComboBoxItem;
import javafx.scene.paint.Color;
import javafx.scene.layout.*;


/**
 * @author sergio
 */


 public class U2UPreferencesScene extends U2UAbstractMain{

    /**
    * numSharedFiles, represents the number of shared files at the same time
    * to other peers in the P2P network
    */
    var numSharedFiles:Integer;
    /**
    * numConDown, represents the maximum number of conecction that this peer 
    * can do to download a file
    */
    var numConDown:Integer;
    /**
    * numConUpload, represents the maximum number of conecction that others peers 
    * can do to this peer to download a shared file
    */
    var numConUpload:Integer;
    /**
    * numDownSim, represents the maximum number of conecction simultaneous that 
    * this peer can do to download files from others peer in the P2P network
    */
    var numDownSim:Integer;


    //Swing Variables

    var sharedText:SwingTextField;
    var conDownText:SwingTextField;
    var conUploadText:SwingTextField;
    var downSimText:SwingTextField;
    var selMTP:SwingComboBox;

    init {

        this.contentPane = Group{

        content:[
                ImageView {
                            image:Image{
                                    url: "{__DIR__}resources/content2.png";
                                }
                            translateX:210;
                            translateY:25;
                },
                Group{
                    translateX:215;
                    content: [
                        Text{
                            translateX:50;
                            translateY:65;
                            content:"Please insert your preferences to upload\n "
                                    "and download files in the P2P network.";
                            textAlignment: TextAlignment.JUSTIFY;
                            fill:Color.DARKGREEN;

                            font: Font.font("Verdana",FontWeight.BOLD ,13);
                        },
                        Group{
                            translateY:130;
                            translateX:15;
                            content:[
                                Text{
                                    content:"Number of shared Files at the same time:";
                                    font: Font.font("Verdana",FontWeight.BOLD ,13);
                                },
                                sharedText = SwingTextField{
                                    translateY:-15;
                                    translateX: 340;
                                    width:40;
                                },
                                Text{
                                    translateY:50;
                                    content:"Highest number of connections per download:";
                                    font: Font.font("Verdana",FontWeight.BOLD ,13);
                                },
                                conDownText = SwingTextField{
                                    translateY:35;
                                    translateX:  340;
                                    width:40;
                                },
                                Text{
                                    translateY:100;
                                    content:"Highest number of connections per upload:";
                                    font: Font.font("Verdana",FontWeight.BOLD ,13);
                                },
                                conUploadText = SwingTextField{
                                    translateY:85;
                                    translateX:  340;
                                    width:40;
                                },
                                Text{
                                    translateY:150;
                                    content:"Number of simultaneous downloads:";
                                    font: Font.font("Verdana",FontWeight.BOLD ,13);
                                },
                                downSimText = SwingTextField{
                                    translateY:135;
                                    translateX: 340;
                                    width:40;
                                },
                                Text{
                                    translateY:200;
                                    content:"Maximum size of the piece of a file:";
                                    font: Font.font("Verdana",FontWeight.BOLD ,13);
                                },
                                selMTP = SwingComboBox{
                                    translateY:185;
                                    translateX: 340;
                                    items: [
                                        SwingComboBoxItem{
                                            value:"16"
                                        },
                                        SwingComboBoxItem{
                                            value:"32"
                                        },
                                        SwingComboBoxItem{
                                            selected:true;
                                            value:"64"
                                        },

                                    ]
                                },
                            ]
                        }
                    ];
                 }
              ];
           }
    }

    override function updateButtons() {

        butDown.aplyPressed = null;
        butShare.aplyPressed = null;
        butSearch.aplyPressed = null;

    }

}



