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
import javafx.scene.effect.Glow;
import javafx.scene.transform.Scale;
import javafx.scene.effect.DropShadow;
/**
 * @author sergio
 */

public class Scene3 extends AbstractScene {


    var transKevinPen: TranslateTransition;
    var transPeople: TranslateTransition;
    var kevinPen:ImageView;
    var people: ImageView;
    

    init {

        transPeople = TranslateTransition{
                byY:-5 toX:15
                duration:1s autoReverse:true
                repeatCount:15
                node: bind people
            }
        transKevinPen = TranslateTransition{
                byX:-4
                duration:1s autoReverse:true
                repeatCount:15
                node: bind kevinPen
            }
        

        this.titleScene = "Scene 3:";
        this.textScene = "Kevin picked up the books to sign it, \namong these was the book \nof blue tag that I had";

    }

    function kevinAppear(): Group {

        var group: Group = Group {

            content: [

                Rectangle {
                    height: 300;
                    width: 100;
                    fill: Color.BLUE;
                    effect:DropShadow {
                            offsetX: 10
                            offsetY: 10
                            color: Color.BLACK
                            radius: 10
                        }


                },
                Rectangle {
                    height: 300;
                    width: 100;
                    fill: Color.BLUE;
                    translateX: 500;
                     effect:DropShadow {
                            offsetX: -10
                            offsetY: 10
                            color: Color.BLACK
                            radius: 10
                        }
                },
                ImageView {
                    image: Image {
                        url: "{__DIR__}resources/campusCol.jpg";
                    }
                    translateX: 200;
                    translateY: 20;
                    fitHeight: 200;
                    fitWidth: 200;
                },
                Rectangle {
                    height: 20;
                    width: 600;
                    translateY: 300;
                    fill: Color.BROWN;
                },
                Rectangle {
                    height: 80;
                    width: 600;
                    translateY: 320;
                    fill: Color.BLACK;
                },
                people = ImageView {
                    image: Image {
                        url: "{__DIR__}resources/grupoGente.png";
                    }
                    id: "grupoGente";
                    translateX: 50;
                    translateY: 320;

                },
                ImageView {
                    image: Image {
                        url: "{__DIR__}resources/bodyMit.png";
                    }
                    id: "kevin";
                    translateX: 200;
                    translateY: 180;


                },
                ImageView {
                    image: Image {
                        url: "{__DIR__}resources/books.png";
                    }
                    translateX: 230;
                    translateY: 180;

                },
                kevinPen = ImageView {
                    image: Image {
                        url: "{__DIR__}resources/pen.png";
                    }
                    translateX: 200;
                    translateY: 230;
                    effect: Glow {
                            level: 0.5
                        }

                },
                ImageView {
                    image: Image {
                        url: "{__DIR__}resources/desk.png";
                    }
                    translateX: 190;
                    translateY: 260;

                }
                

            ]

        }

        group.scaleX = 1.6;
        group.scaleY = 1.6;


        return group;

    }

    override function start() {

        fill = Color.DARKGRAY;
        var player: MediaPlayer = MediaPlayer {
            media : Media {
                source: "{__DIR__}resources/TickingClock.mp3";
            }
            //repeatCount: 2;
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
                            
                            content = kevinAppear();
                            transPeople.play();
                            transKevinPen.play();
                            
                        }

                    },
                    KeyFrame{
                        time: 25s;

                        action:function(){
                            println("termino 2");
                            player.stop();
                            transPeople.stop();
                            transKevinPen.stop();
                            
                            this.playStage.next();
                        }

                    }

                ]
            }

        timeline.play();
    }
}

