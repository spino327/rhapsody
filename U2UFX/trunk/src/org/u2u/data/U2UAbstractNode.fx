/*
 * U2UAbstractNode.fx
 *
 * Created on 19-may-2009, 12:22:02
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
