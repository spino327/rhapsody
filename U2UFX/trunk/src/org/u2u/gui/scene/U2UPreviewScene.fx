/*
 * AudioScene.fx
 *
 * Created on 29-may-2009, 9:51:17
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
