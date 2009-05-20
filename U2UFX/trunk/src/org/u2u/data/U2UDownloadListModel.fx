/*
 * U2UDownloadListModel.fx
 *
 * Created on 19-may-2009, 17:23:59
 */

package org.u2u.data;

import org.u2u.data.U2UAbstractNode;
import java.util.LinkedList;
import org.u2u.filesharing.U2UContentAdvertisementImpl;
import org.u2u.data.U2UDownloadNode;

/**
 * @author sergio
 */

public class U2UDownloadListModel extends U2UAbstractListModel {

    //instance variables
    var list:LinkedList = LinkedList{};

    init {
        list.add(U2UDownloadNode{
                name: "UNO.txt";
                level:20;
            });
        list.add(U2UDownloadNode{
                name: "DOS.png";
                level:200
            });
        list.add(U2UDownloadNode{
                name: "TRES.mpge";
            });
        list.add(U2UDownloadNode{
            name: "CUATRO.avi";
        });
        list.add(U2UDownloadNode{
            name: "CINCO.avi";
        });
    }


    //instance methods
    override function getSize():Integer {
        return list.size();
    }

    override function getNodeAt(index:Integer): U2UAbstractNode {
        return list.get(index) as U2UAbstractNode;
    }

    /**
    *Inserts a adv that represents a file into the list
    */
    override function insertFileIntoModel(adv: U2UContentAdvertisementImpl):Void{
        
        if(this.existNode(adv)){
            //The file exists in the list model
            return;

        }else{
            
            var node:U2UDownloadNode = U2UDownloadNode{
                name: adv.getName();
                length:adv.getLength();
                description:adv.getDescription();
                chunksize:adv.getChunksize();
                cid: adv.getContentId();
                type:adv.getType();
                adv: adv;
            }
            
            list.add(node);
         
        }

    }

    /**
    * Verifies if the advertisement that represents the file is in the list of nodes\
    *@return false if the adv isn't in the list
    */
    function existNode(adv:U2UContentAdvertisementImpl):Boolean{

        var max:Integer = list.size();
        for(i in [0..<max])
        {
            var node:U2UDownloadNode = list.get(i) as U2UDownloadNode;

            if(node.adv.equals(adv))
            {
                return true;
            }
        }

        return false;
    }

    /**
    *Delete the node selected of the list
    */
    override function deleteFileOfModel(selIndex:Integer):Void{

        if(selIndex<=list.size() and selIndex>=0)
        {
            list.remove(selIndex);
        }

    }


}
