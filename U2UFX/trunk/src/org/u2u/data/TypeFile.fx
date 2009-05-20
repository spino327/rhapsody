/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.u2u.data;

import javafx.scene.image.Image;
import java.io.File;


/**
 *
 * @author irene & sergio
 */
def types: Image[] = [

    Image{url:"{__DIR__}resources/images.png"},
        Image{url:"{__DIR__}resources/text.png"},
            Image{url:"{__DIR__}resources/music.png"},
                Image{url:"{__DIR__}resources/video.png"},
                    Image{url:"{__DIR__}resources/others.png"},
];
    

public def IMAGE:String  = "img";
public def TEXT:String  = "txt";
public def MUSIC:String  = "music";
public def VIDEO:String  = "video";
public def OTHER:String  = "other";

public function getIconImage():Image
{
    return types[0];
}

public function getIconText():Image
{
    return types[1];
}

public function getIconMusic():Image
{
    return types[2];
}

public function getIconVideo():Image
{
    return types[3];
}

public function getIconOthers():Image
{
    return types[4];
}

public function getTypeFile(ext:String):String
{
    //Si el archivo es de tipo imagen
    if(ext.toLowerCase().endsWith("png") or ext.toLowerCase().endsWith("tiff") or
           ext.toLowerCase().endsWith("tif") or ext.toLowerCase().endsWith("jpeg") or
              ext.toLowerCase().endsWith("jpg") or ext.toLowerCase().endsWith("gif"))
    {
        return TypeFile.IMAGE;
    }
    else if (ext.toLowerCase().endsWith("pdf") or ext.toLowerCase().endsWith("doc") or
                ext.toLowerCase().endsWith("txt") or ext.toLowerCase().endsWith("dot") or
                 ext.toLowerCase().endsWith("ppt") or ext.toLowerCase().endsWith("xls") or
                  ext.toLowerCase().endsWith("ppa") or ext.toLowerCase().endsWith("mdb") or
                   ext.toLowerCase().endsWith("rtf") or ext.toLowerCase().endsWith("pps") or
                    ext.toLowerCase().endsWith("zip") or ext.toLowerCase().endsWith("tar"))
    {
        return TypeFile.TEXT;
    }
    else if(ext.toLowerCase().endsWith("sda") or ext.toLowerCase().endsWith("pna")  or
             ext.toLowerCase().endsWith("mp3") or ext.toLowerCase().endsWith("mp4")  or
              ext.toLowerCase().endsWith("midi") or ext.toLowerCase().endsWith("mid") or
               ext.toLowerCase().endsWith("mpeg") or ext.toLowerCase().endsWith("wav") or
                ext.toLowerCase().endsWith("wma") or ext.toLowerCase().endsWith("cda"))
    {
        return TypeFile.MUSIC;
    }
    else if(ext.toLowerCase().endsWith("ogg") or ext.toLowerCase().endsWith("ogm")  or
             ext.toLowerCase().endsWith("asf") or ext.toLowerCase().endsWith("avi")  or
              ext.toLowerCase().endsWith("div") or ext.toLowerCase().endsWith("divx") or
               ext.toLowerCase().endsWith("dvd") or ext.toLowerCase().endsWith("divx") or
             ext.toLowerCase().endsWith("mpg") or ext.toLowerCase().endsWith("qt")   or
              ext.toLowerCase().endsWith("qtl") or ext.toLowerCase().endsWith("rpm")  or
               ext.toLowerCase().endsWith("wm") or ext.toLowerCase().endsWith("wmv")   or
                ext.toLowerCase().endsWith("miv") or ext.toLowerCase().endsWith("mov")  or
             ext.toLowerCase().endsWith("movie") or ext.toLowerCase().endsWith("mp2v")or
              ext.toLowerCase().endsWith("mpe") or ext.toLowerCase().endsWith("rpm") or
              ext.toLowerCase().endsWith("flv"))
    {
        return TypeFile.VIDEO;
    }
    else {
        return TypeFile.OTHER;
    }
}

public function getTypeFile(file: File): String
{
    //Si el archivo es de tipo imagen
    if(file.getName().toLowerCase().endsWith("png") or file.getName().toLowerCase().endsWith("tiff") or
          file.getName().toLowerCase().endsWith("tif") or file.getName().toLowerCase().endsWith("jpeg") or
              file.getName().toLowerCase().endsWith("jpg") or file.getName().toLowerCase().endsWith("gif"))
    {
        return TypeFile.IMAGE;
    }
    else if (file.getName().toLowerCase().endsWith("pdf") or file.getName().toLowerCase().endsWith("doc") or
                file.getName().toLowerCase().endsWith("txt") or file.getName().toLowerCase().endsWith("dot") or
                 file.getName().toLowerCase().endsWith("ppt") or file.getName().toLowerCase().endsWith("xls") or
                  file.getName().toLowerCase().endsWith("ppa") or file.getName().toLowerCase().endsWith("mdb") or
                   file.getName().toLowerCase().endsWith("rtf") or file.getName().toLowerCase().endsWith("pps") or
                    file.getName().toLowerCase().endsWith("zip") or file.getName().toLowerCase().endsWith("tar"))
    {
        return TypeFile.TEXT;
    }
    else if(file.getName().toLowerCase().endsWith("sda") or file.getName().toLowerCase().endsWith("pna")  or
             file.getName().toLowerCase().endsWith("mp3") or file.getName().toLowerCase().endsWith("mp4")  or
              file.getName().toLowerCase().endsWith("midi") or file.getName().toLowerCase().endsWith("mid") or
               file.getName().toLowerCase().endsWith("mpeg") or file.getName().toLowerCase().endsWith("wav") or
                file.getName().toLowerCase().endsWith("wma") or file.getName().toLowerCase().endsWith("cda"))
    {
        return TypeFile.MUSIC;
    }
    else if(file.getName().toLowerCase().endsWith("ogg") or file.getName().toLowerCase().endsWith("ogm")  or
             file.getName().toLowerCase().endsWith("asf") or file.getName().toLowerCase().endsWith("avi")  or
              file.getName().toLowerCase().endsWith("div") or file.getName().toLowerCase().endsWith("divx") or
               file.getName().toLowerCase().endsWith("dvd") or file.getName().toLowerCase().endsWith("divx") or
             file.getName().toLowerCase().endsWith("mpg") or file.getName().toLowerCase().endsWith("qt")   or
              file.getName().toLowerCase().endsWith("qtl") or file.getName().toLowerCase().endsWith("rpm")  or
               file.getName().toLowerCase().endsWith("wm") or file.getName().toLowerCase().endsWith("wmv")   or
                file.getName().toLowerCase().endsWith("miv") or file.getName().toLowerCase().endsWith("mov")  or
             file.getName().toLowerCase().endsWith("movie") or file.getName().toLowerCase().endsWith("mp2v")or
              file.getName().toLowerCase().endsWith("mpe") or file.getName().toLowerCase().endsWith("rpm") or
              file.getName().toLowerCase().endsWith("flv"))
    {
        return TypeFile.VIDEO;
    }
    else {
        return TypeFile.OTHER;
    }

}
