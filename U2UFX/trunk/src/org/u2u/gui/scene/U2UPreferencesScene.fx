/*
 * U2UPreferencesScene.fx
 *
 * Created on 27-may-2009, 11:56:54
 */

package org.u2u.gui.scene;

import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.*;
import javafx.ext.swing.SwingTextField;
import javafx.ext.swing.SwingComboBox;
import javafx.ext.swing.SwingComboBoxItem;
import javafx.ext.swing.SwingCheckBox;

import javafx.scene.paint.Color;
import javafx.scene.layout.*;
import javafx.scene.effect.Bloom;
import javafx.scene.effect.DropShadow;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.io.IOException;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import org.u2u.gui.scene.ButtonNode;
import org.u2u.app.U2UFXApp;



/**
 * @author sergio
 */


 public class U2UPreferencesScene extends U2UAbstractMain{

    /**
    * numSharedFiles, represents the number of shared files at the same time
    * to other peers in the P2P network
    */
    var numSharedFiles:String = "20" on replace {
        if(numSharedFiles.equals(""))
        {}else if(Integer.parseInt(numSharedFiles)<=2 or Integer.parseInt(numSharedFiles)>50)
        {
            JOptionPane.showMessageDialog(null, "The amount of shared files must be greater than 2 and less than 11");
            
        }
    };

    /**
    * numConDown, represents the maximum number of conecction that this peer 
    * can do to download a file
    */
    var numConDown:String = "5" on replace {
        
        if(numConDown.equals(""))
        {}if(numConDown.equals("") or Integer.parseInt(numConDown)<1 or Integer.parseInt(numConDown)>100)
        {
            JOptionPane.showMessageDialog(null, "The maximum number of connections per download to be more equal to 1 and equal to less than 100");
        }
    };

    /**
    * numConUpload, represents the maximum number of conecction that others peers 
    * can do to this peer to download a shared file
    */
    var numConUpload:String = "5" on replace{
        
        if(numConUpload.equals(""))
        {}if( numConUpload.equals("") or Integer.parseInt(numConUpload)<1 or Integer.parseInt(numConUpload)>100)
        {
            JOptionPane.showMessageDialog(null, "The maximum number of connections must be greater load equal to 1 and equal to 100 less");
        }
    };

    /**
    * numDownSim, represents the maximum number of conecction simultaneous that 
    * this peer can do to download files from others peer in the P2P network
    */
    var numDownSim:String = "20" on replace {
        
        if(numDownSim.equals(""))
        {}if(numDownSim.equals("") or Integer.parseInt(numDownSim)<=2 or Integer.parseInt(numDownSim)>50)
        {
            JOptionPane.showMessageDialog(null, "The amount of shared files must be greater than 2 and less than 11");
        }
    };

    /**
    *   Maximum tranfer unit for a file
    */
    var mtu:String;
    /**
    * Default configuration for this peer
    */
    var defconfig:Properties;

    /**
    * Properties file that stored the configuration parameters set by the user
    */
    var config:Properties;

    //Swing Variables

    var sharedText:SwingTextField;
    var conDownText:SwingTextField;
    var conUploadText:SwingTextField;
    var downSimText:SwingTextField;
    var selMTU:SwingComboBox;
    

    var mtuSelIndex:Integer = 2 on replace {

        if(mtuSelIndex==0){
            mtu = "16384";
        }else if(mtuSelIndex==1){
            mtu = "32768";
        }else if(mtuSelIndex==2){
            mtu = "65536";
        }
    };
    
    var returnDefConfig:Boolean = false;

    init {

       if(not(new File("conf/.defconfig.properties")).exists())
       {
            createConfig("defconfig");
       }

       configurePeer();
       viewConfig();

        this.contentPane = Group{

        content:[
                ImageView {
                    image:Image{
                            url: "{__DIR__}resources/content2.png";
                        }
                    translateX:210;
                    translateY:25;
                },
                Group{
                    translateX:230;

                    content: [
                        Text{
                            translateX:40;
                            translateY:65;
                            content:"Please insert your preferences to upload\n "
                                    "and download files in the P2P network.";
                            textAlignment: TextAlignment.JUSTIFY;
                            fill:Color.DARKGREEN;
                            font: Font.font("Verdana",FontWeight.BOLD ,13);
                            effect: DropShadow {
                                offsetX: 3
                                offsetY: 3
                                color: Color.SNOW
                                radius: 12
                            }
                        },
                        Group{
                            translateY:130;
                            translateX:10

                            content:[
                                Text{
                                    content:"Number of shared Files at the same time:";
                                    font: Font.font("Verdana",13);
                                },
                                sharedText = SwingTextField{
                                    translateY:-15;
                                    translateX: 320;
                                    width:40;
                                    text: bind numSharedFiles with inverse;
                                },
                                Text{
                                    translateY:50;
                                    content:"Highest number of connections per download:";
                                    font: Font.font("Verdana",13);
                                },
                                conDownText = SwingTextField{
                                    translateY:35;
                                    translateX:  320;
                                    width:40;
                                    text: bind numConDown with inverse;
                                },
                                Text{
                                    translateY:100;
                                    content:"Highest number of connections per upload:";
                                    font: Font.font("Verdana",,13);
                                },
                                conUploadText = SwingTextField{
                                    translateY:85;
                                    translateX:  320;
                                    width:40;
                                    text: bind numConUpload with inverse;
                                },
                                Text{
                                    translateY:150;
                                    content:"Number of simultaneous downloads:";
                                    font: Font.font("Verdana",13);
                                },
                                downSimText = SwingTextField{
                                    translateY:135;
                                    translateX: 320;
                                    width:40;
                                    text: bind numDownSim with inverse;
                                },
                                Text{
                                    translateY:200;
                                    content:"Maximum Transfer Unit:";
                                    font: Font.font("Verdana",13);
                                },
                                selMTU = SwingComboBox{
                                    translateY:185;
                                    translateX: 290;
                                    width:70
                                    items: [
                                        SwingComboBoxItem{
                                            text:"16 KB"
                                            value:16 as Integer
                                        },
                                        SwingComboBoxItem{
                                            text:"32 KB"
                                            value:32 as Integer
                                        },
                                        SwingComboBoxItem{
                                            text:"64 KB"
                                            value:64 as Integer
                                        },
                                    ]
                                    selectedIndex: bind mtuSelIndex with inverse;
                                },
                            ]
                         },
                         ButtonNode{
                            translateY:385;
                            translateX: 170;
                            title:"Apply Changes";
                            imageURL:"{__DIR__}resources/ok.png";
                            action: function():Void{
                                applyChanges();
                            }
                        },
                        SwingCheckBox{
                            
                            translateY:355;
                            translateX: 80;
                            text:"Return to default configuration";
                            selected: bind returnDefConfig with inverse;
                        },

                        ButtonNode{

                            translateY:365;
                            translateX: 30;
                            imageURL:"{__DIR__}resources/configure.png";
                            title:"Reconfig this peer"
                            action: function():Void{

                                U2UFXApp.APP.peerConfig();
                            }
                        },
                        
                        Text{
                            translateY:443;
                            translateX:90;
                            content:"Note: This setup will be applied at the\n"
                                    "next start of the P2P network !!.";
                            textAlignment: TextAlignment.CENTER;
                            fill:Color.BLUE;
                            font: Font.font("Verdana",FontWeight.SEMI_BOLD,FontPosture.ITALIC,11);

                            effect: DropShadow {
                                offsetX: 3
                                offsetY: 3
                                color: Color.LIGHTGRAY
                                radius: 10
                            }
                        }

                    ];
                 }
              ];
           }
    }

    /**
     * Makes the default configuration for this peer
    */
    function createConfig(nameFile:String):Void{
        var out:FileOutputStream  = null;
        defconfig = new Properties();
        try
        {
            defconfig.put("ConDown", "5");
            defconfig.put("Down", "20");
            defconfig.put("Upload", "20");
            defconfig.put("ConUpload", "5");
            defconfig.put("MTU", "65536");
            defconfig.put("soTimeOut","60000");


            out = new FileOutputStream("conf/.{nameFile}.properties");
            try
            {   defconfig.store(out, "Default configuration's file");

            }catch (ex:IOException ) {
                println("Unable for makes the default configuration file in createConfig()");
            }

        } catch (ex:IOException) {
            println("IOException in createConfig()");

        }  finally
        {
            try
            {
                out.close();
            } catch (ex:IOException )
            {
                println("IOException in createConfig() when was closing the out file");
            }
        }
    }

    /**
    * Upload the configuration parameters set in the properties file
    */
    function configurePeer():Void{

        //Init with the default configuration if here don't exists other create by the user
        var con:File = new File("conf/.config.properties");
        if (con.exists())
        {
            try {
                var fin:FileInputStream  = new FileInputStream("conf/.config.properties");
                config = new Properties();
                try {
                    config.load(fin);

                    //estableciendo los parametros de configuracion

                    numConUpload = config.getProperty("ConUpload");
                    numSharedFiles = config.getProperty("Upload");
                    numConDown = config.getProperty("ConDown");
                    numDownSim = config.getProperty("Down");
                    mtu = config.getProperty("MTU");

                } catch (ex:IOException ) {
                    JOptionPane.showMessageDialog(null,"Unable to load the configuration file", "Configuration Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (ex:FileNotFoundException) {
                println("Unable to load the configuration file");
            }
        } else
        {
            var defCon:File  = new File("conf/.defconfig.properties");
            if (defCon.exists())
            {
                setDefaultConfig();
            }
        }

    }

    /**
    * Upload the configuration stored in the properties file and shows it in
    * the preferences scene
    */
    function viewConfig():Void{

        var mtuIndex:Integer= Integer.parseInt(mtu);
        mtuIndex = mtuIndex/1024;

        if(mtuIndex == 16){
            mtuSelIndex = 0;
        }else if(mtuIndex == 32){
           mtuSelIndex = 1;
        }else if(mtuIndex == 64){
            mtuSelIndex = 2;
        }

    }

    /**
    * 	Retrieves the default settings and refresh the instance variables
    */
    function setDefaultConfig():Void
    {
       var fin: FileInputStream  = null;
        try {
            fin = new FileInputStream("conf/.defconfig.properties");
            defconfig = new Properties();
            try {
                defconfig.load(fin);

                numConUpload = defconfig.getProperty("ConUpload");
                numSharedFiles = defconfig.getProperty("Upload");
                numConDown = defconfig.getProperty("ConDown");
                numDownSim = defconfig.getProperty("Down");
                mtu = defconfig.getProperty("MTU");

            } catch (ex:IOException ) {
                    JOptionPane.showMessageDialog(null,"Unable to load the configuration file", "Configuration Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (ex:FileNotFoundException ) {
                println("Unable to load the configuration file");
        } finally {
            try {
                fin.close();
            } catch (ex:IOException ) {
                println("throw IOException in setDefaultConfig() into U2UPreferencesScene");

            }
        }
    }

     /**
     *  Applies the changes  made by user
     */
    function applyChanges():Void{

        if(returnDefConfig)
        {
            //Verifies that exists default configuration
            var file:File = new File("conf/.defconfig.properties");
            if(file.exists())
            {
                setDefaultConfig();
            }else
            {
                //stablish the new configuration equals to the default configuration
                createConfig("config");
                configurePeer();
                viewConfig();
                //showMessageConfig();
            }

        }else{

            if(checkForNull())
            {
                applyConfig();
                configurePeer();
                viewConfig();
                //showMessageConfig();
            }
         }
    }

    /**
    * Check null values in the configuration
    */
    function checkForNull():Boolean{

        if(numSharedFiles.equals(""))
        {   JOptionPane.showMessageDialog(null, "The amount of shared files must be greater than 2 and less than 11");
            return false;
        }else if(numConDown.equals(""))
        {   JOptionPane.showMessageDialog(null, "The maximum number of connections per download to be more equal to 1 and equal to less than 100");
            return false;
        }else if(numConUpload.equals(""))
        {   JOptionPane.showMessageDialog(null, "The maximum number of connections must be greater load equal to 1 and equal to 100 less");
            return false;
        }else if(numDownSim.equals(""))
        {   JOptionPane.showMessageDialog(null, "The amount of shared files must be greater than 2 and less than 11");
            return false;
        }
        return true;
    }


     /**
     * Applies a configuration set by a user to be completed in the next netlogon.
     */
    function applyConfig():Void{

       var con:Properties = new Properties();
       var out:FileOutputStream = null;

       try {

            con.put("ConDown", numConDown);
            con.put("Down", numDownSim);
            con.put("Upload", numSharedFiles);
            con.put("ConUpload",numConUpload);
            con.put("MTU", mtu);
            con.put("soTimeOut","60000");

            out = new FileOutputStream("conf/.config.properties");

            try
            {
                con.store(out, "Last configuration stored");
            } catch (ex:IOException)
            {
                JOptionPane.showMessageDialog(null,"Unable to save configuration","Unable to save configuration",JOptionPane.ERROR_MESSAGE);
               
            }

        } catch (ex:FileNotFoundException ) {
            println("FileNotFoundException in applyConfig() into U2UPreferencesScene");
        }finally
        {
            try
            {
                out.close();
            } catch (ex:IOException )
            {
               println("IOException in applyConfig() into U2UPreferencesScene");
  
            }
        }
    }

    override function updateButtons() {

        butDown.aplyPressed = null;
        butShare.aplyPressed = null;
        butSearch.aplyPressed = null;

    }

}



