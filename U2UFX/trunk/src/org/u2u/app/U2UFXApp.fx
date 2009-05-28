/*
 * U2UFXApp.fx
 *
 * Created on 17-may-2009, 11:17:06
 */

package org.u2u.app;

import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.image.*;

import org.jfxtras.async.JFXWorker;
import org.u2u.gui.*;

import java.util.concurrent.ExecutionException;
import java.io.File;
import java.lang.System;
import java.io.IOException;

import net.jxta.platform.*;
import net.jxta.impl.shell.bin.Shell.Shell;
import java.lang.Exception;
import net.jxta.peergroup.NetPeerGroupFactory;
import net.jxta.peergroup.PeerGroup;
import java.lang.Thread;
import java.lang.InterruptedException;
import org.u2u.gui.scene.U2UIntroAnimation;
import org.u2u.gui.scene.U2UAbstractScene;



//static variables(class variables)

public-read var APP: U2UFXApp;

/** State connect*/
public-read var CONNECT: Integer = 1;
/** State disconnect*/
public-read var DISCONNECT: Integer = 0;
/** Service U2UFSS started*/
public-read var U2UFSS_INIT: Integer = 1;
/** Service U2UFSS stopped*/
public-read var U2UFSS_STOP: Integer = 0; 
/**
 * 
 * @author sergio
 */
public class U2UFXApp {

    var mainStage : Stage;
    var stage: U2UContentStage;
    /** task that init the network*/
    var initTask: JFXWorker;
    /** task that stop the network*/
    var stopTask: JFXWorker;

    var status: Integer;
    var status_u2ufss:Integer = 0; //the service is stoped

    /** U2UShell's instance that run the p2p commands*/
    public-read var shell: Shell;
    /** Peer's peergroup*/
    var netPeerGroup: PeerGroup;

    init {

        println("registering the shutdown function");
        FX.addShutdownAction(function():Void {
            println("call the FX close listener");
            this.quit();
        });
    }


    //lifecycle
    
    /**
     * Responsible for starting the application; for creating and showing
     * the initial GUI.
     * <p>
     * This method is called by the "static" launch method (script function launch),
     * subclasses must override it.  It runs on the event dispatching
     * thread.
     *
     */
    protected function startup():Void
    {
        println("Hi start");
        //init the content stage
        stage = U2UContentStage {
           
            width: 650;
            height: 520;
            style: StageStyle.DECORATED;
            visible: false;
            /*onClose:function():Void{
                println("call the stage close listener");
                this.quit();
            }*/
        };
        //unable the scene
        //stage.disableMainScene();
        this.initShell();
     }

    /**
     * Called after the startup() method has returned.
     * When this method is called, the application's GUI is ready
     * to use.
     * <p>
     * It's usually important for an application to start up as
     * quickly as possible.  Applications can override this method
     * to do some additional start up work, after the GUI is up
     * and ready to use.
     *
     */
    protected function ready():Void
    {
        println("Hi ready");
        //init the intro animation
        var inStage: Stage = Stage {
            title: "File Sharing P2P App"
            icons: Image{url:"{__DIR__}iconu2u.png"};
            width: 650;
            height: 520;
            style: StageStyle.TRANSPARENT;
            visible: true;
            /*onClose: function():Void{
                println("call the intro close listener");
                this.quit();
            }*/
        };
        inStage.scene = U2UAbstractScene.getU2UIntroAnimation(inStage, stage);

    }

    /**
     * Called when the application {@link #exit exits}.
     * Subclasses may override this method to do any cleanup
     * tasks that are neccessary before exiting.  Obviously, you'll want to try
     * and do as little as possible at this point.  This method runs
     * on the event dispatching thread.
     *
     * @see #startup
     * @see #ready
     * @see #exit
     * @see #addExitListener
     */
    public function quit(): Void {

        println("closing the application");

        this.stopShell();

        FX.exit();
    }
    //EO Life cycle

    //P2P

    /**
     * Return the U2UFSS's state
     * @return 1 init, 0 stop
     */
    public function getStatusServiceU2UFSS(): Integer {
        return status_u2ufss;
    }

    /**
     * Return the Peer's state in the P2P Network
     * @return peer's state: CONNECT or DISCONNECT
     */
    public function getStatus(): Integer {
        return status;
    }

    /**
     * try to config the peer
     */
    public function peerConfig(): Void {

        this.shell.executeCmd("peerconfig");
        this.stopShell();

        try {
            Thread.sleep(2000);
        } catch (ex: InterruptedException) {
            ex.printStackTrace();
        }

        this.initShell();
    }


