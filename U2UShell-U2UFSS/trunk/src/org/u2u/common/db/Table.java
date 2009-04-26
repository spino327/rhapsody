/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.u2u.common.db;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

/**
 * This class is the root of the db classes
 * @author irene & sergio
 */
public abstract class Table {

    protected static LinkedBlockingQueue<ConnectTo> conDBQueue = new LinkedBlockingQueue<ConnectTo>(5);
    //protected static final Semaphore door = new Semaphore(1, true);
    protected static boolean isConnected = false;

    /**
     * this method make severals instances of ConnectTo(connections) to the Database
     */
    public static final void connect()
    {
        for(int i = 0; i < 5; i++)
        {
            ConnectTo conDB = new ConnectTo("org.apache.derby.jdbc.EmbeddedDriver");
            boolean isCon = false;
            //check if the db's folder already getU2UContentAdvertisementImpl
            if (!(isCon = conDB.getConnection("jdbc:derby:U2UClient", "U2U", ""))) {
                //if debug mode then put 'createFrom=conf/.U2UClient'
                //for build the distribution then put 'createFrom=conf/.U2UClient'
                isCon = conDB.getConnection("jdbc:derby:U2UClient;createFrom=conf/.U2UClient", "U2U", "");
            }

            if(isCon)
            {
                isConnected = true;
                conDBQueue.offer(conDB);
            }
        }
        
    }

    /**
     * this method disconnect from the database
     */
    public static final void disconnect()
    {
        for(Object obj : conDBQueue.toArray())
        {
            ((ConnectTo) obj).closeConnection();
        }
    }

}
