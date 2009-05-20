/*
 * U2UAbstractNode.fx
 *
 * Created on 19-may-2009, 12:22:02
 */

package org.u2u.data;

import org.u2u.filesharing.U2UContentAdvertisementImpl;
import javafx.scene.Node;


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
    protected var cid: String;
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
    //

    /** rect size*/
    protected var width: Integer = 390;
    protected var height: Integer = 107;

    //gui representation
    /** group that structure the node's view, need to be always the same for a subclass, 
     * need to be define in the subclass' init method
     */
    //var guiView: Group;
    //EOInstance variables

    /**
     * return the GUI Node representation of this Instance, it can be an instance of a subclass of javafx.scene.Node
     */
    public abstract function getNodeView(): Node;
    
}