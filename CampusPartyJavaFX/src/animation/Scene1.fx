/*
 * Scene1.fx
 *
 * Created on 10-jul-2009, 6:43:17
 */

package animation;

import javafx.scene.paint.Color;
import javafx.scene.Group;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.shape.Rectangle;
import javafx.scene.image.*;
import javafx.animation.transition.TranslateTransition;

/**
 * @author sergio
 */

public class Scene1 extends AbstractScene {

    var transKevin: TranslateTransition;
    var transPeople: TranslateTransition;
    var kevin:ImageView;
    var people: ImageView;

    init {
        this.titleScene = "Scene 1:";
        this.textScene =  "7:00 PM Campus Party, Colombia...";
        transKevin = TranslateTransition{
                byX:10 toX:200
                duration:5s autoReverse:false
                repeatCount:1
                node: bind kevin
            }
        transPeople = TranslateTransition{
                byY:-5 toX:15
                duration:1s autoReverse:true
                repeatCount:5
                node: bind people
            }
    }

    function kevinAppear(): Group {
        
        var group: Group = Group {

            content: [

                Rectangle {
                    height: 300;
                    width: 100;
                    fill: Color.BLUE;
                },
                Rectangle {
                    height: 300;
                    width: 100;
                    fill: Color.BLUE;
                    translateX: 500;
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
                kevin = ImageView {
                    image: Image {
                        url: "{__DIR__}resources/bodyMit.png";
                    }
                    id: "kevin"
                    translateY: 180

                }

            ]
            
        }



        return group;

    }

    override function start() {

        fill = Color.DARKGRAY;
        var player: MediaPlayer = MediaPlayer {
            media : Media {
                source: "{__DIR__}resources/TickingClock.mp3";
            }
            volume: 300;
            rate: 5;
            fader: 2;

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
                        time:5s;
                        
                        action:function() {
                            println("termino");
                            player.stop();
                            player = MediaPlayer {
                                media : Media {
                                    source: "{__DIR__}resources/OutdoorCheer.mp3";
                                }
                                volume: 300;
                                rate: 5;
                                fader: 2;
                            }
                            player.play();
                            content = kevinAppear();
                            transKevin.play();
                            transPeople.play();
                        }

                    },
                    KeyFrame{
                        time: 10s;

                        action:function(){
                            println("termino 2");
                            player.stop();
                            //this.playStage.next();
                        }

                    }

                ]
            }

        timeline.play();         

    }

}
