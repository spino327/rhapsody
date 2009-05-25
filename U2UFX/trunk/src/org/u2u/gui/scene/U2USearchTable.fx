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

/**
 * @author Irene
 */

public class U2USearchTable extends CustomNode, U2USearchListener{

    public var results:ResultFile[];
    public var resArray:ArrayList;
    public var selection:Integer on replace {
        println("selection is: {selection}")
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

                    rows: bind for (f in resArray)
                    
                    [

                          SwingTable.TableRow{
                            cells:[
                                SwingTable.TableCell{text: (f as ResultFile).name },
                                    SwingTable.TableCell{text: String.valueOf((f as ResultFile).size) },
                                       SwingTable.TableCell{text: String.valueOf((f as ResultFile).containers) },
                                         SwingTable.TableCell{adv: (f as ResultFile).adv }


//                                SwingTable.TableCell{text:((resArray.get(f))as ResultFile).name },
//                                    SwingTable.TableCell{text: String.valueOf(((resArray.get(f))as ResultFile).size) },
//                                       SwingTable.TableCell{text: String.valueOf(((resArray.get(f))as ResultFile).containers) },
//                                         SwingTable.TableCell{adv: ((resArray.get(f))as ResultFile).adv }

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
            for(i in [0..< sizeof resArray])
            {
                var advFile = (resArray.get(i) as ResultFile).adv;
                if(advFile.equals(adv))
                {
                    res = true;
                    var modRes:ResultFile = resArray.get(i) as ResultFile;
                    modRes.containers = modRes.containers + 1;
                    println("Advertisement Arrived, yujuu!!. Repeat:  {modRes.containers}");
                }
            }

            if(not res)
            {
                insertResultInTable(adv);
                println("Advertisement Arrived, yujuu!!");
            }

          
        }
    }

    /**
    * Insert the info of a advertisement results of a search does in the P2P network
    */
    function insertResultInTable(adv:U2UContentAdvertisementImpl):Void{

        var res:ResultFile = ResultFile{
                name: adv.getName();
                size: adv.getLength();
                containers:1;
                adv: adv;
            }
        resArray.add(res);
        }

    }

    /**
    * Class that represents the information of the files found in the P2P network
    */
    protected class ResultFile{

       public-init var name:String;
       public-init var size:Number;
       public-init var containers:Integer;
       public-init var adv:U2UContentAdvertisementImpl;

    }


    /**
    * Runs a seacrh in the P2P network throught the U2UShell
    */
    public function runsSearch(value:String):Void{

        U2UFXApp.APP.shell.executeCmd("u2ufss -search {value} -n");
    }

