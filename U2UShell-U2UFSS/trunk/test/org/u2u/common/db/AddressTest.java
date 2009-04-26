/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.u2u.common.db;

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
public class AddressTest {

    public AddressTest() {
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
     * Test of setConnection method, of class Address.
     */
//    @Test
//    public void testSetConnection()
//    {
//        System.out.println("setConnection");
//        ConnectTo con = null;
//        Address.setConnection(con);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of create method, of class Address.
     */
    @Test
    public void testCreate()
    {
        System.out.println("create");
        String pipeId = "";
        boolean expResult = false;
        boolean result = Address.create(pipeId);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of delete method, of class Address.
     */
    @Test
    public void testDelete()
    {
        System.out.println("delete");
        String pipeId = "";
        boolean expResult = false;
        boolean result = Address.delete(pipeId);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of exist method, of class Address.
     */
    @Test
    public void testExist()
    {
        System.out.println("exist");
        String pipeId = "";
        boolean expResult = false;
        boolean result = Address.exist(pipeId);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of pipeIdToIDADD method, of class Address.
     */
    @Test
    public void testPipeIdToIDADD()
    {
        System.out.println("pipeIdToIDADD");
        String pipeId = "";
        int expResult = 0;
        int result = Address.pipeIdToIDADD(pipeId);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}