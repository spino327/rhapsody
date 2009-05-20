///*
// * U2UListDownload.fx
// *
// * Created on 19-may-2009, 8:23:04
// */
//
//package org.u2u.data;
//
//import javafx.scene.Group;
//import java.util.LinkedList;
//import javafx.scene.Node;
//import javafx.scene.input.MouseEvent;
//
//import org.u2u.filesharing.U2UContentAdvertisementImpl;
//
///**
// * @author sergio
// */
//public var MODE_LIST:String = "list";
//public var MODE_NODES:String = "nodes";
//
//public class U2UListDownload extends Group{
//
//    //model: Store the data of each node in the list
//    protected var model:U2UListModelDownload;
//    //list: A list the graphical nodes that represents the files
//    protected var list:LinkedList;
//    //view: kind of view that user wants to see
//    protected var view:String;
//    //cont: contains the nodes of the list
//    protected var cont:Node[];
//
//    var x:Number;
//    var y:Number;
//
//
//    init{
//
//        model = U2UListModelDownload{};
//        model.initDataDonwload(this);
//
//        this.getDataModel();
//    }
//
//    /**
//    *   Get the data of the file stored in the node
//    */
//    function getDataModel():Void{
//
//        var numNode = model.getNodeCount();
//        var node:U2UListNodeDownload;
//
//        for(i in [0..<numNode])
//        {
//            var name:String = model.getValueAt(i, 1) as String;
//
//            if(name != null)
//            {
//                //creates the node with the information of the model
//                node = U2UListNodeDownload{name:name};
//                //adss the node to the list
//                list.add(node);
//            }
//
//        }
//
//
//    }
//
//    /**
//    *   Adds a file to be download and then it will be show in the UI
//    */
//    public function addNodeDownload(advDown:U2UContentAdvertisementImpl):Void{
//
//        if(advDown!=null)
//        {
//            if(model.addRowDownload(advDown))
//            {
//
//            }
//        }
//
//    }
//
//    /**
//     *FIXME
//    * Update the list of nodes in the model
//    */
//    public function update(id:Integer, add:Boolean):Void{
//
//        var node:U2UListNodeDownload;
//        var name:String = model.getValueAt(id, 1) as String;
//
//        node = U2UListNodeDownload{name:name};
//
//        this.list.add(node);
//
//    }
//
//    function moveListNode(me:MouseEvent):Void
//    {
//        var movY = me.dragY;
//
//    }
//
//
//
//}
