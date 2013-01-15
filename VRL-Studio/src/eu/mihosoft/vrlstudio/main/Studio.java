/* 
 * Studio.java
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

import eu.mihosoft.vrl.visual.LoggingController;
import eu.mihosoft.vrl.dialogs.*;
import eu.mihosoft.vrl.io.*;
import eu.mihosoft.vrl.reflection.CodeBlockGenerator;
import eu.mihosoft.vrl.reflection.DefaultMethodRepresentation;
import eu.mihosoft.vrl.reflection.TypeRepresentationBase;
import eu.mihosoft.vrl.reflection.UIWindow;
import eu.mihosoft.vrl.reflection.VisualCanvas;
import eu.mihosoft.vrl.reflection.VisualObject;
import eu.mihosoft.vrl.visual.MessageBox;
import eu.mihosoft.vrl.visual.MessageType;
import eu.mihosoft.vrl.visual.SplashScreenGenerator;
//import eu.mihosoft.vrl.io.PluginController;
import eu.mihosoft.vrl.lang.ShellView;
import eu.mihosoft.vrl.lang.VRLShell;
import eu.mihosoft.vrl.lang.groovy.GroovyCodeEditorComponent;
import eu.mihosoft.vrl.lang.groovy.GroovyCompiler;
import eu.mihosoft.vrl.reflection.ComponentManagement;
import eu.mihosoft.vrl.system.*;
import eu.mihosoft.vrl.visual.*;
import eu.mihosoft.vrlstudio.io.WindowBounds;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.DefaultEditorKit;


/**
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
public class Studio extends javax.swing.JFrame {

    private boolean backgroundGrid = true;
    private boolean enableShadow = true;
    private String defaultSessionName;
//    private String currentSessionName;
    private Dimension nonFullScreenSize;
    private boolean studioInitialized = false;
    private String[] arguments;
    private VRLShell shell = new VRLShell();
    private ShellView shellView;
    private VTextPane shellInput;
    private PresentationView presentationView;
    private VProjectController projectController;
    public static Studio THIS;
    private boolean eventFilterEnabled = false;
    private VisualCanvas mainCanvas = new VisualCanvas();
    static final String STUDIO_CONFIG = "vrl-studio.conf";
    private LoggingController loggingController;
    private static boolean showStartDialog = true;
    /**
     * Indicates whether to automatically create versions on save
     */
    private boolean createVersionOnSave = true;
    PreferenceWindow window;
    private ConfigurationFile studioConfig;
    private VRLUpdater updater;
    private StudioUpdateAction updateStudioAction;

    /**
     * Creates new form Studio
     */
    public Studio() {
        initComponents();

        // used for shell and debugging
        THIS = this;

        canvasScrollPane.getViewport().add(mainCanvas);

        // Shortcuts for menu
        saveSessionItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        loadSessionItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        newSessionItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

        versionManagementMenuItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_V,
                KeyEvent.CTRL_DOWN_MASK | KeyEvent.ALT_DOWN_MASK));

        newComponentMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_N,
                KeyEvent.CTRL_DOWN_MASK | KeyEvent.ALT_DOWN_MASK));

        if (VSysUtil.isMacOSX()) {
            quitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

            VSwingUtil.addEnableDisableAWTEventListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    if (e.getActionCommand().
                            equals(VSwingUtil.EVENT_FILTER_ENABLED_ACTION_CMD)
                            && VDialog.showsDialog()) {
                        getStudioMenuBar().setEnabled(false);
                        eventFilterEnabled = true;
                    } else {
                        getStudioMenuBar().setEnabled(true);
                        eventFilterEnabled = false;
                    }
                }
            });

        }

        preferenceMenuItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_COMMA,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

        canvasScrollPane.getVerticalScrollBar().setUnitIncrement(18);
        canvasScrollPane.getHorizontalScrollBar().setUnitIncrement(18);


        // remove ugly borders
        canvasScrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        canvasScrollPane.setViewportBorder(new EmptyBorder(0, 0, 0, 0));
        splitPane.setBorder(new EmptyBorder(0, 0, 0, 0));

        mainCanvas.setIgnoreMessages(false);

        fullScreenModeItem.setVisible(false);
        exportSessionItem.setVisible(false);

        if (VSysUtil.isMacOSX()) {
            setupMacOSXApplicationListener();
        }

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                quitApplication();
            }

            @Override
            public void windowClosed(WindowEvent e) {
                //
            }
        });

        // ensure that splitpane is resized correctly
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Studio.this.splitPane.setDividerLocation(1.0);
            }
        });

        projectController = createProjectController();

        projectController.initRecentProjectsManager(loadRecentSessionsMenu);
        projectController.initRecentSessionsManager(openRecentComponentsMenu);

        VRL.setCurrentProjectController(projectController);

        shellInput = new VTextPane();
        shellView = new ShellView(shellInput);

        shellScrollPane.getViewport().add(shellView.getEditor());
        shellView.setShell(shell);

        initCanvas(mainCanvas);

//        VShortCutAction searchDialogAction = new VShortCutAction(
//                new VShortCut("Search-Dialog",
//                new VKey(KeyEvent.VK_CONTROL),
//                new VKey(KeyEvent.VK_SPACE))) {
//            @Override
//            public void performAction() {
//
//                // leave this code in release mode to find a bug where
//                // search dialog does not appear
//                System.out.println(">> ctrl-space pressed");
//
//                if (!projectController.getCurrentCanvas().
//                        isIgnoreInput()) {
//
//                    // leave this code in release mode to find a bug where
//                    // search dialog does not appear
//                    System.out.println(" --> toggle search-dialog");
//
//                    ComponentManagement.toggleSearchDialog(
//                            projectController.getCurrentCanvas());
//                }
//            }
//        };

//        VSwingUtil.registerShortCutAction(searchDialogAction);

        VShortCutAction mouseLocationAction = new VShortCutAction(
                new VShortCut("Mouse-Location",
                new VKey(KeyEvent.VK_CONTROL),
                new VKey(KeyEvent.VK_ALT),
                new VKey(KeyEvent.VK_M))) {
            @Override
            public void performAction() {

                if (!projectController.getCurrentCanvas().
                        isIgnoreInput()) {

                    getCurrentCanvas().getEffectPane().
                            toggleMouseLocationIndicatorEnableState();

                }
            }
        };

        VSwingUtil.registerShortCutAction(mouseLocationAction);

