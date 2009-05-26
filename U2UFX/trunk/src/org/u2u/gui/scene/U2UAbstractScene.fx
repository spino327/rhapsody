/*
 * U2UAbstractScene.fx
 *
 * Created on 17-may-2009, 16:22:18
 */

package org.u2u.gui.scene;

import org.u2u.gui.U2UContentStage;
import javafx.scene.Scene;
import org.u2u.gui.scene.U2UShareScene;
import org.u2u.gui.scene.U2UDownloadScene;
import javafx.stage.Stage;


/**
 * This class is the Root class of the U2U Scene pool of classes
 * His main propuse is provide an access mechanics to a ContentStage object.
 *
 * @author sergio
 */
public abstract class U2UAbstractScene extends Scene {

    protected var contentStage : U2UContentStage;

}

/**
 *Methods static
 */
public function getU2UIntroAnimation(inStage: Stage, contStage: U2UContentStage): U2UIntroAnimation {

    return U2UIntroAnimation {
        initStage: inStage;
        contentStage: contStage;
        width: 650;
        height: 500;
    }
}

public function getU2UShareScene(cont:U2UContentStage):U2UShareScene{

    return U2UShareScene{
        contentStage:cont;
        width:660;
        height:550;
    }
}

public function getU2USearchScene(cont:U2UContentStage):U2USearchScene{

    return U2USearchScene{
        contentStage:cont;
        width:650;
        height:500
    }
}

public function getU2UDownloadScene(cont:U2UContentStage):U2UDownloadScene{

    return U2UDownloadScene{
        contentStage:cont;
        width:650;
        height:500
    }
}

