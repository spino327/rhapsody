/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.u2u.filesharing.downloadpeer;

import java.util.Arrays;

/**
 * This class represents a problem that the Thinker implementation
 * needs to solve.
 * @author Irene & Sergio
 */
public class U2UDMThinkersProblem {

    /**
     * Problem's type relationship with the U2UFileSharingProtocolListener's protocolEvent method
     */
    public static final int PROTOCOL_EVENT_PROBLEM = 20;

    /**
     * Problem's type relationship with the U2USearchListener's contentAdvertisementEvent method
     */
    public static final int CONTENT_ADVERTISEMENT_EVENT_PROBLEM = 21;

    /**
     * Problem's type relationship with the need of find new sources to download the content
     */
    public static final int NEED_MORE_SOURCES_PROBLEM = 22;

    /**
     * Problem's type relationship with the exist of new Available Chunks to download
     */
    public static final int NEW_AVAILABLE_CHUNKS_EVENT_PROBLEM = 23;

    /**
     * Problem's type relationship with the download of a Chunk
     */
    public static final int DOWNLOADED_CHUNK_EVENT_PROBLEM = 24;

    /**
     * Task's type relationship with the finish of Thinker
     */
    public static final int FINISH_PROBLEM = 25;

    /**
     * Problem's type relationship with the incoming of the chunks' sha1 list
     */
    public static final int AVAILABLE_CHUNKS_SHA1_EVENT_PROBLEM = 26;

    /** problem's type*/
    private final int problemType;
    /** problem's data*/
    private final Object[] problemData;

    private final int[] types = new int[]{
        PROTOCOL_EVENT_PROBLEM, CONTENT_ADVERTISEMENT_EVENT_PROBLEM, NEED_MORE_SOURCES_PROBLEM,
        NEW_AVAILABLE_CHUNKS_EVENT_PROBLEM, DOWNLOADED_CHUNK_EVENT_PROBLEM, FINISH_PROBLEM,
        AVAILABLE_CHUNKS_SHA1_EVENT_PROBLEM
    };
    /**
     * build a problem to solve
     * @param type type of the problem, use the class's constants.
     * @param array the data need to solve the problem
     */
    protected U2UDMThinkersProblem(int type, Object[] array)
    {
        Arrays.sort(types);
        //checking the type
        if(Arrays.binarySearch(types, type) < 0)
        {
            throw new IllegalArgumentException("the type is incorrect");
        }
        //checking the array
        else if(array == null)
        {
            throw new IllegalArgumentException("the array can't be null");
        }
        problemType = type;
        problemData = array;
    }

    /**
     * return the problem's type
     * @return
     */
    protected int getProblemType()
    {
        return problemType;
    }

    /**
     * return the problem's data
     * @return
     */
    protected Object[] getProblemData()
    {
        return problemData;
    }
}
