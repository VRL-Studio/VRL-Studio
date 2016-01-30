/* 
 * CanvasConfig.java
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
import eu.mihosoft.vrl.reflection.VisualCanvas;
import eu.mihosoft.vrl.system.VMessage;
import eu.mihosoft.vrl.visual.MessageType;

/**
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
public class CanvasConfig {

    static final String SHOW_INVOCATION_KEY = "Invocation:show-invocation";
    static final String INVOCATION_DELAY_KEY = "Invocation:delay";
    static final String PARAM_EVALUATION_KEY = "Invocation:param-evaluation";
    static final String VISUAL_SAVE_KEY = "Project:visual-save";
    static final String CREATE_VERSION_ON_SAVE_KEY = "Project:create-version-on-save";
    static final String FLUSH_ON_SAVE_KEY = "Project:flush-on-save";
    static final String ENABLE_AUTO_SCROLL_WITH_DRAGGED_CONTENT_KEY = "Canvas:auto-scroll-with-dragged-content:enable";
    static final String AUTO_SCROLL_SENSITIVE_BORDER_SIZE_KEY = "Canvas:auto-scroll-with-dragged-content:sensitive-border-size";
    static final String CANVAS_GRAPHICS_ENGINE_TYPE_KEY="Canvas:graphics-engine:type";
    static final String CANVAS_GRAPHICS_ENGINE_TYPE_DEFAULT="default";
    static final String CANVAS_GRAPHICS_ENGINE_TYPE_GLG2D="glg2d";
    private VisualCanvas canvas;

    public CanvasConfig(VisualCanvas canvas) {
        this.canvas = canvas;
    }

    public void configChanged(String key, String value) {

        if (key.equals(SHOW_INVOCATION_KEY)) {
            Boolean b = Boolean.parseBoolean(value);
            canvas.enableInvokeWaitEffect(b);
        } else if (key.equals(CanvasConfig.INVOCATION_DELAY_KEY)) {
            Long l = Long.parseLong(value);
            canvas.setInvocationDelay(l);
        } else if (key.equals(CanvasConfig.PARAM_EVALUATION_KEY)) {
            Boolean b = Boolean.parseBoolean(value);
            canvas.getEffectPane().setPulseEffectFor(MessageType.INFO, b);
            canvas.getEffectPane().setPulseEffectFor(MessageType.INFO_SINGLE, b);
            canvas.getEffectPane().setPulseEffectFor(MessageType.PLAIN, b);
            canvas.getEffectPane().setPulseEffectFor(MessageType.SILENT, b);
        } else if (key.equals(CanvasConfig.VISUAL_SAVE_KEY)) {
            Boolean b = Boolean.parseBoolean(value);
            canvas.getProjectController().setVisualSaveIndication(b);
        } else if (key.equals(CanvasConfig.CREATE_VERSION_ON_SAVE_KEY)) {
            Boolean b = Boolean.parseBoolean(value);
            Studio.THIS.setCreateVersionOnSave(b);
        } else if (key.equals(CanvasConfig.FLUSH_ON_SAVE_KEY)) {
            Boolean b = Boolean.parseBoolean(value);
            canvas.getProjectController().setFlushOnSave(b);
        } else if (key.equals(CanvasConfig.ENABLE_AUTO_SCROLL_WITH_DRAGGED_CONTENT_KEY)) {
            Boolean b = Boolean.parseBoolean(value);
            canvas.setAutoScrollEnabled(b);
        }  else if (key.equals(CanvasConfig.AUTO_SCROLL_SENSITIVE_BORDER_SIZE_KEY)) {
            Integer i = Integer.parseInt(value);
            canvas.setAutoScrollSensitiveBorderSize(i);
        } else if (key.equals(CanvasConfig.CANVAS_GRAPHICS_ENGINE_TYPE_KEY)) {
            VMessage.info("Graphics Engine Changed", 
                    "Changes are applied after restarting VRL-Studio.");
        }
        
    }

    public void init(ConfigurationFile config) {

        // return if loading is not possible: nothing to initialize
        if (!config.load()) {
            return;
        }

        if (config.containsProperty(CanvasConfig.SHOW_INVOCATION_KEY)) {
            Boolean b = Boolean.parseBoolean(
                    config.getProperty(SHOW_INVOCATION_KEY));
            canvas.enableInvokeWaitEffect(b);
        }

        if (config.containsProperty(CanvasConfig.INVOCATION_DELAY_KEY)) {
            Long l = Long.parseLong(
                    config.getProperty(INVOCATION_DELAY_KEY));
            canvas.setInvocationDelay(l);
        }

        if (config.containsProperty(CanvasConfig.PARAM_EVALUATION_KEY)) {
            Boolean b = Boolean.parseBoolean(
                    config.getProperty(PARAM_EVALUATION_KEY));
            canvas.getEffectPane().setPulseEffectFor(MessageType.INFO, b);
            canvas.getEffectPane().setPulseEffectFor(MessageType.INFO_SINGLE, b);
            canvas.getEffectPane().setPulseEffectFor(MessageType.PLAIN, b);
            canvas.getEffectPane().setPulseEffectFor(MessageType.SILENT, b);
        }

        if (config.containsProperty(CanvasConfig.VISUAL_SAVE_KEY)) {
            Boolean b = Boolean.parseBoolean(
                    config.getProperty(CanvasConfig.VISUAL_SAVE_KEY));
            if (canvas.getProjectController() != null) {
                canvas.getProjectController().setVisualSaveIndication(b);
            }
        }

        if (config.containsProperty(CanvasConfig.CREATE_VERSION_ON_SAVE_KEY)) {
            Boolean b = Boolean.parseBoolean(
                    config.getProperty(CanvasConfig.CREATE_VERSION_ON_SAVE_KEY));
            Studio.THIS.setCreateVersionOnSave(b);
        }

        if (config.containsProperty(CanvasConfig.FLUSH_ON_SAVE_KEY)) {
            Boolean b = Boolean.parseBoolean(
                    config.getProperty(CanvasConfig.FLUSH_ON_SAVE_KEY));

            if (canvas.getProjectController() != null) {
                canvas.getProjectController().setFlushOnSave(b);
            }
        }

        if (config.containsProperty(CanvasConfig.ENABLE_AUTO_SCROLL_WITH_DRAGGED_CONTENT_KEY)) {
            Boolean b = Boolean.parseBoolean(config.getProperty(CanvasConfig.ENABLE_AUTO_SCROLL_WITH_DRAGGED_CONTENT_KEY));

            canvas.setAutoScrollEnabled(b);
        }
        
        if (config.containsProperty(CanvasConfig.AUTO_SCROLL_SENSITIVE_BORDER_SIZE_KEY)) {
            Integer i = Integer.parseInt(config.getProperty(CanvasConfig.AUTO_SCROLL_SENSITIVE_BORDER_SIZE_KEY));
            canvas.setAutoScrollSensitiveBorderSize(i);
        }
    }
}
