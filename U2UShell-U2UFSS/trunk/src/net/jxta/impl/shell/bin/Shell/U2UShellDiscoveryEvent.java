/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jxta.impl.shell.bin.Shell;

import java.util.Enumeration;
import java.util.EventObject;
import net.jxta.discovery.DiscoveryEvent;

/**
 *  Container for U2U Shell Discovery events.
 */
public class U2UShellDiscoveryEvent extends EventObject{

    private final Enumeration response;
    private final int queryID;

    /**
     *  Creates a new event
     *
     *  @param source The source of the event is the toString object that represents the Shell Object.
     *  @param response The response message for which this event is being generated.
     *  @param queryid The query id associated with the response returned in this event
     */
    public U2UShellDiscoveryEvent(Object source, Enumeration response, int queryid) {
        super(source);
        this.response = response;
        this.queryID = queryid;
    }
    
    /**
     *  Creates a new event from a DiscoveryEvent
     *
     * @param event DiscoveryEvent from we create a U2UShellDiscoveryEvent
     */
    public U2UShellDiscoveryEvent(DiscoveryEvent event)
    {
        this(event.getSource(), event.getResponse().getResponses(), event.getQueryID());
    }

    /**
     *  Returns the response associated with the event, the response is a set of advertisements
     *  converts to String
     *
     *  @return Enumeration
     *
     */
    public Enumeration getResponse() {

        return response;
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
