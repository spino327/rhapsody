/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jxta.impl.shell;

import java.util.Enumeration;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author sergio
 */
public class ShellEnvTest {

    public ShellEnvTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception
    {
    }

    @AfterClass
    public static void tearDownClass() throws Exception
    {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Check that if the collection of EnvVar have not objects then the ShellEnv has not EnvVars.
     */
    @Test
    public void testEmptyCollectionOfEnvVar()
    {
        ShellEnv sE = new ShellEnv();
        assertFalse(sE.elements().hasMoreElements());
    }
    
    @Test
    public void testGetAllElements()
    {
        ShellEnv sE = new ShellEnv();
        for(int i = 0; i<10; i++)
            sE.add(Integer.toString(i), new ShellObject<Object>(Integer.toString(i),"env"+i));
        
        Enumeration en = sE.elements();
        while(en.hasMoreElements())
        {
            System.out.println(en.nextElement());
        }
    }
    
    /**
     * 
     */
     
}