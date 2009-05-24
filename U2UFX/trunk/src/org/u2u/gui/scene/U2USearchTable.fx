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

/**
 * @author Irene
 */

public class U2USearchTable extends CustomNode, U2USearchListener{

    public var results:ResultFile[];
    public var selection:Integer;
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

                    rows: bind for (f in results)[

                          SwingTable.TableRow{
                            cells:[
                                SwingTable.TableCell{text:f.name },
                                    SwingTable.TableCell{text: String.valueOf(f.size) },
                                        SwingTable.TableCell{text: String.valueOf(f.containers) },
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
            insertResultInTable(adv);

        }
        println("LLEGO ANUNCIO");
    }

    function insertResultInTable(adv:U2UContentAdvertisementImpl):Void{

        var res:ResultFile = ResultFile{
            name: adv.getName();
            size: adv.getLength();
            containers:1;
        }

        insert res into results;
    }

}

protected class ResultFile{

   public-init var name:String;
   public-init var size:Number;
   public-init var containers:Integer;

}



public function runsSearch(value:String):Void{

    U2UFXApp.APP.shell.executeCmd("u2ufss -search {value} -n");
}

