/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.u2u.filesharing.fsprotocol;

import java.util.EventObject;

/**
 * Container for U2U File Sharing Protocol events
 * @author Irene & Sergio
 */
public class U2UFileSharingProtocolEvent extends EventObject {

    private final U2UFSProtocolResponse response;
    private final U2UFSProtocolOrder order;
    //
    /**
     * Creates a new event
     * @param response The response to pass to the listeners
     * @param order The order to pass to the listeners
     * @param protocolId id of the protocol
     */
    private U2UFileSharingProtocolEvent(U2UFSProtocolResponse response, U2UFSProtocolOrder order, String protocolId)
    {
        super(protocolId);
        this.response = response;
        this.order = order;
    }

    /**
     * Returns the response associated with the event, the response is an object from a subclass
     * of U2UFSProtocolResponse
     *
     * @return an object from a subclass of U2UFSProtocolResponse
     */
    public U2UFSProtocolResponse getResponse()
    {
        return response;
    }

    /**
     * Returns the order associated with the event, the order is an object from a subclass
     * of U2UFSProtocolOrder
     *
     * @return an object from a subclass of U2UFSProtocolOrder
     */
    public U2UFSProtocolOrder getOrder()
    {
        return order;
    }

    //static
    /**
     * Returns an instance of the U2UFileSharingProtocolEvent with event's order
     * @param order the incoming order
     * @param protocolId id of the protocol
     * @return U2UFileSharingProtocolEvent object
     */
    public static U2UFileSharingProtocolEvent newOrderEvent(U2UFSProtocolOrder order, String protocolId)
    {
        if(order == null)
        {
            throw new NullPointerException("the order can't be null");
        }

        return new U2UFileSharingProtocolEvent(null, order, protocolId);
    }

    /**
     * Returns an instance of the U2UFileSharingProtocolEvent with event's response
     * @param response the incoming response
     * @param protocolId id of the protocol
     * @return U2UFileSharingProtocolEvent object
     */
    public static U2UFileSharingProtocolEvent newResponseEvent(U2UFSProtocolResponse response, String protocolId)
    {
        if(response == null)
        {
            throw new NullPointerException("the response can't be null");
        }

        return new U2UFileSharingProtocolEvent(response, null, protocolId);
    }
}
