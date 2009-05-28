/*
 * U2UAbstractListModel.fx
 *
 * Created on 19-may-2009, 14:50:51
 */

package org.u2u.data;

/**
 * This interface link the model with the List view, manage the nodes.
 *
 * @author Irene Manotas And Sergio Pino
 */
public abstract class U2UAbstractListModel {

    /** Returns the value at the specified index*/
    public abstract function getNodeAt(index:Integer):U2UAbstractNode;

    /** Returns the length of the list*/
    public abstract function getSize():Integer;

}