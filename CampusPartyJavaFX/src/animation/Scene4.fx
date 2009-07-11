/*
 * Scene2.fx
 *
 * Created on 10-jul-2009, 8:31:54
 */

package animation;

import javafx.scene.paint.Color;

import javafx.scene.Group;
import javafx.scene.image.*;
import javafx.scene.media.*;
import javafx.animation.*;
import javafx.scene.shape.Rectangle;
import javafx.animation.transition.*;
import javafx.scene.effect.PerspectiveTransform;
import javafx.scene.effect.Reflection;
/**
 * @author sergio
 */

public class Scene4 extends AbstractScene {


    var transBurglarRun: TranslateTransition;
    var transHooverRun: TranslateTransition;
    var scaleHoover: ScaleTransition;
    var rotateBook: RotateTransition;

    var hoover: ImageView;
    var burglar: ImageView;
    var book: ImageView;
    

    init {

        transHooverRun = TranslateTransition{
                byX: 5 toX: 500
                byY: 5 toY: 200
                duration:5s autoReverse:false
                repeatCount:1
                node: bind hoover
            }
            
        transBurglarRun = TranslateTransition{
                byY: 5 toY:200
                byX: 5 toX:500
                duration:5s autoReverse:false
                repeatCount:1
                node: bind burglar
            }

        scaleHoover = ScaleTransition {
            duration: 5s node: bind hoover
            byX: 1.5 byY: 1.5
            repeatCount:1 autoReverse: false
        }

        rotateBook = RotateTransition {
            duration: 1s node: bind book
            byAngle: 90
            fromAngle: 0
            toAngle: 360
            repeatCount:3 autoReverse: false
        }

 
        this.titleScene = "Scene 4:";
        this.textScene = "When I went to pick up my book I saw a man who was \n"
                         "carrying it. I summon up courage and asked him, but \n"
                         "the man did not want to give me my book, so I told \n"
                         "him that my book was the blue tag and had a dedication. \n"
                         "The man I revised it and returned it. uff I am saved! \n"  
                         "I thought";

    }

    function stolenAppear(): Group {

        var group: Group =  Group {

            content: [

                Rectangle {
                    width: 600;
                    height: 400;
                    fill: Color.LIGHTGREEN;
                    effect: PerspectiveTransform {
                            llx: -100, lly: 250
                            lrx: 500, lry: 450
                            ulx: 100, uly: 50
                            urx: 600, ury: 150
                        }

                },
                burglar = ImageView {
                    image: Image {
                        url: "{__DIR__}resources/burglar.png"
                    }
                    translateX: 200;
                    translateY: 100;
                },
                hoover = ImageView {
                    image: Image {
                        url: "{__DIR__}resources/hoover-right.png"
                    }
                    translateX: 10;
                    translateY: 10;
                }


            ]
        };

        return group;

    }

    function hooverWin(): Group {

        var group: Group =  Group {

            content: [

                Rectangle {
                    width: 600;
                    height: 400;
                    fill: Color.LIGHTGREEN;
                    effect: PerspectiveTransform {
                            llx: -100, lly: 250
                            lrx: 500, lry: 450
                            ulx: 100, uly: 50
                            urx: 600, ury: 150
                        }

                },
                hoover = ImageView {
                    image: Image {
                        url: "{__DIR__}resources/hooverWin.png"
                    }
                    translateX: 250;
                    translateY: 120;
                }


            ]
        };

        return group;

    }

    override function start() {

        fill = Color.DARKGRAY;
        var player: MediaPlayer = MediaPlayer {
            media : Media {
                source: "{__DIR__}resources/Alarm.mp3";
            }
            volume: 300;

        }
        println("inicia");
        player.play();

        var timeline: Timeline  = Timeline{
                repeatCount:1;
                keyFrames: [
                    KeyFrame{
                        time:0s;
                        action:function() {
                            println("init");
                            content = showTitle();
                        }

                    },
                    KeyFrame{
                        time:10s;

                        action:function() {
                            println("termino");
                            //player.pause();
                            player.stop();
                            
                            player = MediaPlayer {
                                media : Media {
                                    source: "{__DIR__}resources/JetFlyBy.mp3";
                                }
                                volume: 300;

                            }
                            player.play();

                            content = stolenAppear();
                            transHooverRun.play();
                            transBurglarRun.play();
                            
                        }

                    },
                    KeyFrame{
                        time: 15s;

                        action:function(){
                            //player.pause();
                            player.stop();

                            player = MediaPlayer {
                                media : Media {
                                    source: "{__DIR__}resources/WeAreTheChampions.mp3";
                                }
                                volume: 300;
                                currentTime: 28s;
                            }
                            player.play();
                            println("termino 2");
                            transHooverRun.stop();
                            transBurglarRun.stop();

                            content = hooverWin();
                            scaleHoover.play();
     
                        }

                    },
                    KeyFrame{
                        time: 25s;

                        action:function(){
                            println("termino 2");

                            content = [

                                book = ImageView {
                                    image: Image {
                                        url: "{__DIR__}resources/book2.png"
                                    }
                                    effect: Reflection {
                                            fraction: 0.75
                                            topOffset: 0.0
                                            topOpacity: 0.5
                                            bottomOpacity: 0.0
                                        }
                                    translateX: 200;
                                }

                            ];
                            rotateBook.playFromStart();

                        }

                    },
                    KeyFrame{
                        time: 35s;

                        action:function(){
                            println("termino 2");
                            
                            content = [

                                book = ImageView {
                                    image: Image {
                                        url: "{__DIR__}resources/book2.png"
                                    }
                                    effect: Reflection {
                                            fraction: 0.75
                                            topOffset: 0.0
                                            topOpacity: 0.5
                                            bottomOpacity: 0.0
                                        }
                                    translateX: 180;
                                }

                            ];
                            rotateBook.playFromStart();
                            //player.pause();
                            player.stop();
                            this.playStage.next();
                        }

                    }

                ]
            }

        timeline.play();
    }
}

