/*
 * U2UAbstractList.fx
 *
 * Created on 19-may-2009, 12:21:26
 */

package org.u2u.gui;

import javafx.scene.Group;

import org.u2u.data.U2UAbstractListModel;
import javafx.scene.input.MouseEvent;
import javafx.scene.Node;
import java.util.Vector;
import javafx.scene.Cursor;

/**
 * @author sergio
 */
public class U2UList extends Group {

    //instance variables
    var model: U2UAbstractListModel = null on replace{
            this.updateUI();
            println("The model of the List change");
    };

    /** this arrya manage the current nodes in the View and two for cached*/
    var cachedNodes: Vector = new Vector();
    /** represents the current position in the model of the first node of the this list*/
    var firstPos: Integer = 0;
    /** group of nodes*/
    var groupList:Group = Group {
        cache: true;
        cursor: Cursor.HAND;

        onMouseClicked:function(me:MouseEvent) {

            this.click(me);
        }

        onMouseDragged:function(me:MouseEvent) {
            this.dragg(me);
        }
    };

    override var content = bind [this.groupList];

    /** drag variables*/
    var memoryDragPoint: Number = 0;
    var memoryDragLength: Number = 0;

    //var yPrimero: Number;

    /** spacing between nodes*/
    var spacingNodes: Number;
    /** */
    var selectedNodeIndex:Integer = 0;


    init {

        this.translateX = 210;
        this.translateY = 25;

        this.cache = true;
    }
  
    //instance functions
    public function setModel(model:U2UAbstractListModel):Void {
        this.model = model;
    }

    /** this methods force the re-drawing of the Lisr*/
    public function updateUI():Void {

        var size:Integer = model.getSize();
        
        //fill the principals nodes
        for(x in [firstPos..< if(size >= 4) then (4) else (size)],
            y in [0..<4]) {
                
            cachedNodes.insertElementAt(model.getNodeAt(x), y);
        }


    }


    function dragg(me:MouseEvent): Void {
        //println("dragg function execute desde:{me.dragAnchorY} longitud:{me.dragY}");
        var g:Node[] = this.groupList.content;
        var delta:Number = 0;

        //println("new = {me.dragY} and old = {this.memoryDragLength}");

        if(this.memoryDragPoint == me.dragAnchorY)
        {
            println("equals");

            delta = me.dragY - this.memoryDragLength;
            this.memoryDragLength = me.dragY;
        }
        else
        {
           
            println("differents");
            this.memoryDragPoint = me.dragAnchorY;
            this.memoryDragLength = me.dragY;
        }

        if(g[0].translateY + delta <= 0)
        {

            var deltaFactor: Float = (1.0 * (delta/2)) / (107/2);
            println("deltaFactor = {deltaFactor}, delta = {delta}, scaleYant = {g[0].scaleY}");
            g[0].scaleY = g[0].scaleY + deltaFactor;
            println("new scaleY = {g[0].scaleY}");

            if(g[0].scaleY <= 0.2)
            {
                var tmp = g[0];
                tmp.scaleY = 1.0;
                g[0] = g[1];
                g[1] = g[2];
                g[2] = g[3];
                g[3] = g[4];
                g[4] = tmp;
            }
            else
            {
                g[1].translateY = g[1].translateY + delta;
                g[2].translateY = g[2].translateY + delta;
                g[3].translateY = g[3].translateY + delta;
                g[4].translateY = g[4].translateY + delta;

            }
        }
        else
        {
            g[0].translateY = g[0].translateY + delta;
            g[1].translateY = g[1].translateY + delta;
            g[2].translateY = g[2].translateY + delta;
            g[3].translateY = g[3].translateY + delta;
            g[4].translateY = g[4].translateY + delta;
        }


        


        println("fig({g[0].translateX}, {g[0].translateY}),  nodo({g[1].translateX}, {g[1].translateY})");

    }

    function click(me:MouseEvent): Void {
        println("click function execute");

    }

}
