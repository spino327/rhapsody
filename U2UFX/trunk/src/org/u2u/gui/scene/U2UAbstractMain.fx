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


/**
 * This class has the propuse of provide a common option pane for his subclasses.
 *
 * @author sergio
 */
public abstract class U2UAbstractMain extends U2UAbstractScene {

    
    var imgLeft:Image;
    var imgBack:Image;
    var imgBShare:Image;
    var imgBSearch:Image;
    var imgBDown:Image;
    var imgConfig:Image;
    var imgHelp: Image;

    var imgBackView:ImageView;
    var imgLeftView:ImageView;
    protected var imgShareView:ImageView;
    protected var imgSearchView:ImageView;
    protected var imgDownView:ImageView;
    var imgConfView:ImageView;
    var imgHelpView: ImageView;

    var groupButtons:Node[];

    protected var contentPane: Node = Group{};

    override var content = bind [
        updateContentPane()
    ];

    init {

        imgLeft = Image{
            url: "{__DIR__}leftpane.png";
        };
        imgBack = Image {
            url:"{__DIR__}Earth-Horizon.png";
        }
        imgBShare = Image{
             url: "{__DIR__}share-button.png";
        };
        imgBSearch= Image{
             url: "{__DIR__}search-button.png";
        };
        imgBDown= Image{
             url: "{__DIR__}download-button.png";
        };
        imgConfig= Image{
             url: "{__DIR__}config-button.png";
        };
        imgHelp= Image{
             url: "{__DIR__}help-button.png";
        };

        groupButtons = Group{
                translateX:10.5;
                translateY:26;
                content: [
                    Group {
                        //spacing:36;
                        translateX:10.5;
                        translateY:26;
                        
                        content: [
                            imgShareView = ImageView{
                                translateX:10.5;
                                translateY:26;
                                image: imgBShare;
                                onMousePressed:function(me:MouseEvent):Void{
                                    println("imgShareView");
                                    
                                    this.contentStage.showShare();
                                    
                                }
                            },
                            imgSearchView = ImageView{
                                translateX:10.5;
                                translateY:135;
                                image: imgBSearch;
                                onMousePressed:function(me:MouseEvent):Void{
                                    println("imgSearchView");
                                    
                                    this.contentStage.showSearch();
                                }
                            },
                            imgDownView = ImageView{
                                translateX:10.5;
                                translateY:244;
                                image: imgBDown;
                                onMousePressed:function(me:MouseEvent):Void{
                                    println("imgDownView");
                                   
                                    this.contentStage.showDownload();
                                    
                                }
                            },
                            Group {
                                translateX:2.5;
                                translateY:353;
                                //spacing:12;
                                effect: InnerShadow { offsetX: 4 offsetY: 4 }
                                content: [
                                    imgConfView = ImageView{
                                        image: bind imgConfig;
                                     },
                                     imgHelpView = ImageView{
                                        translateX: 81;
                                        image: bind imgHelp;
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


