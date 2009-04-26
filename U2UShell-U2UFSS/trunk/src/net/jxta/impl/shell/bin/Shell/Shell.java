/*
 *  Copyright (c) 2001 Sun Microsystems, Inc.  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in
 *  the documentation and/or other materials provided with the
 *  distribution.
 *
 *  3. The end-user documentation included with the redistribution,
 *  if any, must include the following acknowledgment:
 *  "This product includes software developed by the
 *  Sun Microsystems, Inc. for Project JXTA."
 *  Alternately, this acknowledgment may appear in the software itself,
 *  if and wherever such third-party acknowledgments normally appear.
 *
 *  4. The names "Sun", "Sun Microsystems, Inc.", "JXTA" and "Project JXTA" must
 *  not be used to endorse or promote products derived from this
 *  software without prior written permission. For written
 *  permission, please contact Project JXTA at http://www.jxta.org.
 *
 *  5. Products derived from this software may not be called "JXTA",
 *  nor may "JXTA" appear in their name, without prior written
 *  permission of Sun.
 *
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED.  IN NO EVENT SHALL SUN MICROSYSTEMS OR
 *  ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *  USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 *  OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *  SUCH DAMAGE.
 *  ====================================================================
 *
 *  This software consists of voluntary contributions made by many
 *  individuals on behalf of Project JXTA.  For more
 *  information on Project JXTA, please see
 *  <http://www.jxta.org/>.
 *
 *  This license is based on the BSD license adopted by the Apache Foundation.
 *
 *  $Id: Shell.java,v 1.86 2007/02/09 23:12:45 hamada Exp $
 */
package net.jxta.impl.shell.bin.Shell;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;
import net.jxta.discovery.DiscoveryEvent;
import net.jxta.document.AdvertisementFactory;
import net.jxta.id.IDFactory;

import net.jxta.impl.shell.*;
import net.jxta.impl.shell.bin.history.HistoryQueue;
import net.jxta.impl.shell.bin.join.join.PeerGroupShellObject;
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.InputPipe;
import net.jxta.pipe.OutputPipe;
import net.jxta.pipe.PipeService;
import net.jxta.protocol.PipeAdvertisement;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.discovery.DiscoveryService;
import net.jxta.logging.Logging;
import net.jxta.platform.Module;
import net.jxta.protocol.DiscoveryResponseMsg;
import org.u2u.filesharing.U2UContentAdvertisementImpl;
import org.u2u.filesharing.U2UFileSharingServiceListener;
import org.u2u.filesharing.downloadpeer.U2USearchListener;

/**
 * This class implements a JXTA Shell
 */