//        VShortCutAction keyVisualizerAction = new VShortCutAction(
//                new VShortCut("Key-View",
//                new VKey(KeyEvent.VK_CONTROL),
//                new VKey(KeyEvent.VK_ALT),
//                new VKey(KeyEvent.VK_K))) {
//
//            @Override
//            public void performAction() {
//
//                if (!projectController.getCurrentCanvas().
//                        isIgnoreInput()) {
//                }
//            }
//        };
//
//        VSwingUtil.registerShortCutAction(keyVisualizerAction);


        mainCanvas.getEffectPane().startSpot();
        mainCanvas.setIgnoreInput(true);

        final JTextArea logView = new JTextArea();
        logView.setBackground(Color.BLACK);
        logView.setForeground(Color.white);
        logView.setEditable(false);
        logScrollPane.getViewport().add(logView);

        ConfigurationFile config = IOUtil.newConfigurationFile(
                new File(VRL.getPropertyFolderManager().getEtcFolder(),
                Studio.STUDIO_CONFIG));

        loggingController = new LoggingController(logView, config);

        // exclude bottompane (log and shell) from event blocking
        VSwingUtil.addContainerToEventFilter(bottomPane);

        bottomPane.setMinimumSize(new Dimension(0, 0));

        // add menu to log
        final JPopupMenu logMenu = new JPopupMenu("Log");

        JMenuItem deleteLogItem = new JMenuItem("Clear log");

        logMenu.add(deleteLogItem);

        deleteLogItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logView.setText("");
            }
        });

        logView.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    logMenu.show(logView, e.getX(), e.getY());
                }
            }
        });

        // add shortcuts for log
        if (VSysUtil.isMacOSX()) {

            InputMap keyMap = logView.getInputMap();

            KeyStroke keyC = KeyStroke.getKeyStroke(KeyEvent.VK_C,
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
            KeyStroke keyV = KeyStroke.getKeyStroke(KeyEvent.VK_V,
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
            KeyStroke keyX = KeyStroke.getKeyStroke(KeyEvent.VK_X,
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
            KeyStroke keyA = KeyStroke.getKeyStroke(KeyEvent.VK_A,
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());


            keyMap.put(keyC, DefaultEditorKit.copyAction);
            keyMap.put(keyV, DefaultEditorKit.pasteAction);
            keyMap.put(keyX, DefaultEditorKit.cutAction);
            keyMap.put(keyA, DefaultEditorKit.selectAllAction);
        }

        if (VSysUtil.isMacOSX()) {
            VSwingUtil.forceAppleLAF(studioMenuBar);
            VSwingUtil.forceNimbusLAF(splitPane);
        } else {
            VSwingUtil.forceNimbusLAF(this);
        }


        // we need to restrict access to versions due to plagiarism
        deleteAllVersionsMenuItem.setVisible(false);


        initUpdater();
    }

    private void autoUpdate(ConfigurationFile config) {
        
        boolean containsAutoUpdateKey = config.containsProperty(
                PreferenceWindow.CHECK_FOR_UPDATES_ON_STARTUP_KEY);
        
        boolean autoUpdateEnabled = Boolean.valueOf(config.getProperty(
                PreferenceWindow.CHECK_FOR_UPDATES_ON_STARTUP_KEY));
        
        if (!containsAutoUpdateKey || autoUpdateEnabled) {
            checkForUpdates();
        }
            
    }

    private VProjectController createProjectController() {
        return new VProjectController(canvasScrollPane.getViewport(),
                new LoadCanvasConfigurator(this));
    }

    /**
     * Returns the VRL changelog as string.
     *
     * @return the VRL changelog as string
     */
    public static String getChangelog() {
        return IOUtil.readResourceTextFile(
                "/eu/mihosoft/vrlstudio/resources/changelog/changelog.txt");
    }

    /**
     * @return the createVersionOnSave
     */
    public boolean isCreateVersionOnSave() {
        return createVersionOnSave;
    }

    /**
     * @param createVersionOnSave the createVersionOnSave to set
     */
    public void setCreateVersionOnSave(boolean createVersionOnSave) {
        this.createVersionOnSave = createVersionOnSave;
        projectController.setCommitOnSave(createVersionOnSave);
    }

    private void deactivateAllEvents(Canvas canvas) {
        VSwingUtil.deactivateEventFilter();
        VSwingUtil.activateEventFilter(
                canvas, canvas.getMessageBox(), canvas.getDock());
    }

    private void activateAllEvents() {
        VSwingUtil.deactivateEventFilter();
    }

    private void showStartDialog(VisualCanvas canvas) {

        int answer = VDialog.showConfirmDialog(canvas, "Start Dialog",
                "Create or load a project to begin.",
                new String[]{"Create", "Load", "Cancel"});

        if (answer == 0) {
            VSwingUtil.invokeLater(new Runnable() {
                @Override
                public void run() {
                    newSessionItemActionPerformed(null);
                }
            });

        } else if (answer == 1) {
            VSwingUtil.invokeLater(new Runnable() {
                @Override
                public void run() {
                    loadSessionItemActionPerformed(null);
                }
            });

        } else if (answer == 2) {
            // nothing to do
        }
    }

    public final void initCanvas(VisualCanvas canvas) {

        if (presentationView != null) {
            presentationView.dispose(); // important
        }

        presentationView = new PresentationView();
        presentationView.setMainCanvas(canvas);

        canvas.addPaintListener(presentationView);

        canvas.getWindowGroupController().
                initController(showGroupMenu, removeGroupMenu);

        updateMenuItems();

        // init from config
        CanvasConfig config = new CanvasConfig(canvas);

        ConfigurationFile configFile = IOUtil.newConfigurationFile(
                new File(VRL.getPropertyFolderManager().getEtcFolder(), STUDIO_CONFIG));

        config.init(configFile);

        // shell
        initShell(canvas);

        // EXPERIMENTAL FEATURE! LOTS OF PERFORMANCE ISSUES!
//        if (loggingController != null) {
//            LogBackground logBackground = new LogBackground(canvas);
//            loggingController.setLogBackground(logBackground);
//            canvas.add(logBackground);
//        }
    }

    public VisualCanvas getCurrentCanvas() {
        return projectController.getCurrentCanvas();
    }

    public final void initShell(VisualCanvas canvas) {

        shellView.quit();

        shell.init(canvas.getClassLoader());
        GroovyCompiler compiler = new GroovyCompiler();
        shell.addImports(compiler.getImports());
        shell.addImport("import eu.mihosoft.vrl.user.*;");
        shellInput.clear();
        shell.addConstant("canvas", canvas);
        shell.addConstant("inspector", canvas.getInspector());
        shell.addConstant("classloader", canvas.getClassLoader());
        shell.addConstant("studio", THIS);
        shell.addConstant("project", projectController);
        shell.addConstant("clipboard", canvas.getClipBoard());
        shell.addConstant("typeFactory", canvas.getTypeFactory());
        shell.addConstant("windows", canvas.getWindows());
        shell.addConstant("animationManager", canvas.getAnimationManager());
        shell.addConstant("effectpane", canvas.getEffectPane());
        shell.run();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        splitPane = new javax.swing.JSplitPane();
        canvasScrollPane = new javax.swing.JScrollPane();
        bottomPane = new javax.swing.JTabbedPane();
        logScrollPane = new javax.swing.JScrollPane();
        shellScrollPane = new javax.swing.JScrollPane();
        studioMenuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        newSessionItem = new javax.swing.JMenuItem();
        fileTemplatesMenu = new javax.swing.JMenu();
        loadSessionItem = new javax.swing.JMenuItem();
        loadRecentSessionsMenu = new javax.swing.JMenu();
        saveSessionItem = new javax.swing.JMenuItem();
        saveSessionWithMsgItem = new javax.swing.JMenuItem();
        saveAsItem = new javax.swing.JMenuItem();
        exportProjectItem = new javax.swing.JMenuItem();
        jSeparator13 = new javax.swing.JPopupMenu.Separator();
        DefaultProjectMenu = new javax.swing.JMenu();
        saveAsDefaultItem = new javax.swing.JMenuItem();
        resetDefaultProjectItem = new javax.swing.JMenuItem();
        jSeparator10 = new javax.swing.JPopupMenu.Separator();
        projectMenu = new javax.swing.JMenu();
        addPluginConfiguratorMenuItem = new javax.swing.JMenuItem();
        exportProjectasLibraryMenuItem = new javax.swing.JMenuItem();
        manageLibrariesMenuItem = new javax.swing.JMenuItem();
        jSeparator9 = new javax.swing.JPopupMenu.Separator();
        compileProjectMenuItem = new javax.swing.JMenuItem();
        selectPluginsMenuItem = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        versionManagementMenuItem = new javax.swing.JMenuItem();
        deleteAllVersionsMenuItem = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        manageComponentsItem = new javax.swing.JMenuItem();
        newComponentMenuItem = new javax.swing.JMenuItem();
        openRecentComponentsMenu = new javax.swing.JMenu();
        jSeparator8 = new javax.swing.JPopupMenu.Separator();
        importSessionItem = new javax.swing.JMenuItem();
        exportSessionItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        quitItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        groupSelectedComponentsItem = new javax.swing.JMenuItem();
        preferenceMenuItem = new javax.swing.JMenuItem();
        viewMenu = new javax.swing.JMenu();
        windowMenu = new javax.swing.JMenu();
        showGroupMenu = new javax.swing.JMenu();
        jMenuItem9 = new javax.swing.JMenuItem();
        jMenuItem8 = new javax.swing.JMenuItem();
        showNextGroupMenuItem = new javax.swing.JMenuItem();
        showPreviousGroupMenuItem = new javax.swing.JMenuItem();
        removeGroupMenu = new javax.swing.JMenu();
        jSeparator3 = new javax.swing.JSeparator();
        styleMenu = new javax.swing.JMenu();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        startPresentationMenuItem = new javax.swing.JCheckBoxMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        showGridItem = new javax.swing.JCheckBoxMenuItem();
        enableShadowItem = new javax.swing.JCheckBoxMenuItem();
        fullScreenModeItem = new javax.swing.JCheckBoxMenuItem();
        jSeparator14 = new javax.swing.JPopupMenu.Separator();
        showLogInWindowItem = new javax.swing.JMenuItem();
        pluginMenu = new javax.swing.JMenu();
        installPluginMenuItem = new javax.swing.JMenuItem();
        uninstallPluginMenu = new javax.swing.JMenu();
        jSeparator7 = new javax.swing.JPopupMenu.Separator();
        toolMenu = new javax.swing.JMenu();
        debugMenu = new javax.swing.JMenu();
        showMemoryUsageMenuItem = new javax.swing.JCheckBoxMenuItem();
        systemGCMenuItem = new javax.swing.JMenuItem();
        showRepaintAreasMenuItem = new javax.swing.JCheckBoxMenuItem();
        helpMenu = new javax.swing.JMenu();
        whatIsVRLStudio = new javax.swing.JMenuItem();
        creatingYourFirstProject = new javax.swing.JMenuItem();
        definingAWorkflow = new javax.swing.JMenuItem();
        versionManagement = new javax.swing.JMenuItem();
        usingTheShellItem = new javax.swing.JMenuItem();
        DebuggingItem = new javax.swing.JMenuItem();
        jSeparator11 = new javax.swing.JPopupMenu.Separator();
        pluginHelpIndexItem = new javax.swing.JMenuItem();
        infoMenu = new javax.swing.JMenu();
        aboutVRLStudioItem = new javax.swing.JMenuItem();
        aboutVRLItem = new javax.swing.JMenuItem();
        jSeparator12 = new javax.swing.JPopupMenu.Separator();
        showChangelogItem = new javax.swing.JMenuItem();
        showStudioChangelogItem = new javax.swing.JMenuItem();
        jSeparator15 = new javax.swing.JPopupMenu.Separator();
        aboutMenuItem = new javax.swing.JMenuItem();

        jMenu1.setText("File");
        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");
        jMenuBar1.add(jMenu2);

        jMenuItem1.setText("jMenuItem1");

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("VRL-Studio");
        setLocationByPlatform(true);

        splitPane.setDividerLocation(600);
        splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(1.0);
        splitPane.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        splitPane.setMinimumSize(new java.awt.Dimension(0, 0));

        canvasScrollPane.setAutoscrolls(true);
        canvasScrollPane.setMinimumSize(new java.awt.Dimension(0, 0));
        splitPane.setTopComponent(canvasScrollPane);

        logScrollPane.setBackground(new java.awt.Color(0, 0, 0));
        logScrollPane.setMinimumSize(new java.awt.Dimension(0, 0));
        bottomPane.addTab("Log", logScrollPane);

        shellScrollPane.setBackground(new java.awt.Color(0, 0, 0));
        shellScrollPane.setMinimumSize(new java.awt.Dimension(0, 0));
        bottomPane.addTab("Shell", shellScrollPane);

        splitPane.setRightComponent(bottomPane);

        studioMenuBar.setBorder(null);

        fileMenu.setText("File");

        newSessionItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        newSessionItem.setText("New Project");
        newSessionItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newSessionItemActionPerformed(evt);
            }
        });
        fileMenu.add(newSessionItem);

        fileTemplatesMenu.setText("New Project from Template");
        fileMenu.add(fileTemplatesMenu);

        loadSessionItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.CTRL_MASK));
        loadSessionItem.setText("Load Project");
        loadSessionItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadSessionItemActionPerformed(evt);
            }
        });
        fileMenu.add(loadSessionItem);

        loadRecentSessionsMenu.setText("Load Recent Project...");
        fileMenu.add(loadRecentSessionsMenu);

        saveSessionItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        saveSessionItem.setText("Save Project");
        saveSessionItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveSessionItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveSessionItem);

        saveSessionWithMsgItem.setText("Save Project (with message)");
        saveSessionWithMsgItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveSessionWithMsgItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveSessionWithMsgItem);

        saveAsItem.setText("Save Project As...");
        saveAsItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveAsItem);

        exportProjectItem.setText("Export Project");
        exportProjectItem.setToolTipText("Exports the current project with all plugin dependencies.");
        exportProjectItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportProjectItemActionPerformed(evt);
            }
        });
        fileMenu.add(exportProjectItem);
        fileMenu.add(jSeparator13);

        DefaultProjectMenu.setText("Default Project");

        saveAsDefaultItem.setText("Define Current Project as Default ");
        saveAsDefaultItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsDefaultItemActionPerformed(evt);
            }
        });
        DefaultProjectMenu.add(saveAsDefaultItem);

        resetDefaultProjectItem.setText("Reset Default Project (factory settings)");
        resetDefaultProjectItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetDefaultProjectItemActionPerformed(evt);
            }
        });
        DefaultProjectMenu.add(resetDefaultProjectItem);

        fileMenu.add(DefaultProjectMenu);
        fileMenu.add(jSeparator10);

        projectMenu.setText("Development");

        addPluginConfiguratorMenuItem.setText("Create Plugin Configurator");
        addPluginConfiguratorMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addPluginConfiguratorMenuItemActionPerformed(evt);
            }
        });
        projectMenu.add(addPluginConfiguratorMenuItem);

        exportProjectasLibraryMenuItem.setText("Export Project as Library");
        exportProjectasLibraryMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportProjectasLibraryMenuItemActionPerformed(evt);
            }
        });
        projectMenu.add(exportProjectasLibraryMenuItem);

        manageLibrariesMenuItem.setText("Manage Libraries");
        manageLibrariesMenuItem.setEnabled(false);
        manageLibrariesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manageLibrariesMenuItemActionPerformed(evt);
            }
        });
        projectMenu.add(manageLibrariesMenuItem);
        projectMenu.add(jSeparator9);

        compileProjectMenuItem.setText("Build Project");
        compileProjectMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                compileProjectMenuItemActionPerformed(evt);
            }
        });
        projectMenu.add(compileProjectMenuItem);

        fileMenu.add(projectMenu);

        selectPluginsMenuItem.setText("Select Plugins");
        selectPluginsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectPluginsMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(selectPluginsMenuItem);
        fileMenu.add(jSeparator5);

        versionManagementMenuItem.setText("Manage Versions");
        versionManagementMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                versionManagementMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(versionManagementMenuItem);

        deleteAllVersionsMenuItem.setText("Delete History");
        deleteAllVersionsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteAllVersionsMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(deleteAllVersionsMenuItem);
        fileMenu.add(jSeparator6);

        manageComponentsItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_SPACE, java.awt.event.InputEvent.CTRL_MASK));
        manageComponentsItem.setText("Manage Components");
        manageComponentsItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manageComponentsItemActionPerformed(evt);
            }
        });
        fileMenu.add(manageComponentsItem);

        newComponentMenuItem.setText("New Component");
        newComponentMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newComponentMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(newComponentMenuItem);

        openRecentComponentsMenu.setText("Open Recent Component...");
        fileMenu.add(openRecentComponentsMenu);
        fileMenu.add(jSeparator8);

        importSessionItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, java.awt.event.InputEvent.CTRL_MASK));
        importSessionItem.setText("Import Session");
        importSessionItem.setEnabled(false);
        importSessionItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importSessionItemActionPerformed(evt);
            }
        });
        fileMenu.add(importSessionItem);

        exportSessionItem.setText("Export Application");
        exportSessionItem.setEnabled(false);
        exportSessionItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportSessionItemActionPerformed(evt);
            }
        });
        fileMenu.add(exportSessionItem);
        fileMenu.add(jSeparator1);

        quitItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.ALT_MASK));
        quitItem.setText("Quit");
        quitItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quitItemActionPerformed(evt);
            }
        });
        fileMenu.add(quitItem);

        studioMenuBar.add(fileMenu);

        editMenu.setText("Edit");

        groupSelectedComponentsItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_G, java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        groupSelectedComponentsItem.setText("Group selected Components");
        groupSelectedComponentsItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                groupSelectedComponentsItemActionPerformed(evt);
            }
        });
        editMenu.add(groupSelectedComponentsItem);

        preferenceMenuItem.setText("Preferences...");
        preferenceMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                preferenceMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(preferenceMenuItem);

        studioMenuBar.add(editMenu);

        viewMenu.setText("View");

        windowMenu.setText("Window Groups");

        showGroupMenu.setText("Show/Hide Group");
        windowMenu.add(showGroupMenu);

        jMenuItem9.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem9.setText("Show all Groups");
        jMenuItem9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem9ActionPerformed(evt);
            }
        });
        windowMenu.add(jMenuItem9);

        jMenuItem8.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_H, java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem8.setText("Hide all Groups");
        jMenuItem8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem8ActionPerformed(evt);
            }
        });
        windowMenu.add(jMenuItem8);

        showNextGroupMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_RIGHT, java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        showNextGroupMenuItem.setText("Show next Group");
        showNextGroupMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showNextGroupMenuItemActionPerformed(evt);
            }
        });
        windowMenu.add(showNextGroupMenuItem);

        showPreviousGroupMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_LEFT, java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        showPreviousGroupMenuItem.setText("Show previous Group");
        showPreviousGroupMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showPreviousGroupMenuItemActionPerformed(evt);
            }
        });
        windowMenu.add(showPreviousGroupMenuItem);

        removeGroupMenu.setText("Remove Group");
        windowMenu.add(removeGroupMenu);

        viewMenu.add(windowMenu);
        viewMenu.add(jSeparator3);

        styleMenu.setText("Styles");
        viewMenu.add(styleMenu);
        viewMenu.add(jSeparator2);

        startPresentationMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F5, 0));
        startPresentationMenuItem.setText("Start Presentation");
        startPresentationMenuItem.setEnabled(false);
        startPresentationMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startPresentationMenuItemActionPerformed(evt);
            }
        });
        viewMenu.add(startPresentationMenuItem);
        viewMenu.add(jSeparator4);

        showGridItem.setSelected(true);
        showGridItem.setText("Show Grid");
        showGridItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showGridItemActionPerformed(evt);
            }
        });
        viewMenu.add(showGridItem);

        enableShadowItem.setSelected(true);
        enableShadowItem.setText("Enable Shadow");
        enableShadowItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableShadowItemActionPerformed(evt);
            }
        });
        viewMenu.add(enableShadowItem);

        fullScreenModeItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        fullScreenModeItem.setText("Full Screen Mode");
        fullScreenModeItem.setEnabled(false);
        fullScreenModeItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fullScreenModeItemActionPerformed(evt);
            }
        });
        viewMenu.add(fullScreenModeItem);
        viewMenu.add(jSeparator14);

        showLogInWindowItem.setText("Show Log In Window");
        showLogInWindowItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showLogInWindowItemActionPerformed(evt);
            }
        });
        viewMenu.add(showLogInWindowItem);

        studioMenuBar.add(viewMenu);

        pluginMenu.setText("Plugins");

        installPluginMenuItem.setText("Install Plugin");
        installPluginMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                installPluginMenuItemActionPerformed(evt);
            }
        });
        pluginMenu.add(installPluginMenuItem);

        uninstallPluginMenu.setText("Uninstall Plugin");
        pluginMenu.add(uninstallPluginMenu);
        pluginMenu.add(jSeparator7);

        studioMenuBar.add(pluginMenu);

        toolMenu.setText("Tools");
        toolMenu.setEnabled(false);
        studioMenuBar.add(toolMenu);

        debugMenu.setText("Debug");

        showMemoryUsageMenuItem.setText("Show Memory Usage");
        showMemoryUsageMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showMemoryUsageMenuItemActionPerformed(evt);
            }
        });
        debugMenu.add(showMemoryUsageMenuItem);

        systemGCMenuItem.setText("Call System.gc()");
        systemGCMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                systemGCMenuItemActionPerformed(evt);
            }
        });
        debugMenu.add(systemGCMenuItem);

        showRepaintAreasMenuItem.setText("Show Repaint Areas");
        showRepaintAreasMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showRepaintAreasMenuItemActionPerformed(evt);
            }
        });
        debugMenu.add(showRepaintAreasMenuItem);

        studioMenuBar.add(debugMenu);

        helpMenu.setText("Help");

        whatIsVRLStudio.setText("What is VRL-Studio?");
        whatIsVRLStudio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                whatIsVRLStudioActionPerformed(evt);
            }
        });
        helpMenu.add(whatIsVRLStudio);

        creatingYourFirstProject.setText("Creating Your First Project");
        creatingYourFirstProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                creatingYourFirstProjectActionPerformed(evt);
            }
        });
        helpMenu.add(creatingYourFirstProject);

        definingAWorkflow.setText("Defining A Workflow");
        definingAWorkflow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                definingAWorkflowActionPerformed(evt);
            }
        });
        helpMenu.add(definingAWorkflow);

        versionManagement.setText("Version Management");
        versionManagement.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                versionManagementActionPerformed(evt);
            }
        });
        helpMenu.add(versionManagement);

        usingTheShellItem.setText("Using The Shell");
        usingTheShellItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                usingTheShellItemActionPerformed(evt);
            }
        });
        helpMenu.add(usingTheShellItem);

        DebuggingItem.setText("Debugging");
        DebuggingItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DebuggingItemActionPerformed(evt);
            }
        });
        helpMenu.add(DebuggingItem);
        helpMenu.add(jSeparator11);

        pluginHelpIndexItem.setText("Plugin Help Index");
        pluginHelpIndexItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pluginHelpIndexItemActionPerformed(evt);
            }
        });
        helpMenu.add(pluginHelpIndexItem);

        studioMenuBar.add(helpMenu);

        infoMenu.setText("Info");

        aboutVRLStudioItem.setText("About VRL-Studio");
        aboutVRLStudioItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutVRLStudioItemActionPerformed(evt);
            }
        });
        infoMenu.add(aboutVRLStudioItem);

        aboutVRLItem.setText("About VRL ");
        aboutVRLItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutVRLItemActionPerformed(evt);
            }
        });
        infoMenu.add(aboutVRLItem);
        infoMenu.add(jSeparator12);

        showChangelogItem.setText("VRL Changelog");
        showChangelogItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showChangelogItemActionPerformed(evt);
            }
        });
        infoMenu.add(showChangelogItem);

        showStudioChangelogItem.setText("VRL-Studio Changelog");
        showStudioChangelogItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showStudioChangelogItemActionPerformed(evt);
            }
        });
        infoMenu.add(showStudioChangelogItem);
        infoMenu.add(jSeparator15);

        aboutMenuItem.setText("Copyright Notice & Version Info");
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });
        infoMenu.add(aboutMenuItem);

        studioMenuBar.add(infoMenu);

        setJMenuBar(studioMenuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(splitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 585, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(splitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 385, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void newSessionItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newSessionItemActionPerformed

        updateMenuItems();

        setShowGridState(true);

        // location of the new session
        File newFile = null;

        FileDialogManager manager = new FileDialogManager();
        VProjectSessionCreator saver =
                new VProjectSessionCreator(getDefaultSessionName());
        manager.saveFile(this, projectController, saver,
                new ProjectFileFilter());

        // set new project file 
        VProject p = projectController.getProject();
        if (p != null) {
            newFile = p.getFile();
        }

        // if session filename is defined and project has been created
        if (saver.createdProject()) {
            try {
                projectController.loadProject(newFile, false);
            } catch (IOException ex) {

                Logger.getLogger(Studio.class.getName()).
                        log(Level.SEVERE, null, ex);
            }
        } // end if saver.createdProject()

    }//GEN-LAST:event_newSessionItemActionPerformed

    void updateAppTitle() {
        if (projectController.getProject() != null) {
            setTitle(Constants.APP_NAME + ": "
                    + projectController.getProject().
                    getFile().getAbsolutePath() + ", Component: "
                    + projectController.getCurrentSession());
        } else {
            setTitle(Constants.APP_NAME);
        }
    }

    private void saveSessionItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveSessionItemActionPerformed

        if (!projectController.isProjectOpened()) {
            VDialog.showMessageDialog(getCurrentCanvas(), "No Project opened",
                    "Open a project.");
            return;
        }

//        projectController.setCommitOnSave(isCreateVersionOnSave());

        try {
            projectController.saveProject(false);
            VersionManagement.closeDialog(getCurrentCanvas());
        } catch (IOException ex) {
            Logger.getLogger(Studio.class.getName()).
                    log(Level.SEVERE, null, ex);
        }


    }//GEN-LAST:event_saveSessionItemActionPerformed

    private void updateMenuItems() {
        showMemoryUsageMenuItem.setSelected(false);
    }

    private void initUpdater() {
        PluginIdentifier identifier =
                new PluginIdentifier("VRL-Studio",
                new VersionInfo(Constants.VERSION_BASE));

        updater = new VRLUpdater(identifier);
        
        updateStudioAction = new StudioUpdateAction();
    }

    void checkForUpdates() {

        if (updater.isDownloadingRepository()
                || updater.isDownloadingUpdate()) {
            return;
        }
       
        updater.checkForUpdates(updateStudioAction);

    }

//    private void updateStyleMenu(final Canvas canvas) {
//        // TODO fix this for VRL v0.4 release! DO NOT USE TIMER HERE!!!
//        TimerTask tt = new TimerTask() {
//
//            @Override
//            public void run() {
//                setShowGridState((Boolean) canvas.getStyle().
//                        getBaseValues().get(
//                        CanvasGrid.ENABLE_GRID_KEY));
//
//                boolean shadowEnabled = true;
//
//                if (canvas.getStyle().getBaseValues().getFloat(
//                        ShadowBorder.SHADOW_TRANSPARENCY_KEY) == 0.f) {
//                    shadowEnabled = false;
//                }
//
//                enableShadowItem.setSelected(shadowEnabled);
//            }
//        };
//
//        Timer t = new Timer();
//        t.schedule(tt, 2000);
//    }
    public boolean loadSession(String fileName) {

        try {
            VProjectSessionLoader sessionLoader =
                    new VProjectSessionLoader(projectController);

            sessionLoader.loadFile(new File(fileName));
            updateAppTitle();

            //        throw new UnsupportedOperationException("implementation deactivated!");
        } catch (IOException ex) {
            Logger.getLogger(Studio.class.getName()).
                    log(Level.SEVERE, null, ex);

            return false;
        }

        return true;
    }

    private void loadSessionItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadSessionItemActionPerformed

        File directory = null;

        if (projectController.getProject() != null
                && !projectController.getProject().getFile().getAbsolutePath().
                equals(defaultSessionName)) {

            directory = projectController.getProject().getFile().
                    getAbsoluteFile().getParentFile();
        }

        LoadProjectDialog.showDialog(
                this, projectController,
                directory);
    }//GEN-LAST:event_loadSessionItemActionPerformed

    private void groupSelectedComponentsItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_groupSelectedComponentsItemActionPerformed

        boolean onlyTypes = getCurrentCanvas().getClipBoard().
                containsOnlyItemsWithSuperClass(TypeRepresentationBase.class);

        boolean onlyMethods = getCurrentCanvas().getClipBoard().
                containsOnlyItemsOfClass(DefaultMethodRepresentation.class);

        boolean onlyWindows = true;

        for (Class c : getCurrentCanvas().getClipBoard().getClassObjects()) {
            boolean isNoVisualObject = !VisualObject.class.isAssignableFrom(c);
            boolean isNoCustomUI = !UIWindow.class.isAssignableFrom(c);

            if (isNoVisualObject && isNoCustomUI) {
                onlyWindows = false;
                break;
            }
        }

        onlyWindows = onlyWindows && !getCurrentCanvas().getClipBoard().isEmpty();

        if (onlyTypes) {
            UIWindow window = UIWindow.createFromClipboard(
                    "Custom UI", getCurrentCanvas());
            if (window.getContentProvider() != null) {
                getCurrentCanvas().getWindows().add(window);
                getCurrentCanvas().getWindows().setActive(window);
            }
        } else if (onlyMethods) {
            try {
                CodeBlockGenerator generator = new CodeBlockGenerator();
                generator.generateCodeblock(getCurrentCanvas());
            } catch (InstantiationException ex) {
                Logger.getLogger(Studio.class.getName()).
                        log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(Studio.class.getName()).
                        log(Level.SEVERE, null, ex);
            }
        } else if (getCurrentCanvas().getClipBoard().isEmpty()) {
            MessageBox mBox = getCurrentCanvas().getMessageBox();
            mBox.addUniqueMessage("Can't group components:",
                    "Please select at least one component!", null,
                    MessageType.ERROR);
        } else if (onlyWindows) {
            getCurrentCanvas().getWindowGroupController().
                    createGroupFromSelectedWindows();
        } else {
            MessageBox mBox = getCurrentCanvas().getMessageBox();
            mBox.addUniqueMessage("Can't group components:",
                    "Only components of same type can be grouped!"
                    + "<ul>"
                    + " <li>Select windows to define a window group "
                    + "(code windows and non VRL windows are not supported)."
                    + "</li>"
                    + " <li>Select methods to define a code block.</li>"
                    + " <li>Select parameters to group parameters"
                    + " in a new window.</li>"
                    + "</ul>", null, MessageType.ERROR);
        }
    }//GEN-LAST:event_groupSelectedComponentsItemActionPerformed

    private void jMenuItem8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem8ActionPerformed
        getCurrentCanvas().getWindowGroupController().hideAllGroups();
    }//GEN-LAST:event_jMenuItem8ActionPerformed

    private void jMenuItem9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem9ActionPerformed
        getCurrentCanvas().getWindowGroupController().showAllGroups();
    }//GEN-LAST:event_jMenuItem9ActionPerformed

    private void showGridItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showGridItemActionPerformed

        setShowGridState(showGridItem.isSelected());
    }//GEN-LAST:event_showGridItemActionPerformed

    private void setShowGridState(boolean b) {
        backgroundGrid = b;
        showGridItem.setSelected(b);
        getCurrentCanvas().getStyleManager().putBaseValue(
                CanvasGrid.ENABLE_GRID_KEY, b);
    }

    private void exportSessionItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportSessionItemActionPerformed
        //
