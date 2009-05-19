/*
 * U2UListNode.fx
 *
 * Created on 19-may-2009, 8:23:47
 */

package org.u2u.data;

import javafx.scene.image.Image;
import javafx.scene.text.Text;

/**
 * @author sergio
 */

public abstract class U2UListNode{


    protected var name:String;
    protected var lenght:Number;
    protected var description:String;
    protected var type:String;

    protected var backgroundImg:Image;
    protected var typeImg:Image;
    protected var nameText:Text;
    protected var descripText:Text;

    abstract function setNameFile(name:String):Void;
    abstract function setDescription(desc:String):Void;
    abstract function setType(type:String):Void;
    abstract function setlenght(size:Number):Void;

    abstract function setBackgroundImage(img:Image):Void;

    abstract function getNameFile(name:String):Void;
    abstract function getDescription(desc:String):Void;
    abstract function getType(type:String):Void;
    abstract function getlenght(size:Number):Void;

}
