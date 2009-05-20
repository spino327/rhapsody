///*
// * U2UListModelDownload.fx
// *
// * Created on 19-may-2009, 8:31:36
// */
//
//package org.u2u.data;
//
//
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.Map;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.Set;
//
//import org.u2u.gui.scene.U2UDownloadScene;
//import org.u2u.filesharing.U2UContentAdvertisementImpl;
//import org.u2u.filesharing.U2UContentIdImpl;
//import org.u2u.filesharing.U2UFileSharingServiceEvent;
///**
// * @author sergio
// */
//
//public var DOWNLOAD:String  = "download";
//public var PAUSED:String = "paused";
//public var FINISH:String = "finish";
//
//
//public class U2UListModelDownload extends U2UListModel{
//
//    //this variable store the names of the downloads
//    var nameDowns:ArrayList;
//    //this variable store the reference to downloads advertisments U2UContentAdvertisementImpl
//    var advDowns:ArrayList;
//    //this variable store a reference the enviroment variables and the advertisements
//    var refVarEnv:HashMap;
//    //status of donwload
//    var statusDowns:ArrayList;
//    //seeds of downloads
//    var sourcesDowns:ArrayList;
//    //the link to U2UDownloadScene
//    var listView:U2UListDownload;
//
//    //levels: Store the progress level of the download
//    var levels: ArrayList;
//    //selectedBarIndex: Store the Index of the Download
//    var selectedBarIndex: Integer = 0;
//    //Store the number of downloads in the model
//    public var numDowns:Integer = 0;
//
//    //Maxinmun and Minimun level of download
//    def MAX_LEVEL: Number = 200;
//    def MIN_LEVEL:Number = 0;
//
//    override public function getNodeCount():Integer
//    {
//        return numDowns =nameDowns.size();
//    }
//    /*
//    override public function geFieldCount():Integer
//    {
//        return 1;
//    }
//
//    override public function getFieldName():String
//    {
//        return "hola";
//    }
//
//    override public function getFieldClass():Object
//    {
//        return "";
//    }
//    */
//    override public function getValueAt(row:Integer, col:Integer):Object
//    {
//        var adv:U2UContentAdvertisementImpl = null;
//
//        if(col==0)
//        {
//            nameDowns.get(row);
//        }else if(col == 1)
//        {
//
//            adv = advDowns.get(row) as U2UContentAdvertisementImpl;
//            return adv.getLength();
//
//        }else
//        {
//            return null;
//        }
//    }
//
//    override public function setValueAt(val:Object,row:Integer, col:Integer):Void
//    {
//
//        if(col==0)
//        {
//            nameDowns.set(row,val);
//        }
//    }
//
//
//
//
//}
