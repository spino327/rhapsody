/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.u2u.filesharing;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.document.Document;
import net.jxta.document.Element;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocument;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.document.TextElement;
import net.jxta.id.ID;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.share.ContentAdvertisement;
import net.jxta.share.ContentId;
import net.jxta.share.metadata.ContentMetadata;
import org.u2u.common.io.U2UFileInputStream;
import net.jxta.document.XMLDocument;
/**
 * ContentAdvertisement Implementation for created Advertisement for the U2USharingFilesServices del U2U4U 
 */
public class U2UContentAdvertisementImpl extends ContentAdvertisement {

    /** content name (required)*/
    private String name;
    /** content id (required)*/
    private ContentId cid;
    /** content length (optional)*/
    private long length;
    /** content description (optional)*/
    private String description;
    /** metadata of content (optional)*/
    private ContentMetadata[] metadata;
    /** content type (optional)*/
    private String type;
    /** address of content (optional)*/
    private String address;
    /** advertisement for the JxtaServerSocket*/
    private PipeAdvertisement socketAdv;
    /** content chunks' size*/
    private short chunksize;

    /**
     * Name of the document element containing the socket advertisement of the
     * FileSharing service that is sharing the content.
     */
    public static final String EN_SOCKETADDRESS = "socketAdv";
    /**
     * Name of the document element containing the chunks size of the Content's chunks
     */
    public static final String EN_CHUNKSIZE = "chunksize";
    /** 
     * useful for register it in the JXTA's AdvertisementFactory
     */
    public static class Instantiator implements AdvertisementFactory.Instantiator {

        /**
         * {@inheritDoc}
         */
        public String getAdvertisementType( ) {
            return U2UContentAdvertisementImpl.getAdvertisementType();
        }

        /**
         * {@inheritDoc}
         */
        public Advertisement newInstance( ) {
            return new U2UContentAdvertisementImpl();
        }

        /**
         * {@inheritDoc}
         */
        public Advertisement newInstance( net.jxta.document.Element root ) {
            return new U2UContentAdvertisementImpl( (TextElement) root );
        }
    };

    private static final String [] fields = {EN_NAME, EN_CID, EN_TYPE};

    //constructors
    
    public U2UContentAdvertisementImpl()
    {
        
    }
    
    /**
     * 
     * @param name
     * @param cid
     * @param length
     * @param type
     * @param desc 
     */
    public U2UContentAdvertisementImpl(String name, ContentId cid, long length,
            String type, String desc)
    {
        if(name==null)
            throw new NullPointerException("name");
        if(cid==null)
            throw new NullPointerException("cid");

        this.name = name;
        this.cid = cid;
        this.socketAdv = null;
        this.chunksize = 0;
        this.length = length;
        this.type = type;
        this.description = desc;
        this.metadata = null;
        this.address = null;
        
    }

    /**
     * Creates a new U2UContentAdvertisementImpl from the specified TextElement.
     */
    private U2UContentAdvertisementImpl(TextElement textElement) {
        readTextElement(textElement);
    }
    
    //methods
    
    // append elements to specified document element
    private void appendElements(StructuredDocument doc, Element e) {
        e.appendChild(doc.createElement(EN_NAME, name.toLowerCase()));//LowerCase
        e.appendChild(doc.createElement(EN_CID, cid.toString().toLowerCase()));//LowerCase
        if (length != -1) {
            e.appendChild(doc.createElement(EN_LENGTH, Long.toString(length)));
        }
        if (type != null) {
            e.appendChild(doc.createElement(EN_TYPE, type));
        }
        if (description != null) {
            e.appendChild(doc.createElement(EN_DESCRIPTION, description));
        }
        if (metadata != null) {
            for(int a = 0; a < metadata.length; a++) {
                //insert metadata element into doc
                metadata[a].appendDocument(doc, doc);
            }
        }
        if (address != null) {
            e.appendChild(doc.createElement(EN_ADDRESS, address));
        }
        if (socketAdv != null)
        {
            e.appendChild(doc.createElement(EN_SOCKETADDRESS, socketAdv.toString()));
        }
        if (chunksize != -1) {
            e.appendChild(doc.createElement(EN_CHUNKSIZE, Short.toString(chunksize)));
        }
    }
    
