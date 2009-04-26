/*
 * Copyright (c) 2001 Sun Microsystems, Inc.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *       Sun Microsystems, Inc. for Project JXTA."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Sun", "Sun Microsystems, Inc.", "JXTA" and "Project JXTA" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact Project JXTA at http://www.jxta.org.
 *
 * 5. Products derived from this software may not be called "JXTA",
 *    nor may "JXTA" appear in their name, without prior written
 *    permission of Sun.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 *====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of Project JXTA.  For more
 * information on Project JXTA, please see
 * <http://www.jxta.org/>.
 *
 * This license is based on the BSD license adopted by the Apache Foundation.
 *
 * $Id: ContentManager.java,v 1.11 2007/03/22 17:29:02 hamada Exp $
 *
 */

package net.jxta.share;

import net.jxta.share.metadata.ContentMetadata;

import java.io.File;
import java.io.IOException;

/**
 * The ContentManager is used to manage local shared content for a peer.
 */
public abstract class ContentManager {
    /**
     * Shares the specified file using the file name as the share name
     * and a default mime type based on the file name. No description
     * will be provided for the shared content.
     *
     * @param file the File to be shared
     * @return the FileContent object for the shared file
     * @throws IOException           if there was an I/O error
     */
    public FileContent share(File file) throws IOException {
        return share(file, null);
    }

    /**
     * Shares the specified file with a description. The file name will
     * be used as the share name and a default mime type provided based
     * on the file name.
     *
     * @param file the File to be shared
     * @param desc the description of the content
     * @return the FileContent object for the shared file
     * @throws IOException           if there was an I/O error
     */
    public FileContent share(File file, String desc) throws IOException {
        return share(file, file.getName(), getMimeType(file), desc);
    }

    /**
     * Shares the specified file with the provided content name, type,
     * and description.
     *
     * @param file the File to be shared
     * @param name the share name, or null if file name should be used
     * @param type the content type, or null if none
     * @param desc the content description, or null if none
     */
    public abstract FileContent share(File file, String name, String type,
                                      String desc) throws IOException;

    /**
     * Shares the specified file with the provided content name, type,
     * and metadata.
     *
     * @param file     the File to be shared
     * @param name     the share name, or null if file name should be used
     * @param type     the content type, or null if none
     * @param metadata ContentMetadata objects describing <code>file</code>, or
     *                 null if not specified.
     */
    public abstract FileContent share(File file, String name, String type
            , ContentMetadata[] metadata)
            throws IOException;

    /**
     * Removes the specified Content from the list of shared content.
     *
     * @param c the Content to be unshared
     * @throws IllegalArgumentException if the Content was not shared
     * @throws IOException              if there was an I/O error
     */
    public abstract void unshare(Content c) throws IOException;

    /**
     * Removes the specified Content from the list of shared content.
     *
     * @param cAdv Content Advertisement of the content to be unshared
     * @throws IllegalArgumentException if the Content was not shared
     * @throws IOException              if there was an I/O error
     */
    public abstract void unshare(ContentAdvertisement cAdv) throws IOException;

    /**
     * Returns an array of all the shared content.
     */
    public abstract Content[] getContent();

    /**
     * Returns an array of all the shared content with the specified
     * content id.
     *
     * @param id the ContentId of the shared content
     */
    public abstract Content[] getContent(ContentId id);

    /**
     * Returns an array of all the shared content accepted by the specified
     * content filter.
     *
     * @param filter the ContentFilter to use for search shared content
     */
    public abstract Content[] getContent(ContentFilter filter);

    /**
     * Returns the mime type for the specified file, or null if unknown.
     */
    public abstract String getMimeType(File file);
}
