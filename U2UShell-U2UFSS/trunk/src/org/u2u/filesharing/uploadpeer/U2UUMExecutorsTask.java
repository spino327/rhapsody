/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.u2u.filesharing.uploadpeer;

import java.util.Arrays;

/**
 * This class represents a task that the Executor implementation
 * needs to run.
 * @author Irene & Sergio
 */
public class U2UUMExecutorsTask {

    //relationship with the protocol's orders
    /**
     * Task's type relationship with the need to choose if attend or not the CONN order
     */
    public static final int SEND_CONN_RESPONSE_TASK = 50;
    /**
     * Task's type relationship with the send of peer's chunks
     */
    public static final int SEND_RESPONSE_FILE_INFO_QUERY_PEER_CHUNKS_TASK = 51;
    /**
     * Task's type relationship with the send of chunks' SHA-1
     */
    public static final int SEND_RESPONSE_FILE_INFO_QUERY_CHUNKS_SHA1_TASK = 52;
    /**
     * Task's type relationship with the send of a requests chunk's data
     */
    public static final int SEND_RESPONSE_GET_CHUNK_TASK = 53;
    /**
     * Task's type relationship with the incoming of QUIT response from the UploadPeer
     */
    public static final int INCOMING_QUIT_TASK = 54;


    //Executor
    /**
     * Task's type relationship with the finish of Executor
     */
    public static final int FINISH_TASK = 55;

    /** task's type*/
    private final int taskType;
    /** task's data*/
    private final Object[] taskData;

    private final int[] types = new int[] {
        SEND_CONN_RESPONSE_TASK, SEND_RESPONSE_FILE_INFO_QUERY_PEER_CHUNKS_TASK,
        SEND_RESPONSE_FILE_INFO_QUERY_CHUNKS_SHA1_TASK, SEND_RESPONSE_GET_CHUNK_TASK,
        FINISH_TASK, INCOMING_QUIT_TASK
    };
    /**
     * build a task to execute
     * @param type type of the task, use the class's constants.
     * @param array the data need to execute the task
     */
    protected U2UUMExecutorsTask(int type, Object[] array)
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
        taskType = type;
        taskData = array;
    }

    /**
     * return the task's type
     * @return
     */
    protected int getTaskType()
    {
        return taskType;
    }

    /**
     * return the task's data
     * @return
     */
    protected Object[] getTaskData()
    {
        return taskData;
    }

}
