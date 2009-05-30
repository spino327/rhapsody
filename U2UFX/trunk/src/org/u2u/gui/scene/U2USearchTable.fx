/*
 * U2USearchTable.fx
 *
 * Created on 23-may-2009, 9:53:13
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
package org.u2u.gui.scene;

import javafx.scene.Node;
import javafx.scene.CustomNode;
import javafx.scene.Group;
import javafx.scene.text.*;
import org.u2u.filesharing.downloadpeer.U2UContentDiscoveryEvent;
import org.u2u.filesharing.downloadpeer.U2USearchListener;
import java.util.Enumeration;
import org.u2u.filesharing.U2UContentAdvertisementImpl;
import org.u2u.app.U2UFXApp;
import java.util.ArrayList;
import net.jxta.share.ContentId;

/**
 * @author Irene
 */

public class U2USearchTable extends CustomNode, U2USearchListener{

    public var results:ResultFile[] = null on replace {


        println("results change now have {sizeof results} elements");

        if((results != null) and (table != null)) {

            this.table.rows = for (f in results) {
                [SwingTable.TableRow{
                        cells:[
                            SwingTable.TableCell{text: f.name },
                                SwingTable.TableCell{text: String.valueOf(f.size) },
                                   SwingTable.TableCell{text: String.valueOf(f.containers) },
                                     SwingTable.TableCell{adv: f.adv }
                                       SwingTable.TableCell{text: (f.cid).toString() }
                          ]
                      }
                ]
            };


        }


    };
    public var resArray:ArrayList;
    public var selec:Integer on replace {
        println("-------------- selection is: {selec} ---------------------------")
    };
    public var height:Number = 260;

    var table: SwingTable;

    override function create():Node{

        Group{
            content: [

                Text{
                    translateX:350;
                    translateY:70;
                    content:"SEARCH FILES";
                    font: Font.font("Arial",FontWeight.BOLD,20);
                },

                table = SwingTable{
                    width:380;
                    height:bind this.height;
                    translateX:230;
                    translateY:120;
                    columns: [
                        SwingTable.TableColumn{ text: "File" },
                         SwingTable.TableColumn{ text: "Size" },
                          SwingTable.TableColumn{ text: "Nº" },
                    ],

//                    rows: bind for (f in results)
//                    [
//                          SwingTable.TableRow{
//                            cells:[
//                                SwingTable.TableCell{text: f.name },
//                                    SwingTable.TableCell{text: String.valueOf(f.size) },
//                                       SwingTable.TableCell{text: String.valueOf(f.containers) },
//                                         SwingTable.TableCell{adv: f.adv }
//                                           SwingTable.TableCell{text: (f.cid).toString() }
//                              ]
//                          }
//                    ]
                    selection: bind selec with inverse
                }  
            ]
        }
    }


    /**
     * U2USearchListener interface's event for manages advertisement arrives because
     * it has a search through U2U File Sharing Service's Request Manager module
     * @param event generated because arrives a advertisement
     */
    override function contentAdvertisementEvent(event:U2UContentDiscoveryEvent ):Void {

        var ressta:Enumeration  =  event.getResponseAdv();

        while(ressta.hasMoreElements())
        {
            var adv:U2UContentAdvertisementImpl  = ressta.nextElement() as U2UContentAdvertisementImpl;
            var res:Boolean = false;

                insertResultInTable(adv);
                println("Advertisement Arrived, yujuu!!");
        }
    }

    /**
    * Insert the info of a advertisement results of a search does in the P2P network
    */
    function insertResultInTable(adv:U2UContentAdvertisementImpl):Void{

            var cidFile:ContentId;
            var resta:Boolean;

            for(file in results)
            {
                cidFile = file.cid;
                if(cidFile.equals(adv.getContentId()))
                {
                    file.containers = file.containers +1 ;
                    resta = true;
                }
            }
            if(not resta)
            {
                 var res:ResultFile = ResultFile{
                        name: adv.getName();
                        size: adv.getLength();
                        containers:1;
                        adv: adv;
                        cid: adv.getContentId();
                    }
                insert res into results;
            }

        }

    /**
    * Get the advetisment of the file in the search results
    */
    public function getAdvertismentFileSelected():U2UContentAdvertisementImpl{

        return results[this.selec].adv;

    }

    public function deleteAllOldResults():Void{
        //Delete all old results in the table
        delete results;
    }
 }

    /**
    * Runs a seacrh in the P2P network throught the U2UShell
    */
    public function runsSearch(value:String):Void{
        
        //Throw a new search in the P2P network
        U2UFXApp.APP.shell.executeCmd("u2ufss -search {value} -n");
    }
    /**
    * Class that represents the information of the files found in the P2P network
    */
    protected class ResultFile{

       public-init var name:String;
       public-init var size:Number;
       public-init var containers:Integer;
       public-init var adv:U2UContentAdvertisementImpl;
       public-init var cid:ContentId;

    }


   

