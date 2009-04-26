/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.u2u.filesharing.downloadpeer;

import java.util.Enumeration;
import java.util.EventObject;
import java.util.Vector;
import net.jxta.discovery.DiscoveryEvent;
import org.u2u.filesharing.U2UContentAdvertisementImpl;

/**
 *  Container for U2U Content Discovery events.
 */
public class U2UContentDiscoveryEvent extends EventObject {

    private final Enumeration responseAdv;
    private final int queryID;

    /**
     *  Creates a new event
     *
     *  @param source The source of the event is the toString object that represents the U2UFileSharingService Object.
     *  @param response The response message for which this event is being generated.
     *  @param queryid The query id associated with the response returned in this event
     */
    public U2UContentDiscoveryEvent(Object source, Enumeration<U2UContentAdvertisementImpl> response, int queryid) {
        super(source);
        this.responseAdv = response;
        this.queryID = queryid;
    }
    
    /**
     * Creates a new event from a DiscoveryEvent
     *
     * @param event DiscoveryEvent from we create a U2UContentDiscoveryEvent
     * @param advs incoming U2UContentAdvertisementImpl
     */
    @SuppressWarnings("unchecked")
    public U2UContentDiscoveryEvent(DiscoveryEvent event, Vector advs)
    {
        this(event.getSource(), advs.elements(), event.getQueryID());
    }

    /**
     *  Returns the response associated with the event, the response is a set of advertisements
     *  of type U2UContentAdvertisementImpl
     *
     *  @return Enumeration
     *
     */
    public Enumeration getResponseAdv() {

        return responseAdv;
    }
    /**
     *  Returns the query id associated with the response returned in this event
     *
     *  @return query id associated with the response
     */
    public int getQueryID() {

        return queryID;
    }

}