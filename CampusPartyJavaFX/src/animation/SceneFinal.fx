/*
 * SceneFinal.fx
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

public class SceneFinal extends AbstractScene {

    init {

        

        this.titleScene = "Final Scene:";
        this.textScene = "\n\nThank you very much to:\n\n"
                        "- Telebucaramanga\n"
                        "- Universidad Industrial de Santander\n"
                        "- My friends Sergio & Irene\n\n"
                        "Regards.\n\n"
                        "Hoover Fabian Rueda.";

    }

    function sceneAppear(): Group {

        
        var group: Group = Group {

            content: [

                ImageView {
                    image: Image {
                        url: "{__DIR__}resources/logos.png";
                    }
                    translateX: 140;
                    translateY: 150;
                    
                }
            ]

        }

        return group;
    }

    function quitApp():Void{

        FX.exit();

    }

    override function start() {

        fill = Color.DARKGRAY;
        var player: MediaPlayer = MediaPlayer {
            media : Media {
                source: "{__DIR__}resources/Heartbeat.mp3";
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

                            content = sceneAppear();
                           
                        }

                    },
                    KeyFrame{
                        time: 15s;

                        action:function(){
                            println("End");
                            //player.pause();
                            player.stop();
                           
                            quitApp();
                        }

                    }

                ]
            }

        timeline.play();
    }
}

