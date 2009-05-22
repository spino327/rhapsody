/*
 * U2UFXApp.fx
 *
 * Created on 17-may-2009, 11:17:06
 */

package org.u2u.app;

import org.u2u.gui.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.lang.System;
import net.jxta.platform.NetworkManager;

/*
 * @author sergio
 */

public class U2UFXApp {

    var mainStage : Stage;
    public var stage: U2UContentStage;

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
        println("Hola start");

        var ini:Long = System.currentTimeMillis();

        stage = U2UContentStage{
            width: 650;
            height: 520;
            style: StageStyle.DECORATED;
            visible: true;
        };
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
        println("Hola ready");
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
    protected function shutdown():Void
    {
        // TBD should call TaskService#shutdownNow() on each TaskService
    }
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
    app.ready();

    var manager: NetworkManager = new NetworkManager(NetworkManager.ConfigMode.EDGE, "test");
    
    //var conf: NetworkConfigurator = manager.getConfigurator();
}

function run(args:String[])
{
    launch(U2UFXApp {});
}