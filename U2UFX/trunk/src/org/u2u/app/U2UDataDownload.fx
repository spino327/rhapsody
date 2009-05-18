/*
 * U2UDataDownload.fx
 *
 * Created on 18-may-2009, 11:13:36
 */

package org.u2u.app;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;


import org.u2u.gui.scene.U2UDownloadScene;
import org.u2u.filesharing.U2UContentAdvertisementImpl;
import java.util.HashMap;
import org.u2u.filesharing.U2UFileSharingServiceEvent;
/**
 * @author sergio
 */

/**
  *This class manages the model of data for U2UDownloadScene and it must inherit
  *of the U2UFileSharingServiceListener
*/
public class U2UDataDownload {

    //this variable store the names of the downloads
    var nameDowns:ArrayList;
    //this variable store the reference to downloads advertisments U2UContentAdvertisementImpl
    var advDowns:ArrayList;
    //this variable store a reference the enviroment variables and the advertisements
    var refVarEnv:HashMap;
    //status of donwload
    var statusDowns:ArrayList;
    //seeds of downloads
    var sourcesDowns:ArrayList;
    //the link to U2UDownloadScene
    var scene:U2UDownloadScene;
    
    //levels: Store the progress level of the download
    var levels: ArrayList;
    //selectedBarIndex: Store the Index of the Download
    var selectedBarIndex: Integer = 0;
    //Maxinmun and Minimun level of download
    def MAX_LEVEL: Number = 200;
    def MIN_LEVEL:Number = 0;


    def DOWNLOAD:String  = "download";
    def PAUSED:String = "paused";
    def FINISH:String = "finish";

    /**
    *Init the fields of model for Downloads
    */
    public function initDataDonwload(downScene:U2UDownloadScene):Void{

        this.scene = downScene;
        this.nameDowns = ArrayList{};
        this.advDowns = ArrayList{};
        this.refVarEnv = HashSet{};
        this.sourcesDowns = ArrayList{};
        this.statusDowns = ArrayList{};
        this.levels = ArrayList{};
    }

    /**
    *Returns the number of downloads
    */
    public function getNumOfDownloads():Integer{

        nameDowns.size();

    }

    /**
    *Returns the name of enviroment variable that reprsents the download in the
    * U2UShell
    */
    public function getVarRefAdv(donwSel:Integer):String {


        if(selectedBarIndex ==-1)
            return null;

        //U2UContentAdvertisementImpl adv = this.advDown.get(row);
        var namevar:String=null;

        /*if(adv!=null)
        {
            namevar = refVarEnv.get(adv);
        }*/

       return namevar;
    }

    /**
     * Modifies the state of the download 
     * @param namVar, name's enviroment variable that represents the download
     * @param state, state of the download (DOWNLOAD,PAUSED, FINISH)
     */
    public function setStatusDownload(namVar:String, state:String):Void {

        //U2UContentAdvertisementImpl adv = null;
        var adv:String;
        /*for(Map.Entry<U2UContentAdvertisementImpl,String> in: refVarEnv.entrySet())
        {
            String name = in.getValue();

            if(name.equals(namVar))
            {
              adv = in.getKey();
              break;
            }
        }*/

        var index:Integer = advDowns.indexOf(adv);

        if(state.equals(U2UDataDownload.PAUSED))
           { statusDowns.set(index, U2UDataDownload.PAUSED);}
        else
           { statusDowns.set(index, U2UDataDownload.DOWNLOAD);}

    }

     /**
     *Adds a download the name of its enviroment variable that conains its advertisement
     * @param name, name of enviroment variable assigns to its download
     * @param advDown, advertisement 2U2ContentAdvertisementImpl
     */
    public function addReferenceVar(name:String, advDown:U2UContentAdvertisementImpl ):Void {

        refVarEnv.put(advDown, name);
    }

    /**
     * Agrega un nuevo anuncio a la cola de descargas
     * @param adv anuncio que desea ser descargado
     */
    public function addRowDownload(adv:U2UContentAdvertisementImpl):Boolean{
         //downloads.add(adv);
        if(nameDowns.contains(adv.getName())){
            return false;
        }
        else
        {
            //this.insertValuesAdv(adv);
            return true;
        }
    }


    /**
    *   Removes the download of the U2UDownloadScene and quit the download
    */
    public function deleteRowDownload(downSel:Integer):Void
    {
        //Quit the reference of enviroment variable and advertisement
        var adv:U2UContentAdvertisementImpl = advDowns.get(downSel)as U2UContentAdvertisementImpl;
        var name:String = refVarEnv.get(adv) as String;
        refVarEnv.remove(name);

        //Quit the download of the file the all references
        this.nameDowns.remove(downSel);
        this.advDowns.remove(downSel);
        this.levels.remove(downSel);
        this.statusDowns.remove(downSel);
        this.sourcesDowns.remove(downSel);

    }

    /**
     * Modifies the status of download file
     * @param downSel, Number that represents the select download
     * @param status, status of the file: DOWNLOAD or PAUSED
     */
    public function setStatusDownload(downSel:Integer, status:String):Void{
        this.statusDowns.set(downSel, status);

    }

    /**
     * clear all data of download files
     */
    public function clearDataDownloads():Void
    {
        nameDowns.clear();
        advDowns.clear();
        levels.clear();
        refVarEnv.clear();
        statusDowns.clear();
        sourcesDowns.clear();
    }

    /**
    *Stablish the number of sources peers for its download file
    */
    public function setSourceDownload(downSel:Integer, source:String):Void{
        sourcesDowns.set(downSel, source);
    }

