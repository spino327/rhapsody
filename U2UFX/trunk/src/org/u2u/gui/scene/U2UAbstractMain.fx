/*
 * U2UAbstractMain.fx
 *
 * Created on 17-may-2009, 17:10:37
 */

package org.u2u.gui.scene;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;


/**
 * This class has the propuse of provide a common option pane for his subclasses.
 *
 * @author sergio
 */
public abstract class U2UAbstractMain extends U2UAbstractScene {

    
    var imgLeft:Image;
    var imgBack:Image;
    
    var imgBackView:ImageView;
    var imgLeftView:ImageView;

    protected var butShare:ButtonNode;
    protected var butSearch:ButtonNode;
    protected var butDown:ButtonNode;
    var butConfig:ButtonNode;
    var butHelp:ButtonNode;

    var groupButtons:Node[];

    protected var contentPane: Node = Group{};

    override var content = bind [
        updateContentPane()
    ];

    init {
        imgLeft = Image{
            url: "{__DIR__}resources/leftpane.png";
        };
        imgBack = Image {
            url:"{__DIR__}resources/Earth-Horizon.png";
        }
        groupButtons = Group{
                translateX:8;
                translateY:26;
                content: [
                    Group {
                        translateX:10.5;
                        translateY:26;
                        content: [
                            butShare = ButtonNode{
                                translateX:5;
                                translateY:16;
                                imageURL:"{__DIR__}resources/share-button2.png"
                                
                                action:function():Void{
                                    println("imgShareView");
                                    this.contentStage.showShare();
                                }
                                aplyEffect: DropShadow { color:Color.LIGHTGREY offsetX:5 offsetY:5 radius:20}
                            },
                            butSearch = ButtonNode{
                                translateX:5;
                                translateY:125;
                                imageURL: "{__DIR__}resources/search-button.png";
                                action:function():Void{
                                    println("imgSearchView");
                                    this.contentStage.showSearch();
                                }
                                aplyEffect: DropShadow { color:Color.LIGHTGREY offsetX:5 offsetY:5 radius:20}
                            },
                            butDown = ButtonNode{
                                translateX:5;
                                translateY:234;
                                imageURL:"{__DIR__}resources/download-button.png";
                                action:function():Void{
                                    println("imgDownView");
                                    this.contentStage.showDownload();
                                }
                                aplyEffect: DropShadow { color:Color.LIGHTGREY offsetX:5 offsetY:5 radius:20}
                            },
                            Group {
                                translateX:2.5;
                                translateY:335;
                                
                                content: [
                                    
                                    butConfig = ButtonNode{
                                        translateX:12;
                                        title:"Preferences";
                                        imageURL:"{__DIR__}resources/config.png"
                                        },

                                     butHelp = ButtonNode{
                                        translateX:81;
                                        title:"Help";
                                        imageURL:"{__DIR__}resources/help.png"
                                        }
                                ]
                            }
                        ]
                    },

                ]

            }
    }

    bound function updateContentPane():Node {

        Group {
            content:[
                imgBackView = ImageView{
                    image:bind imgBack;
                },

                imgLeftView = ImageView{
                    image:bind imgLeft;
                    translateX:15;
                    translateY:25;
                    opacity: 0.7;
                },

                groupButtons,

                contentPane
            ];
        }
    }

    public abstract function updateButtons(): Void;

}


