/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.u2u.common.db;

import java.util.Enumeration;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.u2u.filesharing.U2UContentAdvertisementImpl;
import org.u2u.filesharing.U2UContentIdImpl;
import org.u2u.filesharing.U2UFileContentImpl;

/**
 *
 * @author sergio
 */
public class SharedFilesTest {

    public SharedFilesTest() {
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
     * Test of setConnection method, of class SharedFiles.
     */
//    @Test
//    public void testSetConnection()
//    {
//        System.out.println("setConnection");
//        ConnectTo con = null;
//        SharedFiles.setConnection(con);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of create method, of class SharedFiles.
     */
    @Test
    public void testCreate()
    {
        System.out.println("create");
        U2UFileContentImpl fci = null;
        boolean expResult = false;
        boolean result = SharedFiles.create(fci);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of delete method, of class SharedFiles.
     */
    @Test
    public void testDelete()
    {
        System.out.println("delete");
        U2UContentIdImpl cid = null;
        boolean expResult = false;
        boolean result = SharedFiles.delete(cid);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of exist method, of class SharedFiles.
     */
    @Test
    public void testExist()
    {
        System.out.println("exist");
        U2UContentIdImpl cid = null;
        boolean expResult = false;
        boolean result = SharedFiles.exist(cid);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getU2UContentAdvertisementImplFromName method, of class SharedFiles.
     */
    @Test
    public void testGetU2UContentAdvertisementImpl()
    {
        System.out.println("getU2UContentAdvertisementImpl");
        String nameFile = "";
        U2UContentAdvertisementImpl expResult = null;
        U2UContentAdvertisementImpl result = SharedFiles.getU2UContentAdvertisementImplFromName(nameFile);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of numberOfRecords method, of class SharedFiles.
     */
    @Test
    public void testNumberOfRecords()
    {
        System.out.println("numberOfRecords");
        U2UContentIdImpl cid = null;
        int expResult = 0;
        int result = SharedFiles.numberOfRecords(cid);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of find method, of class SharedFiles.
     */
    @Test
    public void testFind()
    {
        System.out.println("find");
        U2UContentIdImpl cid = null;
        Enumeration expResult = null;
        Enumeration result = SharedFiles.find(cid);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}