    /**
    *   Gets the status of download file
    * @param downSel, Number the select downloading file
    * @return state of the downloading file: DOWNLOAD o PAUSED
    */
    public function getStatusDonwload(downSel:Integer):String{
        return String.valueOf(statusDowns.get(downSel));

    }

    /**
     * @return ArrayList that contains the names of the active downloads
     */
    public function getVarEnvActiveDownloads():ArrayList{

        var active:ArrayList = ArrayList{};
        //var elem:Integer;
        //var x:Integer=0;

        for (xi in [0..<sizeof statusDowns])
        {
             if(statusDowns.get(xi ).equals(U2UDataDownload.DOWNLOAD))
            {
                var adv:U2UContentAdvertisementImpl  = advDowns.get(xi) as U2UContentAdvertisementImpl;
                active.add(refVarEnv.get(adv));
            }
        }


        for(xi in [0..< sizeof statusDowns])
        {
            if(statusDowns.get(xi as Integer).equals(U2UDataDownload.DOWNLOAD))
            {
                var adv:U2UContentAdvertisementImpl  = advDowns.get(xi) as U2UContentAdvertisementImpl;
                active.add(refVarEnv.get(adv));
            }
        }

        return active;
    }



/**
     * Retorna un ArrayList de los nombres de las descargas inactivas
     * @return
     */
    public function getVarEnvInactiveDownloads():ArrayList {

        var inactive:ArrayList = ArrayList{};

        for(i in [0..<sizeof statusDowns])
        {
            if(statusDowns.get(i).equals(U2UDataDownload.PAUSED))
            {
                var adv:U2UContentAdvertisementImpl;
                adv = advDowns.get(i) as U2UContentAdvertisementImpl;
                inactive.add(refVarEnv.get(adv));
            }
        }

        return inactive;
    }


//    public function serviceEvent(event:U2UFileSharingServiceEvent):Void {
//        //Si el evento es generado por una consulta de progreso de la descarga
//        if(event.getType()==U2UFileSharingServiceEvent.PROGRESS)
//        {
//            var obj:Object[] = event.getInformation();
//            //obtenemos el hash de parejas clave valor que contienen el progreso de cada descarga
//
//
//            var val:HashMap = HashMap{};
//
//            var pos:Integer = -1;
//
//            for(elem in val.entrySet())
//            {
//                U2UContentIdImpl id = new U2UContentIdImpl(in.getKey());
//
//                pos = existAdvertisementDownloading(id);
//
//                if(pos != -1)
//                {
//                    //Stablish the progress of the downloads
//                    setProgressBar(pos, in.getValue().intValue());
//                }
//            }
//        }
//        else if(event.getType()==(U2UFileSharingServiceEvent.SOURCES_DOWN))
//        {
//            Object[] obj = event.getInformation();
//            //obtenemos el hash de parejas clave valor que contienen el progreso de cada descarga
//
//            @SuppressWarnings("unchecked")
//            HashMap<String,Integer> val = new HashMap((HashMap) obj[0]);
//
//            int pos =-1;
//
//            for(Map.Entry<String, Integer> in : val.entrySet())
//            {
//                U2UContentIdImpl id = new U2UContentIdImpl(in.getKey());
//
//                pos = existAdvertisementDownloading(id);
//
//                if(pos != -1)
//                {
//                    //Stablish the progress of the downloads
//                    setSourcesDown(pos, in.getValue().intValue());
//                }
//            }
//        }
//      }

//    //--------------------------------------------------------------------------
//    //--------------------------------------------------------------------------
//    //methods private
//    //--------------------------------------------------------------------------
//    //--------------------------------------------------------------------------
//
//    /**
//     * Search in the array of advertisement if exist any with the same ContetndId
//     * @param id, U2UContetndIdImpl
//     * @return int that represents the position of the advertisement
//     */
//    private int existAdvertisementDownloading(U2UContentIdImpl id) {
//
//        if(id==null)
//            return -1;
//
//        for(int i=0;i<advDown.size();i++)
//        {
//            U2UContentIdImpl cid = (U2UContentIdImpl) ((U2UContentAdvertisementImpl)advDown.get(i)).getContentId();
//            if(cid.equals(id))
//                return i;
//        }
//
//        return -1;
//    }
//
//    /**
//     * Inserta los datos de un adv en las variables de la tabla
//     */
//    private void insertValuesAdv(U2UContentAdvertisementImpl adv)
//    {
//        Jprogress progBar = new Jprogress();
//
//        this.advDown.add(adv);
//        this.nameDown.add(adv.getName());
//        this.progressDown.add(progBar);
//        this.statusDown.add(DOWNLOAD);
//        this.sourcesDown.add("Buscando Fuentes");
//    }
//
//   /**
//     * Modifica el el progreso de una descarga
//     */
//    private void setProgressBar(int row, int  value)
//    {
//        Jprogress progress = progressDown.get(row);
//        progress.setValue(value);
//
//        if(value == 100)
//        {
//            setStatusDownload(row, FINISH);
//            setSourcesDown(row, 100);
//        }
//
//        table.updateUI();
//
//    }

    function setSourcesDown(downSel:Integer, value:Integer):Void{
       
       if(value == 0 or value < 0)
        {
            sourcesDowns.set(downSel, "No hay Fuentes");
        }
        else if(value==100)
        {
           sourcesDowns.set(downSel, "Completo");
        }
        else
        {
            sourcesDowns.set(downSel,""+value+" Fuentes");
        }

        //table.updateUI();
    }


}
