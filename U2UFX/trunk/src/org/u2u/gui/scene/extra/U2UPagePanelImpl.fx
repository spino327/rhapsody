/*
 * U2UPagePanel.fx
 *
 * Created on 27-may-2009, 9:59:50
 *
 * based on code from PDFRender project at java.net
 */

package org.u2u.gui.scene.extra;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;

import com.sun.pdfview.PDFPage;
import com.sun.pdfview.Flag;


import javax.swing.JPanel;
import javafx.util.Bits;

import java.lang.Math;


/*
 * $Id: PagePanel.java,v 1.2 2007/12/20 18:33:33 rbair Exp $
 *
 * Copyright 2004 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */


/**
 * A Swing-based panel that displays a PDF page image.  If the zoom tool
 * is in use, allows the user to select a particular region of the image to
 * be zoomed.
 */
public class U2UPagePanelImpl extends JPanel, ImageObserver, MouseListener, MouseMotionListener
{
    /** The image of the rendered PDF page being displayed */
    var currentImage:Image ;

    /** The current PDFPage that was rendered into currentImage */
    var currentPage:PDFPage ;

    /* the current transform from device space to page space */
    var currentXform:AffineTransform ;

    /** The horizontal offset of the image from the left edge of the panel */
    var offx:Integer;

    /** The vertical offset of the image from the top of the panel */
    var offy:Integer;

    /** the current clip, in device space */
    var clip:Rectangle2D ;

    /** the clipping region used for the image */
    var prevClip:Rectangle2D ;

    /** the size of the image */
    var prevSize:Dimension ;

    /** the zooming marquee */
    var zoomRect:Rectangle ;

    /** whether the zoom tool is enabled */
    var useZoom:Boolean= false;

    //    /** a listener for page changes */
    //    PageChangeListener listener;

    /** a flag indicating whether the current page is done or not. */
    var flag:Flag = new Flag();

    // Color boxcolor= new Color(255,200,200);

