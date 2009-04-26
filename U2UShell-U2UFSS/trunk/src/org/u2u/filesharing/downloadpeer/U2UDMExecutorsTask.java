/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.u2u.filesharing.downloadpeer;

import java.util.Arrays;

/**
 * This class represents a task that the Executor implementation
 * needs to run.
 * @author Irene & Sergio
 */
public class U2UDMExecutorsTask {

    /**
     * Task's type relationship with the arrived of new sources
     */
    public static final int INCOMING_NEW_SOURCES_TASK = 30;
    /**
     * Task's type relationship with the need of looking for sources
     */
    public static final int FINDING_SOURCES_TASK = 31;
    /**
     * Task's type relationship with the need of try to download chunks from a the remote peer
     */
    public static final int DOWNLOAD_RANDOM_CHUNK_TASK = 32;
    /**
     *
     */

    //relationship with the protocol's orders

    /**
     * Task's type relationship with the need of know info about the content,
     * this sends an INFO order of type fileInfoQueryChunksSha1(list of chunks' hash) only
     */
    public static final int SEND_FILE_INFO_QUERY_CHUNKS_SHA1_TASK = 33;
    /**
     * Task's type relationship with the need of know info about the content,
     * this sends an INFO order of type fileInfoQueryPeerChunks(remote peer's chunks) only
     */
    public static final int SEND_FILE_INFO_QUERY_PEER_CHUNKS_TASK = 34;
    /**
     * Task's type relationship with the incoming of file info response with the Chunks' SHA1
     * from the UploadPeer
     */
    public static final int INCOMING_CHUNKS_SHA1_INFO_TASK = 35;
    /**
     * Task's type relationship with the incoming of file info response with the Peer's Chunks
     * from the UploadPeer
     */
    public static final int INCOMING_PEER_CHUNKS_INFO_TASK = 36;
    /**
     * Task's type relationship with the incoming of Get Chunk response with the chunk's data
     * from the UploadPeer
     */
    public static final int INCOMING_CHUNKS_TASK = 37;
    /**
     * Task's type relationship with the need of know info about the content,
     * this sends an INFO order of type fileInfoQueryPeerChunks(remote peer's chunks) to all the protocols
     */
    public static final int SEND_TO_ALL_FILE_INFO_QUERY_PEER_CHUNKS_TASK = 38;

    /**
     * Task's type relationship with the incoming of QUIT response from the UploadPeer
     */
    public static final int INCOMING_QUIT_TASK = 39;

    //EORelationship with the protocol's orders

    //Executor
    /**
     * Task's type relationship with the finish of Executor
     */
    public static final int FINISH_MANAGER_TASK = 40;
    /**
     * Task's type relationship with the shared file's download finished
     */
    public static final int FINISH_THE_DOWNLOAD_TASK = 41;
    //EOExecutor
    

    /** task's type*/
    private final int taskType;
    /** task's data*/
    private final Object[] taskData;

    private final int[] types = new int[] {
        INCOMING_NEW_SOURCES_TASK, FINDING_SOURCES_TASK, DOWNLOAD_RANDOM_CHUNK_TASK,
        FINISH_MANAGER_TASK, FINISH_THE_DOWNLOAD_TASK, SEND_FILE_INFO_QUERY_CHUNKS_SHA1_TASK,
        SEND_FILE_INFO_QUERY_PEER_CHUNKS_TASK,INCOMING_CHUNKS_SHA1_INFO_TASK,
        INCOMING_PEER_CHUNKS_INFO_TASK, INCOMING_CHUNKS_TASK, SEND_TO_ALL_FILE_INFO_QUERY_PEER_CHUNKS_TASK,
        INCOMING_QUIT_TASK
    };
    /**
     * build a task to execute
     * @param type type of the task, use the class's constants.
     * @param array the data need to execute the task
     */
    protected U2UDMExecutorsTask(int type, Object[] array)
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
