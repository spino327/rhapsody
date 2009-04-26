                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.u2u.filesharing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.id.ID;
import net.jxta.id.IDFactory;
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeService;
import net.jxta.platform.NetworkManager;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.rendezvous.RendezVousService;
import net.jxta.rendezvous.RendezvousEvent;
import net.jxta.rendezvous.RendezvousListener;
import net.jxta.share.FileContent;
import net.jxta.socket.JxtaServerSocket;
import net.jxta.socket.JxtaSocket;
import org.u2u.common.db.SharedFiles;
import org.u2u.filesharing.downloadpeer.U2UDownloadingManager;
import org.u2u.filesharing.downloadpeer.U2URequestManagerImpl;
import org.u2u.filesharing.downloadpeer.U2USearchListener;
import org.u2u.filesharing.uploadpeer.U2UContentManagerImpl;
import org.u2u.filesharing.uploadpeer.U2UUploadingManager;

/**
 *
 * @author sergio e irene
 */
public class U2UFileSharingService {
    
    
    // The following two constants defines how long ContentAdvertisements are published
    // in Discovery.
    // FIXME: 11/03/2002 lomax@jxta.org We need to have a better lifecycle management
    // of the advertisements. Current settings are probably fine for usual usage of CMS
    // for the time being.
    
    private final static long DEFAULT_LOCAL_ADV_LIFETIME = DiscoveryService.DEFAULT_LIFETIME;
    private final static long DEFAULT_REMOTE_ADV_LIFETIME = DiscoveryService.DEFAULT_LIFETIME;//2 * 60 * 1000L; // 2 minutes
    private final String SERVICE_NAME = "U2UFileSharingService";
    private PeerGroup group;
    private final PipeAdvertisement socketAdv;
    //variable that manage files's register and publish in the network
    private final U2UContentManagerImpl conMan;
    //variable that search for content in the network
    private final U2URequestManagerImpl resMan;
    /**
     * store the configuration mode of peer (EDGE or  or RELAY)
     * 
     */
    private final String peerConfig;

    /**
     * The input pipe that received incoming connections
     */
    private JxtaServerSocket serverSocket;

    //hashMaps
    /**
     * Variable that manage instances of U2UDownloadingManager that represents file's downloadManagers
     * 1) sha1:XXXXX of the file that handle the specific instance of the U2UDownloadingManager
     * 2) the specific instance of the U2UDownloadingManage
     */
    private final HashMap<String, U2UDownloadingManager> downloadManagers = new HashMap<String, U2UDownloadingManager>();
    /**
     * Variable that manage instances of U2UUploadingManager that represents file's uploadManagers
     * 1) sha1:XXXXX of the file that handle the specific instance of the U2UDownloadingManager
     * 2) the specific instance of the U2UUploadingManager
     */
    private final HashMap<String, U2UUploadingManager> uploadManagers = new HashMap<String, U2UUploadingManager>();
    
    //Pools
    /**
     * thread pool for manage the U2UDownloadingManager instances 
     */
    private final ThreadPoolExecutor downloadsPool;
    /**
     * thread pool for manage the U2UUploadingManager instances
     */
    private final ThreadPoolExecutor uploadsPool;
    /**
     * thread pool for manage the connections to service
     */
    private final ExecutorService handlerPool = Executors.newSingleThreadExecutor();

    //Futures
    /**
     * References to the Future instances that represents the submit of a U2UDownloadingManager
     * 1) sha1:XXXXX of the file that handle the specific instance of the U2UDownloadingManager
     * 2) Future reference
     */
    private final HashMap<String, Future<?>> dmFutures = new HashMap<String, Future<?>>();
    /**
     * References to the Future instances that represents the submit of a U2UUploadingManager
     * 1) sha1:XXXXX of the file that handle the specific instance of the U2UUploadingManager
     * 2) Future reference
     */
    private final HashMap<String, Future<?>> umFutures = new HashMap<String, Future<?>>();

    //listener's DiscoveryService
    private U2USearchListener listener;

    private HashSet<U2UFileSharingServiceListener> lisSrv = new HashSet<U2UFileSharingServiceListener>();

    private boolean isServiceRunning;
    //ServerSocket Handler
    /**
     * instance of ServerSocketHandler
     */
    private ServerSocketHandler serverSocketHandler;

