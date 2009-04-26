/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.u2u.filesharing;

import java.util.EventObject;

/**
 *
 * @author irene & sergio
 */
public class U2UFileSharingServiceEvent extends EventObject{
   

    private int type;
    private Object[] information;

    public static final int PROGRESS = 0;
    public static final int DOWNLOAD = 1;
    public static final int PAUSEDOWNLOAD = 2;
    public static final int STOPDOWNLOAD = 3;
    public static final int SHARED = 4;
    public static int SOURCES_DOWN = 5;
    public static int UPLOADS = 6;

    public U2UFileSharingServiceEvent(U2UFileSharingService srv, Object[] info, int type)
    {
        super(srv);
        this.information = info;
        this.type = type;
    }

    public Object[] getInformation()
    {
        return information;
    }


    public int getType()
    {
        return type;
    }
}
