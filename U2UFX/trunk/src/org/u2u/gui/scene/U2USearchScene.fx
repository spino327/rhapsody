/*
 * U2USearchScene.fx
 *
 * Created on 20-may-2009, 12:10:51
 */

package org.u2u.gui.scene;

import org.u2u.filesharing.U2UContentAdvertisementImpl;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.Group;
import javafx.scene.effect.Glow;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.*;

import javafx.ext.swing.SwingTextField;
import javafx.ext.swing.SwingButton;
import javafx.ext.swing.SwingIcon;
import javafx.scene.text.*;

import org.u2u.gui.scene.U2USearchTable;

import javax.swing.JOptionPane;

/**
 * @author sergio
 */
public class U2USearchScene extends U2UAbstractMain{

    var imgBackground:Image;

    var imgBackView:ImageView;
    var table:U2USearchTable;
    var textField: SwingTextField;
    var icon: SwingIcon;

    var conDown:Integer = 1;

    init {

        imgBackground = Image{
            url:"{__DIR__}resources/content2.png";
        }

        icon = SwingIcon{
            image:Image{
                url:"{__DIR__}resources/search.png";
            }
        }
        this.contentPane = Group{
            var buttonSearch:SwingButton;
            content: [

                ImageView {
                    translateX:210;
                    translateY:25;
                    image:imgBackground;

                },
                Text{
                    content : "Keyword: "
                    translateX:this.width/2 - textField.width-80;
                    translateY:102;
                    font: Font.font("Verdana",FontWeight.BOLD,12)

                },

                textField = SwingTextField{
                    width: 210;
                    translateX:this.width/2 - textField.width-10;
                    translateY:90;

                },

                buttonSearch =  SwingButton{
                    translateX:this.width-110;
                    translateY:88;
                    icon:bind icon;
                    action: function():Void{
                        searchFiles(textField.text);
                    }
                },
                
                table = U2USearchTable{},

                ImageView{
                    translateX: 352;
                    translateY: this.height - 115;
                    scaleX:-0.9;
                    scaleY:-1;
                    image: Image{url:"{__DIR__}resources/frame.png"}
                },

                ButtonNode{
                    translateX: 400;
                    translateY: this.height - 105;
                    title:"Download";
                    imageURL:"{__DIR__}resources/download.png";
                    action:function():Void{
                       this.runDownloadFile();
                    }
                }
            ]
        };
    }

    /**
    * This function thwow a search in the P2P network thought JXTA. It search files
    * containing the key work value.
    */
    function searchFiles(value:String):Void{

        if(value == null or value.equals("") or value.length()<=0){
            //show a message: value isn't nothing
            JOptionPane.showMessageDialog(null,"You didn't introduce the keyword to search");

        }else{
                //It deletes old results
                table.deleteAllOldResults();
                //It runs a new search in the P2P network
                U2USearchTable.runsSearch(value);
        }
    }

    /**
    * Run a download for a file selected in the results's table
    */
    function runDownloadFile():Void{

        var adv:U2UContentAdvertisementImpl = table.getAdvertismentFileSelected();

        if(adv != null)
        {
            this.contentStage.downloadAFile(adv);
            JOptionPane.showMessageDialog(null, "Init finding sources to download...");
            this.contentStage.showDownload();
        }else
        {
            JOptionPane.showMessageDialog(null,"There is no file to download!\n Please perform a search.");

        }
    }
   
    /**
    *@return the index of the node selected in the scene
    */
    public function getAdvSelected():U2UContentAdvertisementImpl{
        var adv:U2UContentAdvertisementImpl = null;
        return adv;
    }

    /**
    * Return the search listener
    */
    public function getSearchListener():U2USearchTable{
        return table;
    }

    /**
    * Updtae the buttons for this scene
    */
    override function updateButtons() {
        butSearch.aplyPressed = Glow{level:0.3
        input:DropShadow{offsetX:3 color:Color.BLACK}};
        butShare.aplyPressed =  null;
        butDown.aplyPressed =  null;
    }

}
