/*
 * U2UAbstractNodeRender.fx
 *
 * Created on 26-may-2009, 18:33:36
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
package org.u2u.gui;

import javafx.scene.Node;
import org.u2u.data.U2UAbstractNode;
import javafx.scene.image.Image;
import org.u2u.data.TypeFile;

/**
 * This class' subclasses know how to render a specific a U2UAbstractNode's subclass
 * The U2UList uses it for render the nodes on himself.
 * This interface defines the method required by any object that would like to be a renderer for cells in a U2UList.
 * @author sergio
 */
public abstract class U2UAbstractNodeRender {

    /** rect size, if you want to change the width and height asign a new values for each of them in the subclass*/
    protected var width: Integer = 370;
    protected var height: Integer = 90;
    /**
     * return the GUI Node representation of the U2UAbstractNode's subclass instance,
     * it can be an instance of a subclass of javafx.scene.Node
     */
    public abstract function getNodeView(dataNode: U2UAbstractNode): Node;

    /** return the image to show*/
    protected function getTypeFile(node: U2UAbstractNode): Image {

        var nam:String = node.getName();
        var type:String= TypeFile.getTypeFile(nam.substring(nam.indexOf('.')+1));

        var imgType:Image = TypeFile.getImageTypeFile(type);

        return imgType;
    }
}