    public void appendDocument(StructuredDocument doc, Element el) {
        Element e = doc.createElement(getAdvertisementType());
        el.appendChild(e);
        appendElements(doc, e);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ContentId getContentId() {
        return cid;
    }

    @Override
    public long getLength() {
        return length;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public ContentMetadata[] getMetadata() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Deprecated
    @Override
    public String getAddress() {
        return address;
    }

    /**
     * Returns the socket's PipeAdv
     * @return the PipeAdvertisement that represent the socket
     */
    public PipeAdvertisement getSocketAdv() {
        return socketAdv.clone();
    }

    /**
     * Returns the chunk's size
     * @return the chunk's size set for this Content
     */
    public int getChunksize()
    {
        return chunksize;
    }

    @Override
    public ID getID() {
        return null;
    }

    @Override
    public String[] getIndexFields() {
        return fields;
    }
    
    @Override
    public Document getDocument(MimeMediaType encodeAs) {
        StructuredDocument doc = StructuredDocumentFactory.newStructuredDocument(encodeAs,
                                                        getAdvertisementType());
        appendElements(doc, doc);
        return doc;
    }
    
    public static String getAdvertisementType() {
         return "jxta:U2UContentAdvertisement";
    }
    
    /**
     * Return an U2UFileInputStream representation for the this U2UContentAdvertisement
     * @return
     */
    public U2UFileInputStream getInputStream() {
        ObjectOutputStream out = null;
        U2UFileInputStream in = null;
        File file = null;

        try {
            
            file = File.createTempFile("adv", "tmp");
            file.deleteOnExit();
            
            out = new ObjectOutputStream(new FileOutputStream(file));

            out.writeObject(this);

            in = new U2UFileInputStream(file);
             
        } catch (IOException ex) {
            Logger.getLogger(U2UContentAdvertisementImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                Logger.getLogger(U2UContentAdvertisementImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return in;
    }
    @Deprecated
    @Override
    public void setAddress(String address) {
        this.address = new String(address);
    }

    /**
     * Modify the U2UContentAdvertisement's pipeAdv that represents the socekt's adv
     * @param socketAdv represents the PipeAdv object
     */
    public void setSocketAdv(PipeAdvertisement socketAdv)
    {
        if(socketAdv == null)
            this.socketAdv = null;
        else
            this.socketAdv = socketAdv.clone();

    }

    /**
     * Modify the U2UContentAdvertisement's chunksize that represents the length in kilo bytes of
     * each chunk of the Content, eg 64 are 64*1024 bytes
     * @param chunksize
     */
    public void setChunksize(short chunksize)
    {
        this.chunksize = chunksize;
    }

    // reads advertisement from specified TextElement
    private void readTextElement(TextElement textElement) {
        
        ArrayList<ContentMetadata> metadataElems = new ArrayList<ContentMetadata>();
        
        if (!textElement.getName().equals(getAdvertisementType())) {
            throw new IllegalArgumentException(
                "Not a ContentAdvertisement element: " + textElement.getName());
        }
        Enumeration e = textElement.getChildren();
        while (e.hasMoreElements()) {
            TextElement te = (TextElement)e.nextElement();
            String s = te.getName();
            String t = te.getTextValue();
            
            if ((t != null)||(te.getChildren() !=null)) {
                if (s.equals(EN_NAME)) {
                    name = t.toLowerCase();
                } else
                    if (s.equals(EN_CID)) {
                        try {
                            cid = new U2UContentIdImpl(t);
                        } catch (IllegalArgumentException ex) {
                            throw new IllegalArgumentException(
                                "Invalid content id: " + t);
                        }
                    } else
                        if (s.equals(EN_LENGTH)) {
                            try {
                                length = Long.parseLong(t);
                            } catch (NumberFormatException ex) {
                                throw new IllegalArgumentException(
                                    "Invalid content length: " + t);
                            }
                        } else
                            if (s.equals(EN_TYPE)) {
                                type = t;
                            } else
                                if (s.equals(EN_DESCRIPTION)) {
                                    description = t;
                                } else
                                    if (s.equals(EN_METADATA)) {
                                        //FIXME SUPPORT METADATA
                                        throw new UnsupportedOperationException("Not yet implemented");
                                        //metadataElems.add(ContentMetadataFactory.newInstance(te));
                                    } else //FIXME spino327@gmail.com REMOVE THE ENPOINT ADDRESS FROM THIS CLASS AND THE SUPER
                                        if (s.equals(EN_ADDRESS)) {
                                            address = t;
                                        } else
                                            if (s.equals(EN_SOCKETADDRESS)) {
                                                try
                                                {
                                                    // socketAdv = (PipeAdvertisement) AdvertisementFactory.newAdvertisement(xmlel);
                                                    InputStream is = new ByteArrayInputStream(t.getBytes());
                                                    // Create a factory
                                                    XMLDocument xmldoc = (XMLDocument)StructuredDocumentFactory.
                                                            newStructuredDocument(MimeMediaType.XMLUTF8, is);

                                                    socketAdv = (PipeAdvertisement) AdvertisementFactory.
                                                            newAdvertisement(xmldoc);

                                                }
                                                catch (IOException ex)
                                                {
                                                    Logger.getLogger(U2UContentAdvertisementImpl.class.getName()).log(Level.SEVERE, null, ex);
                                                }
                                            } else
                                                if (s.equals(EN_CHUNKSIZE)) {
                                                    try {
                                                        chunksize = Short.parseShort(t);
                                                    } catch (NumberFormatException ex) {
                                                        throw new IllegalArgumentException(
                                                            "Invalid content chunk size: " + t);
                                                    }
                                                }
            } else
                if (s.equals(EN_METADATA)) {
                    //a metadata element may have a null value if all of its
                    //information is stored in sub-elements
                    throw new UnsupportedOperationException("Handling the MetaData, Not yet implemented");
                    //metadataElems.add(ContentMetadataFactory.newInstance(te));
                }
        }
        if (name == null) {
            throw new IllegalArgumentException(
                "Missing required 'name' element");
        }
        if (cid == null) {
            throw new IllegalArgumentException(
                "Missing required 'cid' element");
        }
        if(metadataElems.size() != 0) {
            metadata = new ContentMetadata[metadataElems.size()];
            metadataElems.toArray(metadata);
        }
    }
    
}
