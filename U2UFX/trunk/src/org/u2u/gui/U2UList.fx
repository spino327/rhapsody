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
import javafx.scene.Cursor;
import java.util.HashMap;
import java.util.Set;

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
    var cachedNodes: HashMap = new HashMap();
    /** this array manage the relative positions of the current nodes at the model*/
    var cachedPos: HashMap = new HashMap();
    /** represents the current position in the model of the first node of the this list*/
    var firstPos: Integer = 0 on replace {
        println("firstPos change");
        this.updateUI();
    };

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

    /** drag variables*/
    var memoryDragPoint: Number = 0;
    var memoryDragLength: Number = 0;
    /** */
    var selectedNodeIndex:Integer = 0;
    /** Number of nodes to render with the settings*/
    var numOfNodes: Integer;
    /** Number of pixels for the vertical margins with the settings*/
    var marginV: Integer;
    /** Number of pixels for the horizontal margins with the settings*/
    var marginH: Integer;

    /** spacing between nodes*/
    public-init var spacingNodes: Integer = 4;
    /** the specific render*/
    public-init var render: U2UAbstractNodeRender;

    init {

        this.translateX = 230;
        this.translateY = 40;
        this.cache = true;

        //init the positions on the HashMap
        //450 is the background image's heigh
        numOfNodes = 450 / (render.height + spacingNodes);
        marginV = (450 - numOfNodes*(render.height + spacingNodes)) / 2;
        //420 is the background image's width
        marginH = (420 - render.width) / 2;

        println("numOfNOdes = {numOfNodes}, marginsV = {marginV}, marginsH = {marginH}, heigh={render.height}, spacing = {spacingNodes}");

        cachedNodes.put("prev", null);
        cachedPos.put("prev", null);
        for(i in [0..<numOfNodes]) {
            cachedNodes.put("{i}", null);
            cachedPos.put("{i}", null);
        }
        cachedNodes.put("next", null);
        cachedPos.put("next", null);

        println("final hasmap size = {cachedNodes.size()}");

    }
  
    //instance functions
    public function setModel(model:U2UAbstractListModel):Void {
        this.model = model;
    }

    /** this methods force the re-drawing of the Lisr*/
    public function updateUI():Void {

//        if((model != null) and (model.getSize() > 0))  {
//            var size:Integer = model.getSize();
//
//            var transY = 4;
//
//            var node: Node =  render.getNodeView(model.getNodeAt(0));
//            node.translateY = transY;
//            node.translateX = 15;
//
//            con = [node];
//
//            this.content = this.groupList;
//        }

        if(model != null) {

            var size:Integer = model.getSize();

            //fixing the first
            firstPos = if(firstPos >= size) then (firstPos - size) else firstPos;
            var i: Integer = 0;
            if(size > numOfNodes) {

                //System.out.println("se mueven");
                if(size - firstPos >= numOfNodes) {

                    println("size - firstPos >= numOfNodes");

                    for(x in [firstPos..<(numOfNodes + firstPos)])
                    {
                        var itmp: Integer = i++;
                        println("valor de x[{itmp}] = {x}");
                        cachedPos.put("{itmp}", x);
                    }
                    //(firstPos - 1 >= 0 ? (firstPos - 1) : (size - 1))
                    var down = if(firstPos - 1 >= 0) then (firstPos - 1) else (size - 1);
                    println("cached down[-1] = {down}");
                    cachedPos.put("prev", down);

                    var up = if(firstPos + numOfNodes < size) then (firstPos + numOfNodes) else (size - (firstPos + numOfNodes));
                    println("cached up[4] = {up}");
                    cachedPos.put("next", up);
                }
                else
                {
                    println("size - firstPos < numOfNodes");

                    for(x in [firstPos..<size])
                    {
                        var itmp: Integer = i++;
                        println("valor de x[{itmp}] = {x}");
                        cachedPos.put("{itmp}", x);
                    }

                    for(x in [0..<(numOfNodes - (size - firstPos))])
                    {
                        var itmp: Integer = i++;
                        println("valor de x[{itmp}] = {x}");
                        cachedPos.put("{itmp}", x);
                    }

                    var down = if(firstPos - 1 >= 0) then (firstPos - 1) else (size - 1);
                    println("cached down[-1] = {down}");
                    cachedPos.put("prev", down);

                    var up = (numOfNodes - (size - firstPos));
                    println("cached up[4] = {up}");
                    cachedPos.put("next", up);
                }
            }
            else
            {
                println("no se mueven");

                for(x in [0..<size])
                {
                    var itmp: Integer = i++;
                    println("valor de x[{itmp}] = {x}");
                    cachedPos.put("{itmp}", x);
                }
            }

            //drawing
            var cont: Node[] = [];
            //without prev and next
            var to: Integer = if(size <= (cachedPos.size()-2)) then (size) else (cachedPos.size()-2);
            for(x in [0..<to]) {

                println("{x}, {cachedPos.get("{x}")}");
                
                var node = render.getNodeView(model.getNodeAt(cachedPos.get("{x}") as Integer));

                node.translateY = render.height*x + spacingNodes*(x+1);
                node.translateX = marginH;
                cachedNodes.put("{x}", node);
                insert node into cont;
            }

            this.groupList.content = cont;
            this.content = this.groupList.content;

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
