///*
// * U2UDataDownload.fx
// *
// * Created on 18-may-2009, 11:13:36
// */
//
//package org.u2u.data;
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
//
///**
// * @author sergio
// */
//
///**
//  *This class manages the model of data for U2UDownloadScene and it must inherit
//  *of the U2UFileSharingServiceListener
//*/
//public class U2UDataDownload {
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
//    var scene:U2UDownloadScene;
//
//    //levels: Store the progress level of the download
//    var levels: ArrayList;
//    //selectedBarIndex: Store the Index of the Download
//    var selectedBarIndex: Integer = 0;
//    //Maxinmun and Minimun level of download
//    def MAX_LEVEL: Number = 200;
//    def MIN_LEVEL:Number = 0;
//
//
//    public var DOWNLOAD:String  = "download";
//    public var PAUSED:String = "paused";
//    public var FINISH:String = "finish";
//
//    /**
//    *Init the fields of model for Downloads
//    */
//    public function initDataDonwload(downScene:U2UDownloadScene):Void{
//
//        this.scene = downScene;
//        this.nameDowns = ArrayList{};
//        this.advDowns = ArrayList{};
//        this.refVarEnv = HashMap{};
//        this.sourcesDowns = ArrayList{};
//        this.statusDowns = ArrayList{};
//        this.levels = ArrayList{};
//    }
//
//    /**
//    *Returns the number of downloads
//    */
//    public function getNumOfDownloads():Integer{
//
//        nameDowns.size();
//
//    }
//
//    /**
//    *Returns the name of enviroment variable that reprsents the download in the
//    * U2UShell
//    */
//    public function getVarRefAdv(downSel:Integer):String {
//
//
//        if(selectedBarIndex ==-1)
//            return null;
//
//        var adv:U2UContentAdvertisementImpl  = this.advDowns.get(downSel) as U2UContentAdvertisementImpl;
//        var namevar:String=null;
//
//        if(adv!=null)
//        {
//            namevar = refVarEnv.get(adv) as String;
//        }
//
//       return namevar;
//    }
//
//    /**
//     * Modifies the state of the download
//     * @param namVar, name's enviroment variable that represents the download
//     * @param state, state of the download (DOWNLOAD,PAUSED, FINISH)
//     */
//    public function setStatusDownload(namVar:String, state:String):Void {
//
//        var adv:U2UContentAdvertisementImpl = null;
//        var set:Set;
//        var iterator:Iterator;
//        var entry:Map.Entry;
//
//        set = refVarEnv.entrySet();
//        iterator = set.iterator();
//
//        while(iterator.hasNext())
//        {
//             entry =  iterator.next() as Map.Entry;
//             var name:String = entry.getValue() as String;
//
//            if(name.equals(namVar))
//            {
//              adv = entry.getKey() as U2UContentAdvertisementImpl;
//              break;
//            }
//        }
//
//        var index:Integer = advDowns.indexOf(adv);
//
//        if(state.equals(U2UDataDownload.PAUSED))
//           { statusDowns.set(index, U2UDataDownload.PAUSED);}
//        else
//           { statusDowns.set(index, U2UDataDownload.DOWNLOAD);}
//
//    }
//
//     /**
//     *Adds a download the name of its enviroment variable that conains its advertisement
//     * @param name, name of enviroment variable assigns to its download
//     * @param advDown, advertisement 2U2ContentAdvertisementImpl
//     */
//    public function addReferenceVar(name:String, advDown:U2UContentAdvertisementImpl ):Void {
//
//        refVarEnv.put(advDown, name);
//    }
//
//    /**
//     * Adds a new advertisements to downloads files
//     * @param adv, advertisement of file that will be download
//     */
//    public function addRowDownload(adv:U2UContentAdvertisementImpl):Boolean{
//
//
//            if(nameDowns.contains(adv.getName())){
//                return false;
//            }
//            else
//            {
//                this.insertValuesAdv(adv);
//                return true;
//            }
//    }
//
//
//    /**
//    *   Removes the download of the U2UDownloadScene and quit the download
//    */
//    public function deleteRowDownload(downSel:Integer):Void
//    {
//        //Quit the reference of enviroment variable and advertisement
//        var adv:U2UContentAdvertisementImpl = advDowns.get(downSel)as U2UContentAdvertisementImpl;
//        var name:String = refVarEnv.get(adv) as String;
//        refVarEnv.remove(name);
//
//        //Quit the download of the file the all references
//        this.nameDowns.remove(downSel);
//        this.advDowns.remove(downSel);
//        this.levels.remove(downSel);
//        this.statusDowns.remove(downSel);
//        this.sourcesDowns.remove(downSel);
//
//    }
//
//    /**
//     * Modifies the status of download file
//     * @param downSel, Number that represents the select download
//     * @param status, status of the file: DOWNLOAD or PAUSED
//     */
//    public function setStatusDownload(downSel:Integer, status:String):Void{
//        this.statusDowns.set(downSel, status);
//
//    }
//
//    /**
//     * clear all data of download files
//     */
//    public function clearDataDownloads():Void
//    {
//        nameDowns.clear();
//        advDowns.clear();
//        levels.clear();
//        refVarEnv.clear();
//        statusDowns.clear();
//        sourcesDowns.clear();
//    }
//
//    /**
//    *Stablish the number of sources peers for its download file
//    */
//    public function setSourceDownload(downSel:Integer, source:String):Void{
//        sourcesDowns.set(downSel, source);
//    }
//
//    /**
//    *   Gets the status of download file
//    * @param downSel, Number the select downloading file
//    * @return state of the downloading file: DOWNLOAD o PAUSED
//    */
//    public function getStatusDonwload(downSel:Integer):String{
//        return String.valueOf(statusDowns.get(downSel));
//
//    }
//
//    /**
//     * @return ArrayList that contains the names of the active downloads
//     */
//    public function getVarEnvActiveDownloads():ArrayList{
//
//        var active:ArrayList = ArrayList{};
//
//        for (xi in [0..<sizeof statusDowns])
//        {
//             if(statusDowns.get(xi ).equals(U2UDataDownload.DOWNLOAD))
//            {
//                var adv:U2UContentAdvertisementImpl  = advDowns.get(xi) as U2UContentAdvertisementImpl;
//                active.add(refVarEnv.get(adv));
//            }
//        }
//
//
//        for(xi in [0..< sizeof statusDowns])
//        {
//            if(statusDowns.get(xi as Integer).equals(U2UDataDownload.DOWNLOAD))
//            {
//                var adv:U2UContentAdvertisementImpl  = advDowns.get(xi) as U2UContentAdvertisementImpl;
//                active.add(refVarEnv.get(adv));
//            }
//        }
//
//        return active;
//    }
//
//
//
//    /**
//     * @return ArrayList that contains the names of the inactive downloads
//     */
//    public function getVarEnvInactiveDownloads():ArrayList {
//
//        var inactive:ArrayList = ArrayList{};
//
//        for(i in [0..<sizeof statusDowns])
//        {
//            if(statusDowns.get(i).equals(U2UDataDownload.PAUSED))
//            {
//                var adv:U2UContentAdvertisementImpl;
//                adv = advDowns.get(i) as U2UContentAdvertisementImpl;
//                inactive.add(refVarEnv.get(adv));
//            }
//        }
//
//        return inactive;
//    }
//
//    /*
//    public function serviceEvent(event:U2UFileSharingServiceEvent):Void {
//        //Si el evento es generado por una consulta de progreso de la descarga
//        if(event.getType()==U2UFileSharingServiceEvent.PROGRESS)
//        {
//            var obj:Object[] = event.getInformation();
//            //obtenemos el hash de parejas clave valor que contienen el progreso de cada descarga
//
//            //HashMap<String,Integer> val = new HashMap((HashMap) obj[0]);
//            var val = new HashMap(obj[0] as HashMap);
//
//            var pos:Integer = -1;
//
//            var set:Set = val.entrySet();
//            var iter:Iterator = set.iterator();
//            var entry:Map.Entry;
//
//            while(iter.hasNext())
//            {
//                entry =  iter.next() as Map.Entry;
//                var id = new  U2UContentIdImpl(entry.getKey());
//
//                pos = existAdvertisementDownloading(id);
//
//                if(pos != -1)
//                {
//                    //Stablish the progress of the downloads
//                    //setProgressBar(pos, entry.getValue().intValue());
//                }
//            }
//
//        }
//        else if(event.getType()==(U2UFileSharingServiceEvent.SOURCES_DOWN))
//        {
//            var obj:Object[] = event.getInformation();
//
//            //get hash that contains the progress for each download
//            var val:HashMap = new HashMap(obj[0] as HashMap );
//
//            var pos:Integer =-1;
//            var set2:Set = val.entrySet();
//            var iter2:Iterator = set2.iterator();
//            var entry:Map.Entry;
//
//            while(iter2.hasNext())
//            {
//                entry = iter2.next() as Map.Entry;
//                var id:U2UContentIdImpl = new U2UContentIdImpl{iter2.getKey()};
//
//                pos = existAdvertisementDownloading(id);
//
//                if(pos != -1)
//                {
//                    //Stablish the progress of the downloads
//                    setSourcesDown(pos, iter2.getValue().intValue());
//                }
//            }
//        }
//      }*/
//
//
//    /**
//     * Search in the array of advertisement if exist any with the same ContetndId
//     * @param id, U2UContetndIdImpl
//     * @return int that represents the position of the advertisement
//     */
//    function existAdvertisementDownloading(id:U2UContentIdImpl):Integer {
//
//        if(id==null)
//        {
//            return -1;
//        }
//
//        for(i in [0 .. <sizeof advDowns])
//        {
//            var advImp:U2UContentAdvertisementImpl = advDowns.get(i) as U2UContentAdvertisementImpl;
//            var cid:U2UContentIdImpl = advImp.getContentId() as U2UContentIdImpl;
//
//            if(cid.equals(id))
//            {
//                return i;
//            }
//        }
//
//        return -1;
//    }
//
//    /**
//     * Inserts data of an advertisement into Data download
//     */
//    function insertValuesAdv(adv:U2UContentAdvertisementImpl):Void
//    {
//        this.advDowns.add(adv);
//        this.nameDowns.add(adv.getName());
//        this.levels.add(0);
//        this.statusDowns.add(DOWNLOAD);
//        this.sourcesDowns.add("Buscando Fuentes");
//    }
//
//   /**
//     * Modifica el el progreso de una descarga
//     */
//    function setProgressDownload(downSel:Integer, value:Integer):Void
//    {
//        var levelDown:Integer = levels.set(downSel,value) as Integer;
//
//        if(value == 200)
//        {
//            setStatusDownload(downSel, FINISH);
//            setSourcesDown(downSel, 200);
//        }
//
//        //table.updateUI();
//
//    }
//
//    function setSourcesDown(downSel:Integer, value:Integer):Void{
//
//       if(value == 0 or value < 0)
//        {
//            sourcesDowns.set(downSel, "No hay Fuentes");
//        }
//        else if(value==100)
//        {
//           sourcesDowns.set(downSel, "Completo");
//        }
//        else
//        {
//            sourcesDowns.set(downSel,"{value} Fuentes");
//        }
//
//        //table.updateUI();
//    }
//
//
//}