//        if (currentSessionName != null) {
//            ExportSessionDialog dialog = new ExportSessionDialog();
//
//            dialog.setAlwaysOnTop(true);
//
//            dialog.setMainCanvas(mainCanvas);
//            dialog.setCurrentFileName(currentSessionName);
//            dialog.setParentFrame(this);
//            dialog.setVisible(true);
//
//            dialog.centerDialog(this);
//
//        } else {
//            mainCanvas.getMessageBox().addMessage("Can't export session:",
//                    "Please save the current session first to export it!",
//                    MessageType.ERROR);
//        }
    }//GEN-LAST:event_exportSessionItemActionPerformed

    private void quitItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quitItemActionPerformed
        //
        quitApplication();
    }//GEN-LAST:event_quitItemActionPerformed

    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
        //
        showAboutDialog();
    }//GEN-LAST:event_aboutMenuItemActionPerformed

    private void showNextGroupMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showNextGroupMenuItemActionPerformed
        //
        getCurrentCanvas().getWindowGroupController().showNextGroup();
    }//GEN-LAST:event_showNextGroupMenuItemActionPerformed

    private void showPreviousGroupMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showPreviousGroupMenuItemActionPerformed
        //
        getCurrentCanvas().getWindowGroupController().showPreviousGroup();
    }//GEN-LAST:event_showPreviousGroupMenuItemActionPerformed

    private void enableShadowItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableShadowItemActionPerformed
        //
        enableShadow = enableShadowItem.isSelected();

        if (enableShadow) {
            // TODO don't use static value 0.45f because this does override
            // the transparency defined by the style
            getCurrentCanvas().getStyleManager().putBaseValue(
                    ShadowBorder.SHADOW_TRANSPARENCY_KEY, 0.45f);
        } else {
            getCurrentCanvas().getStyleManager().putBaseValue(
                    ShadowBorder.SHADOW_TRANSPARENCY_KEY, 0.0f);
        }
    }//GEN-LAST:event_enableShadowItemActionPerformed

    private void fullScreenModeItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fullScreenModeItemActionPerformed
        //
        if (fullScreenModeItem.isSelected()) {
//            nonFullScreenSize = getSize();
//            setPreferredSize(Toolkit.getDefaultToolkit().getScreenSize());
//            pack();
//            setResizable(false);
//            setVisible(true);
//
//            SwingUtilities.invokeLater(new Runnable() {
//
//                @Override
//                public void run() {
//                    Point p = new Point(0, 0);
//                    SwingUtilities.convertPointToScreen(p, getContentPane());
//                    Point l = getLocation();
//                    l.x -= p.x;
//                    l.y -= p.y;
//                    setLocation(l);
//                }
//            });
            VGraphicsUtil.enterFullscreenMode(this);
        } else {
//            setPreferredSize(nonFullScreenSize);
//            pack();
//            setResizable(true);
//            setVisible(true);
            VGraphicsUtil.leaveFullscreenMode(this);
        }

    }//GEN-LAST:event_fullScreenModeItemActionPerformed

    private void showMemoryUsageMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showMemoryUsageMenuItemActionPerformed
        //
        getCurrentCanvas().showMemoryDisplay(showMemoryUsageMenuItem.isSelected());
    }//GEN-LAST:event_showMemoryUsageMenuItemActionPerformed

    private void systemGCMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_systemGCMenuItemActionPerformed
        // TODO add your handling code here:
        System.gc();
    }//GEN-LAST:event_systemGCMenuItemActionPerformed

    private void importSessionItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importSessionItemActionPerformed
        // TODO add your handling code here:

        if (ImportCanvasXMLSessionDialog.showDialog(mainCanvas)) {
            mainCanvas.getMessageBox().addMessage("Session Import:",
                    ">> the selected session has been successfully imported.",
                    MessageType.INFO);
        }
    }//GEN-LAST:event_importSessionItemActionPerformed

    private void showRepaintAreasMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showRepaintAreasMenuItemActionPerformed
        // TODO add your handling code here:
        getCurrentCanvas().showRepaintAreas(showRepaintAreasMenuItem.isSelected());
        getCurrentCanvas().repaint();
    }//GEN-LAST:event_showRepaintAreasMenuItemActionPerformed

    private void startPresentationMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startPresentationMenuItemActionPerformed
        // TODO add your handling code here:
        if (startPresentationMenuItem.isSelected()) {
            int numberOfScreens = VGraphicsUtil.getNumberOfScreens();

            if (numberOfScreens < 2) {
                System.err.println("Only one screen found.");
                getCurrentCanvas().getMessageBox().addUniqueMessage(
                        "Cannot start Presentation:",
                        ">> only one screen found."
                        + " Please connect a second monitor and/or change your"
                        + " screen configuration.",
                        null, MessageType.ERROR);
                startPresentationMenuItem.setSelected(false);
            } else {
                getCurrentCanvas().enableCaptureBuffer(true);
                int presentationScreenID = 1;

                if (VGraphicsUtil.getScreenId(this) == 1) {
                    presentationScreenID = 0;
                }

                getCurrentCanvas().repaint();

                VGraphicsUtil.enterFullscreenMode(
                        presentationView, presentationScreenID, false);

                getCurrentCanvas().repaint();
                this.requestFocus();
                getCurrentCanvas().repaint();
            }
        } else {
            presentationView.setVisible(false);
            getCurrentCanvas().enableCaptureBuffer(false);
        }
    }//GEN-LAST:event_startPresentationMenuItemActionPerformed