    /**
     * Connect the peer to the P2P network and init the U2UShell
     */
    protected function initShell(): Void {
//
//        viewPpal.setStatusBarProgress(true);
//        viewPpal.disableConnect();

        if(initTask != null)
        {
            if(not initTask.cancelled)
            {
                initTask.cancel();
                initTask = null;
            }

        }

        initTask = JFXWorker {
            
            inBackground: function():Object {
                // Establish the default store location via long established hackery.
                var jxta_home: String = System.getProperty("JXTA_HOME", ".jxta");

                if (not jxta_home.endsWith(File.separator)) {
                    jxta_home += File.separator;
                }

                var homedir: File = new File(jxta_home);
                if (not homedir.exists()) {
                    homedir.mkdirs();
                }
//
//              //init JXTA building a Peer Group instance
                initTask.publish(["setting the p2p network...", 0.2]);
                var netPeerGroupFactory: NetPeerGroupFactory = new NetPeerGroupFactory();
                initTask.publish(["initializing the p2p network...", 0.4]);
                netPeerGroup = netPeerGroupFactory.getInterface();
                initTask.publish(["waiting for U2UShell...", 0.8]);
                shell = new Shell(netPeerGroup);
                initTask.publish(["ok...", 1.0]);
                
                
                if ((netPeerGroup != null) and (shell != null))
                {
                    status = U2UFXApp.CONNECT; // new state connected
                }
                
                return status;
            }
            
            process: function(data) {
//                var files = data as File[];
//                insert files[0..(24-count)] into searchResults;
//                count += sizeof data;
//                resultText = "Found {count} files";
                println(data);
            }
            
            onDone: function(result) {
                
                var res: Integer = result as Integer;

                if(res == U2UFXApp.CONNECT)
                {
                    //viewPpal.setStatusMenusItems(true);

                    //init the service u2ufss
                    //shell.executeCmd("u2ufss -init "+ config.getConfigMode().toString());
                    shell.executeCmd("u2ufss -init" 
                        " {if (netPeerGroup.isRendezvous()) then ("RENDEZVOUS") else ("EDGE")}");
                    status_u2ufss = U2UFSS_INIT;

                    //registerSearchListener();
                    //registerServiceListeners();
                    stage.registerListeners();
                    //Show the sharedfiles
                    shell.executeCmd("u2ufss -showsf ");
                }

                //peerId = netPeerGroup.getPeerID().toString();
                //peerId = netPeerGroup.substring(peerId.length() - 4 );

                //viewPpal.setPeerID(peerId);
                //viewPpal.setSocketID();
                this.ready();

            }
            
            onFailure: function(ex:ExecutionException):Void {
                ex.printStackTrace();
            }
        };
    }

    /**
     * Disconnect the peer to the P2P network and init the U2UShell
     */
    protected function stopShell(): Void {

        println("disconnecting for the network...");

        if(status == U2UFXApp.CONNECT) {
//                    viewPpal.disableDisconnect();
//                    viewPpal.setStatusMenusItems(false);

            if(shell != null) {

                println("shell != null");

                shell.executeCmd("u2ufss -stop");
                shell.executeCmd("search -f");//flush all the contents' advertisements
                shell.executeCmd("peers -f");//flush all the peers' advertisements
                shell.executeCmd("exit");
                status_u2ufss = U2UFSS_STOP;
            }

            status= U2UFXApp.DISCONNECT;
            netPeerGroup.stopApp();


        }
        else if(status == U2UFXApp.DISCONNECT) {

            println("already disconnected");
        }

        shell = null;
        netPeerGroup = null;

        println("Stopped service was successful!");
        System.gc();

    }
    //EO P2P

}

//static methods

/**
 * Creates an instance of the specified {@code Application}
 * subclass, sets the {@code ApplicationContext} {@code
 * application} property, and then calls the new {@code
 * Application's} {@code startup} method.  The {@code launch} method is
 * typically called from the Application's {@code main}:
 * <pre>
 *     public static void main(String[] args) {
 *         Application.launch(MyApplication.class, args);
 *     }
 * </pre>
 * The {@code applicationClass} constructor and {@code startup} methods
 * run on the event dispatching thread.
 *
 */
function launch(app:U2UFXApp)
{
    app.startup();
    //app.ready();

    var manager: NetworkManager = new NetworkManager(NetworkManager.ConfigMode.EDGE, "test");
    
    //var conf: NetworkConfigurator = manager.getConfigurator();
}

function run(args:String[])
{
    APP = U2UFXApp {};
    launch(APP);
}
