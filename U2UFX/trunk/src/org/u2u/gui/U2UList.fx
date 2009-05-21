/*
 * U2UAbstractList.fx
 *
 * Created on 19-may-2009, 12:21:26
 */

package org.u2u.gui;

import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import org.u2u.data.U2UAbstractListModel;
import javafx.scene.input.MouseEvent;
import javafx.scene.Node;
import java.lang.Math;
import javafx.scene.effect.PerspectiveTransform;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import org.u2u.data.U2UDownloadNode;


/**
 * @author sergio
 */
public class U2UList extends Group {

    //instance variables
    var model: U2UAbstractListModel = null on replace{
            this.updateUI();
        };
    

    /** represents the current position in the model of the first node of the this list*/
    var firstPos: Integer = 0;

    var memoryDragPoint: Number = 0;
    var memoryDragLength: Number = 0;

    var yPrimero: Number;

    /** spacing between nodes*/
    var spacingNodes: Number;
    /** */
    var selectedNodeIndex:Integer = 0;


    init {

        this.translateX = 210;
        this.translateY = 25;
    }


    /** group of nodes*/
    var groupList:Group = Group {

        onMouseClicked:function(me:MouseEvent) {

            this.click(me);
        }

        onMouseDragged:function(me:MouseEvent) {
            this.dragg(me);
        }
    };

   

    //instance functions
    public function setModel(model:U2UAbstractListModel):Void {
        this.model = model;
    }

    /** this methods force the re-drawing of the Lisr*/
    function updateUI():Void {

        var size:Integer = model.getSize();

        var con:Node[] = [];

        //translation axis Y
        var transY = 4;

        for(x in [firstPos..firstPos+3])
        {
            var hNode = 107;

            if(x==0){
//
                model.getNodeAt(x).getNodeView().translateY = transY;
                model.getNodeAt(x).getNodeView().translateX = 15;
                insert model.getNodeAt(x).getNodeView() into con;
                 
            }else{

                model.getNodeAt(x).getNodeView().translateY = hNode*x + transY*(x+1);
                model.getNodeAt(x).getNodeView().translateX = 15;
                insert model.getNodeAt(x).getNodeView() into con;
            }
        }
        if(size>4)
        {
           model.getNodeAt(size-1).getNodeView().translateX = 15;
           model.getNodeAt(size-1).getNodeView().visible=false;
           insert model.getNodeAt(size-1).getNodeView() into con;
        }

        this.groupList.content = [con];

        this.content = this.groupList;

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

        if(g[1].translateY + delta >=0)
        {
            g[1].effect = null;
            g[1].translateY = g[1].translateY + delta;
        }
        else
        {

            var uy: Number = g[1].translateY + delta;
            var ly: Number = g[1].translateY + 107.0;

            println("uy:{uy}, ly:{ly}");
            //efecto
            //g[1].translateY = g[1].translateY + delta;
            g[1].effect = PerspectiveTransform {
                llx: 15.0, lly: ly
                lrx: 390.0, lry: ly
                ulx: 15.0 + 100, uly: 0
                urx: 390.0 - 100, ury: 0
            }


            //g[5].translateY=g[5].translateY-4;
            //g[5].visible = true;
            //println("x = {g[1].}");
        }

        //g[1].translateY = g[1].translateY + delta;
        g[2].translateY = g[2].translateY + delta;
        g[3].translateY = g[3].translateY + delta;
        g[4].translateY = g[4].translateY + delta;


        println("fig({g[0].translateX}, {g[0].translateY}),  nodo({g[1].translateX}, {g[1].translateY})");

    }

    function click(me:MouseEvent): Void {
        println("click function execute");

    }

}