    /*
     * This class handle the accept of new sockets. 
     */
    private class ServerSocketHandler implements Runnable
    {
        private JxtaServerSocket server;
        private boolean running;
        private int soTimeOut;
        /**
         *
         * @param serSoc
         */
        public ServerSocketHandler(JxtaServerSocket serSoc, int soTimeOut)
        {
            this.server = serSoc;
            this.soTimeOut = soTimeOut;
        }

        public void run()
        {
            //init
            running = true;

            //EOInit

            while (running)
            {
                try {
                    System.out.println("Waiting for connections, Server Socket soTimeOut = " + server.getSoTimeout());
                    JxtaSocket socket = (JxtaSocket) server.accept();

                    socket.setSoTimeout(soTimeOut);
                    System.out.println("socket soTimeOut = " + socket.getSoTimeout());

                    if (socket != null) {
                        System.out.println("New socket connection accepted");

                        handlerPool.submit(new U2UConnectionHandler(socket,
                                U2UFileSharingService.this));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            //finish

            //EOFinish
        }

        /**
         * Stopping the handler
         */
        public void finish()
        {
            System.out.println("FSS stopping the ServerSocketHandler");
            this.running = false;
        }
    }

    /**
     * class that handle the rendezvous events
     */
    private class HandleRDV implements RendezvousListener{

        public void rendezvousEvent(RendezvousEvent event)
        {
            if(isServiceRunning)
            {
                switch(event.getType())
                {
                    case RendezvousEvent.CLIENTCONNECT:
                    {
                        System.out.println("--------\nFSS-HandleRDV- CLIENTCONNECT = " + event.getPeer() + "\n--------");

                        DiscoveryService ds = group.getDiscoveryService();
                        ds.getRemoteAdvertisements(event.getPeer(), DiscoveryService.PEER, null, null, 1);

                        break;
                    }
                    case RendezvousEvent.CLIENTDISCONNECT:
                    case RendezvousEvent.CLIENTFAILED:
                    {
                        StringBuffer msg = new StringBuffer("--------\nFSS-HandleRDV- CLIENTDISCONNECT or CLIENTFAILED = ");
                        msg.append(event.getPeer());
                        msg.append("\n starting to remove the U2UContentAdvertisementImpl relationship with the peer:\n");

                        try
                        {
                            DiscoveryService ds = group.getDiscoveryService();
                            //remove the advertisment of the disconnected peer
                            ds.flushAdvertisements(event.getPeer(), DiscoveryService.PEER);

                            //remove the U2UContentAdvertisement relationship with the disconnected peer
                            Enumeration<Advertisement> enumadv = ds.getLocalAdvertisements(DiscoveryService.ADV, "cid", null);

                            while(enumadv.hasMoreElements())
                            {
                                Advertisement adv = enumadv.nextElement();

                                if(adv instanceof U2UContentAdvertisementImpl)
                                {
                                    U2UContentAdvertisementImpl advu2u =
                                            (U2UContentAdvertisementImpl) adv;

                                    PipeAdvertisement pipeAdv = advu2u.getSocketAdv();

                                    //socket's name: Socket:uuid-xxx...
                                    String peerIDadv = pipeAdv.getName().substring(7);

                                    //peerID urn:jxta:uuid-xxx...
                                    String peerID = event.getPeer().substring(9);

                                    if(peerIDadv.equals(peerID))
                                    {
                                        msg.append("\n flushAdvertisement = " + advu2u.getName());
                                        ds.flushAdvertisement(advu2u);
                                    }
                                }
                            }

                            System.out.println(msg.toString());

                        }
                        catch (IOException ex)
                        {
                            Logger.getLogger(U2UFileSharingService.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        break;
                    }

                }
            }
            
        }
    }

    //registering the type of advertisement into AdvertisementFactory
    static {
        AdvertisementFactory.registerAdvertisementInstance(
                U2UContentAdvertisementImpl.getAdvertisementType(),
                new U2UContentAdvertisementImpl.Instantiator());
    }


    //constructor
    /**
     * 
     * @param pg peergroup
     * @param rendezvous true if the peer going to act as a rendezvous peer
     */
    public U2UFileSharingService(PeerGroup pg, boolean rendezvous, boolean relay)
    {  
        //init
        this.init(pg, null, null);
        //making the socket advertisement
        socketAdv = this.generateSocketAdv();

        //Peer Config
        if(rendezvous)
        {
            peerConfig = NetworkManager.ConfigMode.RENDEZVOUS.toString();

            RendezVousService rs = group.getRendezVousService();
            rs.startRendezVous();

            if(rs.isRendezVous())
            {
                rs.addListener(new HandleRDV());
            }
        }
        else if(relay)
        {
            peerConfig = NetworkManager.ConfigMode.RELAY.toString();
        }
        else
        {
            peerConfig = NetworkManager.ConfigMode.EDGE.toString();
        }

        System.out.println("---------SERVICE U2UFSS MODE CONFIG PEER: " + peerConfig + "-----------");
        //EOPeer Config

        //making the socket server
        try {
            //serverSocket = new JxtaServerSocket(group, createSocketAdvertisement());
            serverSocket = new JxtaServerSocket(group, this.socketAdv);
            serverSocket.setSoTimeout(0);//infinity loop

        } catch (IOException e) {
            System.out.println("failed to create a server socket");
            e.printStackTrace();
            System.exit(-1);
        }
        
        //initialize variables
        conMan = new U2UContentManagerImpl(this);
        resMan = new U2URequestManagerImpl(this);

        //pool
        //try to read the Properties file
        int ndowns = 10;
        int nups = 5;
        int soTO = 60000;
        File properties = new File("conf/.config.properties");
        if(properties.exists())
        {
            FileInputStream fis = null;
            try {

                fis = new FileInputStream(properties);
                Properties settings = new Properties();
                settings.load(fis);

                String cd = settings.getProperty("Down", "10");
                String cu = settings.getProperty("Upload", "5");
                String sto = settings.getProperty("soTimeOut", "60000");

                ndowns = Integer.parseInt(cd);
                nups = Integer.parseInt(cu);
                soTO = Integer.parseInt(sto);
                
                if(ndowns < 0)
                {
                    ndowns = 10;
                }

                if(nups < 0)
                {
                    nups = 5;
                }

                if(soTO <= 0)
                {
                    soTO = 60000;
                }
                
            } catch (NumberFormatException ex) {
                Logger.getLogger(U2UDownloadingManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(U2UDownloadingManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(U2UDownloadingManager.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    if(fis != null)
                    {
                        fis.close();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(U2UDownloadingManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        downloadsPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(ndowns);
        uploadsPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(nups);
        //

        //init the server socket handler
        serverSocketHandler = new ServerSocketHandler(serverSocket, soTO);
        (new Thread(serverSocketHandler, "U2UFSS ServerSocketHandler")).start();

        //share all the contents at the Shared files table
        System.out.println("---------------\n FSS share all the shared files at the database = "
                + conMan.shareAllTheSharedFilesAtDB() + "\n---------------");

        //isRunning
        isServiceRunning = true;
    }

    public void showAllCompleteSharedFiles()
    {
        Object[] val = conMan.getCompleteSharedFilesDB();
        invokeListenerMethod(new U2UFileSharingServiceEvent(this, val,
                U2UFileSharingServiceEvent.SHARED));
    }
    
    /**
     * get the progress of the all downloads
     */
    public void getDownloadProgress() {
        //if exist active downloads
        HashMap<String, Integer> rta = new HashMap<String, Integer>();

        for(Map.Entry<String, U2UDownloadingManager> in : downloadManagers.entrySet())
        {
            //get tehe reference of the U2UDownloadingManager
           U2UDownloadingManager down = in.getValue();
           //get the sha1 of the advertisement
           String sha1 = in.getKey();
           //get download progress
           int per = down.getProgress();
           //save the values in the hash of response
           rta.put(sha1, Integer.valueOf(per));
        }

        //
        invokeListenerMethod(new U2UFileSharingServiceEvent(this, new Object[]{rta},
                U2UFileSharingServiceEvent.PROGRESS));
    }

    /**
     * get the uploads for this peer
     */
    public void getUploads()
    {
         //if exist active downloads
        /* Integer nu = new Integer(0);

        for(Map.Entry<String, U2UUploadingManager> in : uploadManagers.entrySet())
        {
            //get tehe reference of the U2UUploadingManager
           U2UUploadingManager upload = in.getValue();
           //get uploads
           nu = upload.getUploadConnections();
        }

        //
        invokeListenerMethod(new U2UFileSharingServiceEvent(this, new Object[]{nu},
                U2UFileSharingServiceEvent.UPLOADS));*/
        //if exist active downloads
        HashMap<String, Integer> rta = new HashMap<String, Integer>();
        String name;
        
        for(Map.Entry<String, U2UUploadingManager> in : uploadManagers.entrySet())
        {
            //get tehe reference of the U2UDownloadingManager
           U2UUploadingManager upload = in.getValue();
           //get the sha1 of the advertisement
           String sha1 = in.getKey();
           U2UContentIdImpl cid = new U2UContentIdImpl(sha1);
           name = SharedFiles.getNameOfTheSharedFile(cid);
                     
           //get uploads per download
           int nu = upload.getUploadConnections();
           //save the values in the hash of response
           if(!name.equals(null))
           {
               rta.put(name, Integer.valueOf(nu));
           }
        }

        //
        invokeListenerMethod(new U2UFileSharingServiceEvent(this, new Object[]{rta},
                U2UFileSharingServiceEvent.UPLOADS));
    }

    /**
     * Force for finding new sources
     * @param adv file's advertisement
     * @return
     */
    public boolean forceFindNewSources(U2UContentAdvertisementImpl adv)
    {
         if(adv==null) {
            return false;
        }

        String cid = adv.getContentId().toString();

        if((downloadManagers.containsKey(cid)) )
        {
            U2UDownloadingManager down = downloadManagers.get(cid);
           //forcing for finding new sources for download the file
            down.forcingFindNewSources();

            return true;
        }

        return false;
    }

    /**
     * get the current sources for the download
     */
    public void getDownloadSources()
    {
        //if exist active downloads
        HashMap<String, Integer> rta = new HashMap<String, Integer>();

        for(Map.Entry<String, U2UDownloadingManager> in : downloadManagers.entrySet())
        {
            //get tehe reference of the U2UDownloadingManager
           U2UDownloadingManager down = in.getValue();
           //get the sha1 of the advertisement
           String sha1 = in.getKey();
           //get download progress
           int con = down.getDownloadConnections();
           //save the values in the hash of response
           rta.put(sha1, Integer.valueOf(con));
        }

        //
        invokeListenerMethod(new U2UFileSharingServiceEvent(this, new Object[]{rta},
                U2UFileSharingServiceEvent.SOURCES_DOWN));
    }
   

    /**
     * Downloads file associated with the notice given
     * @param adv
     * @return 
     */
    public boolean downloadFile(U2UContentAdvertisementImpl adv) {

        if(adv==null) {
            return false;
        }
        if((downloadManagers.containsKey(adv.getContentId().toString())) ||
                (downloadsPool.getActiveCount() >= downloadsPool.getCorePoolSize()))
        {
            //el archivo con este id ya esta en la lista de descargas
            return false;
        }

        //if the advertisment isn't at the database in the SharedFiles table, register it.
        //or if the advertisment is at the database in the SharedFiles table, publish it.
        boolean res = this.shareIncompleteFile(adv);

        if(res)
        {
            U2UDownloadingManager down = new U2UDownloadingManager(this, adv);

            //store the reference download
            downloadManagers.put(adv.getContentId().toString(), down);

            //submit the task
            //save the reference of future
            dmFutures.put(adv.getContentId().toString(),downloadsPool.submit(down));
        }

        return res;
    }

    /**
     * Restart the download's file
     * @param adv advertisemente that represents the file
     * @return true, if download is successful
     */
    public boolean restartDownload(U2UContentAdvertisementImpl adv)
    {
        if(adv==null) {
            return false;
        }

        String cid = adv.getContentId().toString();

        if((downloadManagers.containsKey(cid)) )
        {
            U2UDownloadingManager down = downloadManagers.get(cid);
            //store the reference download
            //downloadManagers.put(adv.getContentId().toString(), down);
            //submit the task
            //save the reference of future
            dmFutures.put(adv.getContentId().toString(), downloadsPool.submit(down));

            return true;
        }
      
        return false;
    }

    /**
     * Pause a download of a file trhough a advertisement U2UContentAdvertisementImpl
     * @param adv advertisement U2UContentAdvertisementImpl
     * @return true, if the file download was paused
     */
    public boolean pauseDownload(U2UContentAdvertisementImpl adv) {
        //Pausa la descaraga del anuncio dado
        U2UDownloadingManager downMan = downloadManagers.get(adv.getContentId().toString());
        
        if(downMan==null) {
            return false;
        }
        //stop the download file
        boolean res = downMan.pause();
        
        if(res)
        {
            //quit reference of downloadignManager of poolThread
            dmFutures.remove(adv.getContentId().toString());
            downloadsPool.purge();
            return true;
        }
        else
        {
            return false;
        }
    }

     /**
     * Stop and Remove a download of a file through a advertisement U2UContentAdvertisementImpl
     * @param adv advertisement U2UContentAdvertisementImpl
     * @return true, if the file download was stoped and removed
     */
    public boolean removeDownload(U2UContentAdvertisementImpl adv)
    {
        //Pausa la descaraga del anuncio dado
        U2UDownloadingManager downMan = downloadManagers.get(adv.getContentId().toString());
        
        if(downMan==null) {
            return false;
        }
       
        boolean res = downMan.stop();

        if(res)
        {
            //quit reference of downloadignManager of poolThread
            dmFutures.remove(adv.getContentId().toString());
            downloadsPool.purge();
            return true;
        }
        else {
            return false;
        }
    }

    public boolean search(String tag, String forAdv) {

        if(listener==null) {
            return false;
        }

        if(tag==null || forAdv==null) {
            return false;
            //if(!resMan.existRegisterListener(listener))
            //  resMan.addSearchListener(listener);
        }

        //if(!resMan.existRegisterListener(listener))
          //  resMan.addSearchListener(listener);

        resMan.searchContent(tag, forAdv);
        return true;
    }

    /**
     * share a File: Register the file in the database and publish it
     * @param pathFile location's file
     * @param descFile description's file
     * @return true if the file was shared
     */
    public boolean shareFile(String pathFile, String descFile)
    {
        boolean res = false;
        File file = new File(pathFile);

        if(descFile == null)
        {
            descFile = "Sin Descripción";
        }

        try
        {
            conMan.share(file, descFile);
            res = true;
        } catch (IOException ex) {
            System.out.println("No se pudo compartir el archivo en el servicio u2ufss");
            Logger.getLogger(U2UFileSharingService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return res;
    }

    /**
     * share a incomplete File: Register the incomplete file in the database and publish it,
     * based of the "almost truths" logic
     * @param adv
     * @return
     */
    public boolean shareIncompleteFile(U2UContentAdvertisementImpl adv)
    { 
        return conMan.shareIncompleteData(adv);
    }

     /**
     * Unshare a file that is shared
     * @param nameFile name fo the file shared
     * @return true if the file can be unshared
     */
    public boolean unShareFile(String nameFile) {
        boolean res = false;
        res = conMan.unshare(nameFile);
        return res;
    }

    public void publishContent(FileContent fc) {
        
        if (fc == null) {
            // No content. Nothing to do
            return;
        }
        
        DiscoveryService disco = group.getDiscoveryService();
        
        try {
            U2UContentAdvertisementImpl adv = (U2UContentAdvertisementImpl) fc.getContentAdvertisement();
            //adv.setAddress(getEndpointAddress());
            adv.setSocketAdv(this.getSocketAdv());

            disco.publish(adv,
                    DEFAULT_LOCAL_ADV_LIFETIME,
                    DEFAULT_REMOTE_ADV_LIFETIME);
            disco.remotePublish(adv,
                    DEFAULT_REMOTE_ADV_LIFETIME);

            
        } catch (Exception ex) {
            Logger.getLogger(U2UFileSharingService.class.getName()).log(Level.SEVERE, "Can't publish advertisement: ", ex);
        }
    }

    public void init(PeerGroup group, ID assignedID, Advertisement implAdv) {
        this.group = group;
        //

    }

    /**
     * Stop the service
     */
    public void stopApp() {

        isServiceRunning = false;
        //Pause the downloads and uploads
        //paused downloads
        for(Map.Entry<String, U2UDownloadingManager> in : downloadManagers.entrySet())
        {
            in.getValue().pause();
        }
        //pause uploads
        for(Map.Entry<String, U2UUploadingManager> in : uploadManagers.entrySet())
        {
            in.getValue().stop();
        }

        //stopping everything
        serverSocketHandler.finish();

        try {
            serverSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(U2UFileSharingService.class.getName()).log(Level.SEVERE, null, ex);
        }

        downloadsPool.shutdown();
        uploadsPool.shutdown();
        handlerPool.shutdown();
    }

     /**
     * Stablish the listener for the U2UContentAdvertisements
     * @param listener
     */
    public void setSearchListener(U2USearchListener listener) {

        this.listener = listener;
        resMan.addSearchListener(listener);
    }

    public PeerGroup getGroup()
    {
        return group;
    }
   
     /**
     * Return the PipeAdvertisement that represents the JxtaServerSocket, with a unique ID for every peer
     * @return a PipeAdvertisement object
     */
    public PipeAdvertisement getSocketAdv()
    {
        return socketAdv.clone();
    }

    /**
     * Check if any instance of U2UUploadingManager is handle this sha1
     * @param sha1
     * @return a instance od U2UUploadingManager, otherwise null
     */
    protected U2UUploadingManager getUploadingManager(String sha1)
    {
        U2UUploadingManager upload = null;

        //checking if exits
        if(uploadManagers.containsKey(sha1))
        {
            upload = uploadManagers.get(sha1);
        }
        //try to create one
        else
        {
            //checking the limit of uploads
            if(uploadManagers.size() < uploadsPool.getCorePoolSize())
            {
                U2UContentIdImpl cid = new U2UContentIdImpl(sha1);
                //Do exist the advertisement at the database?
                if(SharedFiles.exist(cid))
                {
                    upload = new U2UUploadingManager(this, cid);
                    //FIXME spino327 e irenelizeth : missing Future references
                    //sending the uploading to pool
                    uploadsPool.submit(upload);
                    //sending the uploading to hashtable
                    uploadManagers.put(sha1, upload);
                    
                }
            }
        }

        return upload;
    }
     /**
     * Returns the endpoint address for this service.
     * @deprecated
     * @return String the endpoint address.
     */
     public String getEndpointAddress() {
        /*return "jxta://" + getPeerId(group) +
                "/" + serviceName +
                "/" + getGroupId(group);*/
        //return "tcp://192.168.0.3:2525/"+serviceName;
        return null;
     }

     /**
      * add a listener for U2UFileSharingServiceEvent
      * @param listener
      */
     public void addU2UFSSListener(U2UFileSharingServiceListener listener)
     {
         lisSrv.add(listener);
     }

      /**
      * remove listener for U2UFileSharingServiceEvent
      * @param listener
      */
     public void removeU2UFSSListener(U2UFileSharingServiceListener listener)
     {
         lisSrv.remove(listener);
     }


    //-------------------------------------------------------------------------
    //-------------------------------------------------------------------------
    //Methods private
    //-------------------------------------------------------------------------
    //-------------------------------------------------------------------------

    
    /**
     * generate the socket advertisement
     * @return
     */
    private PipeAdvertisement generateSocketAdv()
    {
        PipeID socketID = null;
        PipeAdvertisement adv = null;

        //read the SocketId from the .JxtaSocketID file into the conf directory
        FileInputStream fin = null;

        try
        {
            File file = new File("conf/.JxtaSocketID");

            //exist the Socket Id?
            if(file.exists())
            {
                fin = new FileInputStream(file);
                ObjectInputStream ois = new ObjectInputStream(fin);
                socketID = (PipeID) ois.readObject();
            }
            else
            {
                socketID = IDFactory.newPipeID(group.getPeerGroupID());

                FileOutputStream fos = new FileOutputStream(file);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                // write in the file the SocketID's object
                oos.writeObject(socketID);
            }

        } catch (FileNotFoundException ex)
        {
            Logger.getLogger(U2UFileSharingService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex)
        {
            //if the file can't write then the SockectID's object can't be create
            socketID = null;
            Logger.getLogger(U2UFileSharingService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex)
        {
            socketID = null;
            Logger.getLogger(U2UFileSharingService.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if(fin != null)
            {
                try
                {
                    fin.close();
                } catch (IOException ex)
                {
                    Logger.getLogger(U2UFileSharingService.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        if(null != socketID)
        {
            adv = (PipeAdvertisement)
                AdvertisementFactory.newAdvertisement(PipeAdvertisement.getAdvertisementType());
            adv.setPipeID(socketID);
            adv.setName("Socket:" + this.getPeerId(group));//PeerId
            adv.setType(PipeService.UnicastType);
            adv.setDescription("Socket:" + group.getPeerName());//PeerName
        }

        return adv;
    }

    /**
     * invoke the method for the inteface
     */
    private void invokeListenerMethod(U2UFileSharingServiceEvent event) {

        // are there any registered discovery listeners,
        // generate the event and callback.

        Object[] allListeners = lisSrv.toArray(new Object[0]);

        for (Object allListener : allListeners) {
            ((U2UFileSharingServiceListener) allListener).serviceEvent(event);
        }

    }


    private String getGroupId(PeerGroup group)
    {
        return group.getPeerGroupID().getUniqueValue().toString();
    }

    private String getPeerId(PeerGroup group)
    {
        return group.getPeerID().getUniqueValue().toString();
    }

    

}
