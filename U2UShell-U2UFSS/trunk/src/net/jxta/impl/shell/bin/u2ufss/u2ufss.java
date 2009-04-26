/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jxta.impl.shell.bin.u2ufss;

import net.jxta.discovery.DiscoveryService;
import net.jxta.impl.shell.GetOpt;
import net.jxta.impl.shell.ShellApp;
import net.jxta.impl.shell.ShellEnv;
import net.jxta.impl.shell.ShellObject;
import net.jxta.peergroup.PeerGroup;
import net.jxta.platform.NetworkManager;
import org.u2u.filesharing.U2UContentAdvertisementImpl;
import org.u2u.filesharing.U2UFileSharingService;
import org.u2u.filesharing.U2UFileSharingServiceListener;
import org.u2u.filesharing.downloadpeer.U2USearchListener;

/**
 *
 * @author Irene & Sergio
 */
public class u2ufss  extends ShellApp{

    ShellEnv env;
    private DiscoveryService discovery;
    private PeerGroup group;
    private ShellObject obj;
    private U2UFileSharingService fss;
   
    public u2ufss() {
    }

    /**
     * El metodo startApp hereda de la clase ShellApp
     * @param args, array de String que identifica los paramteros ingresados por el ausuario
     * @return int, que representa el resultado de la ejecución del comando
     */
    public int startApp(String[] args)
    {
        if ((args == null) || (args.length < 1))
        {
           return syntaxError();
        }
        
        this.env = getEnv();
        this.discovery = getGroup().getDiscoveryService();

        if (args[0].equals("-init") || args[0].equals("-i"))
        {
            return initServiceU2UFSS(group, args);
        }
        if (args[0].equals("-stop") || args[0].equals("-s"))
        {
            if(fss==null)
            {
                 return showWarnning();
            }
            return stopServiceU2UFSS();
        }
        if (args[0].equals("-share") || args[0].equals("-sh"))
        {
            if(fss==null)
            {
                 return showWarnning();
            }
            return shareAdv(args);
        }
        if (args[0].equals("-unshare") || args[0].equals("-un"))
        {
            if(fss==null)
            {
                 return showWarnning();
            }
            return unShareAdv(args);
        }
        if (args[0].equals("-search") || args[0].equals("se"))
        {
            if(fss==null)
            {
                 return showWarnning();
            }
            return searchAdv(args);
        }
        if (args[0].equals("-download") || args[0].equals("d"))
        {
            if(fss==null)
            {
                 return showWarnning();
            }

            return downloadAdv(args);

        }if (args[0].equals("-stopdownload") || args[0].equals("sd"))
        {
            if(fss==null)
            {
                 return showWarnning();
            }

            return stopDownload(args);

        }if (args[0].equals("-pausedownload") || args[0].equals("pd"))
        {
            if(fss==null)
            {
                 return showWarnning();
            }

            return pauseDownload(args);

        }if (args[0].equals("-restartdownload") || args[0].equals("rd"))
        {
            if(fss==null)
            {
                 return showWarnning();
            }

            return restartDownload(args);
        }
        if (args[0].equals("-progress") || args[0].equals("p"))
        {
            if(fss==null)
            {
                return showWarnning();
            }
            return generateQuestionProgress();
         }
        if (args[0].equals("-register") || args[0].equals("r"))
        {
            if(fss==null)
            {
                return showWarnning();
            }
            return registerSearchListener();
         }
        if (args[0].equals("-addlistener") || args[0].equals("al"))
        {
            if(fss==null)
            {
                return showWarnning();
            }
            return addServiceListener(args);
         }
        if (args[0].equals("-removelistener"))
        {
            if(fss==null)
            {
                return showWarnning();
            }
            return removeServiceListener(args);
         }
        if (args[0].equals("-findsources"))
        {
            if(fss==null)
            {
                return showWarnning();
            }
            return findMoreSources(args);
         }
        if (args[0].equals("-uploads"))
        {
            if(fss==null)
            {
                return showWarnning();
            }
            return howMuchUploads();
         }
        if (args[0].equals("-showsf"))
        {
            if(fss==null)
            {
                return showWarnning();
            }
            return showSharedFilesInDB();
         }
         else
         {
            consoleMessage("Incorrect used");
            println("");
            return syntaxError();
         }
    }
        
