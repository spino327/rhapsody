/*
 * U2UAbstractNodeRender.fx
 *
 * Created on 26-may-2009, 18:33:36
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
