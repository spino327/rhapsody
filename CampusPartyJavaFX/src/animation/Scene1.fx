/*
 * Scene1.fx
 *
 * Created on 10-jul-2009, 6:43:17
 */

package animation;

import javafx.scene.paint.Color;
import javafx.animation.Interpolator;
import javafx.scene.layout.VBox;
import javafx.geometry.HPos;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.effect.Glow;
import javafx.scene.Group;
import javafx.scene.text.TextOrigin;
import javafx.scene.text.TextAlignment;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/**
 * @author sergio
 */

public class Scene1 extends AbstractScene {

    init {
        this.title = "7:00 PM Campus Party, Colombia..."
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

                    },
                    KeyFrame{
                        time:5s;
                        
                        action:function() {
                            println("termino");
                            player.stop();
                        }

                    },
                    KeyFrame{
                        time: 10s;

                        action:function(){
                            println("termino 2");
                        }

                    }

                ]
            }

        this.content = Group {

             content: [
                Text {
                    content: "Scena 1:";
                    font: Font.font("Papyrus", 30);
                    fill: Color.WHITESMOKE;
                    effect: Glow {
                            level: 1
                        }
                    
                    translateX: 20;
                    translateY: 60;
                    textOrigin: TextOrigin.BOTTOM;
                    textAlignment: TextAlignment.LEFT;

                },

                Text {
                    content: this.title;

                    font: Font.font("Papyrus", 30);
                    fill: Color.BLACK;
                    effect: Glow {
                            level: 1
                        }

                    translateX: 20;
                    translateY: 250;
                    textOrigin: TextOrigin.BOTTOM;
                    textAlignment: TextAlignment.LEFT;
                }


             ];
         }

         timeline.play();
    }

}
