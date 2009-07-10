
package animation;

import javafx.scene.Scene;


/**
 * This class is the Root class of the U2U Scene pool of classes
 * His main propuse is provide an access mechanics to a ContentStage object.
 *
 * @author sergio
 */
public abstract class AbstractScene extends Scene {

    protected var playStage : PlayStage;
    protected var title: String;
    //protected var animation:


    public abstract function start(): Void;


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
