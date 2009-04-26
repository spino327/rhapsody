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
public class ChunksTest {
    private static ConnectTo conDB;

    public ChunksTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception
    {
//        conDB = new ConnectTo("org.apache.derby.jdbc.EmbeddedDriver");
//        //check if the db's folder already getU2UContentAdvertisementImpl
//        if (!conDB.getConnection("jdbc:derby:U2UClient", "U2U", "")) {
//            //if debug mode then put 'createFrom=conf/.U2UClient'
//            //for build the distribution then put 'createFrom=conf/.U2UClient'
//            conDB.getConnection("jdbc:derby:U2UClient;createFrom=conf/.U2UClient", "U2U", "");
//        }
//
//        Chunks.setConnection(conDB);
        Table.connect();
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
     * Test of setConnection method, of class Chunks.
     */
    /*@Test
    public void testSetConnection()
    {
        System.out.println("setConnection");
        ConnectTo con = null;
        Chunks.setConnection(con);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }*/

    /**
     * Test of create method, of class Chunks.
     */
    /*@Test
    public void testCreate()
    {
        System.out.println("create");
        String sha1_sf = "";
        String sha1_chunk = "";
        short pos_chunk = 0;
        boolean status = false;
        boolean expResult = false;
        boolean result = Chunks.create(sha1_sf, sha1_chunk, pos_chunk, status);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }*/

    /**
     * Test of delete method, of class Chunks.
     */
    /*@Test
    public void testDelete()
    {
        System.out.println("delete");
        String sha1_chunk = "";
        boolean expResult = false;
        boolean result = Chunks.delete(sha1_chunk);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }*/

    /**
     * Test of exist method, of class Chunks.
     */
    @Test
    public void testExist()
    {
        System.out.println("exist");
        String sha1_chunk = "sha1:77682623f6263d37dd219ee8d7c02a96b305073c";
        boolean expResult = true;
        //boolean result = Chunks.exist(sha1_chunk);
        //assertEquals(expResult, result);
    }

    /**
     * Test of numberOfChunks method, of class Chunks.
     */
    @Test
    public void testNumberOfChunks()
    {
        System.out.println("numberOfChunks");
        String sha1_sf = "sha1:0186086e46a639cdb9011d16ac717fea901528a6";
        int expResult = 6;
        int result = Chunks.numberOfChunks(sha1_sf);
        assertEquals(expResult, result);
    }

    /**
     * Test of chunkPosToChunkSha1 method, of class Chunks.
     */
    @Test
    public void testChunkPosToChunkSha1()
    {
        System.out.println("chunkPosToChunkSha1");
        String sha1_sf = "sha1:0186086e46a639cdb9011d16ac717fea901528a6";
        int pos = 0;
        String expResult = "6406ad9f06ea994500905be16adbf6716a4edb1a";
        //String result = Chunks.chunkPosToChunkSha1(sha1_sf, pos);
        //assertEquals(expResult, result);
    }

    /**
     * Test of getReverseChunksPositions method, of class Chunks.
     */
    @Test
    public void testGetReverseChunksPositions()
    {
        System.out.println("getReverseChunksPositions");
        String sha1_sf = "sha1:0186086e46a639cdb9011d16ac717fea901528a6";
        short[] pos = new short[] {1, 5, 3};
        short[] expResult = new short[] {0, 2, 4};
        short[] result = Chunks.getReverseChunksPositions(sha1_sf, pos);

        int i = 0;
        for(short p : expResult)
        {
            assertEquals(p, result[i++]);
        }

    }

    /**
     * Test of getReverseChunksPositions method, of class Chunks.
     */
    @Test
    public void testGetRandomChunkToDownload()
    {
        System.out.println("getRandomChunkToDownload");
        String sha1_sf = "sha1:0186086e46a639cdb9011d16ac717fea901528a6";
        
        short noExpResult = -1;
        short result = Chunks.getRandomChunkToDownload(sha1_sf, "urn:jxta:uuid-59616261646162614E504720503250337208407FE6B94D5CBF0DA169129D7FCD04");

        assertTrue(result > noExpResult);

    }
}