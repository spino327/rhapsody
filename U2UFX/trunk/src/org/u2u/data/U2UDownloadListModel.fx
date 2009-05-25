/*
 * U2UDownloadListModel.fx
 *
 * Created on 19-may-2009, 17:23:59
 */

package org.u2u.data;

import org.u2u.data.U2UAbstractNode;
import java.util.LinkedList;
import org.u2u.filesharing.U2UContentAdvertisementImpl;
import org.u2u.data.U2UDownloadNode;
import net.jxta.share.ContentId;
import javax.swing.JOptionPane;
import org.u2u.filesharing.U2UFileSharingServiceEvent;
import org.u2u.filesharing.U2UFileSharingServiceListener;

/**
 * @author sergio
 */

public class U2UDownloadListModel extends U2UAbstractListModel, U2UFileSharingServiceListener {

    //instance variables
    var list:LinkedList = LinkedList{};

    //instance methods
    override function getSize():Integer {
        return list.size();
    }

    override function getNodeAt(index:Integer): U2UAbstractNode {
        return list.get(index) as U2UAbstractNode;
    }

    /**
    * Inserts a adv that represents a file into the list
    * @return true, if the file can be stored, false in other case
    */
    override function insertFileIntoModel(adv: U2UContentAdvertisementImpl):Boolean{
        
        if(this.existNode(adv)){
            //The file exists in the list model
            JOptionPane.showConfirmDialog(null, "This File is already downloading");
            return false;
        }else{
            
            var node:U2UDownloadNode = U2UDownloadNode{
                name: adv.getName();
                length:adv.getLength();
                description:adv.getDescription();
                chunksize:adv.getChunksize();
                cid: adv.getContentId();
                type:adv.getType();
                adv: adv;
            }
            
            list.add(node);
         }

         return true;
    }

    /**
    * Verifies if the advertisement that represents the file is in the list of nodes\
    *@return false if the adv isn't in the list
    */
    function existNode(adv:U2UContentAdvertisementImpl):Boolean{

        for (file in list){
            var cidFile:ContentId = (file as U2UDownloadNode).cid;
            if(cidFile.equals(adv.getContentId())){
                return true;
            }
        }
        return false;
    }

    /**
    *Delete the node selected of the list
    */
    override function deleteFileOfModel(selIndex:Integer):Void{

        if(selIndex<=list.size() and selIndex>=0)
        {
            println("Delete file {selIndex} name : {((list.get(selIndex)) as U2UDownloadNode).name}");
            list.remove(selIndex);
        }
    }

      override function serviceEvent(event:U2UFileSharingServiceEvent):Void {
        //Si el evento es generado por una consulta de progreso de la descarga
//        if(event.getType()==U2UFileSharingServiceEvent.PROGRESS)
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
      }

}
