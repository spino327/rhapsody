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

/**
 * @author Irene
 */

public class U2USearchTable extends CustomNode {

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



}

protected class ResultFile{

   public-init var name:String;
   public-init var size:Number;
   public-init var containers:Integer;

}