    /**
     * Create a new PagePanel, with a default size of 800 by 600 pixels.
     */
    public function PagePanel():Void {
        //super;
        setPreferredSize(new Dimension(650, 500));
        setFocusable(true);
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    /**
     * Stop the generation of any previous page, and draw the new one.
     * @param page the PDFPage to draw.
     */
    public function showPage(page:PDFPage ):Void {
	// stop drawing the previous page
        if (currentPage != null and prevSize != null) {
            currentPage.stop(prevSize.width, prevSize.height,  prevClip);
        }

        // set up the new page
        currentPage= page;

        if (page==null) {
            // no page
            currentImage= null;
            clip= null;
                currentXform = null;
                repaint();
        } else {
            // start drawing -- clear the flag to indicate we're in progress.
            flag.clear();
            //	    System.out.println("   flag cleared");

            var sz:Dimension = getSize();
            if (sz.width+sz.height==0) {
            // no image to draw.
            return;
            }
	    //	    System.out.println("Ratios: scrn="+((float)sz.width/sz.height)+
	    //			       ", clip="+(clip==null ? 0 : clip.getWidth()/clip.getHeight()));

	    // calculate the clipping rectangle in page space from the
	    // desired clip in screen space.
            var useClip:Rectangle2D  = clip;
            if (clip != null and currentXform != null) {
                useClip = currentXform.createTransformedShape(clip).getBounds2D();
            }

            var pageSize:Dimension  = page.getUnstretchedSize(sz.width, sz.height,
                                                         useClip);

	    // get the new image
            currentImage= page.getImage(pageSize.width, pageSize.height,
                                        useClip, this);

	    // calculate the transform from screen to page space
	        currentXform = page.getInitialTransform(pageSize.width,
                                                    pageSize.height,
                                                    useClip);
            try {
                currentXform = currentXform.createInverse();
            } catch (nte:NoninvertibleTransformException ) {
                println("Error inverting page transform!");
                nte.printStackTrace();
            }

            prevClip = useClip;
            prevSize = pageSize;

            repaint();
        }
    }

    /**
     * @deprecated
     */
    public function flush():Void {
	//	images.clear();
	//	lruPages.clear();
	//	nextPage= null;
	//	nextImage= null;
    }



    public function zoomInPage():Void{

         setClip(new Rectangle2D.Double(650- offx, 500 - offy,
                                       650, 500));

            zoomRect= null;

    }

    /**
     * Draw the image.
     */
    override function paint(g:Graphics):Void {
        var sz:Dimension = getSize();
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        if (currentImage==null) {
            // No image -- draw an empty box
            // [[MW: remove the scary red X]]
            //	    g.setColor(Color.red);
            //	    g.drawLine(0, 0, getWidth(), getHeight());
            //	    g.drawLine(0, getHeight(), getWidth(), 0);
            g.setColor(Color.black);
            g.drawString("No page selected", getWidth()/2-30, getHeight()/2);
        } else {
            // draw the image
            var imwid:Integer= currentImage.getWidth(null);
            var imhgt:Integer= currentImage.getHeight(null);

            // draw it centered within the panel
            offx= (sz.width-imwid)/2;
            offy= (sz.height-imhgt)/2;

            if ((imwid==sz.width and imhgt<=sz.height) or
            (imhgt==sz.height and imwid<=sz.width)) {

                g.drawImage(currentImage, offx, offy, this);

            } else {
            // the image is bogus.  try again, or give up.
                flush();
                if (currentPage!=null) {
                    showPage(currentPage);
                }
            g.setColor(Color.red);
            g.drawLine(0, 0, getWidth(), getHeight());
            g.drawLine(0, getHeight(), getWidth(), 0);
            }
        }
        // draw the zoomrect if there is one.
        if (zoomRect!=null) {
            g.setColor(Color.red);
            g.drawRect(zoomRect.x, zoomRect.y,
                   zoomRect.width, zoomRect.height);
        }
        // debugging: draw a rectangle around the portion that just changed.
        //	g.setColor(boxColor);
        //	Rectangle r= g.getClipBounds();
        //	g.drawRect(r.x, r.y, r.width-1, r.height-1);
    }

    /**
     * Gets the page currently being displayed
     */
    public function getPage():PDFPage  {
	return currentPage;
    }

    /**
     * Gets the size of the image currently being displayed
     */
    public function getCurSize():Dimension  {
        return prevSize;
    }

    /**
     * Gets the clipping rectangle in page space currently being displayed
     */
    public function getCurClip():Rectangle2D  {
        return prevClip;
    }

    /**
     * Waits until the page is either complete or had an error.
     */
    public function  waitForCurrentPage():Void  {
        flag.waitForFlag();
    }

    /**
     * Handles notification of the fact that some part of the image
     * changed.  Repaints that portion.
     * @return true if more updates are desired.
     */
    public override function imageUpdate(img:Image , infoflags:Integer , x:Integer, y:Integer,
			       width:Integer, height:Integer):Boolean {
        // System.out.println("Image update: " + (infoflags & ALLBITS));
        var res:Bits = Bits{};
        var sz:Dimension = getSize();
        if ((infoflags != 0) and (res.bitOr(SOMEBITS,ALLBITS ))!=0) {
             //[[MW: dink this rectangle by 1 to handle antialias issues]]
            repaint(x+offx, y+offy, width, height);
        }

        if (infoflags!=0 and (res.bitOr((res.bitOr(ALLBITS,ERROR)),ABORT)!=0)) {
            flag.set();
            println("   flag set");
            return false;
        }else {
            return true;
        }
    }

//    public void addPageChangeListener(PageChangeListener pl) {
//	listener= pl;
//    }

//    public void removePageChangeListener(PageChangeListener pl) {
//	listener= null;
//    }

    /**
     * Turns the zoom tool on or off.  If on, mouse drags will draw the
     * zooming marquee.  If off, mouse drags are ignored.
     */
    public function useZoomTool(use:Boolean):Void {
        useZoom= use;
    }

    /**
     * Set the desired clipping region (in screen coordinates), and redraw
     * the image.
     */
    public function setClip(clip:Rectangle2D):Void {
        this.clip= clip;
        showPage(currentPage);
    }

    /** x location of the mouse-down event */
    var downx:Integer;

    /** y location of the mouse-down event */
    var downy:Integer;

    /** Handles a mousePressed event */
    public override function mousePressed(evt:MouseEvent ):Void {
        downx= evt.getX();
        downy= evt.getY();
    }

    /**
     * Handles a mouseReleased event.  If zooming is turned on and there's
     * a valid zoom rectangle, set the image clip to the zoom rect.
     */
    public override function  mouseReleased(evt:MouseEvent ):Void {
	// calculate new clip
	if (not useZoom or zoomRect==null or
	    zoomRect.width==0 or  zoomRect.height==0) {
	    zoomRect= null;
	    return;
	}

        setClip(new Rectangle2D.Double(zoomRect.x - offx, zoomRect.y - offy,
                                       zoomRect.width, zoomRect.height));

        zoomRect= null;
    }
    public override function mouseClicked(evt:MouseEvent ):Void {}
    public override function mouseEntered(evt:MouseEvent ):Void {}
    public override function mouseExited(evt:MouseEvent ):Void {}
    public override function mouseMoved(evt:MouseEvent ):Void {}

    /**
     * Handles a mouseDragged event. Constrains the zoom rect to the
     * aspect ratio of the panel unless the shift key is down.
     */
    public override function mouseDragged(evt:MouseEvent ):Void {
	if (useZoom) {
	    var x:Integer= evt.getX();
	    var y:Integer= evt.getY();
	    var dx:Integer= Math.abs(x-downx);
	    var dy:Integer= Math.abs(y-downy);
	    // constrain to the aspect ratio of the panel
	    if ((evt.getModifiers() == 0) and (evt.SHIFT_MASK==0)) {
            var aspect:Float= (dx as Float)/(dy as Float);
            var waspect:Float= (getWidth() as Float)/(getHeight() as Float);
            if (aspect>waspect) {
                dy= (dx/waspect) as Integer;
            } else {
                dx= (dy*waspect) as Integer;
            }
	    }
	    if (x<downx) {
            x= downx-dx;
	    }
	    if (y<downy) {
            y= downy-dy;
	    }
	    var old:Rectangle= zoomRect;
	    // ignore small rectangles
	    if (dx<5 or dy<5) {
            zoomRect= null;
	    } else {
            zoomRect= new Rectangle(Math.min(downx, x), Math.min(downy, y),
					dx, dy);
	    }
	    // calculate the repaint region.  Should be the union of the
	    // old zoom rect and the new one, with an extra pixel on the
	    // bottom and right because of the way rectangles are drawn.
	    if (zoomRect!=null) {
            if (old!=null) {
                old.add(zoomRect);
            } else {
                old= new Rectangle(zoomRect);
            }
	    }
	    if (old!=null) {
            old.width++;
            old.height++;
	    }
	    if (old!=null) {
            repaint(old);
	    }
	}
    }


}

