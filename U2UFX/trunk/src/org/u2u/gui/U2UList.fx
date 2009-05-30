/*
 * U2UAbstractList.fx
 *
 * Created on 19-may-2009, 12:21:26
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
package org.u2u.gui;

import javafx.scene.Group;

import org.u2u.data.U2UAbstractListModel;
import javafx.scene.input.MouseEvent;
import javafx.scene.Node;
import javafx.scene.Cursor;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;
import javafx.scene.effect.Glow;
import javafx.scene.effect.Flood;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.lang.Math;
import javafx.animation.Interpolator;
import javafx.animation.Timeline;

import org.memefx.popupmenu.*;
import javafx.scene.text.Font;
import javafx.scene.effect.Shadow;
import java.lang.System;

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
        println("firstPos change {this.firstPos}");
        //this.updateUI();
    };
    /** the nodes can be move?*/
    var canMove: Boolean = false on replace {
        println("canMove change {this.canMove}");
    };

    override var content = [] on replace {
        println("the U2UList content change, now it have {sizeof this.content} elements");
    };


    /** group of nodes*/
//    var groupList:Group = Group {
//
//        cache: true;
//        cursor: Cursor.HAND;
//
//        onMouseClicked:function(me:MouseEvent) {
//            this.click(me);
//        }
//        onMouseDragged:function(me:MouseEvent) {
//            this.dragg(me);
//        }
//    };

    /** drag variables*/
    var memoryDragPoint: Float = 0;
    var memoryDragLength: Float = 0;
    /** memory of the factor dY/height*/
    var memoryDyH: Float = 0;
    /** must execute the fixing algorithm?*/
    var mustFix: Boolean = false;
    /** time line for the transitions in the drag method*/
    var transition: Timeline = Timeline{};

    /** this varibale handle the Selected node's position in the U2UList*/
    var selectedNodeIndex: Integer = -1;
    /** this variable handle the previous Selected node's reference*/
    var previousSelectedNodeReference: Node = null;
    /** Number of nodes to render with the settings*/
    public-read var numOfNodes: Integer;
    /** Number of pixels for the vertical margins with the settings*/
    //var marginV: Integer;
    /** Number of pixels for the horizontal margins with the settings*/
    //var marginH: Integer;


    /** spacing between nodes*/
    public-init var spacingNodes: Integer = 4;
    /** the specific render*/
    public-init var render: U2UAbstractNodeRender;

    init {

        this.cache = true;

        //init the positions on the HashMap
        //450 is the background image's heigh
        numOfNodes = 450 / (render.height + spacingNodes);
        //marginV = (450 - numOfNodes*(render.height + spacingNodes)) / 2;
        //420 is the background image's width
        //marginH = (420 - render.width) / 2;
        this.translateX = 210 + (420 - render.width) / 2;
        this.translateY = 25 + (450 - numOfNodes*(render.height + spacingNodes)) / 2;

        println("numOfNOdes = {numOfNodes}, heigh={render.height}, spacing = {spacingNodes}");

        //cachedNodes.put("prev", null);
        //cachedPos.put("prev", null);
        for(i in [0..<numOfNodes]) {
            cachedNodes.put("{i}", null);
            cachedPos.put("{i}", null);
        }
        //cachedNodes.put("next", null);
        //cachedPos.put("next", null);

        println("final hasmap size = {cachedNodes.size()}");

        onMouseReleased = function(me: MouseEvent) {

            println("onMouseReleased");
            this.released(me);
        };


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
        this.selectedNodeIndex = -1;

        if(model != null) {

            var size:Integer = model.getSize();

            //fixing the first
            firstPos = if(firstPos >= size) then (firstPos - size) else firstPos;
            var i: Integer = 0;
            //can be move
            if(size > numOfNodes) {

                this.canMove = true;

                if(size - firstPos >= numOfNodes) {

                    for(x in [firstPos..<(numOfNodes + firstPos)])
                    {
                        var itmp: Integer = i++;
                        println("value of x[{itmp}] = {x}");
                        cachedPos.put("{itmp}", x);
                    }
                    //(firstPos - 1 >= 0 ? (firstPos - 1) : (size - 1))
//                    var down = if(firstPos - 1 >= 0) then (firstPos - 1) else (size - 1);
//                    println("cached down[-1] = {down}");
//                    cachedPos.put("prev", down);

//                    var up = if(firstPos + numOfNodes < size) then (firstPos + numOfNodes) else (size - (firstPos + numOfNodes));
//                    println("cached up[4] = {up}");
//                    cachedPos.put("next", up);
                }
                else
                {

                    for(x in [firstPos..<size])
                    {
                        var itmp: Integer = i++;
                        println("value of x[{itmp}] = {x}");
                        cachedPos.put("{itmp}", x);
                    }

                    for(x in [0..<(numOfNodes - (size - firstPos))])
                    {
                        var itmp: Integer = i++;
                        println("value of x[{itmp}] = {x}");
                        cachedPos.put("{itmp}", x);
                    }

//                    var down = if(firstPos - 1 >= 0) then (firstPos - 1) else (size - 1);
//                    println("cached down[-1] = {down}");
//                    cachedPos.put("prev", down);

//                    var up = (numOfNodes - (size - firstPos));
//                    println("cached up[4] = {up}");
//                    cachedPos.put("next", up);
                }
            }
            else
            {
                this.canMove = false;

                for(x in [0..<size])
                {
                    var itmp: Integer = i++;
                    println("value of x[{itmp}] = {x}");
                    cachedPos.put("{itmp}", x);
                }
            }

            //drawing
            var cont: Node[] = [];
            //without prev and next
            var to: Integer = if(size <= (cachedPos.size())) then (size) else (cachedPos.size());
            for(x in [0..<to]) {

                println("{x}, {cachedPos.get("{x}")}");
                
                var node = render.getNodeView(model.getNodeAt(cachedPos.get("{x}") as Integer));

                //adding the feature functions, click and drag
                node.onMouseClicked = function(me: MouseEvent) {

                    this.click(me);
                };
                node.onMouseDragged = function(me: MouseEvent) {

                    this.drag(me);
                }
                node.onMouseEntered = function(me: MouseEvent) {

                    //println("onMouseEntered within the node");

                    //FIXME must be a class for it
                    for(element in (me.node as Group).content) {
                        var ntmp = element as Node;
                        if(ntmp.id.equals("background")) {
                            (ntmp as Rectangle).effect = Glow {
                                level: 0.5;
                            }
                            break;
                        }
                    }
//                    me.node.effect = Glow {
//                        level: 0.5;
//                    }
                    
                };
                node.onMouseExited = function(me: MouseEvent) {

                    //println("onMouseExited within the node");
                    //me.node.effect = null;
                    //FIXME must be a class for it
                    for(element in (me.node as Group).content) {
                        var ntmp = element as Node;
                        if(ntmp.id.equals("background")) {
                            (ntmp as Rectangle).effect = null;
                            break;
                        }
                    }
                };

                node.translateY = render.height*x + spacingNodes*(x+1);
                node.cursor = Cursor.HAND;
                cachedNodes.put("{x}", node);
                insert node into cont;
            }

            this.content = cont;

            System.gc();
        }

    }


    /** This event is partner with the drag method, mouse released*/
    function released(me:MouseEvent): Void {

        println("released, must do the algorithm = {mustFix}");

        if(this.mustFix) {

            if(this.canMove) {

                this.updateUI();
            }
            else {
                
                var delta: Float = (-1)*this.content[0].translateY;
                for(element in this.content) {

                        var tmpNode: Node = element as Node;
                        tmpNode.translateY = tmpNode.translateY + delta;
                }
            }


            this.mustFix = false;
        }
        
    }


    /** this method manage the drag event of all the Nodes*/
    function drag(me:MouseEvent): Void {
        
        if(this.memoryDragPoint == me.dragAnchorY)
        {
            //1. calculating the DeltaY/render.height factor, know how many positions move
            var deltaMove: Float = me.dragY - this.memoryDragLength;;//delta between y2 and y1
            var factor: Float; //how many coins move
            var factorFix: Float;//factorFix = dY/height - memory

            if(this.canMove) {

                factor = me.dragY/render.height;//new
                factorFix = factor - this.memoryDyH;

                if(Math.abs(factorFix) > 0.5) {
                    this.memoryDyH = this.memoryDyH + factorFix ;
                    println("memoryDyH change to {this.memoryDyH}");

                    //add coins to the content
                    var size: Integer = model.getSize();
                    var facFixRound: Integer = Math.round(factorFix);
                    
                    if(factorFix > 0) {
                        println("down");

                        for(x in [1..facFixRound]) {

                            var pos: Integer = firstPos - x;
                            //fixing the post
                            pos = if(pos < 0) then (pos + size) else (pos);
                            pos = if(pos >= size) then (pos - size) else (pos);
                            println("pos = {pos}");
                            
                            var ntmp: Node = render.getNodeView(model.getNodeAt(pos));
                            ntmp.translateY = this.content[0].translateY - (render.height + spacingNodes);
                            insert ntmp before content[0];
                        }

                    }
                    else if (factorFix < 0) {
                        println("up, abs({Math.abs(facFixRound)})");

                        for(x in [1..Math.abs(facFixRound)]) {

                            var pos: Integer = firstPos + numOfNodes + x;
                            //fixing the post
                            pos = if(pos < 0) then (pos + size) else (pos);
                            pos = if(pos >= size) then (pos - size) else (pos);
                            println("pos = {pos}");

                            var ntmp: Node = render.getNodeView(model.getNodeAt(pos));
                            ntmp.translateY = this.content[(sizeof this.content) - 1].translateY + (render.height + spacingNodes);
                            insert ntmp into this.content;
                        }
                    }

                    firstPos = firstPos - facFixRound;
                    println("facFixRound = {facFixRound}, firstPos = {firstPos}");
                    //fixing the first
                    firstPos = if(firstPos < 0) then (firstPos + size) else (firstPos);
                    println("firstPos = {firstPos}");
                    firstPos = if(firstPos >= size) then (firstPos - size) else (firstPos);
                    println("firstPos = {firstPos}");


                }

            }

            //2. move and play
            for(element in this.content) {

                    var tmpNode: Node = element as Node;
                    tmpNode.translateY = tmpNode.translateY + deltaMove;
            }

            //3. fixing
            //the fixing is do by the released method in this class
            this.mustFix = true;
            
        }
        else
        {
            println("differents");

            this.memoryDragPoint = me.dragAnchorY;
            this.memoryDyH = 0;
            this.mustFix = false;
        }
        this.memoryDragLength = me.dragY;


    }

    /** this method manage the click event of all the Nodes*/
    function click(me:MouseEvent): Void {
        println("****click function execute on the U2UList");
        //incoming Node
        var innode: Node = me.node;
        //looking the Node's position in the U2UList
        for(x in this.cachedNodes.entrySet()) {

            var entry: Map.Entry = x as Map.Entry;

            var key: String = entry.getKey() as String;
            var value: Node = entry.getValue() as Node;

            if(innode.equals(value)) {
                println("****the selected Node was the ({key})");
                //registering it in the selectedNodeIndex variable
                this.selectedNodeIndex = this.cachedPos.get(key) as Integer;
                //changing the look of the node
                if(this.previousSelectedNodeReference != null) {
                    //FIXME must be a class for it
                    for(element in (this.previousSelectedNodeReference as Group).content) {
                        var ntmp = element as Node;
                        if(ntmp.id.equals("background")) {
                            (ntmp as Rectangle).fill = Color.GREY;
                            break;
                        }
                    }
                }
                //FIXME must be a class for it
                for(element in (innode as Group).content) {
                    var ntmp = element as Node;
                    if(ntmp.id.equals("background")) {
                        (ntmp as Rectangle).fill = Color.BLUEVIOLET;
                        break;
                    }
                }



                this.previousSelectedNodeReference = innode;

            }
            
        }

    }

    /**
     * return the current selected node in the U2UList, if there isn any node selected so return -1,
     * otherwise return 0..n-1
     */
    public function getSelectedIndex(): Integer {

        return this.selectedNodeIndex;
    }



}
