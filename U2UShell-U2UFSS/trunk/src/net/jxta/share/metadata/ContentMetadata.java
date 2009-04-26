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
 * $Id: ContentMetadata.java,v 1.9 2007/05/14 22:08:28 bondolo Exp $
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
 * ContentMetadata is an interface for classes used to store metadata for a
 * Content object.  A ContentAdvertisement can then use a ContentMetadata
 * implementation to generate a &lt;metadata> block and insert it inside
 * itself.  The StructuredTextDocument representation of a &lt;metadata> block
 * is as follows:<br><br>
 * <p/>
 * <code>&lt;metadata><br>
 * &nbsp;&nbsp;&lt;scheme><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;name><i>scheme name</i>&lt;/name><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;location><i>location of resource used to parse
 * and query the scheme</i>&lt;/location><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;content-type><i>content type of the raw metadata,
 * otherwise "text/litexml"</i>&lt;/content-type><br>
 * &nbsp;&nbsp;&lt;/scheme><br>
 * &nbsp;&nbsp;<i>metadata payload</i><br>
 * &lt;/metadata></code>
 *
 * @author $Author: bondolo $
 * @version $Revision: 1.9 $
 */
public abstract class ContentMetadata implements java.io.Serializable {

    private final static transient Logger LOG = Logger.getLogger(ContentMetadata.class.getName());

    /**
     * The name of the metadata element
     */
    public static final String EN_METADATA = "metadata";

    /**
     * The name of the scheme element
     */
    public static final String EN_SCHEME = "scheme";

    /**
     * The name of the name element
     */
    public static final String EN_NAME = "name";

    /**
     * The name of the content-type element
     */
    public static final String EN_CONTENT_TYPE = "content-type";

    /**
     * The name of the location element
     */
    public static final String EN_LOCATION = "location";

    /**
     * Factory method for generating a new MetadataQuery object that can be
     * used to query instances of this class.
     *
     * @return A MetadataQuery object that can be used to query instances of
     *         this class, or null if no such object can be generated.  In the case of
     *         ContentMetadata.newQuery(), null is always returned because
     *         ContentMetadata is a generic container for metadata and defines no means
     *         of being queried.
     * @throws IllegalArgumentException if query was not formatted properly.
     */
    public static MetadataQuery newQuery(String query)
            throws IllegalArgumentException {
        return null;
    }

    /**
     * Get a ContentMetadataConstructor that can be used to create instances of
     * this class, or null if no such object can be created.  This method
     * should be overridden if there is a way of constructing this
     * ContentMetadata class using an Element.
     */
    public static ContentMetadataConstructor getConstructor() {
        return null;
    }

    /** 
     * Stores the name of the metadata scheme used
     */
    protected String name;
    
    /** 
     * Stores the location of information defining the metadata scheme
     */
    protected String location;

    /** 
     * Stores the content type of the metadata
     */
    protected String content_type;

    /**
     * Initialize the <code>name</code> <code>content_type</code> and
     * <code>location</code> fields from a <code>&lt;metadata></code> block.
     *
     * @param metadata the <code>&lt;metadata></code> block
     * @throws IllegalArgumentException if
     *                                  <code>metadata.getKey().equals(EN_METADATA)</code> returns false.
     * @throws NullPointerException     if <code>metadata</code> is null
     */
    protected void init(Element metadata) throws IllegalArgumentException {
	if(metadata == null) {
	    throw new NullPointerException("null argument");
        }

	if(!metadata.getKey().equals(EN_METADATA)) {
	    throw new IllegalArgumentException("invalid key name: " +metadata.getKey());
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
		} else if(sChildKey.equals(EN_CONTENT_TYPE)) {
		    if((content_type != null) && (Logging.SHOW_WARNING && LOG.isLoggable(Level.WARNING)) ) {
			LOG.warning("multiple <content-type> elements");
                    }
		    content_type = temp.toString();
		}
	    }
	    
	    //write to the log if either name or debug is empty or null
	    if((name == null) && (Logging.SHOW_FINE && LOG.isLoggable(Level.FINE))) {
		LOG.fine("scheme has undefined name");
            }
            
	    if((content_type == null) && (Logging.SHOW_FINE && LOG.isLoggable(Level.FINE))) {
		LOG.fine("scheme has undefined content type");
            }
	} else if(Logging.SHOW_FINE && LOG.isLoggable(Level.FINE)) {
	    LOG.fine("<scheme> element not found");
	}
	
	if(children.hasMoreElements() && Logging.SHOW_WARNING && LOG.isLoggable(Level.WARNING)) {
	    LOG.warning("multiple <scheme> elements");
        }
    }

    /**
     * Return the name of the metadata scheme being used by this object, or
     * null if unspecified.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the location of the resource that is needed to parse and query 
     * metadata using this scheme.
     *
     * @return a String representation of the URL location of the resource, or
     *         null if unspecified
     */
    public String getLocation() {
        return location;
    }
    
    /**
     * Get the content type of the metadata returned by getValue()
     */
    public String getContentType() {
        return content_type;
    }

    /**
     * @return the string representation of the metadata payload, or null if it
     * has no string representation.
     */
    public abstract String getValue();

    /**
     * Insert this metadata element into an advertisement
     *
     * @param adv a StructuredDocument representation of the advertisement
     * @param el  the element in adv to insert this metadata element into
     */
    public void appendDocument(StructuredDocument adv, Element el) {
        Element mEl = adv.createElement(EN_METADATA, this.getValue());
        el.appendChild(mEl);
        appendElements(adv, mEl);
    }
    
    /**
     * This function defines how the child elements of this metadata element
     * should be inserted into an element of a StructuredDocument.
     *
     * @param root  the StructuredDocument which <code>child</code> is a part of
     * @param child the Element object into which to insert the metadata
     */
    protected void appendElements(StructuredDocument root, Element child) {
	//only add the <scheme> element if one of its subtags is non-null
	if((name != null) || (location != null) || (content_type != null)) {
	    Element schemeEl = root.createElement(EN_SCHEME);
	    child.appendChild(schemeEl);
	
	    if(name != null) {
		schemeEl.appendChild(root.createElement(EN_NAME, name));
            }

	    if(location != null) {
		schemeEl.appendChild(root.createElement(EN_LOCATION,location));
            }
	
	    if(content_type != null) {
		schemeEl.appendChild(root.createElement(EN_CONTENT_TYPE, content_type));
            }
	}
    }
    
    /**
     * Generates a StructuredDocument representation of this metadata element
     * 
     * @param mmt the mime type of the StructuredDocument to be returned
     * @return a StructuredDocument of type <code>EN_METADATA</code> with
     *         containing all of the metadata information stored in this object.
     */
    public StructuredDocument getStructuredDocument(MimeMediaType mmt) {
    	StructuredDocument doc = StructuredDocumentFactory.newStructuredDocument(mmt, EN_METADATA, getValue());
    	appendElements(doc, doc);
    	return doc;
    }

    /**
     * A function for generating safe copies of this ContentMetadata object.
     * This particular implementation of clone() will throw a
     * CloneNotSupportedException every time, but it should be overridden if
     * clone() functionality is needed in a subclass.
     *
     * @return a safe copy of this ContentMetadata object
     */
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
}
