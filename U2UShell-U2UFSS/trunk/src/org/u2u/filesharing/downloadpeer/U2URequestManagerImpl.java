/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.u2u.filesharing.downloadpeer;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.discovery.DiscoveryService;
import net.jxta.peergroup.PeerGroup;
import org.u2u.filesharing.U2UContentAdvertisementImpl;
import org.u2u.filesharing.U2UFileSharingService;

/**
 * RequestModule implementation for managing search of shared content at the U2U network
 */
public class U2URequestManagerImpl implements DiscoveryListener {

    /**
     * The table of U2USearchListeners.
     */
    private Set<U2USearchListener> searchListeners = new HashSet<U2USearchListener>();
    private U2UFileSharingService fss;
    private PeerGroup group;
    private DiscoveryService disSer;
    private int actualQueryId;//send to the listeners only for this actualQueryId
    /** Event Dispatcher*/
    private final U2URMEventDispatcher dispacher;
    /**
     * Event dispacher
     */
    private class U2URMEventDispatcher implements Runnable {

        //FIXME spino327@gmail.com a Set isn't synchronized
        private Set<U2USearchListener> listeners;
        /**queue that represents the objects send to the protocol's listeners*/
        private LinkedBlockingQueue<U2UContentDiscoveryEvent> toListenersQueue;

        private boolean isStoped;

        public U2URMEventDispatcher(Set<U2USearchListener> list)
        {
            listeners = list;
            toListenersQueue = new LinkedBlockingQueue<U2UContentDiscoveryEvent>();
        }

        public void run()
        {
            //init
            isStoped = false;

            //FIXME spino327@gmail.com not security implemented!
            while(!isStoped)
            {
                //getting object from the Queue
                U2UContentDiscoveryEvent obj = null;//blocking
                try
                {
                    obj = toListenersQueue.take();
       
                    // are there any registered U2USearch listeners,
                    // generate the event and callback.
                    long t0 = System.currentTimeMillis();

                    Object[] allListeners = listeners.toArray(new Object[0]);

                    for (Object allListener : allListeners) {
                        ((U2USearchListener) allListener).contentAdvertisementEvent(obj);
                    }

                    Logger.getLogger(U2URequestManagerImpl.class.getName()).log(
                            Level.SEVERE, null, "Called all listenters to query #" + obj.getQueryID() +
                            " in :" + (System.currentTimeMillis() - t0));

                } catch (InterruptedException ex)
                {
                    Logger.getLogger(U2URequestManagerImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }

            //free
            this.listeners = null;
            this.toListenersQueue = null;
        }

        public void stopEventDispatcher()
        {
            this.isStoped = true;
        }

        /**
         * Sends the event to the listeners of the request manager
         * @param obj U2UContentDiscoveryEvents to send to the listeners
         */
        protected synchronized void invokeListenerMethod(U2UContentDiscoveryEvent event) {

            this.toListenersQueue.offer(event);//non blocking
        }
    }

    /**
     * Create a new instance using the specific U2UFileSharingService object
     * @param service
     */
    public U2URequestManagerImpl(U2UFileSharingService service)
    {
        fss = service;
        group = fss.getGroup();
        disSer = group.getDiscoveryService();
        disSer.addDiscoveryListener(this);

        //event dispacher
        this.dispacher = new U2URMEventDispatcher(searchListeners);
        new Thread(this.dispacher, "eventDispatcher@requestManager"+this.hashCode()).start();
    }

    /**
     * invoke the method for the inteface 
     */     
    /*private void invokeListenerMethod(Set listeners, DiscoveryEvent event) {


        // are there any registered discovery listeners,
        // generate the event and callback.
        long t0 = System.currentTimeMillis();

        Object[] allListeners = listeners.toArray(new Object[0]);

        for (Object allListener : allListeners) {
            ((U2USearchListener) allListener).contentAdvertisementEvent(new U2UContentDiscoveryEvent(event));
        }

        Logger.getLogger(U2URequestManagerImpl.class.getName()).log(
                Level.SEVERE, null, "Called all listenters to query #" + event.getQueryID() +
                " in :" + (System.currentTimeMillis() - t0));
    }*/
    
    /**
     * implementation of the only required method by the DiscoveryListener,
     * @param event 
     */
    @SuppressWarnings("unchecked")
    public void discoveryEvent(DiscoveryEvent event)
    {
        Enumeration en = event.getSearchResults();
        Vector<U2UContentAdvertisementImpl> advs = new Vector<U2UContentAdvertisementImpl>();
        //Get the PeerId of this peer uuid-xxxxx
        String idPeer = group.getPeerID().getUniqueValue().toString();
        //any U2UContentAdvertisementImpl?
        while(en.hasMoreElements())
        {
            Object obj = en.nextElement();
            if((obj instanceof U2UContentAdvertisementImpl) &&
                (event.getQueryID() == actualQueryId))
            {
                //invokeListenerMethod(searchListeners, event);
                //dispacher.invokeListenerMethod(event);
                U2UContentAdvertisementImpl adv = (U2UContentAdvertisementImpl)obj;
                //Get the PeerID of the remote peer. Format: Socket:uuid-xxx
                String idPeerAdv = (adv.getSocketAdv().getName()).substring(7);

                if(!idPeerAdv.equals(idPeer))
                {
                    advs.add((U2UContentAdvertisementImpl)obj);
                }
                
            }
        }

        //call the listeners
        if(advs.size() > 0)
        {
            dispacher.invokeListenerMethod(new U2UContentDiscoveryEvent(event, advs));
        }
        
    }

    /**
     * Search the content in the U2UNetwork
     * @param attribute of the content to search
     * @param value of the content to search
     * @return true if the search was successfull
     */
    public boolean searchContent(String attribute, String value)
    {
        if(attribute!=null)
        {
            String val = (value != null? ("*" + value + "*").toLowerCase() : null);
            actualQueryId = disSer.getRemoteAdvertisements(null, DiscoveryService.ADV, attribute , val, 25);
            return true;
        }
        return false;
    }

    public boolean existRegisterListener(U2USearchListener lis)
    {
        if(searchListeners.contains(lis))
            return true;
        else
            return false;
    }
    
    /**
     * Add a listener for U2USearchListener events
     * @param listener a object that implements the interface U2USearchListener
     */
    public synchronized void addSearchListener(U2USearchListener listener)
    {
        searchListeners.add(listener);
    }
    
    /**
     * Remove a listener for U2USearchListener events
     * @param listener a object that implements the interface U2USearchListener
     * @return true if it was successful
     */
    public synchronized boolean removeSearchListener(U2USearchListener listener) {

        return searchListeners.remove(listener);
    }


}
