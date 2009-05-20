/*
 * U2UDownloadListModel.fx
 *
 * Created on 19-may-2009, 17:23:59
 */

package org.u2u.data;

import org.u2u.data.U2UAbstractNode;
import java.util.LinkedList;

/**
 * @author sergio
 */

public class U2UDownloadListModel extends U2UAbstractListModel {

    //instance variables
    var list:LinkedList = LinkedList{};

    init {
        list.add(U2UDownloadNode{
                name: "UNO";
            });
        list.add(U2UDownloadNode{
                name: "DOS";
            });
        list.add(U2UDownloadNode{
                name: "TRES";
            });

    }


    //instance methods
    override function getSize():Integer {
        return list.size();
    }

    override function getNodeAt(index:Integer): U2UAbstractNode {
        return list.get(index) as U2UAbstractNode;
    }

}
