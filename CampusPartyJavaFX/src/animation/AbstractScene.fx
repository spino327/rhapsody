
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
//
//public function getU2USearchScene(cont:U2UContentStage):U2USearchScene{
//
//    return U2USearchScene{
//        contentStage:cont;
//        width:650;
//        height:500
//    }
//}
//
//public function getU2UDownloadScene(cont:U2UContentStage):U2UDownloadScene{
//
//    return U2UDownloadScene{
//        contentStage:cont;
//        width:650;
//        height:500
//    }
//}
//
//public function getU2UHelpScene(cont:U2UContentStage):U2UHelpScene{
//
//    return U2UHelpScene{
//        contentStage:cont;
//        width:800;
//        height:600
//    }
//}
//
//public function getU2UPreferencesScene(cont:U2UContentStage):U2UPreferencesScene{
//
//    return U2UPreferencesScene{
//        contentStage:cont;
//        width:650;
//        height:500
//    }
//}
//
//public function getU2UPreviewScene(cont:U2UContentStage):U2UPreviewScene{
//
//    return U2UPreviewScene{
//        contentStage:cont;
//        width:650;
//        height:500
//    }
//}
