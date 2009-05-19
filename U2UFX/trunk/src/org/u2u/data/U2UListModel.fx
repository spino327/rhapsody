/*
 * U2UListModel.fx
 *
 * Created on 19-may-2009, 8:04:09
 */

package org.u2u.data;

/**
 * @author sergio
 */

/**
* This class manages the model of data for a nodes's list
*/

public abstract class U2UListModel {

    abstract function getNodeCount():Integer;

    /*
    abstract function geFieldCount():Integer;

    abstract function getFieldName():String;

    abstract function getFieldClass():Object;*/

    abstract function getValueAt(row:Integer, col:Integer):Object;

    abstract function setValueAt(val:Object,row:Integer, col:Integer):Void;

}
