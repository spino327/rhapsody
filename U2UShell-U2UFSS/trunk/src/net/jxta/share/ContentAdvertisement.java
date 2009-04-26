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
 * $Id: ContentAdvertisement.java,v 1.13 2007/05/14 22:08:29 bondolo Exp $
 *
 */

package net.jxta.share;

import net.jxta.share.metadata.ContentMetadata;
import net.jxta.document.ExtendableAdvertisement;
import net.jxta.document.StructuredDocument;
import net.jxta.document.Element;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocument;
import net.jxta.id.ID;
import net.jxta.share.metadata.ContentMetadata;

import java.io.Serializable;

/**
 * A ContentAdvertisement contains information about shared content
 * within a peer group.
 */
public abstract class ContentAdvertisement extends ExtendableAdvertisement implements Serializable
{
    /** Name of the document element containing the name of the
     * content.
     */
    public static final String EN_NAME = "name";
    
    /**
     * Name of the document element containing the content id. 
     */
    public static final String EN_CID = "cid";
    
    /** 
     * Name of the document element containing the type of the
     * content.
     */
    public static final String EN_TYPE = "type";
    
    /** 
     * Name of the document element containing the length of the
     * content.
     */
    public static final String EN_LENGTH = "length";
    
    /** 
     * Name of the document element containing the description of the
     * content.
     */
    public static final String EN_DESCRIPTION = "description";
    
    /**
     * Name of the document element containing any other metadata
     * describing the content.
     */
    public static final String EN_METADATA = "metadata";
    
    /** 
     * Name of the document element containing the address of the
     * CMS service that is sharing the content.
     */
    public static final String EN_ADDRESS = "address";

    /**
     * Returns the advertisement type
     */
    public static String getAdvertisementType() {
        return "jxta:ContentAdvertisement";
    }
    
    /**
     * {@inheritDoc}
     **/
    @Override
    public final String getBaseAdvType() {
        return getAdvertisementType();
    }

    /**
     * Appends this advertisement document to the specified structured
     * document element.
     */
    public abstract void appendDocument(StructuredDocument doc, Element el);

    /**
     * Returns the name of the content.
     */
    public abstract String getName();

    /**
     * Returns the id of the content.
     */
    public abstract ContentId getContentId();

    /**
     * Returns the length of the content, or -1 if not specified.
     */
    public abstract long getLength();

    /**
     * Returns the mime type of the content, or null if not specified.
     */
    public abstract String getType();

    /**
     * Returns the description of the content, or null if not specified.
     */
    public abstract String getDescription();

    /**
     * Returns the metadata describing this content, or null if not specified.
     */
    public abstract ContentMetadata[] getMetadata();

    /**
     * Returns the address of the CMS service that is sharing the content,
     * or null if not specified.
     */
    public abstract String getAddress();

    /**
     * Returns the address of the CMS service that is sharing the content,
     * or null if not specified.  This address should be an endpoint address
     * in the following format: <code>jxta://[peer id]/CMS/[peergroup name]</code>
     * , specifying the id of the peer and name of peergroup in which the CMS service
     * that is sharing the content is running.
     */
    public abstract void setAddress(String address);
}
