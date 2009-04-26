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
 * $Id: LiteXMLMetadata.java,v 1.10 2007/05/14 22:08:28 bondolo Exp $
 *
 */
package net.jxta.share.metadata;

import net.jxta.document.Element;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocument;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.logging.Logging;

import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <code>LiteXMLMetadata</code> is a class for storing metadata whose payload
 * can be represented as in the LiteXML used by <code>StructuredDocument</code>
 * and <code>Element</code> objects.
 *
 * @see net.jxta.document.StructuredDocument
 * @see net.jxta.document.Element
 */
public abstract class LiteXMLMetadata extends ContentMetadata {

    private final static transient Logger LOG = Logger.getLogger(LiteXMLMetadata.class.getName());
    
    /**
     * The content type of LiteXML-encoded metadata
     */
    public static final String LITEXML_CONTENT_TYPE = "text/litexml";

    /**
     * <code>content_type</code> is automatically initialized to
     * <code>LITEXML_CONTENT_TYPE</code>
     */
    protected final String content_type = LITEXML_CONTENT_TYPE;

    /**
     * Initialize the <code>name</code> and <code>location</code> fields from a
     * <code>&lt;metadata></code> block.
     *
     * @param metadata the <code>&lt;metadata></code> block
     * @throws IllegalArgumentException - if <code>metadata</code> is null or
     *                                  <code>metadata.getKey().equals(EN_METADATA)</code> returns false.
     */
    protected void init(Element metadata) throws IllegalArgumentException {
	if(metadata == null) {
	    throw new IllegalArgumentException("null argument");
        }

	if(!metadata.getKey().equals(EN_METADATA)) {
	    throw new IllegalArgumentException("invalid key name: " + metadata.getKey());
        }
        
	Element schemeEl;
	Enumeration children = metadata.getChildren(EN_SCHEME);
	if(children.hasMoreElements()) {
	    schemeEl = (Element)children.nextElement();
	    String sChildKey;
	    Object temp;
	    Element sChildEl;
	    Enumeration sChildren = schemeEl.getChildren();
	    while(sChildren.hasMoreElements()) {
		sChildEl = (Element)sChildren.nextElement();
		sChildKey = sChildEl.getKey().toString();
		temp = sChildEl.getValue();

		//if no value is set for an element, assume that the value is
		// meant to be an empty string
		if(temp == null) {
		    temp = "";
                }
		if(sChildKey.equals(EN_NAME)) {
		    if((name != null) && (Logging.SHOW_WARNING && LOG.isLoggable(Level.WARNING))) {
			LOG.warning("multiple <name> elements");
                    }
		    name = temp.toString();
		} else if(sChildKey.equals(EN_LOCATION)) {
		    if((location != null) && (Logging.SHOW_WARNING && LOG.isLoggable(Level.WARNING))) {
			LOG.warning("multiple <location> elements");
                    }
		    location = temp.toString();
		}
	    }
	    
	    //write to the log if either name or debug is empty or null
	    if((name == null) && (Logging.SHOW_FINE && LOG.isLoggable(Level.FINE))) {
		LOG.fine("scheme has undefined name");
            }
	} else if(Logging.SHOW_FINE && LOG.isLoggable(Level.FINE)) {
	    LOG.fine("<scheme> element not found");
	}
	
	if(children.hasMoreElements() && Logging.SHOW_WARNING && LOG.isLoggable(Level.WARNING)) {
	    LOG.warning("multiple <scheme> elements");
        }
    }

    /**
     * Get the string representation of the metadata payload, or null if the
     * metadata has no string representation.
     *
     * @return null because LiteXMLMetadata does not represent its metadata as
     *         a string.
     */
    public String getValue() {
        return null;
    }

    /**
     * Return all of the metadata elements as children of a specific
     * StructuredDocument.
     */
    public abstract Element[] getMetadataElements(StructuredDocument root);


    /**
     * Insert the child elements of this metadata element into a specified
     * element of a StructuredDocument.
     *
     * @param root  the StructuredDocument which <code>child</code> is a part of
     * @param child the Element object into which to insert the metadata
     */
    protected void appendElements(StructuredDocument root, Element child) {
        Element schemeEl = root.createElement(EN_SCHEME);
        child.appendChild(schemeEl);

        if (name != null)
            schemeEl.appendChild(root.createElement(EN_NAME, name));

        if (location != null)
            schemeEl.appendChild(root.createElement(EN_LOCATION, location));

        //content_type can never be null because it is final and initialized to
        //a non-null value
        schemeEl.appendChild(root.createElement(EN_CONTENT_TYPE,
                content_type));

        Element[] metadataElements = getMetadataElements(root);
        for (int i = 0; i < metadataElements.length; i++)
            child.appendChild(metadataElements[i]);
    }


    /**
     * Generates a StructuredDocument representation of this metadata element
     *
     * @param mmt the mime type of the StructuredDocument to be returned
     * @return a StructuredDocument of type <code>EN_METADATA</code> with
     *         containing all of the metadata information stored in this object.
     */
    public StructuredDocument getStructuredDocument(MimeMediaType mmt) {
    	StructuredDocument doc = StructuredDocumentFactory.newStructuredDocument(mmt,EN_METADATA);
    	appendElements(doc, doc);

        return doc;
    }
}
