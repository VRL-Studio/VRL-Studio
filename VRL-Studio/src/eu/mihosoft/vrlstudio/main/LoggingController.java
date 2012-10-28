/* 
 * LoggingController.java
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

package eu.mihosoft.vrlstudio.main;

import eu.mihosoft.vrl.io.ConfigurationFile;
import eu.mihosoft.vrl.io.IOUtil;
import eu.mihosoft.vrl.io.VPropertyFolderManager;
import eu.mihosoft.vrl.system.Messaging;
import eu.mihosoft.vrl.system.RedirectableStream;
import eu.mihosoft.vrl.system.VRL;
import java.awt.Color;
import java.io.File;
import java.io.PrintStream;
import javax.swing.JTextArea;

/**
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
public class LoggingController {

    private static final PrintStream OUT = System.out;
    private static final PrintStream ERR = System.err;
    private JTextArea view;
    private RedirectableStream out;
    private RedirectableStream err;
    private RedirectableStream msgout;
    static final String SHOW_OUT_LOG_KEY = "Log:show-out";
    static final String SHOW_ERR_LOG_KEY = "Log:show-err";

    public LoggingController(JTextArea view) {
        this.view = view;

        initialize();
    }

    private void initialize() {
        out = Messaging.createRedirectableStreamWithView(
                Messaging.STD_OUT, view, OUT, Color.white, false);

        err = Messaging.createRedirectableStreamWithView(
                Messaging.STD_ERR, view, ERR, Color.red, false);
        
        msgout = Messaging.createRedirectableStreamWithView(
                Messaging.MSG_OUT, view, OUT, Color.white, false);
        
        msgout.setRedirectToStdOut(true);
        msgout.setRedirectToUi(true);
        
        out.setRedirectToStdOut(true);
        err.setRedirectToStdOut(true);
        
        System.setOut(out);
        System.setErr(err);

        ConfigurationFile config = IOUtil.newConfigurationFile(
                new File(VRL.getPropertyFolderManager().getEtcFolder(),
                Studio.STUDIO_CONFIG));

        // return if loading is not possible: nothing to initialize
        if (!config.load()) {
            return;
        }

        if (config.containsProperty(SHOW_OUT_LOG_KEY)) {
            boolean enableOut = Boolean.parseBoolean(
                    config.getProperty(SHOW_OUT_LOG_KEY));
            setStdOutEnabled(enableOut);
        }

        if (config.containsProperty(SHOW_ERR_LOG_KEY)) {
            boolean enableErr = Boolean.parseBoolean(
                    config.getProperty(SHOW_ERR_LOG_KEY));
            setStdErrEnabled(enableErr);
        }
    }

    public void setStdOutEnabled(boolean v) {
        out.setRedirectToUi(v);
    }

    public void setStdErrEnabled(boolean v) {
        err.setRedirectToUi(v);
    }
}
