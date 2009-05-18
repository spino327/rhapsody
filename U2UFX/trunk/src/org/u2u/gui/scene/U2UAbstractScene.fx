/*
 * U2UAbstractScene.fx
 *
 * Created on 17-may-2009, 16:22:18
 */

package org.u2u.gui.scene;

import javafx.scene.Scene;
import org.u2u.gui.ContentStage;

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
 *
 */
public function getU2UIntroAnimation(cont: ContentStage): U2UIntroAnimation {

    return U2UIntroAnimation {
        contentStage: cont;
    }
}
