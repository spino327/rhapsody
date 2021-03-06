/*
 * U2UDownloadListModel.fx
 *
 * Created on 19-may-2009, 17:23:59
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

package org.u2u.data;

import net.jxta.share.ContentId;
import org.u2u.app.U2UFXApp;
import org.u2u.filesharing.U2UFileSharingServiceEvent;
import org.u2u.filesharing.U2UFileSharingServiceListener;
import org.u2u.filesharing.U2UContentIdImpl;
import org.u2u.filesharing.U2UContentAdvertisementImpl;
import org.u2u.data.U2UDownloadNode;
import org.u2u.data.U2UAbstractNode;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.util.Map;
import javax.swing.Timer;
import javax.swing.JOptionPane;

/**
 * @author sergio
 */
public class U2UDownloadListModel extends U2UAbstractListModel, U2UFileSharingServiceListener {

    //instance variables
    var list:LinkedList = LinkedList{};
    var timer:Timer;
    var listener:ProgressTask;
    /** number of actived downloads, a download become inactive when reach the 100 points*/
    var numActDown:Integer = 0 on replace {
        println("numActDown change to {numActDown}");
    };

    init {
        listener = ProgressTask{
            count: bind numActDown;
            timerContainer: bind timer;
        };

        timer = new Timer(1000, listener);
    }


    //instance methods
    override function getSize():Integer {
        return list.size();
    }

    /**
    * Gets a node of the list model through index of the node in the list
    */
    override function getNodeAt(index:Integer): U2UAbstractNode {
        return list.get(index) as U2UAbstractNode;
    }

    /**
    * Inserts a adv that represents a file into the list
    * @return true, if the file can be stored, false in other case
    */
    public function insertFileIntoModel(adv: U2UContentAdvertisementImpl, env: String):Boolean{
        
        if(this.existNode(adv)){
            //The file exists in the list model
            JOptionPane.showMessageDialog(null, "This File is already downloading");
            return false;
        }
        else {

            //we have one more active download
            this.numActDown++;

            if(not timer.isRunning())
            {
                println("Start the timer for checking progress, lintening {timer.getActionListeners().length}");
                timer.restart();
            }

            var node:U2UDownloadNode = U2UDownloadNode{
                name: adv.getName();
                length: adv.getLength();
                description: adv.getDescription();
                chunksize: adv.getChunksize();
                cid: adv.getContentId();
                type: adv.getType();
                adv: adv;
                shellEnv: env;
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
    public function deleteFileOfModel(selIndex:Integer):Void{

        if(selIndex<=list.size() and selIndex>=0)
        {
            println("Delete file {selIndex} name : {((list.get(selIndex)) as U2UDownloadNode).name}");
            list.remove(selIndex);
        }
    }

    override function serviceEvent(event:U2UFileSharingServiceEvent):Void {

        //if the event is generated by a progress query for downloads
        if((event.getType() == U2UFileSharingServiceEvent.PROGRESS))
        {
            var obj:Object[] = event.getInformation();

            //get hash of couples key-value that contains the progress for each download
            var val:HashMap = obj[0] as HashMap;

            var pos:Integer =-1;
            var set:Set = val.entrySet();
            var iter:Iterator = set.iterator();
            var entry:Map.Entry;
            /* this var help us to decide if we have active downloads*/
            var countNoActTmp: Integer = 0;
            var countDownTmp: Integer = 0;
            while(iter.hasNext())
            {
                entry = iter.next() as Map.Entry;
                var valCid:String = entry.getKey() as String;
                var id:U2UContentIdImpl = new U2UContentIdImpl(valCid);

                pos =  existAdvertisementDownloading(id);

                if(pos != -1)
                {
                    var value:Float = (entry.getValue() as Float);
//                    println("---------------------------------------------");
//                    println("Modifies the level of the progress to file {(list.get(pos) as U2UDownloadNode).name}");
//                    println("New value: {value}");
//                    println("---------------------------------------------");

                    //checking if we have 100 points
                    countDownTmp++;
                    if(value == 100) {
                        countNoActTmp++;
                    }

                    var node:U2UDownloadNode = list.get(pos) as U2UDownloadNode;
                    //node.level = entry.getValue() as Integer;
                    node.updateLevel(value*2);
                }

            }
            //decide about active downloads
            //if we have already active downloads, if((countDownTmp - countNoActTmp) > 0) so go on!
            this.numActDown = countDownTmp - countNoActTmp;
            //if we have finsihed downloads so we show it in the Shared Scene
            if(countNoActTmp > 0) {
                U2UFXApp.APP.shell.executeCmd("u2ufss -showsf ");
            }

        }
      
    }

    /**
    * Returns the index of the downloading file in other case returns -1
    */
    function existAdvertisementDownloading(id:U2UContentIdImpl):Integer{

        var pos = -1;
        for (x in [0..< list.size()])
        {
            var cid:U2UContentIdImpl = ((list.get(x) as U2UDownloadNode).cid as U2UContentIdImpl);

            if(id.equals(cid)){
                pos = x;
            }
        }

        return pos;
    }

    /**
    * This method stop the timer that does progress queries
    */
    public function stopProgressQuery():Void{

        if(timer.isRunning()){

            timer.stop();
        }
    }

}

class ProgressTask extends ActionListener {

    /** this variable needs to be binding with the numActDown*/
    public-init var count: Integer = 0 on replace {
        println("ProgressTask.count change to {numActDown}");
    };
    /** this variable is a reference of the timer container*/
    public-init var timerContainer: Timer = null on replace {
        println("ProgressTask.timerContainer change to {timerContainer}");
    };

    override function actionPerformed(e:ActionEvent){

        /*Se ejecuta el comando u2ufss conActiveDown la opción progress
        *para averiguar el estado de las descargas (este comando genera
        * un evento U2UFileSharingServiceEvent de tipo PROGRESS)*/
        if(this.count > 0) {
            U2UFXApp.APP.shell.executeCmd("u2ufss -progress");
        }
        else {
            println("no more active downloads exists");
            
            this.timerContainer.stop();
        }

    }

}