    /**
     * organizes the parameters of command 
     * @param args arguments for the command entered 
     * @return array with the values of file's description and path
     * organized into a single string in a position of the returned array
     */
    private String[] getCorrectArgs(String[] args) {
       
        boolean cont = true;
        String[] argOpt;
        StringBuffer pathBuf = new StringBuffer();
        StringBuffer descBuf = new StringBuffer();
        
        int pi = 0;//init the path string
        int pf = 0;// finalize the path string
        int di = 0;//init the description string
        
        if(args[1].equals("-p"))
        {
          pi= 2;
        }else
        {
          cont = false;
        }
        if(cont)
        {
            for(int i=0; i<args.length-1;i++)
            {
                if(args[i+1].equals("-d"))
                {
                    pf= i;
                    di= i + 2;
                    i=args.length-1;
                }         
            }

            int df = 0;//finalize the description string
            
            if((di==0))
            {   //finalize the path string
                pf = args.length-1;
                //init array options
                argOpt = new String[2];
            }else

            {   df = args.length-1;
                //init array options
                argOpt = new String[4];
                //compose the description string
                for(int i = di; i<=df; i++)
                {
                    String temp = args[i];
                    temp = temp.concat(" ");
                    descBuf.append(temp);
                    if(i == df)
                    {
                       argOpt[2]="-d";
                       argOpt[3] = descBuf.toString();
                    }
                }
             }
            //compose the path String
            for(int i=pi;i<=pf;i++)
            {
                String temp = args[i];
                if(i!=pf)
                {
                    temp = temp.concat(" ");
                    pathBuf.append(temp);
                }
                else if(i==pf)
                {
                    pathBuf.append(temp);
                    argOpt[0]="-p";
                    argOpt[1]=pathBuf.toString();
                }
            } 
            //array organized
            return argOpt;
        }
        else
        {
            return null;
        }   
    }

   
    private int initServiceU2UFSS(PeerGroup group, String[] args) {
            this.env = getEnv();

        /*
        *  get the std group
        *  El servicio U2UFileSharingService necesita del grupo donde va a funcionar
        */
        obj = env.get("stdgroup");

        group = (PeerGroup) obj.getObject();

        if(group == null )
        {
            return ShellApp.appMiscError;
        }

        boolean rendezvous, relay;

        //checking the args, [-init, mode] = 2
        if((args == null) || !(args.length == 2))
        {
            return ShellApp.appMiscError;
        }
        else if(args[1].equals(NetworkManager.ConfigMode.RENDEZVOUS.toString()))
        {
            rendezvous = true;
            relay = false;
        }else if(args[1].equals(NetworkManager.ConfigMode.RELAY.toString()))
        {
            rendezvous = false;
            relay = true;
        }
        else
        {
            //not rendezvous, not relay: edge
            rendezvous = false;
            relay = false;
        }
        //init the service U2UFSS
        fss = new U2UFileSharingService(group, rendezvous, relay);

        consoleMessage("Service U2UFSS was initialized successful");
        return ShellApp.appNoError;
    }
   
    private int registerSearchListener() {

        ShellEnv env = getEnv();
        String name = "listener";

        if(!env.contains(name))
        {
            consoleMessage("The listener hasn't save into enviroment's variable ");
            return ShellApp.appParamError;
        }

        ShellObject<U2USearchListener> obj = (ShellObject<U2USearchListener>) env.get(name);

        if(obj==null)
        {
            consoleMessage("The listener has not been registered into enviroment variable");
            return ShellApp.appParamError;
        }

        U2USearchListener listener = obj.getObject();

        fss.setSearchListener(listener);
        print("successful register search listener");
        return ShellApp.appNoError;
        
    }