private void newComponentMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newComponentMenuItemActionPerformed
    try {
        //
        //    NewComponentDialog.show(getCurrentCanvas());
        projectController.createComponent();
    } catch (IOException ex) {
        Logger.getLogger(Studio.class.getName()).log(Level.SEVERE, null, ex);
    }
}//GEN-LAST:event_newComponentMenuItemActionPerformed

private void versionManagementMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_versionManagementMenuItemActionPerformed
//
    if (!projectController.isProjectOpened()) {
        VDialog.showMessageDialog(getCurrentCanvas(), "No Project opened",
                "Open a project to manage its versions.");
        return;
    }

    VersionManagement.showVersionDialog(getCurrentCanvas());
}//GEN-LAST:event_versionManagementMenuItemActionPerformed

private void deleteAllVersionsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteAllVersionsMenuItemActionPerformed
//
    if (!projectController.isProjectOpened()) {
        VDialog.showMessageDialog(getCurrentCanvas(), "No Project opened",
                "Open a project to manage its versions.");
        return;
    }

    if (VDialog.showConfirmDialog(getCurrentCanvas(),
            "Delete History:",
            "<html><div align=Center>"
            + "<p>Do you really want to delete the complete history?<p>"
            + "<p><b>This action cannot be undone!</b></p>"
            + "</div></html>",
            VDialog.DialogType.YES_NO) != VDialog.YES) {
        return;
    }

    try {
        projectController.getProject().getProjectFile().deleteHistory();
    } catch (IOException ex) {
        Logger.getLogger(Studio.class.getName()).
                log(Level.SEVERE, null, ex);
    }

}//GEN-LAST:event_deleteAllVersionsMenuItemActionPerformed

    private void saveSessionWithMsgItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveSessionWithMsgItemActionPerformed
        //

        if (!projectController.isProjectOpened()) {
            VDialog.showMessageDialog(getCurrentCanvas(), "No Project opened",
                    "Open a project.");
            return;
        }

        try {

            boolean prevousState = projectController.isCommitOnSave();

            projectController.setCommitOnSave(true);

            projectController.saveProject(new CommitListener() {
                @Override
                public String commit() {
                    CommitDialogInfo commitInfo = RDialog.showConfirmDialog(
                            getCurrentCanvas(), "Commit Dialog",
                            new CommitDialogInfo(),
                            "Commit", false);
                    if (commitInfo.isValid()) {
                        return commitInfo.getCommitMessage();
                    } else {
                        return "visual session "
                                + projectController.getCurrentSession()
                                + " changed";
                    }
                }
            }, false);

            projectController.setCommitOnSave(prevousState);

            VersionManagement.closeDialog(getCurrentCanvas());

        } catch (IOException ex) {
            Logger.getLogger(Studio.class.getName()).log(Level.SEVERE, null, ex);
        }

    }//GEN-LAST:event_saveSessionWithMsgItemActionPerformed

    private void preferenceMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_preferenceMenuItemActionPerformed
        //
        if (window == null) {
            window = new PreferenceWindow();
            window.setVisible(false);
            window.setStudio(this);

            window.initConfig(
                    new File(VRL.getPropertyFolderManager().getEtcFolder(), STUDIO_CONFIG));

            VGraphicsUtil.centerOnScreen(window, VGraphicsUtil.getScreenId(this));

            window.setPreferenceFolderLocation(
                    VRL.getPropertyFolderManager().getPropertyFolder().getAbsolutePath());

            VGraphicsUtil.centerOnWindow(this, window);
            window.setVisible(true);
        }
    }//GEN-LAST:event_preferenceMenuItemActionPerformed

    private void installPluginMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_installPluginMenuItemActionPerformed
        //

        final JFileChooser fc = new JFileChooser();

        fc.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.getName().toLowerCase().endsWith(".jar") || f.isDirectory();
            }

            @Override
            public String getDescription() {
                return "VRL Plugins (*.jar)";
            }
        });

        fc.setMultiSelectionEnabled(true);

        int returnVal = fc.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            final File[] files = fc.getSelectedFiles();

            Runnable r = new Runnable() {
                @Override
                public void run() {
                    for (File file : files) {

                        VRL.installPlugin(file, new InstallPluginAction() {
                            @Override
                            public boolean overwrite(File src, File dest) {
                                return VDialog.showConfirmDialog(getCurrentCanvas(),
                                        "Overwrite existing Plugin?",
                                        "Shall the plugin " + src.getName()
                                        + " be replaced?",
                                        VDialog.DialogType.YES_NO) == VDialog.YES;
                            }

                            @Override
                            public void isNoPlugin(File src) {
                                getCurrentCanvas().getMessageBox().addMessage(
                                        "Cannot Install Plugin:",
                                        ">> the file "
                                        + Message.EMPHASIZE_BEGIN
                                        + src
                                        + Message.EMPHASIZE_END + " is no VRL plugin!",
                                        MessageType.ERROR);
                            }

                            @Override
                            public void cannotInstall(Exception ex) {
                                getCurrentCanvas().getMessageBox().addMessage(
                                        "Cannot Install Plugin:",
                                        ">> the following exception occured: "
                                        + ex.getMessage(),
                                        MessageType.ERROR);
                            }

                            @Override
                            public void installed(File f) {
                                getCurrentCanvas().getMessageBox().addMessage(
                                        "Installed Plugin:",
                                        ">> the plugin "
                                        + f.getName() + " has been installed.",
                                        MessageType.INFO);
                            }

                            @Override
                            public void analyzeStart(File src) {
//                                getCurrentCanvas().getMessageBox().addUniqueMessage(
//                                        "Analyzing file:",
//                                        ">> analyzing "
//                                        + Message.EMPHASIZE_BEGIN
//                                        + src.getName()
//                                        + Message.EMPHASIZE_END + ".",
//                                        null,
//                                        MessageType.INFO);
                                VSwingUtil.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        getCurrentCanvas().getEffectPane().startSpin();
                                    }
                                });

                            }

                            @Override
                            public void analyzeStop(File src) {
//                                getCurrentCanvas().getMessageBox().addUniqueMessage(
//                                        "Analyzing file:",
//                                        ">> analyzing "
//                                        + Message.EMPHASIZE_BEGIN
//                                        + src.getName()
//                                        + Message.EMPHASIZE_END + ".",
//                                        null,
//                                        MessageType.INFO);

                                VSwingUtil.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        getCurrentCanvas().getEffectPane().stopSpin();
                                    }
                                });
                            }
                        });
                    } // end for
                }
            }; // end runnable

            getCurrentCanvas().getMessageBox().addMessage(
                    "Installed Plugins:",
                    "Restart VRL-Studio to use the plugins.",
                    MessageType.INFO);

            Thread thread = new Thread(r);
            thread.start();
        }
    }//GEN-LAST:event_installPluginMenuItemActionPerformed

    private void exportProjectasLibraryMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportProjectasLibraryMenuItemActionPerformed
        //


        if (!projectController.isProjectOpened()) {
            VDialog.showMessageDialog(getCurrentCanvas(), "No Project opened",
                    "Open a project to export it.");
            return;
        }
        try {
            projectController.saveProject(false);
        } catch (IOException ex) {
            Logger.getLogger(Studio.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (getCurrentCanvas().getProjectController().isProjectOpened()) {
            File src = getCurrentCanvas().
                    getProjectController().getProject().getContentLocation();

            File projectFile = projectController.getFile();

            File dest =
                    new File(projectFile.getAbsolutePath().substring(
                    0, projectFile.getAbsolutePath().length() - 4) + "jar");

            boolean export = true;

            if (dest.isFile()) {
                export = VDialog.showConfirmDialog(getCurrentCanvas(),
                        "File Exists / Overwrite file?",
                        "Shall the file \""
                        + dest.getAbsolutePath()
                        + "\" be overwritten?",
                        VDialog.DialogType.YES_NO) == VDialog.AnswerType.YES;

            }

            if (export) {
                boolean success = false;

                try {
//                    IOUtil.copyFile(src, dest);
                    IOUtil.zipContentOfFolder(src,
                            dest);
                    success = true;
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(Studio.class.getName()).
                            log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(Studio.class.getName()).
                            log(Level.SEVERE, null, ex);
                }

                if (success) {
                    Message m = getCurrentCanvas().getMessageBox().addMessage(
                            "Export successful:",
                            ">> project sucessfully exported.",
                            MessageType.INFO);

                    getCurrentCanvas().getMessageBox().messageRead(m);

                } else {
                    VDialog.showMessageDialog(getCurrentCanvas(),
                            "Cannot export file",
                            "Export impossible due to I/O errors!");
                }
            }
        }
    }//GEN-LAST:event_exportProjectasLibraryMenuItemActionPerformed

    private void addPluginConfiguratorMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addPluginConfiguratorMenuItemActionPerformed
        //

        if (!projectController.isProjectOpened()) {
            VDialog.showMessageDialog(getCurrentCanvas(), "No Project opened",
                    "Open a project to create a plugin.");
            return;
        }

        PluginInfo info = PluginConfiguratorDialog.show(getCurrentCanvas());

        // return if dialog cancelled
        if (info == null) {
            return;
        }

        String code = PluginConfiguratorGenerator.generate(
                getCurrentCanvas().getProjectController(), info);

        GroovyCodeEditorComponent editor = new GroovyCodeEditorComponent(code);

        getCurrentCanvas().addObject(editor);

    }//GEN-LAST:event_addPluginConfiguratorMenuItemActionPerformed

    private void manageLibrariesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manageLibrariesMenuItemActionPerformed
        //


        if (!projectController.isProjectOpened()) {
            VDialog.showMessageDialog(getCurrentCanvas(), "No Project opened",
                    "Open a project to manage its libraries.");
            return;
        }


        ProjectLibraryDialog.showDialog(getCurrentCanvas());
    }//GEN-LAST:event_manageLibrariesMenuItemActionPerformed

    private void compileProjectMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_compileProjectMenuItemActionPerformed
        //
        if (!projectController.isProjectOpened()) {
            VDialog.showMessageDialog(getCurrentCanvas(), "No Project opened",
                    "Open a project to compile it.");
            return;
        }
        projectController.build(true, true);
    }//GEN-LAST:event_compileProjectMenuItemActionPerformed

    private void selectPluginsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectPluginsMenuItemActionPerformed
        //
        if (!projectController.isProjectOpened()) {
            VDialog.showMessageDialog(getCurrentCanvas(), "No Project opened",
                    "Open a project to select plugins.");
            return;
        }

        SelectUsedPluginsDialog.show(projectController);

    }//GEN-LAST:event_selectPluginsMenuItemActionPerformed

    private void whatIsVRLStudioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_whatIsVRLStudioActionPerformed
        //
