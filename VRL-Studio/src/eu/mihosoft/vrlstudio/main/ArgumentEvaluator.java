/* 
 * ArgumentEvaluator.java
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009–2015 Steinbeis Forschungszentrum (STZ Ölbronn),
 * Copyright (c) 2007–2018 by Michael Hoffer
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
 * Third, add an additional notice, stating that you modified VRL. A suitable
 * notice might read
 * "VRL source code modified by YourName 2012".
 * 
 * Note, that these requirements are in full accordance with the LGPL v3
 * (see 7. Additional Terms, b).
 *
 * Please cite the publication(s) listed below.
 *
 * Publications:
 *
 * M. Hoffer, C. Poliwoda, & G. Wittum. (2013). Visual reflection library:
 * a framework for declarative GUI programming on the Java platform.
 * Computing and Visualization in Science, 2013, 16(4),
 * 181–192. http://doi.org/10.1007/s00791-014-0230-y
 */

package eu.mihosoft.vrlstudio.main;

import eu.mihosoft.vrl.io.VArgUtil;
import eu.mihosoft.vrl.io.VPropertyFolderManager;
import eu.mihosoft.vrl.io.vrlx.VRLXSessionController;
import eu.mihosoft.vrl.io.vrlx.VRLXReflection;
import eu.mihosoft.vrl.reflection.VisualCanvas;
import eu.mihosoft.vrl.system.VRL;
import eu.mihosoft.vrl.visual.VGraphicsUtil;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Evaluates command line arguments.
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
public class ArgumentEvaluator {

    private Studio frame;
    private VisualCanvas mainCanvas;

    /**
     * Constructor.
     * @param frame the frame
     * @param mainCanvas the main canvas
     */
    public ArgumentEvaluator(Studio frame, VisualCanvas mainCanvas) {
        this.frame = frame;
        this.mainCanvas = mainCanvas;
    }
//
//    public static String[] knownArgs() {
//        return new String[]{"-resolution",
//                    "-enable3d",
//                    "-defaultProject",
//                    "-enableDebugging"};
//    }

    /**
     * Stets the frame resolution.
     * @param args the arguments
     */
    public void setResolution(String[] args) {
        String resolution = VArgUtil.getArg(args, "-resolution");
        System.out.println(">> Resolution:");

        if (resolution != null && resolution.matches("\\d+x\\d+")) {
            String[] values = resolution.split("x");
            int w = new Integer(values[0]);
            int h = new Integer(values[1]);

            frame.setSize(w, h);
            frame.getSplitPane().setDividerLocation(frame.getHeight());

            System.out.println(" --> setting resolution: " + w + "x" + h + ".");
        } else if (!resolution.matches("\\d+x\\d+")) {
            System.out.println(" --> SYNTAX ERROR: resolution must be specified"
                    + " as WIDTHxHEIGHT. Example: 640x480.");
        }

        if (resolution == null) {
            System.out.println(" --> WARNING: no resolution specified!");
        }
    }

    public void setPluginOptions(String[] args) {
        VRL.evaluatePluginArguments(args);
    }

    public void setRenderingOptions(String[] args) {
        String render3D = VArgUtil.getArg(args, "-enable3d");
        System.out.println(">> 3D Options:");

        if (render3D != null) {
            if (render3D.equals("yes")) {
                System.out.println(
                        " --> enabling 3D rendering");
                VGraphicsUtil.set3DEnvironment();
            } else if (render3D.equals("no")) {
                System.out.println(
                        " --> disabling 3D rendering");
                VGraphicsUtil.disable3DGraphics();
            } else {
                System.out.println(
                        " --> ERROR: wrong value specified! Valid values:"
                        + " [yes/no]");
            }
        } else {
            System.out.println(
                    " --> Warning: no 3D option specified!");
        }
    }

    public void setDefaultFile(String[] args) {
        System.out.println(">> Default Project Options:");
        String fileName = VArgUtil.getArg(args, "-defaultProject");

        if (fileName != null) {

            if (!fileName.toLowerCase().endsWith(".vrlp")) {
                System.out.println(
                        " --> error: specified file does not end with .vrlp!"
                        + " Using empty project.");
            } else {
                frame.setDefaultSessionName(fileName);
//            frame.loadSession(fileName);
                System.out.println(
                        " --> using specified default project file: "
                        + fileName);
            }
        } else {
            System.out.println(
                    " --> no project specified. Using empty project.");
        }
    }

    public boolean loadFile(String[] args) {
        String fileName = VArgUtil.getArg(args, "-file");

        boolean result = false;

        if (fileName != null) {

            System.out.println(">> Load Project Options:");

            if (!fileName.toLowerCase().endsWith(".vrlp")) {
                System.out.println(
                        " --> error: specified file does not end with .vrlp!"
                        + " Not loading file.");
            } else {
//                frame.setDefaultSessionName(fileName);

                result = frame.loadSession(fileName);
                System.out.println(
                        " --> loading specified project file: "
                        + fileName);
            }
        } else {
            //
        }

        return result;
    }

    public void setDebugOptions(String[] args) {
        String enablePlugins = VArgUtil.getArg(args, "-enableDebugging");
        System.out.println(">> Debugging Options:");

        // default value
        frame.getDebugMenu().setVisible(false);

        if (enablePlugins != null) {
            if (enablePlugins.equals("yes")) {
                System.out.println(
                        " --> enabling debugging");
                frame.getDebugMenu().setVisible(true);
            } else if (enablePlugins.equals("no")) {
                System.out.println(
                        " --> disabling debugging");
                frame.getDebugMenu().setVisible(false);
            } else {
                System.out.println(
                        " --> ERROR: wrong value specified! Valid values:"
                        + " [yes/no]");
            }
        } else {
            System.out.println(
                    " --> Warning: no debugging option specified! Default: no");
        }
    }
}