    private int addServiceListener(String[] args)
    {
        ShellEnv env = getEnv();
        

         if((args[1].length()<=0)||(args[1]==null))
        {
            consoleMessage("Illegal option");
            shortHelp();
            return ShellApp.appParamError;
        }

        String name = args[1];

        if(!env.contains(name))
        {
            consoleMessage("The Service listener hasn't save into enviroment's variable ");
            return ShellApp.appParamError;
        }

        ShellObject<U2UFileSharingServiceListener> obj = (ShellObject<U2UFileSharingServiceListener>) env.get(name);

        if(obj==null)
        {
            consoleMessage("The service listener has not been registered into enviroment variable");
            return ShellApp.appParamError;
        }

        U2UFileSharingServiceListener listener = (U2UFileSharingServiceListener) obj.getObject();

        fss.addU2UFSSListener(listener);
        print("successful register service listener");
        return ShellApp.appNoError;
    }

    private int removeServiceListener(String[] args)
    {
        ShellEnv env = getEnv();


         if((args[1].length()<=0)||(args[1]==null))
        {
            consoleMessage("Illegal option");
            shortHelp();
            return ShellApp.appParamError;
        }

        String name = args[1];

        if(!env.contains(name))
        {
            consoleMessage("The Service listener hasn't save into enviroment's variable ");
            return ShellApp.appParamError;
        }

        ShellObject<U2UFileSharingServiceListener> obj = (ShellObject<U2UFileSharingServiceListener>) env.get(name);

        if(obj==null)
        {
            consoleMessage("The service listener has not been registered into enviroment variable");
            return ShellApp.appParamError;
        }

        U2UFileSharingServiceListener listener = (U2UFileSharingServiceListener) obj.getObject();

        fss.removeU2UFSSListener(listener);

        env.remove(name);

        return ShellApp.appNoError;
    }

    private int searchAdv(String[] args) {
        
        if((args[1].length()<=0)||(args[1]==null)||
                (args[1].equals("-n"))||(args[1].equals("-d")))
        {
            consoleMessage("Illegal option");
            shortHelp();
            return ShellApp.appParamError;
        }

        StringBuffer temp = new StringBuffer();
        
        String sfor = new String();
        String tag = null;
        boolean flag = false;

        for(int i=1; i<args.length; i++)
        {
            if(args[i].equals("-n"))
            {
                if(flag)
                {   //quitar el espacio al final de la palabra de busqueda
                    temp.delete(temp.length()-1, temp.length());
                }
                sfor = temp.toString();
                tag="name";
                i=args.length;
            }
            else if(args[i].equals("-d"))
            {
                if(flag)
                {   //quitar el espacio al final de la palabra de busqueda
                    temp.delete(temp.length()-1, temp.length());
                }
                sfor = temp.toString();
                tag = "description";
                i=args.length;
            }
            else
            {
                temp.append(args[i]);
                if(i!=args.length-1)
                   {
                    temp.append(" ");
                    flag = true;
                   }
            }
        }

        if(tag==null)
        {
            temp.toString();
            tag = "name";
        }

        boolean res = fss.search(tag, sfor);
        return ShellApp.appNoError;
    }
   
    private int unShareAdv(String[] args) {

        if(args[1].equals(" ") || args[1] == null )
        {
            consoleMessage("Illegal option");
            shortHelp();
            return ShellApp.appParamError;
        }
        //organize the file name
        StringBuffer name = new StringBuffer();
        for(int i=1;i<args.length;i++)
        {
            name.append(args[i]);
        }
        String nameFile = name.toString();

        boolean res = fss.unShareFile(nameFile);

        if(!res)
        {
            consoleMessage("Can't unshare the file "+nameFile);
            return ShellApp.appMiscError;
        }else
        {
            consoleMessage("Successful file unshared "+nameFile);
            return ShellApp.appMiscError;
        }

    }
    
