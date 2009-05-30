/*
 * U2UJPanel.fx
 *
 * Created on 26-may-2009, 23:46:43
 *
 * based on code from http://jfx.wikia.com/wiki/SwingComponents
 */

package org.u2u.gui.scene.extra;

import javafx.ext.swing.SwingComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;


/**
 * @author sergio

 *We get something of this code from http://jfx.wikia.com/wiki/SwingComponents#Panels
 */

public class U2UJPanel extends SwingComponent {

    //main panel that contains the top and buttom panels
    var panel: JPanel;
    //top panel that contains the buttons at the top of the main panel
    var flowPanelTop: JPanel;
    //button panel that contains buttons at the end of the main panel
    var flowPanelBottom:JPanel;

    public var top: SwingComponent[] on replace{
        for(component in top){
            flowPanelTop.add(component.getJComponent());
        }
        panel.add( flowPanelTop, BorderLayout.NORTH);
    }

    /*public var left: SwingComponent on replace{
        panel.add( left.getJComponent(), BorderLayout.EAST);
    }*/

    public var center: SwingComponent on replace{
        println("[center] set component: {center.getJComponent()}");
        panel.add(center.getJComponent(), BorderLayout.CENTER);
    }

    /*public var right: SwingComponent on replace{
        panel.add( right.getJComponent(), BorderLayout.WEST);
    }*/

    public var bottom: SwingComponent[] on replace{
        for(component in bottom){
            flowPanelBottom.add(component.getJComponent());
        }
        panel.add( flowPanelBottom, BorderLayout.SOUTH);
    }


    public override function createJComponent(){

        panel = new JPanel(new BorderLayout());
        flowPanelTop = new JPanel(new FlowLayout());
        flowPanelBottom = new JPanel(new FlowLayout());
        
        return panel;
    }


}



