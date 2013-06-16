/* 
 * Constants.java
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

/**
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
public class Constants {

    public static String APP_NAME = "VRL-Studio";
    // version number
    public static final String VERSION_BASE = "0.4.5.5";
    // "HEAD" or "unstable" etc.
    public static final String VERSION_SUFFIX = "-HEAD";
    // final version string: 
    public static final String VERSION = VERSION_BASE + VERSION_SUFFIX;
    public static final String COPYRIGHT = "2007-"
            + /*<VRL_COMPILE_DATE_YEAR>*/ "2013"/*</VRL_COMPILE_DATE_YEAR>*/
            + " by Michael Hoffer"
            + "<br>&<br>2009-"
            + /*<VRL_COMPILE_DATE_YEAR>*/ "2013"/*</VRL_COMPILE_DATE_YEAR>*/
            + " Steinbeis Forschungszentrum (STZ Ölbronn)";
    public static final String COPYRIGHT_SIMPLE = "2007-"
            + /*<VRL_COMPILE_DATE_YEAR>*/ "2013"/*</VRL_COMPILE_DATE_YEAR>*/
            + " by Michael Hoffer";
    public static final String RESOURCES_DIR = "resources/studio-resources/";
}
