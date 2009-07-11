
package animation;

import javafx.scene.Scene;
import javafx.scene.Group;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.paint.Color;
import javafx.scene.effect.Glow;
import javafx.scene.text.TextOrigin;
import javafx.scene.text.TextAlignment;


/**
 * This class is the Root class of the U2U Scene pool of classes
 * His main propuse is provide an access mechanics to a ContentStage object.
 *
 * @author sergio
 */
public abstract class AbstractScene extends Scene {

    protected var playStage : PlayStage;
    protected var titleScene: String;
    protected var textScene: String;
    //protected var animation:


    public abstract function start(): Void;

    protected function showTitle(): Group {
        
        return Group {

             content: [
                Text {
                    content: this.titleScene;
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
                    content: this.textScene;

                    font: Font.font("Papyrus", 20);
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
    }


}

/**
 *Methods static
 */
public function getIntroAnimation(play: PlayStage): IntroAnimation {

    return IntroAnimation {
        playStage: play;
        width: 600;
        height: 400;
    }
}

public function getScene1(play: PlayStage):Scene1{

    return Scene1{
        playStage: play;
        width: 800;
        height: 600;
    }
}

public function getScene2(play: PlayStage):Scene2{

    return Scene2{
        playStage: play;
        width: 800;
        height: 600;
    }
}

public function getScene3(play: PlayStage):Scene3{

    return Scene3{
        playStage: play;
        width: 800;
        height: 600;
    }
}

public function getScene4(play: PlayStage):Scene4{

    return Scene4{
        playStage: play;
        width: 800;
        height: 600;
    }
}

public function getSceneFinal(play: PlayStage):SceneFinal{

    return SceneFinal{
        playStage: play;
        width: 800;
        height: 600;
    }
}