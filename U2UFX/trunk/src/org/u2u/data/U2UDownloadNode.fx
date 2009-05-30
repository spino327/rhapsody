/*
 * U2UDownloadNode.fx
 *
 * Created on 19-may-2009, 16:27:40
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
package org.u2u.data;

import org.u2u.filesharing.U2UContentAdvertisementImpl;

/**
 * @author sergio
 */

public def DOWNLOAD:String = "download";
public def PAUSE:String = "pause";
public def OK:String = "ok";

public class U2UDownloadNode extends U2UAbstractNode {

    /** level of download of this node [max:200- min:0] */
    public-read var level:Float on replace {
        println("the level changet to {level}");
        if(level == 200)
        {
            this.status = U2UDownloadNode.OK;
        }
    };
    /**status for this file: DOWNLOAD or PAUSE*/
    public-read var status:String = U2UDownloadNode.DOWNLOAD;

    //instance methods
    override function getName(): String {
        return this.name;
    }

    /** return the cid in string format*/
    override function getCID(): String {
        return this.cid.toString();
    }

    /** return the sharedfile's length in bytes*/
    override function getLength(): Long {
        return this.length;
    }

    /** return the sharedfile's description*/
    override function getDescription(): String {
        return this.description;
    }

    override function getType(): String {
        return this.type;
    }

    /** chunk's size in kilobytes*/
    override function getChunksize(): Short {
        return this.chunksize;
    }

    override function getAdv(): U2UContentAdvertisementImpl {
        return this.adv
    }

    override function getShellEnv(): String {
        return this.shellEnv;
    }



    /**
    * Change the level of download of the node
    */
    public function updateLevel(lev:Float):Void {
        
        if(lev>=0 and lev<=200 )
        {
            this.level = lev;
        }
    }

    /**
    * Set the estatus of this file: DOWNLOAD or PAUSE
    */
    public function setStatus(status:String):Void{

        if(status.equals(U2UDownloadNode.DOWNLOAD)
            or status.equals(U2UDownloadNode.PAUSE) )
        {
            this.status = status;
        }

    }

//    function getTypeFile():Image {
//
//        var nam:String = this.name as String;
//        var type:String= TypeFile.getTypeFile(nam.substring(nam.indexOf('.')+1));
//
//        var imgType:Image = TypeFile.getImageTypeFile(type);
//
//        return imgType;
//    }



}