public class Shell
        extends ShellApp
        implements Runnable
{

    /**
     * Logger
     */
    private static final transient java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(Shell.class.getName());
    /** 
     * Description of the Field
     */
    public final static String HISTORY_ENV_NAME = "History";
    /**
     * Description of the Field
     */
    public final static String JXTA_SHELL_EMBEDDED_KEY = "JXTA_SHELL_EMBEDDED";
    /**
     * Description of the Field
     */
    public final static String PARENT_SHELL_ENV_NAME = "parentShell";
    /**
     * Description of the Field
     */
    public final static String CMD_PROMPT = "JXTA>";
    /**
     *  Tracks how many shell instances we have created.
     */
    private static AtomicInteger shellInstance = new AtomicInteger(0);
    /**
     *  Unique instance number for this shell instance.
     */
    private final int thisInstance;
    private ShellConsole cons = null;
    private boolean execShell = true;
    private boolean gotMyOwnFrame = false;
    /**
     * If true then this shell is a sub-shell of a another shell.
     */
    private boolean gotParent = false;
    /**
     * The shell environment of our parent shell.
     */
    private ShellEnv parentEnv = null;
    private String pipecmd = null;
    private BufferedReader scriptReader = null;
    /**
     * Child Shells will install a env var in their parent for their instance
     */
    private String parentEnvEnvName = null;
    private Thread thread = null;

    
    
    //U2U Implementation
    /**
     * This instance variable is the responsable for handling the discovery Events and 
     * send it to the diferents listeners outside the shell object, normaly the GUI classes
     */
    private DiscoveryListener shellDiscoveryListerner = new DiscoveryListener() {

        /**
         * implementation of the only required method by the DiscoveryListener,
         */
        @SuppressWarnings("unchecked")
        public void discoveryEvent(DiscoveryEvent event)
        {
            int discoveryType;
            DiscoveryResponseMsg res = event.getResponse();
            Set listeners = null;

            switch(discoveryType = res.getDiscoveryType())
            {
                case DiscoveryService.PEER:
                    listeners = peerAdvListeners;
                    break;
                    
                case DiscoveryService.GROUP:
                    listeners = peerGroupAdvListeners;
                    break;
                    
                case DiscoveryService.ADV:
                    listeners = generalAdvListeners;
                    break;
            }
            
            invokeListenerMethod(listeners, event, discoveryType);
        }

        /**
         * invoke the method for the especific inteface 
         */     
        private void invokeListenerMethod(Set listeners, DiscoveryEvent event, int type) {


            // are there any registered discovery listeners,
            // generate the event and callback.
            long t0 = System.currentTimeMillis();
            
            Object[] allListeners = listeners.toArray(new Object[0]);
            
            for (Object allListener : allListeners) {
                
                switch(type)
                {
                    case DiscoveryService.PEER:
                        ((PeerAdvertisementListener) allListener).peerAdvertisementEvent(new U2UShellDiscoveryEvent(event));
                        break;
                        
                    case DiscoveryService.GROUP:
                        ((PeerGroupAdvertisementListener) allListener).peerGroupAdvertisementEvent(new U2UShellDiscoveryEvent(event));
                        break;
                        
                    case DiscoveryService.ADV:
                        ((GeneralAdvertisementListener) allListener).generalAdvertisementEvent(new U2UShellDiscoveryEvent(event));
                        break;    
                }
                
            }
                        
            if (Logging.SHOW_FINE && LOG.isLoggable(Level.FINE)) {
                LOG.fine("Called all listenters to query #" + event.getQueryID() + " in :" + (System.currentTimeMillis() - t0));
            }
        }
    };
    
   
    /**
     * The table of Peer Advertisement listeners.
     */
    private Set<PeerAdvertisementListener> peerAdvListeners = new HashSet<PeerAdvertisementListener>();
    
    /**
     * The table of Peer Group Advertisement listeners.
     */
    private Set<PeerGroupAdvertisementListener> peerGroupAdvListeners = new HashSet<PeerGroupAdvertisementListener>();
    
    /**
     * The table of General Advertisement listeners.
     */
    private Set<GeneralAdvertisementListener> generalAdvListeners = new HashSet<GeneralAdvertisementListener>();
    
    //constructors
    /**
     * Default constructor (don't delete)
     */
    public Shell()
    {
        thisInstance = shellInstance.incrementAndGet();
    }
    
    /**
     * Create a new shell with the specified console
     */
    public Shell(ShellConsole console) {
        this();

        cons = console;
    }

    /**
     * Create a new shell with embedded functionality.
     *
     * @param embedded
     */
    public Shell(boolean embedded) {
        this();

        System.setProperty(JXTA_SHELL_EMBEDDED_KEY, Boolean.toString(embedded));
    }
    
    //U2U Implementation
    /**
     * Create a new shell with the specefic PeerGroup
     */
    public Shell(PeerGroup pg)
    {
        this();
        this.init(pg, null, null);
        
        this.startApp(null);
        
        this.getGroup().getDiscoveryService().addDiscoveryListener(shellDiscoveryListerner);
    }

    //methods
    /**
     * Main processing method for the Shell object
     */
    public void run()
    {
        try {
            if (null != pipecmd) {
                startApp(new String[0]);
            }
            else {
                runShell();
            }
        } catch (Throwable all) {
            System.out.flush();
            System.err.println("Uncaught Throwable in thread :" + Thread.currentThread().getName());
            all.printStackTrace(System.err);
        }
    }

    /**
     * {@inheritDoc}
     */
    public int startApp(String[] argv)
    {

        GetOpt options = new GetOpt(argv, "xsf:e:");

        while (true) {
            int option;
            try {
                option = options.getNextOption();
            } catch (IllegalArgumentException badopt) {
                consoleMessage("Illegal argument :" + badopt);
                return syntaxError();
            }

            if (-1 == option) {
                break;
            }

            switch (option) {
                case 'f':
                    String scriptfile = options.getOptionArg();
                    if (!initScriptFile(scriptfile)) {
                        consoleMessage("Cannot access " + scriptfile);
                        return ShellApp.appMiscError;
                    }
                    break;

                case 'e':
                    execScript(options.getOptionArg());
                    return Shell.appNoError;

                case 's':
                    gotMyOwnFrame = true;
                    break;

                case 'x':
                    execShell = false;
                    break;

                default:
                    return syntaxError();
            }
        }

        ShellEnv env = getEnv();

        if (null == env) {
            // There is no Parent Shell

            gotMyOwnFrame = true;
            env = new ShellEnv();

            ShellObject<PeerGroup> stdgrpobj = new PeerGroupShellObject("Default Group", getGroup());
            env.add("stdgroup", stdgrpobj);

            PeerGroup child = null;
            PeerGroup current = getGroup();
            while (true) {
                PeerGroup next = current.getParentGroup();
                if (next == null) {
                    break;
                }
                child = current;
                current = next;
            }

            // Unless one of our ancestor groups does not support getParent and thus we
            // know nothing, we can always find the platform.
            if (current != null) {
                ShellObject<PeerGroup> worldgrpobj;
                if (current.getPeerGroupID().equals(getGroup().getPeerGroupID())) {
                    worldgrpobj = stdgrpobj;
                }
                else {
                    worldgrpobj = new PeerGroupShellObject("World Peer Group", current);
                }

                env.add("worldgroup", worldgrpobj);
            }

            // Unless our initial group is the platform, our before-last ancestor is
            // the netpg.
            if (child != null) {
                ShellObject<PeerGroup> rootgrpobj;
                if (child.getPeerGroupID().equals(getGroup().getPeerGroupID())) {
                    rootgrpobj = stdgrpobj;
                }
                else {
                    rootgrpobj = new PeerGroupShellObject("Root Peer Group", child);
                }

                env.add("rootgroup", rootgrpobj);
            }          
            
        }
        else {
            // This is a child Shell.

            gotParent = true;

            // Recover the parent env, and duplicate it
            parentEnv = env;

            env = new ShellEnv(parentEnv);

            parentEnvEnvName = parentEnv.createName();

            // Store this Shell into the parent's environment
            parentEnv.add(parentEnvEnvName, new ShellObject<Shell>("Child Shell " + Integer.toString(thisInstance), this));

            // and store our parent into our environment
            ShellObject<Shell> parentShell = (ShellObject<Shell>) parentEnv.get("shell");
            if (parentShell != null) {
                env.add(PARENT_SHELL_ENV_NAME, parentShell);
            }
        }

        setEnv(env);

        // Store this Shell into the environment

        /*
         * Hardwiring the shell environment variable here allows us to retrieve
         * it from exit (or wherever), when we need to get the current instance
         * of the shell.
         */
        env.add("shell", new ShellObject<Shell>("Shell " + Integer.toString(thisInstance), this));

        if (gotMyOwnFrame) {
            if (null == cons) {
                //cons = ShellConsole.newConsole(this, "JXTA Shell - (" + getGroup().getPeerName() + ")");
                cons = ShellConsole.newConsole(this, "JXTA Shell - (" + getGroup().getPeerName() + ")", false);
            }

            env.add("console", new ShellObject<ShellConsole>("console", cons));

            cons.setStatusGroup(getGroup());

            // Create the default InputPipe
            ShellInputPipe defaultInputPipe = new ShellInputPipe(getGroup(), cons);

            env.add("stdin", new ShellObject<InputPipe>("Default InputPipe", defaultInputPipe));

            env.add("consin", new ShellObject<InputPipe>("Default Console InputPipe", defaultInputPipe));

            setInputPipe(defaultInputPipe);
            setInputConsPipe(defaultInputPipe);

            // Create the default OutputPipe
            ShellOutputPipe defaultOutputPipe = new ShellOutputPipe(getGroup(), cons);

            env.add("stdout", new ShellObject<OutputPipe>("Default OutputPipe", defaultOutputPipe));

            env.add("consout", new ShellObject<OutputPipe>("Default Console OutputPipe", defaultOutputPipe));

            setOutputPipe(defaultOutputPipe);
            setOutputConsPipe(defaultOutputPipe);

            // start the shell on its own thread.
            thread = new Thread(getGroup().getHomeThreadGroup(), this, "U2U Shell " + thisInstance);
            thread.start();

            if (Logging.SHOW_INFO && LOG.isLoggable(Level.INFO)) {
                LOG.info("Shell started.");
            }
            // HACK 20070814 bondolo This test is required because the Shell uses a return value (-1) not supported by StdPeerGroup.
            if (null == argv) {
                return Module.START_OK;
            }
            else {
                return ShellApp.appSpawned;
            }
        }
        else {
            ShellObject console = env.get("console");

            cons = (ShellConsole) console.getObject();

            if (Logging.SHOW_INFO && LOG.isLoggable(Level.INFO)) {
                LOG.info("Shell starting.");
            }

            // a child shell just runs until exit.
            if (null != pipecmd) {
                processCmd(pipecmd);
            }
            else {
                runShell();
            }

            return ShellApp.appNoError;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void stopApp()
    {
        // Only stop once.
        if (stopped) {
            return;
        }

        super.stopApp();

        // Remove itself from the parent ShellEnv (GC)
        if (parentEnv != null) {
            parentEnv.remove(parentEnvEnvName);
        }

        // Destroy ourself
        if (gotMyOwnFrame) {
            cons.setStatusGroup(null);
            cons.destroy();
            cons = null;
        }

        // Interrupt our parsing thread.
        if (thread != null) {
            thread.interrupt();
        }

        if (Logging.SHOW_INFO && LOG.isLoggable(Level.INFO)) {
            LOG.info("Shell stopped.");
        }

        if (isRootShell()) {
            PeerGroup pg = getGroup();

            getEnv().clear();

            // Until we fix all non-daemon threads in non-jxta code...
            if (!isEmbedded()) {
                pg.stopApp();

//                System.exit(0);
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * <p/>Use the value of stdgroup environment variable.
     */
    @Override
    public PeerGroup getGroup()
    {
        ShellEnv env = getEnv();

        ShellObject stdgroup = null;
        if (null != env) {
            stdgroup = env.get("stdgroup");
        }

        return (null != stdgroup) ? (PeerGroup) stdgroup.getObject() : super.getGroup();
    }

    /**
     * if true then this is a root shell. A root shell is the shell which owns
     * the console.
     *
     * @return if true then this shell owns the console.
     */
    public boolean isRootShell()
    {
        return !gotParent;
    }

    /**
     * Description of the Method
     *
     * @return error result code.
     */
    private int syntaxError()
    {
        consoleMessage("Usage: Shell [-f filename] [-e cmds] [-s] [-x]");

        return ShellApp.appParamError;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription()
    {
        return "U2U Shell command interpreter";
    }

    /**
     * {@inheritDoc}
     */
    /**
     * Description of the Method
     *
     * @param script
     */
    private void execScript(String script)
    {
        try {
            processMultipleCmd(script);
        } catch (Exception ez1) {
            printStackTrace("Failed with ", ez1);
        }
    }

    /**
     * Returns the ShellConsole object associated with this shell. It may be
     * null, if this shell does not run in its own window
     *
     * @return ShellConsole object associated with this shell
     */
    public ShellConsole getConsole()
    {
        return cons;
    }

    /**
     * Returns the HistoryQueue that holds the cmds in a history list If the
     * queue does not exist, it is created.
     *
     * @return HistoryQueue object used to retrieve commands
     */
    private HistoryQueue getHistoryQueue()
    {
        ShellEnv env = getEnv();
        ShellObject obj = env.get(HISTORY_ENV_NAME);
        HistoryQueue queue = null;

        if (obj != null) {
            if (HistoryQueue.class.isAssignableFrom(obj.getObjectClass())) {
                queue = (HistoryQueue) obj.getObject();
            }
        }
        else {
            String exclude[] = {cons.getCursorDownName(), cons.getCursorUpName()};

            queue = new HistoryQueue(exclude);
            env.add(HISTORY_ENV_NAME, new ShellObject<HistoryQueue>("History", queue));
        }

        return queue;
    }

    /**
     * Prepare to run a command on a pipe.
     *
     * @param cmd The command to be executed.
     */
    private void initPipe(String cmd)
    {
        String myName = "U2U Shell - " + thisInstance + " : [" + cmd + "]";

        pipecmd = cmd;
        thread = new Thread(getGroup().getHomeThreadGroup(), this, myName);
        thread.start();
    }

    /**
     * Description of the Method
     *
     * @param fn
     * @return {@code true} if the script file was found otherwise false {@code false}.
     */
    private boolean initScriptFile(String fn)
    {
        try {
            scriptReader = new BufferedReader(new FileReader(fn));
            return true;
        } catch (Exception e) {
            printStackTrace("Failed with ", e);
            return false;
        }
    }

    /**
     * Description of the Method, Execute commands at a .jshrc file
     */
    private void startupFile()
    {
        File startupFile = new File(".jshrc");

        if (!startupFile.exists()) {
            return;
        }

        //delete BufferedReader
        BufferedReader scriptReader = null;
        try {
            scriptReader = new BufferedReader(new FileReader(startupFile));
            // nothing to do
            String cmd = scriptReader.readLine();

            while (cmd != null) {
                processMultipleCmd(cmd);
                cmd = scriptReader.readLine();
            }
        } catch (Exception e) {
            if (LOG.isLoggable(java.util.logging.Level.WARNING)) {
                LOG.log(Level.WARNING, "Failure with .jshrc ", e);
            }
        } finally {
            try {
                if (null != scriptReader) {
                    scriptReader.close();
                }
            } catch (IOException ignored) {
                //ignored
            }
        }
    }

    /**
     * Process the a single command
     *
     * @param cmd the command string.
     */
    private int processCmd(String cmd)
    {
        int appStatusError;
        
        if (LOG.isLoggable(java.util.logging.Level.INFO)) {
            LOG.info("BEGINING OF COMMAND : " + cmd);
        }

        // get the args as a list of tokens
        List<String> args = new ArrayList<String>(Arrays.asList(tokenizeLine(cmd)));

        if (args.size() < 1) {
            return ShellApp.appMiscError;
        }

        // Get the returnvar, if any.
        String returnvar = null;
        if (args.size() >= 2) {
            if ("=".equals(args.get(1))) {
                returnvar = args.remove(0);
                args.remove(0);
            }
        }

        String app = args.remove(0);

        // echo the command if the echo enviroment variable is defined
        if (getEnv().contains("echo")) {
            consoleMessage("Executing command : " + cmd);
        }

        // "clear" is an internal command; just handle it here, nothing to load.

        if (app.equals("clear")) {
            cons.clear();
            return ShellApp.appNoError;
        }

//        original settings
//        ShellApp appl = loadApp(returnvar, app, getEnv());
        
        ShellApp appl = this.getApp(app);
//        
        //hack sergio: return the status for the app command
        if (null != appl) {
            appl.setReturnVariable(returnvar);
            appl.resetAppStatus();
            appStatusError = exec(appl, args.toArray(new String[args.size()]));
        }
        else {
            consoleMessage("Could not load command '" + app + "'");
            appStatusError = ShellApp.appMiscError;
        }
        
        return appStatusError;
    }

    /**
     * Process the <cmd>(";" <cmd>)* commands
     * <p/>
     * <p/>FIXME 20010611 bondolo@jxta.org does not handle quoting in any form.
     *
     * @param cmd the command string.
     */
    private void processMultipleCmd(String cmd)
    {

        HistoryQueue queue = getHistoryQueue();

        if (queue != null) {
            queue.addCommand(cmd);
        }

        StringTokenizer tokens = new StringTokenizer(cmd, ";");
        while (tokens.hasMoreElements()) {
            processPipeCmd(tokens.nextToken());
        }
    }

    /**
     * Process the <cmd> ("|" <cmd>)* commands
     * <p/>
     * <p/>FIXME 20010611 bondolo@jxta.org does not handle quoting in any form.
     *
     * @param cmd the command string.
     */
    private void processPipeCmd(String cmd)
    {

        List<String> cmds = new ArrayList<String>();

        StringTokenizer tokens = new StringTokenizer(cmd, "|");

        while (tokens.hasMoreElements()) {
            cmds.add(tokens.nextToken());
        }

        // at the beginning start with stdin and stdout

        PeerGroup current = (PeerGroup) getEnv().get("stdgroup").getObject();
        InputPipe stdin = (InputPipe) getEnv().get("stdin").getObject();
        OutputPipe stdout = (OutputPipe) getEnv().get("stdout").getObject();

        // these are for building the pipeline

        InputPipe pipein = null;
        OutputPipe pipeout = null;
        InputPipe lastin = stdin;
        Thread willDependOn = null;

        // The first and last command in the pipe needs to be treated separatly

        PipeService pipes = current.getPipeService();

        for (int i = 0; i < cmds.size() - 1; i++) {
            /*
             *  create Shell cmd pipe to link the two
             */
            PipeAdvertisement padv;

            try {
                padv = (PipeAdvertisement) AdvertisementFactory.newAdvertisement(
                        PipeAdvertisement.getAdvertisementType());
                padv.setPipeID(IDFactory.newPipeID(current.getPeerGroupID()));
                padv.setType(PipeService.UnicastType);

                pipein = pipes.createInputPipe(padv);
                pipeout = pipes.createOutputPipe(padv, Collections.singleton(current.getPeerID()), 0);
            } catch (IOException ex) {
                printStackTrace("Could not construct pipes for piped command.", ex);
            }

            /*
             *  create the environment by cloning the parent.
             */
            ShellEnv pipeenv = new ShellEnv(getEnv());

            pipeenv.add("stdout", new ShellObject<OutputPipe>("Default OutputPipe", pipeout));

            pipeenv.add("stdin", new ShellObject<InputPipe>("Default InputPipe", lastin));

            /*
             *  create a new Shell process to run this pipe command
             */
            Shell pipeShell = (Shell) loadApp(null, "Shell", pipeenv);

            pipeShell.setJoinedThread(willDependOn);
            pipeShell.initPipe(cmds.get(i));
            willDependOn = pipeShell.thread;

            /*
             *  update last in pipe for the next command
             */
            lastin = pipein;
        }

        /*
         *  Set the pipeline for the last command and let it go/
         *  only stdin needs redirection since stdout is the right one
         */
        getEnv().add("stdout", new ShellObject<OutputPipe>("Default OutputPipe", stdout));

        ShellObject<InputPipe> oldin = (ShellObject<InputPipe>) getEnv().get("stdin");

        getEnv().add("stdin", new ShellObject<InputPipe>("Default InputPipe", lastin));

        setJoinedThread(willDependOn);

        processCmd(cmds.get(cmds.size() - 1));

        setJoinedThread(null);

        // restore the original stdin
        getEnv().add("stdin", oldin);
    }

    /**
     * This method implements the default input stream (keyboard).
     */
    private void runShell()
    {

        if (execShell || (scriptReader == null)) {
            consprintln("=============================================");
            consprintln("=======<[ Welcome to the U2U Shell ]>=======");
            consprintln("=============================================");
        }

        // check if there is a .jshrc file
        /*if (execShell) {
        startupFile();
        }*/

        while (!stopped) {
            String cmd;

            try {
                /*if (scriptReader != null) {
                cmd = scriptReader.readLine();
                } else {*/
                cons.setPrompt(CMD_PROMPT);

                cmd = waitForInput();
            //}
            } catch (IOException e) {
                System.err.println("Shell is reconnecting to console");
                // This shell has lost its standard InputPipe. Try
                // to reconnect to the special keyboard InputPipe.
                setInputPipe(getInputConsPipe());
                continue;
            }

            if (cmd == null) {
                if (!stopped) {
                    exec(null, "exit", new String[0], getEnv());
                }
                break;
            }

            processMultipleCmd(cmd);
        }
    }

    /**
     * Return true if this is an embedded shell. IE used by an application that
     * wouldn't like it if System.exit were called.
     *
     * @return The embedded value
     * @author <a href="mailto:burton@openprivacy.org">Kevin A. Burton</a>
     */
    public static boolean isEmbedded() {

        String value = System.getProperty(JXTA_SHELL_EMBEDDED_KEY, "false");

        return Boolean.valueOf(value);
    }

    /**
     * converts a command line string into a series of tokens.
     */
    public String[] tokenizeLine(String line)
    {
        List<String> tokens = new ArrayList<String>();

        StringBuilder currentToken = new StringBuilder();
        int current = 0;
        int quote = -1;
        boolean escape = false;

        while (current < line.length()) {
            final char currentChar = line.charAt(current);

            if (escape) {
                currentToken.append(currentChar);
                escape = false;
            }
            else if (-1 != quote) {
                if (currentChar == quote) {
                    quote = -1;
                }
                else {
                    currentToken.append(currentChar);
                }
            }
            else {
                switch (currentChar) {
                    case ' ':
                    case '\t':
                        if (currentToken.length() > 0) {
                            tokens.add(currentToken.toString());
                            currentToken.setLength(0);
                        }
                        break;

                    case '=':
                    case '|':
                    case ';':
                        if (currentToken.length() > 0) {
                            tokens.add(currentToken.toString());
                            currentToken.setLength(0);
                        }
                        tokens.add(Character.toString(currentChar));
                        break;

                    case '"':
                    case '\'':
                        quote = currentChar;
                        break;

                    case '\\':
                        escape = true;
                        break;

                    default:
                        currentToken.append(currentChar);
                        break;
                }
            }

            current++;
        }

        if (currentToken.length() > 0) {
            tokens.add(currentToken.toString());
            currentToken.setLength(0);
        }

        return tokens.toArray(new String[tokens.size()]);
    }
    
    //U2U Implementation

    /**
     * Return a ShellApp
     */
    private ShellApp getApp(String appName)
    {
        ShellApp app = null;
        ShellEnv env = getEnv();
        //load app
        if (!env.contains(appName)) {
            app = loadApp(null, appName, env);
            if(app != null)
                env.add(appName, new ShellObject<ShellApp>(appName, app));
        }
        else {
            app = (ShellApp) env.get(appName).getObject();
        }

        return app;
    }

    /**
     * which return a clone of the object inside of ShellObject, if the clone() methos existed in the oiginal object's class. 
     * ShellObject is getting from the ShellEnv  using a specific key.
     */
    public Object getShellObjectEnv(String key)
    {
        return this.getShellObjectEnv(key, true);
    }
    
    private Object getShellObjectEnv(String key, boolean clone) 
    {
        Object obj = null;
        ShellObject sObj = null;
        ShellEnv env = getEnv();

        if ((sObj = env.get(key)) != null) {
            obj = sObj.getObject();

            //reflexion
            if(clone)
            {
                Method m = null;
                try {
                    m = obj.getClass().getMethod("clone", new Class[]{});
                } catch (NoSuchMethodException ex) {
                    Logger.getLogger(Shell.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SecurityException ex) {
                    Logger.getLogger(Shell.class.getName()).log(Level.SEVERE, null, ex);
                }

                if(m != null)
                {
                    try {
                        obj = m.invoke(obj, new Object[]{});
                    } catch (IllegalAccessException ex) {
                        Logger.getLogger(Shell.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IllegalArgumentException ex) {
                        Logger.getLogger(Shell.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (InvocationTargetException ex) {
                        Logger.getLogger(Shell.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } 
        }

        return obj;
    }

      /**
     * Create a new enviroment's variable through a U2UContentAdvertisementImpl
     * @param adv U2UContentAdvertisementImpl
     * @param name variable's name
     * 
     */
    public void createVarEnvU2UAdvertisement(U2UContentAdvertisementImpl adv, String fileName)
    {
        ShellEnv env = getEnv();
        ShellObject<U2UContentAdvertisementImpl> newAdv = new ShellObject<U2UContentAdvertisementImpl>("U2UContentAdvertisementImpl", adv);
        env.add(fileName, newAdv);
    }

     /**
     * Create a new enviroment's variable that contains a U2USearchListener object
     * @param obj U2USearchListener object
     */
    public void createVarEnvSearchListener(U2USearchListener obj)
    {
        ShellEnv env = getEnv();
        ShellObject<U2USearchListener> shellObj = new ShellObject<U2USearchListener>("U2USearchListener", obj);
        env.add("listener", shellObj);
    }

    /**
     * Create a new enviroment's variable that contains a U2UFileSharingServiceListener object
     * @param obj U2UFileSharingServiceListener object
     */
    public void createVarEnvServiceListener(String name, U2UFileSharingServiceListener obj)
    {
        ShellEnv env = getEnv();
        ShellObject<U2UFileSharingServiceListener> shellObj = new ShellObject<U2UFileSharingServiceListener>("U2UFileSharingServiceListener", obj);
        env.add(name, shellObj);
    }

    /**
     * Deletes a enviroment's variable
     * @param nameEnv name's variable
     * Util when a shared file has been unshared
     */
    public void deleteVariableEnv(String nameEnv)
    {
        ShellEnv env = getEnv();
        boolean res = env.remove(nameEnv);
        if(res)
            System.out.println("variable "+nameEnv+" creada");
        else
            System.out.println("Can't delete var "+nameEnv);      
    }
    
    /**
     * return true if the command was executed 
     */
    public boolean executeCmd(String statement)
    {
        return (this.processCmd(statement) == ShellApp.appNoError ? true : false );
    }

    
    /**
     * {@inheritDoc}
     */
    public synchronized void addPeerAdvertisementListener(PeerAdvertisementListener listener) {

        peerAdvListeners.add(listener);
    }

    /**
     * {@inheritDoc}
     */
    public synchronized boolean removePeerAdvertisementListener(PeerAdvertisementListener listener) {

        return peerAdvListeners.remove(listener);
    }
    
    /**
     * {@inheritDoc}
     */
    public synchronized void addPeerGroupAdvertisementListener(PeerGroupAdvertisementListener listener) {

        peerGroupAdvListeners.add(listener);
    }

    /**
     * {@inheritDoc}
     */
    public synchronized boolean removePeerGroupAdvertisementListener(PeerGroupAdvertisementListener listener) {

        return peerGroupAdvListeners.remove(listener);
    }
    
    /**
     * {@inheritDoc}
     */
    public synchronized void addGeneralAdvertisementListener(GeneralAdvertisementListener listener) {

        generalAdvListeners.add(listener);
    }

    /**
     * {@inheritDoc}
     */
    public synchronized boolean removeGeneralAdvertisementListener(GeneralAdvertisementListener listener) {

        return generalAdvListeners.remove(listener);
    }
    
}