    private int shareAdv(String[] args) {
        boolean res = false;
        if(args.length<=1)
        {
            shortHelp();
            return ShellApp.appParamError;
        }          
        String[] argOpt = getCorrectArgs(args);
        
        if(argOpt == null)
        {
            shortHelp();
            return ShellApp.appParamError;
        }
        
        String path = null;
        String des = null;
        //boolean dflag = false;
        
        //options: p(path) and d(description)
        GetOpt getopt = new GetOpt(argOpt, "p:d:");   
        int c;
        
        while ((c = getopt.getNextOption()) != -1) 
        {
                switch (c) {
                    case'p':
                        path = getopt.getOptionArg();
                        break;
                    case'd':
                        des = getopt.getOptionArg();
                        //dflag = true;
                        break;    
                    default:
                        consoleMessage("Illegal option");
                        shortHelp();
                        return ShellApp.appParamError;
                }
                //if the parameters are null, show error ilegal option
                if((path==null)&& (des==null))
                {
                   consoleMessage("Illegal option");
                   shortHelp();
                   return ShellApp.appParamError;
                }   
        }
       
        res =fss.shareFile(path, des);
        
        if(res)
        {
            consoleMessage("successful file sharing");
            return ShellApp.appNoError;
        } else
        {
            consoleMessage("file could not be shared");
            return ShellApp.appMiscError;
        }
        
    }

    private int downloadAdv(String[] args) {
        
        if(args[1]==null || args[1].equals(" "))
        {
            consoleMessage("Illegal option");
            shortHelp();
            return ShellApp.appParamError;
        }
        
        String name = args[1];
        boolean res = env.contains(name);
        
        if(!res)
        {
            print("Variable "+name+" don't exist");
            return ShellApp.appParamError;
        }
        
        ShellObject<U2UContentAdvertisementImpl> obj = (ShellObject<U2UContentAdvertisementImpl>) env.get(name);
        U2UContentAdvertisementImpl adv = obj.getObject();
        
        fss.downloadFile(adv);
        
        return ShellApp.appNoError;
    }

    private int pauseDownload(String[] args) {

        if((args[1].length()<=0)||(args[1]==null))
        {
            consoleMessage("Illegal option");
            shortHelp();
            return ShellApp.appParamError;
        }

        String name= args[1];

        if(!env.contains(name))
        {
            print("variable "+name+" don't exist");
            shortHelp();
            return ShellApp.appMiscError;
        }

        ShellObject<U2UContentAdvertisementImpl> obj = (ShellObject<U2UContentAdvertisementImpl>) env.get(name);
        U2UContentAdvertisementImpl adv = obj.getObject();

        if(adv==null)
            return ShellApp.appMiscError;

        boolean res = fss.pauseDownload(adv);

        if(res)
            println("Downloading of "+adv.getName()+" paused successful");
        else
            println("Downloading of "+adv.getName()+" don't paused successful");
        return ShellApp.appNoError;

    }

    private int restartDownload(String[] args) {

        if((args[1].length()<=0)||(args[1]==null))
        {
            consoleMessage("Illegal option");
            shortHelp();
            return ShellApp.appParamError;
        }

        String name= args[1];

        if(!env.contains(name))
        {
            print("variable "+name+" don't exist");
            shortHelp();
            return ShellApp.appMiscError;
        }

        ShellObject<U2UContentAdvertisementImpl> obj = (ShellObject<U2UContentAdvertisementImpl>) env.get(name);
        U2UContentAdvertisementImpl adv = obj.getObject();

        if(adv==null)
            return ShellApp.appMiscError;

        boolean res = fss.restartDownload(adv);

        if(res)
            println("Downloading of "+adv.getName()+" was restart successful");
        else
            println("Downloading of "+adv.getName()+" can't restart");
        return ShellApp.appNoError;

    }

