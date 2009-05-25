/*
 * U2USearchTable.fx
 *
 * Created on 23-may-2009, 9:53:13
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

    public var results:ResultFile[];
    public var resArray:ArrayList;
    public var selection:Integer on replace {
        println("-------------- selection is: {selection} ---------------------------")
    };
    public var height:Number = 260;

    override function create():Node{

        Group{
            content: [

                Text{
                    translateX:350;
                    translateY:70;
                    content:"SEARCH FILES";
                    font: Font.font("Arial",FontWeight.BOLD,20);
                },

                SwingTable{
                    width:380;
                    height:bind this.height;
                    translateX:230;
                    translateY:150;
                    columns: [
                        SwingTable.TableColumn{ text: "File" },
                         SwingTable.TableColumn{ text: "Size" },
                          SwingTable.TableColumn{ text: "Nº" },
                    ],

                    rows: bind for (f in results)
                    [
                          SwingTable.TableRow{
                            cells:[
                                SwingTable.TableCell{text: f.name },
                                    SwingTable.TableCell{text: String.valueOf(f.size) },
                                       SwingTable.TableCell{text: String.valueOf(f.containers) },
                                         SwingTable.TableCell{adv: f.adv }
                                           SwingTable.TableCell{text: (f.cid).toString() }
                              ]
                          }
                    ]
                    selection: bind selection with inverse
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

        return results[this.selection].adv;

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


   

