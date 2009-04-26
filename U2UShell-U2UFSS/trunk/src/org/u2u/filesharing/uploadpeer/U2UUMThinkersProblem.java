/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.u2u.filesharing.uploadpeer;

import java.util.Arrays;

/**
 * This class represents a problem that the Thinker implementation
 * needs to solve.
 * @author Irene & Sergio
 */
public class U2UUMThinkersProblem {

    /**
     * Problem's type relationship with the incoming connection request that the service pass to us
     */
    public static final int INCOMING_CONN_REQUEST_PROBLEM = 40;
    /**
     * Problem's type relationship with the U2UFileSharingProtocolListener's protocolEvent method
     */
    public static final int PROTOCOL_EVENT_PROBLEM = 41;
    /**
     * Problem's type relationship with the finish of the thinker
     */
    public static final int FINISH_PROBLEM = 42;


    /** problem's type*/
    private final int problemType;
    /** problem's data*/
    private final Object[] problemData;

    private final int[] types = new int[]{
        INCOMING_CONN_REQUEST_PROBLEM, PROTOCOL_EVENT_PROBLEM, FINISH_PROBLEM
    };
    /**
     * build a problem to solve
     * @param type type of the problem, use the class's constants.
     * @param array the data need to solve the problem
     */
    protected U2UUMThinkersProblem(int type, Object[] array)
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