    private int findMoreSources(String[] args) {

        if((args[1].length()<=0)||(args[1]==null))
        {
            consoleMessage("Illegal option");
            shortHelp();
            return ShellApp.appParamError;
        }

        String name= args[1];

        if(!env.contains(name))
        {
            print("variable "+name+" don't exist");
            shortHelp();
            return ShellApp.appMiscError;
        }

        ShellObject<U2UContentAdvertisementImpl> obj = (ShellObject<U2UContentAdvertisementImpl>) env.get(name);
        U2UContentAdvertisementImpl adv = obj.getObject();

        if(adv==null)
            return ShellApp.appMiscError;

        boolean res = fss.forceFindNewSources(adv);

        if(res)
            println("Finding new sources for "+adv.getName()+"  was successful");
        else
            println("Can't finding new sources for "+adv.getName()+" !!!");
        return ShellApp.appNoError;

    }

    private int stopDownload(String[] args) {
        
        if((args[1].length()<=0)||(args[1]==null))
        {
            consoleMessage("Illegal option");
            shortHelp();
            return ShellApp.appParamError;
        }

        String name= args[1];

        if(!env.contains(name))
        {
            print("variable "+name+" don't exist");
            shortHelp();
            return ShellApp.appMiscError;
        }

        ShellObject<U2UContentAdvertisementImpl> obj = (ShellObject<U2UContentAdvertisementImpl>) env.get(name);
        U2UContentAdvertisementImpl adv = obj.getObject();

        if(adv==null)
            return ShellApp.appMiscError;

        boolean res = fss.removeDownload(adv);

        if(res)
            println("Downloading of "+adv.getName()+" was stoped and deleted successful");
        else
            println("Downloading of "+adv.getName()+" can't deleted");
        return ShellApp.appNoError;
    }

    private int generateQuestionProgress()
    {
        //Inform to service U2UFSS to get status for the active downloads
        fss.getDownloadProgress();
        //Inform to the GUI the sources peers for the downloads
        fss.getDownloadSources();
        return ShellApp.appNoError;
    }

    private int stopServiceU2UFSS() {

        if(fss != null)
        {
            fss.stopApp();
            fss = null;
            return ShellApp.appNoError;
        }
        //El servicio debe ser iniciado para ser detenido
        //se lanza un error
        println("The service U2UFSS has not initialized");
        return ShellApp.appMiscError;

    }

    private int howMuchUploads()
    {
        if(fss != null)
        {
            fss.getUploads();
            return ShellApp.appNoError;
        }
        //El servicio debe ser iniciado para realizar esta consulta
        //se lanza un error
        println("The service U2UFSS has not initialized");
        return ShellApp.appMiscError;
    }
    
    private int showSharedFilesInDB()
    {
        if(fss != null)
        {
            fss.showAllCompleteSharedFiles();
            return ShellApp.appNoError;
        }
        //El servicio debe ser iniciado para realizar esta consulta
        //se lanza un error
        println("The service U2UFSS has not initialized");
        return ShellApp.appMiscError;
    }

    private void shortHelp() {
        println("NAME");
        println("     u2ufss - init the service ");
        println(" ");
        println("SYNOPSIS");
        println("     u2ufss -init (init the service U2UFSS) ");
        println("     u2ufss -share -p pathFile -d description File (share a file) ");
        println("     u2ufss -unshare nameFile (unshare a file) ");
        println("     u2ufss -search <U2UContentAdvertisementImpl> (search a file)");
        println("     u2ufss -unshare <U2UContentAdvertisementImpl> (unshared a file)");
        println("     u2ufss -download <U2UContentAdvertisementImpl> (download a file)");
        println("     u2ufss -pausedownload <U2UContentAdvertisementImpl> (pause a file's download)");
        println("     u2ufss -restartdownload <U2UContentAdvertisementImpl> (restart a file's download)");
        println("     u2ufss -stopdownload <U2UContentAdvertisementImpl> (stop and drop a file's download)");
        println("     u2ufss -stop (stop a service U2UFSS)");
      
    }

