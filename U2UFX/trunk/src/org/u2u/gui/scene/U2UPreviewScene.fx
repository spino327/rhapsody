/*
 * AudioScene.fx
 *
 * Created on 29-may-2009, 9:51:17
 */

package org.u2u.gui.scene;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.Media;
import javafx.scene.media.MediaError;
import javafx.scene.text.Text;
import javafx.scene.Group;
/**
 * @author sergio
 */

public class U2UPreviewScene extends U2UAbstractMain{


    var imgBackground:Image;
    var imgBackView:ImageView;
    var mediaAudio:MediaPlayer;
    var sourceMedia:String;
    
    var table:U2UPreviewTable;

    init {
            imgBackground = Image{
                url:"{__DIR__}resources/content2.png";
            };

            imgBackView = ImageView{
                image:imgBackground;
                translateX:210;
                translateY:25;
            };



           this.contentPane = Group{

               content: [
                    imgBackView ,

                    table = U2UPreviewTable{}

               ];

            };


     }


     /**
    * Return the shared listener
    */
    public function getShareListener():U2UPreviewTable{
        return table;
    }

     public function setSourceMedia(src:String){

         this.sourceMedia = src;
     }

     public function playAudio():Void{

        mediaAudio.play();

     }

     override function updateButtons() {

            butDown.aplyPressed = null;
            butShare.aplyPressed = null;
            butSearch.aplyPressed = null;
    }


}