//        HelpPDFViewer.newView(
//                "Help: What is VRL-Studio?",
//                VRL.getPropertyFolderManager().
//                getResourcesFolder().getAbsolutePath()
//                + "/help/what-is-vrl-studio.pdf", true);

        VSysUtil.openURI(new File(
                "resources/studio-resources/help/what-is-vrl-studio.html").toURI());

    }//GEN-LAST:event_whatIsVRLStudioActionPerformed

    private void creatingYourFirstProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_creatingYourFirstProjectActionPerformed
        //
//        HelpPDFViewer.newView(
//                "Help: What is VRL-Studio?",
//                VRL.getPropertyFolderManager().
//                getResourcesFolder().getAbsolutePath()
//                + "/help/what-is-vrl-studio.pdf", true);

        VSysUtil.openURI(new File(
                "resources/studio-resources/help/creating-your-first-project.html").toURI());
    }//GEN-LAST:event_creatingYourFirstProjectActionPerformed

    private void definingAWorkflowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_definingAWorkflowActionPerformed
        //

        VSysUtil.openURI(new File(
                "resources/studio-resources/help/defining-a-workflow.html").toURI());
    }//GEN-LAST:event_definingAWorkflowActionPerformed

    private void versionManagementActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_versionManagementActionPerformed
        //
        VSysUtil.openURI(new File(
                "resources/studio-resources/help/version-management.html").toURI());
    }//GEN-LAST:event_versionManagementActionPerformed

    private void saveAsItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsItemActionPerformed
        //
        if (!projectController.isProjectOpened()) {
            VDialog.showMessageDialog(getCurrentCanvas(), "No Project opened",
                    "Open a project.");
            return;
        }


        FileDialogManager manager = new FileDialogManager();

        class DummySaveAs implements FileSaver {

            public File dest;

            @Override
            public void saveFile(Object o, File file, String ext)
                    throws IOException {
                // we won't save anything
                dest = file;
            }

            @Override
            public String getDefaultExtension() {
                return "vrlp";
            }
        }

        DummySaveAs saver = new DummySaveAs();

        manager.saveFile(this, projectController, saver,
                new ProjectFileFilter());

        if (saver.dest != null) {
            try {
                projectController.saveProjectAs(saver.dest, false);
                projectController.loadProject(saver.dest, false);
                VersionManagement.closeDialog(getCurrentCanvas());
                updateAppTitle();
            } catch (IOException ex) {
                Logger.getLogger(Studio.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_saveAsItemActionPerformed

    private void pluginHelpIndexItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pluginHelpIndexItemActionPerformed
        //
        VSysUtil.openURI(new File(
                VRL.getVRLPluginDataController().getHelpFolder(),
                "plugin-index.html").toURI());
    }//GEN-LAST:event_pluginHelpIndexItemActionPerformed

    private void aboutVRLStudioItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutVRLStudioItemActionPerformed
        //
        try {

            VSysUtil.openURI(
                    new URI("http://vrl-studio.mihosoft.eu"));
        } catch (URISyntaxException ex) {
            Logger.getLogger(Studio.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_aboutVRLStudioItemActionPerformed

    private void aboutVRLItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutVRLItemActionPerformed
        //
        try {
            VSysUtil.openURI(
                    new URI("http://vrl.mihosoft.eu"));
        } catch (URISyntaxException ex) {
            Logger.getLogger(Studio.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_aboutVRLItemActionPerformed

    private void saveAsDefaultItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsDefaultItemActionPerformed
        //
        if (!projectController.isProjectOpened()) {
            VDialog.showMessageDialog(getCurrentCanvas(), "No Project opened",
                    "Open a project.");
            return;
        }

        if (!projectController.askForSave("Define Custom Default Project:",
                "<p>Save current Project.<p>"
                + "<p>Do you want to save the current session before defining "
                + "the default project?</p><br>")) {
            return;
        }

        File dest = new File(
                getDefaultSessionName());

        boolean success = false;
        String msg = "<pre>";
        try {
            IOUtil.copyFile(projectController.getFile(), dest);
            success = true;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Studio.class.getName()).log(Level.SEVERE, null, ex);
            msg += ex.toString();
        } catch (IOException ex) {
            Logger.getLogger(Studio.class.getName()).log(Level.SEVERE, null, ex);
            msg += "\n\n" + ex.toString();
        }

        msg += "</pre>";

        if (success) {
            Message m = VMessage.info("Defined Custom Default Project",
                    ">> successfully defined a new default project.");
            VMessage.defineMessageAsRead(m);
        } else {
            VMessage.error("Cannot define default project!", msg);
        }
    }//GEN-LAST:event_saveAsDefaultItemActionPerformed

    private void resetDefaultProjectItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetDefaultProjectItemActionPerformed
        //
        // check whether template folder in the app bundle contains a
        // default.vrlp
        // - copy it to the .vrl folder if it exists
        // - delete the default.vrlp in the .vrl folder if it doesn't
        File defaultFilePath = new File(getDefaultSessionName());
        File src = new File(
                "resources/studio-resources/property-folder-template/resources/project-templates/"
                + defaultFilePath.getName());

        File dest = new File(
                getDefaultSessionName());

        boolean success = false;

        if (src.exists()) {
            System.out.println(">> resetting default project to factory project");

            String msg = "<pre>";
            try {
                IOUtil.copyFile(src, dest);
                success = true;
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Studio.class.getName()).log(Level.SEVERE, null, ex);
                msg += ex.toString();
            } catch (IOException ex) {
                Logger.getLogger(Studio.class.getName()).log(Level.SEVERE, null, ex);
                msg += "\n\n" + ex.toString();
            }
            msg += "</pre>";
            if (!success) {
                VMessage.error("Cannot save default project!", msg);
            }
        } else {
            System.out.println(">> resetting default project to factory project (clean, no template)");
            IOUtil.deleteDirectory(dest);
            success = true;
        }

        if (success) {
            Message m = VMessage.info("Resetted Default Project",
                    ">> successfully resetted default project to factory settings.");
            VMessage.defineMessageAsRead(m);
        } else {
            VMessage.error("Cannot Reset Default Project",
                    ">> default project cannot be resetted to factory settings! "
                    + "(See error log for details)");
        }
    }//GEN-LAST:event_resetDefaultProjectItemActionPerformed

    private void manageComponentsItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manageComponentsItemActionPerformed
        //
        if (!projectController.getCurrentCanvas().
                isIgnoreInput()) {

            // leave this code in release mode to find a bug where
            // search dialog does not appear
            System.out.println(">> toggle search-dialog");

            ComponentManagement.toggleSearchDialog(
                    projectController.getCurrentCanvas());
        }
    }//GEN-LAST:event_manageComponentsItemActionPerformed

    private void usingTheShellItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_usingTheShellItemActionPerformed
        //
        VSysUtil.openURI(new File(
                "resources/studio-resources/help/using-the-shell.html").toURI());
    }//GEN-LAST:event_usingTheShellItemActionPerformed

    private void DebuggingItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DebuggingItemActionPerformed
        //
        VSysUtil.openURI(new File(
                "resources/studio-resources/help/debugging.html").toURI());
    }//GEN-LAST:event_DebuggingItemActionPerformed

    private void exportProjectItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportProjectItemActionPerformed
        //
        if (!projectController.isProjectOpened()) {
            VDialog.showMessageDialog(getCurrentCanvas(), "No Project opened",
                    "Open a project.");
            return;
        }

        boolean export = VDialog.showConfirmDialog(getCurrentCanvas(),
                "Export Project?",
                "<html>"
                + "<div align=\"center\">"
                + "Shall the current project be exported?<br><br>"
                + "All plugins that are used by the project will be included.<br><br>"
                + "<b>Note:</b><br><vr>"
                + "Please make sure that the plugin licenses allow the distribution of plugins.<br>"
                + "If you are unsure please contact the plugin developers!"
                + "</div>"
                + "</html>",
                VDialog.DialogType.YES_NO) == VDialog.AnswerType.YES;

        if (!export) {
            return;
        }

        FileDialogManager manager = new FileDialogManager();

        class DummySaveAs implements FileSaver {

            public File dest;

            @Override
            public void saveFile(Object o, File file, String ext)
                    throws IOException {
                // we won't save anything
                dest = file;
            }

            @Override
            public String getDefaultExtension() {
                return "vrlp";
            }
        }

        DummySaveAs saver = new DummySaveAs();

        manager.saveFile(this, projectController, saver,
                new ProjectFileFilter());

        if (saver.dest != null) {
            try {
                projectController.export(saver.dest, true);
            } catch (IOException ex) {
                Logger.getLogger(Studio.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }//GEN-LAST:event_exportProjectItemActionPerformed

    private void showLogInWindowItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showLogInWindowItemActionPerformed
        //

        if (VSwingUtil.getTopmostParent(bottomPane) != this) {
            return;
        }

        final JFrame frame = new JFrame("VRL-Studio - Log/Shell");

        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(600, 400);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                splitPane.setBottomComponent(bottomPane);
            }
        });

        frame.setVisible(true);
        frame.getContentPane().add(bottomPane);
    }//GEN-LAST:event_showLogInWindowItemActionPerformed

    private void showChangelogItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showChangelogItemActionPerformed
        //
        eu.mihosoft.vrl.system.ChangelogDialog.showDialog();

    }//GEN-LAST:event_showChangelogItemActionPerformed

    private void showStudioChangelogItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showStudioChangelogItemActionPerformed
        //
        ChangelogDialog.showDialog();
    }//GEN-LAST:event_showStudioChangelogItemActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(final String args[]) {

        VSwingUtil.fixSwingBugsInJDK7(); // ensure no comparison bug occures

        final SplashScreenGenerator generator = new SplashScreenGenerator();
        generator.setCopyrightText(Constants.COPYRIGHT_SIMPLE);
        generator.initGlobalSplashScreen();

        Thread thread = new Thread() {
            @Override
            public void run() {
                generator.showSplashScreen();
            }
        };

        thread.start();

        ClassPathUpdater.addAllJarsInDirectory(
                new File(eu.mihosoft.vrl.system.Constants.CUSTOM_LIB_DIR));

        ArgumentEvaluator evaluator = new ArgumentEvaluator(null, null);

        String[] newArgs = new String[args.length + 2];

        System.arraycopy(args, 0, newArgs, 0, args.length);

        newArgs[args.length] = "-property-folder-template";
        newArgs[args.length + 1] =
                Constants.RESOURCES_DIR + "/property-folder-template";