    private int showWarnning() {
        consoleMessage("WARNNIG: init the service first!!");
        return syntaxError();
    }

    private int syntaxError() {
        println("Usage: u2ufss "+ getDescription());
        println(" ");
        println("    u2ufss -[i]nit");
        println("    u2ufss -[sh]are <pathFile> [-d <file's description>");
        println("    u2ufss -[se]arch <U2UContentAdvertisementImpl>");
        println("    u2ufss -[un]share <U2UContentAdvertisementImpl>");
        println("    u2ufss -[d]ownload <U2UContentAdvertisementImpl> ");
        println("    u2ufss -pausedownload <U2UContentAdvertisementImpl> ");
        println("    u2ufss -restartdownload <U2UContentAdvertisementImpl> ");
        println("    u2ufss -stopdownload <U2UContentAdvertisementImpl> ");
        println("    u2ufss -[s]top");
        println(" ");
        println("command u2ufss not was executed correctly ");
        return ShellApp.appParamError;
    }

    @Override
    public void help() {

        println("NAME");
        println("     u2ufss - " + getDescription());
        println(" ");
        println("SYNOPSIS");
        println(" ");
        println("    u2ufss -[i]nit");
        println("    u2ufss -[sh]are <U2UContentAdvertisementImpl>");
        println("    u2ufss -[se]arch <U2UContentAdvertisementImpl>");
        println("    u2ufss -[d]ownload <U2UContentAdvertisementImpl> ");
        println("    u2ufss -pausedownload <U2UContentAdvertisementImpl> ");
        println("    u2ufss -restartdownload <U2UContentAdvertisementImpl> ");
        println("    u2ufss -stopdownload <U2UContentAdvertisementImpl> ");
        println("    u2ufss -stop");
        println(" ");
        println("DESCRIPTION");
        println(" ");
        println("The u2ufss command implements a command using the service U2UFileSharingService");
        println("to share and downloading files in the P2P network ");
        println("where two users on two remote peers can share files.");
        println("to use 'u2ufss' the user needs to init the U2UFSS.");
        println("via the following steps:");
        println(" ");
        println("Step 1: Init via 'u2ufss -init' command. This command");
        println("        creates a new instance of U2UFSS. ");
        println(" ");
        println("Step 2: Share a file via 'u2ufss -share <U2UContentAdvertisementImpl>' command. This command");
        println("        share the file share represented by a file in a U2UContentAdvertisementImpl");
        println("        advertisement through the registration module fileshares ");
        println("        U2UFSS service");
        println(" ");
        println("Step 3: User can download a file from anothers users via the command");
        println("        'u2ufss -download <U2UContentAdvertisementImpl>'. This command will download the file");
        println("        that is represented in a U2UContentAdvertisementImpl advertisement");
        println(" ");
        println("To stop the service U2UFSS. The user can stop the u2ufss command");
        println("listener daemon by entering the command 'u2ufss -stop'");
        println(" ");
        println("OPTIONS");
        println(" ");
        println("    -init      init the service U2UFSS.");
        println("    -share     Share a file represent through a advertisment.");
        println("    -search    Search a file represent through a advertisment.");
        println("    -download  download a file represent through a advertisment.");
        println("    -pausedownload     pause a download.");
        println("    -restartdownload     stop and remove a download.");
        println("    -stopdownload     start a download.");
        println("    -stop      Stop the service U2UFSS");
        println(" ");
        println(" ");
        println("SEE ALSO");
        println(" ");
        println("    xfer sftp mkpipe");
    }

    @Override
    public String getDescription() {
        return "Start using the service U2UFSS";
    }



}
