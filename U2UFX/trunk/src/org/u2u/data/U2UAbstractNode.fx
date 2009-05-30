/*
 * U2UAbstractNode.fx
 *
 * Created on 19-may-2009, 12:22:02
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
import javafx.scene.Node;
import net.jxta.share.ContentId;


//class varibles, static

/**
 * This class represent an Abstract Node, that in the context of the U2UProject, will be an advertisement
 * in P2P network.
 *
 * @author sergio
 */
public abstract class U2UAbstractNode {

    //instance variables

    //Advertisement's info
    /** content name (required)*/
    protected var name: String;
    /** content id (required)*/
    protected var cid: ContentId;
    /** content length (optional)*/
    protected var length: Long;
    /** content description (optional)*/
    protected var description: String;
    /** content type (optional)*/
    protected var type: String;
    /** content chunks' size*/
    protected var chunksize: Short;
    /** real advertisement*/
    protected var adv: U2UContentAdvertisementImpl;
    /** Shell's environment variable name*/
    protected var shellEnv: String;


    /** return the name of the advertisment*/
    public abstract function getName(): String;
    /** return the cid in string format*/
    public abstract function getCID(): String;
    /** return the sharedfile's length in bytes*/
    public abstract function getLength(): Long;
    /** return the sharedfile's description*/
    public abstract function getDescription(): String;

    public abstract function getType(): String;
    /** chunk's size in kilobytes*/
    public abstract function getChunksize(): Short;
    public abstract function getAdv(): U2UContentAdvertisementImpl;
    public abstract function getShellEnv(): String;

}
