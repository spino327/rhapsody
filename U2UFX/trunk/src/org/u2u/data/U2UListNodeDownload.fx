/*
 * U2UListNodeDownload.fx
 *
 * Created on 19-may-2009, 9:04:17
 */

package org.u2u.data;

import javafx.scene.image.Image;

/**
 * @author sergio
 */

public class U2UListNodeDownload extends U2UListNode {

    override function setNameFile(name:String):Void{
        this.name = name;
    }
    
    override function setDescription(desc:String):Void{
        this.description = desc;
    }

    override function setType(type:String):Void{
        this.type = type;
    }

    override function setlenght(size:Number):Void{
        this.lenght = size;
    }

    override function setBackgroundImage(img:Image):Void{
        this.backgroundImg = img;
    }

    override function getNameFile(name:String):Void{
        return this.name;
    }
    override function getDescription(desc:String):Void{
        return this.description;
    }
    override function getType(type:String):Void{
        return this.type;
    }

    override function getlenght(size:Number):Void{
        return this.lenght;
    }

}
