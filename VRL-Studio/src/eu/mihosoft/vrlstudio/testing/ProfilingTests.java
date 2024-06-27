/* 
 * ProfilingTests.java
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007–2024 by Michael Hoffer,
 * Copyright (c) 2015–2018 G-CSC, Uni Frankfurt,
 * Copyright (c) 2009–2015 Steinbeis Forschungszentrum (STZ Ölbronn)
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

package eu.mihosoft.vrlstudio.testing;

import eu.mihosoft.vrlstudio.main.Studio;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

/**
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
public class ProfilingTests {

    private Studio studio;

    public void init(String[] args) {
        Studio.main(args);

        while (studio == null) {
            studio = Studio.THIS;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(ProfilingTests.class.getName()).
                        log(Level.SEVERE, null, ex);
            }
            System.err.println("Wait for Studio-Frame initialization...");
        }
    }

    public void loadSessionTest(
            final String fileName,
            final int numLoads,
            final long duration) {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                for (int i = 0; i < numLoads; i++) {
                    System.err.println(">> -------- TEST " + i + " --------");
                    try {
                        Thread.sleep(duration);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ProfilingTests.class.getName()).
                                log(Level.SEVERE, null, ex);
                    }
                    try {
                        SwingUtilities.invokeAndWait(new Runnable() {

                            @Override
                            public void run() {
                                studio.loadSession(fileName);

                            }
                        });

                    } catch (InterruptedException ex) {
                        Logger.getLogger(ProfilingTests.class.getName()).
                                log(Level.SEVERE, null, ex);
                    } catch (InvocationTargetException ex) {
                        Logger.getLogger(ProfilingTests.class.getName()).
                                log(Level.SEVERE, null, ex);
                    }
                } // end for
            } // end run
        }); // end thread

        thread.start();
    }

    public static void main(String[] args) {
        ProfilingTests tests = new ProfilingTests();
        tests.init(args);
        System.err.println("Run tests...");
//        tests.loadSessionTest("src/eu/mihosoft/vrlstudio/testing/memoryprofile01.vrlx", 10, 5000);
        tests.loadSessionTest("/Users/miho/memoryprofile02.vrlx", 300, 10000);
    }
}