//        if (!VSysUtil.isMacOSX()) {
        VRL.getPropertyFolderManager().setAlreadyRunningTask(new Runnable() {
            @Override
            public void run() {

                String msg = "<html><div align=left>"
                        + "VRL-Studio is already running!"
                        + "<br><br>"
                        + "Close all other instances of VRL-Studio and "
                        + "try again."
                        + "</div></html>";

                JOptionPane.showMessageDialog(null,
                        msg,
                        "VRL-Studio already running!",
                        JOptionPane.ERROR_MESSAGE);

                System.exit(0);
            }
        });
//        }

        VRL.initAll(newArgs);

        SplashScreenGenerator.printBootMessage(
                ">> plugins initialized.");

        //
        evaluator.setRenderingOptions(args);
        //

        VSwingUtil.invokeLater(
                new Runnable() {
                    @Override
                    public void run() {

                        // load frame position and bounds
                        ConfigurationFile config = IOUtil.newConfigurationFile(
                                new File(VRL.getPropertyFolderManager().getEtcFolder(),
                                STUDIO_CONFIG));

                        boolean loaded = config.load();

                        // check whether to show start-dialog
                        if (config.containsProperty(
                                PreferenceWindow.DIALOG_ON_START_KEY)) {
                            showStartDialog =
                                    Boolean.parseBoolean(config.getProperty(
                                    PreferenceWindow.DIALOG_ON_START_KEY));
                        }

                        Studio frame = new Studio();
                        frame.studioConfig = config;

                        // if on linux or windows, set icon
                        if (!VSysUtil.isMacOSX()) {
                            try {
                                Image img = Toolkit.getDefaultToolkit().createImage(
                                        "resources/mime/vrl-app-icon.png");
                                frame.setIconImage(img);
                            } catch (Exception ex) {
                                System.out.println(
                                        ">> cannot set image for application window.");
                            }
                        }

                        frame.arguments = args;

                        ArgumentEvaluator evaluator =
                                new ArgumentEvaluator(frame, frame.getInitialCanvas());

                        evaluator.setDebugOptions(args);

                        frame.setVisible(true);

                        // check whether to restore position
                        boolean restore = config.containsProperty(
                                PreferenceWindow.RESTORE_WIN_POS_KEY);

                        if (restore) {
                            restore = Boolean.parseBoolean(config.getProperty(
                                    PreferenceWindow.RESTORE_WIN_POS_KEY));
                        }

                        if (!loaded || !restore) {
                            // sets the window size (3/4w,3/4h)
                            // and displays the window at the center
                            Dimension screenDim = VGraphicsUtil.getScreenDimension(0);
                            Dimension winDim = new Dimension(
                                    screenDim.width - screenDim.width / 4,
                                    screenDim.height - screenDim.height / 4);
                            frame.setSize(winDim);

                            VGraphicsUtil.centerOnScreen(frame, 0);
                        } else {
                            WindowBounds windowBounds = new WindowBounds(config);
                            windowBounds.setWindowBounds(frame);
                        }


                        // resize splitpane
                        frame.getSplitPane().setDividerLocation(1.0);
                        frame.getSplitPane().setResizeWeight(1);
                        frame.setSize(frame.getSize());
                        frame.splitPane.updateUI();
                        frame.canvasScrollPane.updateUI();
                        frame.shellScrollPane.updateUI();
                        frame.splitPane.setDividerLocation(frame.getHeight());

                        frame.deactivateAllEvents(frame.getCurrentCanvas());

                        // initialize plugins 
                        VRL.addCanvas(frame.mainCanvas, new ArrayList<PluginDependency>());

                        VRL.registerFileTemplatesMenu(
                                new MenuAdapter(frame.fileTemplatesMenu));
                        VRL.registerFileMenu(new MenuAdapter(frame.fileMenu));
                        VRL.registerEditMenu(new MenuAdapter(frame.editMenu));
                        VRL.registerViewMenu(new MenuAdapter(frame.viewMenu));
                        VRL.registerToolMenu(new MenuAdapter(frame.toolMenu));
                        VRL.registerDebugMenu(new MenuAdapter(frame.debugMenu));
                        VRL.registerInfoMenu(new MenuAdapter(frame.infoMenu));
                        VRL.registerStyleMenu(new MenuAdapter(frame.styleMenu));
                        VRL.registerPluginMenu(new MenuAdapter(frame.pluginMenu),
                                new MenuAdapter(frame.uninstallPluginMenu));


                        if (!VSysUtil.isMacOSX()) {
                            evaluator.setDefaultFile(args);

                            if (!evaluator.loadFile(args) && Studio.showStartDialog) {
                                frame.showStartDialog(frame.getCurrentCanvas());
                            }

                            frame.studioInitialized = true;

                            frame.autoUpdate(config);
                        }

                        frame.activateAllEvents();

                        SplashScreenGenerator.setProgress(100);


                    }
                });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem DebuggingItem;
    private javax.swing.JMenu DefaultProjectMenu;
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JMenuItem aboutVRLItem;
    private javax.swing.JMenuItem aboutVRLStudioItem;
    private javax.swing.JMenuItem addPluginConfiguratorMenuItem;
    private javax.swing.JTabbedPane bottomPane;
    private javax.swing.JScrollPane canvasScrollPane;
    private javax.swing.JMenuItem compileProjectMenuItem;
    private javax.swing.JMenuItem creatingYourFirstProject;
    private javax.swing.JMenu debugMenu;
    private javax.swing.JMenuItem definingAWorkflow;
    private javax.swing.JMenuItem deleteAllVersionsMenuItem;
    private javax.swing.JMenu editMenu;
    private javax.swing.JCheckBoxMenuItem enableShadowItem;
    private javax.swing.JMenuItem exportProjectItem;
    private javax.swing.JMenuItem exportProjectasLibraryMenuItem;
    private javax.swing.JMenuItem exportSessionItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenu fileTemplatesMenu;
    private javax.swing.JCheckBoxMenuItem fullScreenModeItem;
    private javax.swing.JMenuItem groupSelectedComponentsItem;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JMenuItem importSessionItem;
    private javax.swing.JMenu infoMenu;
    private javax.swing.JMenuItem installPluginMenuItem;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem8;
    private javax.swing.JMenuItem jMenuItem9;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator10;
    private javax.swing.JPopupMenu.Separator jSeparator11;
    private javax.swing.JPopupMenu.Separator jSeparator12;
    private javax.swing.JPopupMenu.Separator jSeparator13;
    private javax.swing.JPopupMenu.Separator jSeparator14;
    private javax.swing.JPopupMenu.Separator jSeparator15;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JPopupMenu.Separator jSeparator6;
    private javax.swing.JPopupMenu.Separator jSeparator7;
    private javax.swing.JPopupMenu.Separator jSeparator8;
    private javax.swing.JPopupMenu.Separator jSeparator9;
    private javax.swing.JMenu loadRecentSessionsMenu;
    private javax.swing.JMenuItem loadSessionItem;
    private javax.swing.JScrollPane logScrollPane;
    private javax.swing.JMenuItem manageComponentsItem;
    private javax.swing.JMenuItem manageLibrariesMenuItem;
    private javax.swing.JMenuItem newComponentMenuItem;
    private javax.swing.JMenuItem newSessionItem;
    private javax.swing.JMenu openRecentComponentsMenu;
    private javax.swing.JMenuItem pluginHelpIndexItem;
    private javax.swing.JMenu pluginMenu;
    private javax.swing.JMenuItem preferenceMenuItem;
    private javax.swing.JMenu projectMenu;
    private javax.swing.JMenuItem quitItem;
    private javax.swing.JMenu removeGroupMenu;
    private javax.swing.JMenuItem resetDefaultProjectItem;
    private javax.swing.JMenuItem saveAsDefaultItem;
    private javax.swing.JMenuItem saveAsItem;
    private javax.swing.JMenuItem saveSessionItem;
    private javax.swing.JMenuItem saveSessionWithMsgItem;
    private javax.swing.JMenuItem selectPluginsMenuItem;
    private javax.swing.JScrollPane shellScrollPane;
    private javax.swing.JMenuItem showChangelogItem;
    private javax.swing.JCheckBoxMenuItem showGridItem;
    private javax.swing.JMenu showGroupMenu;
    private javax.swing.JMenuItem showLogInWindowItem;
    private javax.swing.JCheckBoxMenuItem showMemoryUsageMenuItem;
    private javax.swing.JMenuItem showNextGroupMenuItem;
    private javax.swing.JMenuItem showPreviousGroupMenuItem;
    private javax.swing.JCheckBoxMenuItem showRepaintAreasMenuItem;
    private javax.swing.JMenuItem showStudioChangelogItem;
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JCheckBoxMenuItem startPresentationMenuItem;
    private javax.swing.JMenuBar studioMenuBar;
    private javax.swing.JMenu styleMenu;
    private javax.swing.JMenuItem systemGCMenuItem;
    private javax.swing.JMenu toolMenu;
    private javax.swing.JMenu uninstallPluginMenu;
    private javax.swing.JMenuItem usingTheShellItem;
    private javax.swing.JMenuItem versionManagement;
    private javax.swing.JMenuItem versionManagementMenuItem;
    private javax.swing.JMenu viewMenu;
    private javax.swing.JMenuItem whatIsVRLStudio;
    private javax.swing.JMenu windowMenu;
    // End of variables declaration//GEN-END:variables

    VisualCanvas getInitialCanvas() {
        return mainCanvas;
    }

    /**
     * @return the defaultSessionName
     */
    public String getDefaultSessionName() {
        return defaultSessionName;
    }

    /**
     * @param defaultSessionName the defaultSessionName to set
     */
    public void setDefaultSessionName(String defaultSessionName) {

        if (!new File(defaultSessionName).isAbsolute()) {
            this.defaultSessionName =
                    VRL.getPropertyFolderManager().getProjectTemplatesFolder()
                    + "/" + defaultSessionName;
        } else {
            this.defaultSessionName = defaultSessionName;
        }
    }

    /**
     * @return the removeGroupMenu
     */
    public javax.swing.JMenu getRemoveGroupMenu() {
        return removeGroupMenu;
    }

    /**
     * @param removeGroupMenu the removeGroupMenu to set
     */
    public void setRemoveGroupMenu(javax.swing.JMenu removeGroupMenu) {
        this.removeGroupMenu = removeGroupMenu;
    }

    /**
     * @return the showGroupMenu
     */
    public javax.swing.JMenu getShowGroupMenu() {
        return showGroupMenu;
    }

    /**
     * @param showGroupMenu the showGroupMenu to set
     */
    public void setShowGroupMenu(javax.swing.JMenu showGroupMenu) {
        this.showGroupMenu = showGroupMenu;
    }

    /**
     * @return the pluginMenu
     */
    public javax.swing.JMenu getPluginMenu() {
        return pluginMenu;
    }

    /**
     * @return the debugMenu
     */
    public javax.swing.JMenu getDebugMenu() {
        return debugMenu;
    }

    /**
     * This is an application listener that is initialised when running the
     * program on mac os x. by using this appListener, we can use the typical
     * apple-menu bar which provides own about, preferences and quit-menu-items.
     */
    private void setupMacOSXApplicationListener() {
        try {
            // get mac os-x application class
            Class appc = Class.forName("com.apple.eawt.Application");
            // create a new instance for it.
            Object app = appc.newInstance();

            // get the application-listener class. here we can set our action to the apple menu
            Class lc = Class.forName("com.apple.eawt.ApplicationListener");
            Object listener =
                    Proxy.newProxyInstance(
                    lc.getClassLoader(), new Class[]{lc}, new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) {
                    if (method.getName().equals("handleQuit")) {
                        System.out.println("OS X specific: handleQuit");
                        // call the general exit-handler from the desktop-application-api
                        // here we do all the stuff we need when exiting the application
                        quitApplication();
                    }
//                    if (method.getName().equals("handlePreferences")) {
//                        // show settings window
////                        settingsWindow();
//                    }

                    if (method.getName().equals("handleOpenApplication")) {
                        System.out.println("OS X specific: handleOpenApplication");

                        if (!studioInitialized) {

                            ArgumentEvaluator evaluator =
                                    new ArgumentEvaluator(
                                    Studio.this, Studio.this.getInitialCanvas());

//                            evaluator.setPluginOptions(arguments);
                            evaluator.setDebugOptions(arguments);

                            evaluator.setDefaultFile(arguments);

//                            if (evaluator.loadFileFile(arguments)) {
//                                studioInitialized = true;
//                            }
                        }

                        Thread t = new Thread(new Runnable() {
                            @Override
                            public void run() {

                                try {
                                    Thread.sleep(1000);


                                } catch (InterruptedException ex) {
                                    Logger.getLogger(
                                            Studio.class.getName()).log(
                                            Level.SEVERE, null, ex);
                                }

                                System.out.println("OS X specific: init done");

                                if (!studioInitialized && Studio.showStartDialog) {
                                    showStartDialog(getCurrentCanvas());
                                }

                                studioInitialized = true;

                                Studio.this.autoUpdate(studioConfig);
                            }
                        });

                        t.start();

                    }

                    if (method.getName().equals("handleOpenFile")) {
                        System.out.println("OS X specific: handleOpenFile");

                        // we know that this method takes one argument
                        // see http://developer.apple.com/library/mac/#documentation/Java/Reference/JavaSE6_AppleExtensionsRef/api/com/apple/eawt/ApplicationEvent.html
                        Object event = args[0];
                        try {
                            Method getFileName = event.getClass().getMethod("getFilename", new Class<?>[]{});
                            final String fileName = (String) getFileName.invoke(event, new Object[]{});

                            if (!studioInitialized) {
                                studioInitialized = true;
                            }

                            VSwingUtil.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    activateAllEvents();
                                    loadSession(fileName);
                                }
                            });



                        } catch (NoSuchMethodException ex) {
                            Logger.getLogger(Studio.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (SecurityException ex) {
                            Logger.getLogger(Studio.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IllegalAccessException ex) {
                            Logger.getLogger(Studio.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IllegalArgumentException ex) {
                            Logger.getLogger(Studio.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (InvocationTargetException ex) {
                            Logger.getLogger(Studio.class.getName()).log(Level.SEVERE, null, ex);
                        }


                    }
                    if (method.getName().equals("handleAbout")) {
                        System.out.println("OS X specific: handleAbout");
                        // show own aboutbox
                        showAboutDialog();
                        // set handled to true, so other actions won't take place any more.
                        // if we leave this out, a second, system-own aboutbox would be displayed
                        Method m;


                        try {
                            m = args[0].getClass().getMethod("setHandled", boolean.class);
                            m.invoke(args[0], true);

                        } catch (NoSuchMethodException ex) {
                            Logger.getLogger(Studio.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (SecurityException ex) {
                            Logger.getLogger(Studio.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IllegalAccessException ex) {
                            Logger.getLogger(Studio.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IllegalArgumentException ex) {
                            Logger.getLogger(Studio.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (InvocationTargetException ex) {
                            Logger.getLogger(Studio.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                    return null;
                }
            });

            try {
                // add application listener that listens to actions on the apple menu items
                Method m = appc.getMethod("addApplicationListener", lc);
                m.invoke(app, listener);


                // register that we want that Preferences menu. by default, only the about box is shown
                // but no pref-menu-item
//                Method enablePreferenceMethod =
//                        appc.getMethod("setEnabledPreferencesMenu", new Class[] {boolean.class});
//                enablePreferenceMethod.invoke(app, new Object[] {Boolean.TRUE});

            } catch (NoSuchMethodException ex) {
                ex.printStackTrace();
            } catch (SecurityException ex) {
                ex.printStackTrace();
            } catch (InvocationTargetException ex) {
                ex.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    private void showAboutDialog() {

        // we do not show a dialog if event filter is enabled
        if (eventFilterEnabled && VSysUtil.isMacOSX()) {
            return;
        }


//        AboutDialog aboutDlg = new AboutDialog();
//        aboutDlg.setApplicationName(Constants.APP_NAME);
//        aboutDlg.setVersionInfo(Constants.VERSION);
//        aboutDlg.setCopyrightNotice(Constants.COPYRIGHT);
//        aboutDlg.centerDialog(this);
//        aboutDlg.setVisible(true);

        VDialog.showMessageDialog(getCurrentCanvas(),
                "About",
                "<html><div align=Center>"
                + "<p><b><font size=10>"
                + Message.generateHTMLSpace(5) + Constants.APP_NAME
                + Message.generateHTMLSpace(5) + "</b></p><br>"
                + "<p><font size=4>" + Constants.VERSION + "</p><br><br>"
                + "<p><font size=4> (using VRL "
                + VRL.getVersionIdentifier() + ")"
                + "</p><br><br><br>"
                + "<p><font size=3>" + Constants.COPYRIGHT + "</p>"
                + "</div>"
                + "<br><br>"
                + "<b>License:</b> LGPL v3 (see <tt><b>Plugins->VRL->Copyright Information</b></tt> for details)<br><br>"
                + "This version includes  copyright notice and attribution requirements. According to the LGPL this<br>"
                + "information must be displayed even if you modify the source code of VRL. Neither the<br>"
                + "VRL Canvas attribution icon nor any copyright statement/attribution may be removed.<br><br>"
                + "<b><font size=5>Attribution Requirements:</font></b><br><br>"
                + "If you create derived work you must do three things regarding copyright notice and author attribution.<br><br>"
                + "<b>First</b>, the following text must be displayed on the Canvas: <b>\"based on VRL source code\"</b>.<br>"
                + "In this case the VRL canvas icon must be removed.<br><br>"
                + "<b>Second</b>, keep the links to \"About VRL-Studio\" and \"About VRL\".<br>"
                + "The copyright notice must remain.<br><br>"
                + "<b>Third</b>, add an additional notice, stating that you modified VRL and/or VRL-Studio. In addition<br>"
                + "you must cite the publications listed below. A suitable notice might read<br>"
                + "\"VRL source code modified by YourName 2012\".<br><br>"
                + "<b>Note</b>, that these requirements are in full accordance with the LGPL v3 (see 7. Additional Terms, b).<br><br>"
                + "<pre>"
                + "M. Hoffer, C.Poliwoda, G.Wittum.\n"
                + "Visual Reflection Library -\n"
                + "A Framework for Declarative GUI Programming on the Java Platform.\n"
                + "Computing and Visualization in Science, 2011, in press.\n"
                + "</pre><br>"
                + "</html>");
    }

    void quitApplication() {

//        if (projectController.getProject() != null
//                && projectController.getCurrentCanvas() != null
//                && VDialog.showConfirmDialog(getCurrentCanvas(),
//                "Quit VRL-Studio:",
//                "<html><div align=Center>"
//                + "<p>Do you really want to quit?<p>"
//                + "<p><b>Unsaved changes will be lost!</b></p>"
//                + "</div></html>",
//                VDialog.DialogType.YES_NO) != VDialog.YES) {
//            return;
//        }

        if (projectController.getProject() != null
                && projectController.getCurrentCanvas() != null
                && projectController.getProject().isOpened()) {

            int answer = VDialog.showConfirmDialog(getCurrentCanvas(),
                    "Quit VRL-Studio:",
                    "<html><div align=Center>"
                    + Message.generateHTMLSpace(50)
                    + "<p>Do you want to save the current session?</p><br>"
                    + "<p><b>Unsaved changes will be lost!</b></p>"
                    + "</div></html>",
                    new String[]{"Save", "Discard", "Cancel"});

            if (answer == 0) {
                try {
                    projectController.saveProject(false);
                } catch (IOException ex) {
                    Logger.getLogger(Studio.class.getName()).
                            log(Level.SEVERE, null, ex);
                    VDialog.AnswerType result =
                            VDialog.showConfirmDialog(getCurrentCanvas(),
                            "Error while saving project!",
                            "Dou you still want to proceed?", VDialog.YES_NO);

                    if (result == VDialog.NO) {
                        return;
                    }
                }
            } else if (answer == 1) {
                // nothing to do
            } else if (answer == 2) {
                return;
            }
        }

        // write frame position and bounds
        ConfigurationFile config = IOUtil.newConfigurationFile(
                new File(VRL.getPropertyFolderManager().getEtcFolder(), STUDIO_CONFIG));
        config.load();
        WindowBounds windowBounds = new WindowBounds(this, config);
        config.save();

        if (projectController.isProjectOpened()) {
            try {
                projectController.closeProject();

                System.out.println(">> quit.");
                VRL.exit(0);

            } catch (IOException ex) {
                Logger.getLogger(Studio.class.getName()).
                        log(Level.SEVERE, null, ex);

                VDialog.AnswerType result =
                        VDialog.showConfirmDialog(getCurrentCanvas(),
                        "Error while closing project!",
                        "Dou you still want to proceed?", VDialog.YES_NO);
                if (result == VDialog.YES) {
                    System.out.println(">> quit.");
                    VRL.exit(0);
                }
            }
        } else {

            System.out.println(">> quit.");
            VRL.getPropertyFolderManager().unlockFolder();
            VRL.exit(0);
        }
    }

    /**
     * @return the splitPane
     */
    public javax.swing.JSplitPane getSplitPane() {
        return splitPane;


    }

    /**
     * @return the loggingController
     */
    public LoggingController getLoggingController() {
        return loggingController;
    }

    /**
     * @return the bottomPane
     */
    public javax.swing.JTabbedPane getBottomPane() {
        return bottomPane;
    }

    /**
     * @return the studioMenuBar
     */
    public javax.swing.JMenuBar getStudioMenuBar() {
        return studioMenuBar;
    }

    /**
     * Enables hidden functionality not usefull for the average user.
     */
    public void iAmRoot() {
        deleteAllVersionsMenuItem.setVisible(true);
        debugMenu.setVisible(true);
    }
}
class LoadCanvasConfigurator implements CanvasConfigurator {

    private Studio studio;

    public LoadCanvasConfigurator(final Studio studio) {
        this.studio = studio;
    }

    @Override
    public void configurePreLoad(VisualCanvas canvas) {
        //
    }

    @Override
    public void configurePostLoad(VisualCanvas canvas) {
        studio.initCanvas(canvas);
        studio.updateAppTitle();
    }
}
