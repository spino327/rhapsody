/*
 * U2UAbstractScene.fx
 *
 * Created on 17-may-2009, 16:22:18
 */
/**
 * Copyright (c) 2009, Sergio Pino and Irene Manotas
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of Sergio Pino and Irene Manotas. nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * @author: Sergio Pino and Irene Manotas
 * Website: http://osum.sun.com/profile/sergiopino, http://osum.sun.com/profile/IreneLizeth
 * emails  : spino327@gmail.com - irenelizeth@gmail.com
 * Date   : March, 2009
 * This license is based on the BSD license adopted by the Apache Foundation.
 *
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

public function getU2UHelpScene(cont:U2UContentStage):U2UHelpScene{

    return U2UHelpScene{
        contentStage:cont;
        width:800;
        height:600
    }
}

public function getU2UPreferencesScene(cont:U2UContentStage):U2UPreferencesScene{

    return U2UPreferencesScene{
        contentStage:cont;
        width:650;
        height:500
    }
}

public function getU2UPreviewScene(cont:U2UContentStage):U2UPreviewScene{

    return U2UPreviewScene{
        contentStage:cont;
        width:650;
        height:500
    }
}
