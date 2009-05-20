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

/**
 * @author sergio
 */
public class U2UList extends Group {

    //instance variables
    var model: U2UAbstractListModel;
    var imgBackground:Image = Image{
            url:"{__DIR__}resources/content.png";
    };

    /** represents the current position in the model of the first node of the this list*/
    var firstPos: Integer = 0;

    var memoryDragPoint: Number = 0;
    var memoryDragLength: Number = 0;

     /**
    * background image of the List
    */
    var imgBackView:ImageView = ImageView{
        image: bind imgBackground;
    };
    /** spacing between nodes*/
    var spacingNodes: Number;
    /** */

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
    public function updateUI():Void {

        var size:Integer = model.getSize();

        var con:Node[] = [];

        //translation axis Y
        var transY = 4;

        for(x in [firstPos..firstPos+2])
        {
            var hNode = model.getNodeAt(x).getNodeView().
            model.getNodeAt(x).getNodeView().translateY = *x;
            insert model.getNodeAt(x).getNodeView() into con;
        }

        this.groupList.content = [imgBackView,con];

        this.content = this.groupList;

    }

    function dragg(me:MouseEvent): Void {
        //println("dragg function execute desde:{me.dragAnchorY} longitud:{me.dragY}");
        var g:Node[] = this.groupList.content;
        var delta:Number = 5;


        if(this.memoryDragPoint == me.dragAnchorY)
        {
            println("equals");

            if(Math.abs(this.memoryDragLength) < Math.abs(me.dragY))
            {
                delta = -1*delta;
            }

        }
        else
        {
            println("differents");
            this.memoryDragPoint = me.dragAnchorY;
            this.memoryDragLength = me.dragY;

            delta = if(this.memoryDragLength < 0) then (-1*delta) else delta;
        }


        g[1].translateY = g[1].translateY + delta;



        /*if(me.dragY > 0)
        {
            println("me.dragY = {me.dragY} => +3");
            g[0].translateY = g[0].translateY + 3;
        }
        else
        {
            println("me.dragY = {me.dragY} => -3");
            g[0].translateY = g[0].translateY - 3;
        }*/

    }

    function click(me:MouseEvent): Void {
        println("click function execute");
    }

}
