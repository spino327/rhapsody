/*
 * U2UAbstractScene.fx
 *
 * Created on 17-may-2009, 16:22:18
 */

package org.u2u.gui.scene;

import org.u2u.gui.ContentStage;
import javafx.scene.Scene;
import org.u2u.gui.scene.U2UShareScene;
import org.u2u.gui.scene.U2UDownloadScene;


/**
 * This class is the Root class of the U2U Scene pool of classes
 * His main propuse is provide an access mechanics to a ContentStage object.
 *
 * @author sergio
 */
public abstract class U2UAbstractScene extends Scene {

    protected var contentStage : ContentStage;
}

/**
 *Methods static
 */
public function getU2UIntroAnimation(cont: ContentStage): U2UIntroAnimation {

    return U2UIntroAnimation {
        contentStage:cont;
        width:650;
        height:500;
    }
}

public function getU2UShareScene(cont:ContentStage):U2UShareScene{

    return U2UShareScene{
        contentStage:cont;
        width:650;
        height:500
        }
}

public function getU2USearchScene(cont:ContentStage):U2USearchScene{

    return U2USearchScene{
        contentStage:cont;
        width:650;
        height:500
        }
}

public function getU2UDownloadScene(cont:ContentStage):U2UDownloadScene{

    return U2UDownloadScene{
        contentStage:cont;
        width:650;
        height:500
        }
}

