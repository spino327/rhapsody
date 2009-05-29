/*
 * U2UPagePanel.fx
 *
 * Created on 26-may-2009, 21:22:18
 */

package org.u2u.gui.scene.extra;

import com.sun.pdfview.PagePanel;
import com.sun.pdfview.PDFPage;

import javafx.ext.swing.SwingComponent;
import javafx.ext.swing.SwingButton;

import org.u2u.gui.scene.extra.U2UPagePanelImpl;


/**
 * @author sergio
 */

public class U2UPagePanel extends SwingComponent{

    var panel:U2UPagePanelImpl;

    public var content: SwingComponent[] on replace{
        for(component in content){
            panel.add(component.getJComponent());
        }
    }

   // public var content: SwingComponent[];



    public override function createJComponent(){
        panel = new U2UPagePanelImpl();
        return panel;
    }

    public function showPagePdf(page:PDFPage):Void{

        panel.showPage(page);
        panel.useZoomTool(true);
        
    }

    public function zoomInP():Void{

        panel.zoomInPage();
    }



}
