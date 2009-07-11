/*
 * Scene2.fx
 *
 * Created on 10-jul-2009, 8:31:54
 */

package animation;

import javafx.scene.paint.Color;

import javafx.scene.Group;
import javafx.scene.image.*;
import javafx.scene.Node;
import javafx.scene.media.*;
import javafx.animation.*;
import javafx.scene.shape.Rectangle;
import javafx.animation.transition.*;
import javafx.scene.effect.Glow;
import javafx.scene.effect.DropShadow;
/**
 * @author sergio
 */

public class Scene2 extends AbstractScene {



    var transPeople: TranslateTransition;
    var transHoover: RotateTransition;
    var people: ImageView;
    var hoover: ImageView;

    init {

        transPeople = TranslateTransition{
                byY:-5 toX:15
                duration:3s autoReverse:true
                repeatCount:5
                node: bind people
            }
        transHoover = RotateTransition{
                byAngle:12 toAngle:-12
                duration:1s autoReverse:true
                repeatCount:15
                node: bind hoover
            }

        this.titleScene = "Scene 2:";
        this.textScene = "While I was thinking about...\n How I get a sign of Kevin Mitnick?";

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
                    translateX: 400;
                    translateY: 180;

                },
                hoover = ImageView {
                    image: Image {
                        url: "{__DIR__}resources/hoover-left.png";
                    }
                    translateX: 500;
                    translateY: 220;
                    effect:Glow {
                            level: 0.5
                        }

                },
                ImageView {
                    image: Image {
                        url: "{__DIR__}resources/body.png";
                    }
                    translateX: 504.5;
                    translateY: 309;
                    effect:Glow {
                            level: 0.5
                        }
                },
                ImageView {
                    image: Image {
                        url: "{__DIR__}resources/ask.png";
                    }
                    translateX: 504.5;
                    translateY: 140;
                    effect:Glow {
                            level: 0.5
                        }
                }

            ]

        }

        return group;

    }

    override function start() {

        fill = Color.DARKGRAY;
        var player: MediaPlayer = MediaPlayer {
            media : Media {
                source: "{__DIR__}resources/Heartbeat.mp3";
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
                            transHoover.play();
                        }

                    },
                    KeyFrame{
                        time: 25s;

                        action:function(){
                            println("termino 2");
                            //player.pause();
                            player.stop();
                            transPeople.stop();
                            transHoover.stop();
                            this.playStage.next();
                        }

                    }

                ]
            }

        timeline.play();
    }
}

