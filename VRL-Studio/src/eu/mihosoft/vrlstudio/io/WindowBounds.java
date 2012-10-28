/* 
 * WindowBounds.java
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009–2012 Steinbeis Forschungszentrum (STZ Ölbronn),
 * Copyright (c) 2007–2012 by Michael Hoffer
 * 
 * This file is part of VRL-Studio.
 *
 * VRL-Studio is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3
 * as published by the Free Software Foundation.
 * 
 * see: http://opensource.org/licenses/LGPL-3.0
 *      file://path/to/VRL/src/eu/mihosoft/vrl/resources/license/lgplv3.txt
 *
 * VRL-Studio is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * This version of VRL-Studio includes copyright notice and attribution requirements.
 * According to the LGPL this information must be displayed even if you modify
 * the source code of VRL-Studio. Neither the VRL Canvas attribution icon nor any
 * copyright statement/attribution may be removed.
 *
 * Attribution Requirements:
 *
 * If you create derived work you must do three things regarding copyright
 * notice and author attribution.
 *
 * First, the following text must be displayed on the Canvas:
 * "based on VRL source code". In this case the VRL canvas icon must be removed.
 * 
 * Second, keep the links to "About VRL-Studio" and "About VRL". The
 * copyright notice must remain.
 *
 * Third, add an additional notice, stating that you modified VRL. In addition
 * you must cite the publications listed below. A suitable notice might read
 * "VRL source code modified by YourName 2012".
 * 
 * Note, that these requirements are in full accordance with the LGPL v3
 * (see 7. Additional Terms, b).
 *
 * Publications:
 *
 * M. Hoffer, C.Poliwoda, G.Wittum. Visual Reflection Library -
 * A Framework for Declarative GUI Programming on the Java Platform.
 * Computing and Visualization in Science, 2011, in press.
 */

package eu.mihosoft.vrlstudio.io;

import eu.mihosoft.vrl.io.ConfigurationFile;
import eu.mihosoft.vrl.visual.VGraphicsUtil;
import java.awt.Dimension;
import java.awt.Window;
import javax.swing.JFrame;

/**
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
public class WindowBounds {

    private int x;
    private int y;
    private int width;
    private int height;
    private int screenID;
    private static final String KEY_X = "Window:x";
    private static final String KEY_Y = "Window:y";
    private static final String KEY_W = "Window:w";
    private static final String KEY_H = "Window:h";
    private static final String KEY_SCREEN_ID = "Window:screen-id";

    public WindowBounds(ConfigurationFile config) {
        try {
            x = Integer.parseInt(config.getProperty(KEY_X));
            y = Integer.parseInt(config.getProperty(KEY_Y));
            width = Integer.parseInt(config.getProperty(KEY_W));
            height = Integer.parseInt(config.getProperty(KEY_H));
            screenID = Integer.parseInt(config.getProperty(KEY_SCREEN_ID));
        } catch (NumberFormatException ex) {
            //
        }
    }

    public WindowBounds(Window w, ConfigurationFile config) {
        this.screenID = VGraphicsUtil.getScreenId(w);
        this.x = w.getX();
        this.y = w.getY();
        this.width = w.getWidth();
        this.height = w.getHeight();

        config.setProperty(KEY_X, "" + x);
        config.setProperty(KEY_Y, "" + y);
        config.setProperty(KEY_W, "" + width);
        config.setProperty(KEY_H, "" + height);
        config.setProperty(KEY_SCREEN_ID, "" + screenID);
    }

    public void setWindowBounds(Window w) {

        width = Math.max(600, width);
        height = Math.max(400, height);
        
        Dimension d =VGraphicsUtil.getScreenDimension(screenID);
       
        width = Math.min(d.width, width);
        height = Math.min(d.height, height);

        w.setLocation(x, y);
        w.setSize(width, height);
    }

    /**
     * @return the x
     */
    public int getX() {
        return x;
    }

    /**
     * @param x the x to set
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * @return the y
     */
    public int getY() {
        return y;
    }

    /**
     * @param y the y to set
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * @param width the width to set
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    /**
     * @param height the height to set
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * @return the screenID
     */
    public int getScreenID() {
        return screenID;
    }

    /**
     * @param screenID the screenID to set
     */
    public void setScreenID(int screenID) {
        this.screenID = screenID;
    }
}
