///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package eu.mihosoft.vrlstudio.main;
//
///**
// * =========================================== Java Pdf Extraction Decoding
// * Access Library ===========================================
// *
// * Project Info: http://www.jpedal.org (C) Copyright 1997-2008, IDRsolutions and
// * Contributors.
// *
// * This file is part of JPedal
// *
// * This library is free software; you can redistribute it and/or modify it under
// * the terms of the GNU Lesser General Public License as published by the Free
// * Software Foundation; either version 2.1 of the License, or (at your option)
// * any later version.
// *
// * This library is distributed in the hope that it will be useful, but WITHOUT
// * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
// * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
// * details.
// *
// * You should have received a copy of the GNU Lesser General Public License
// * along with this library; if not, write to the Free Software Foundation, Inc.,
// * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
// */
////needed for some countries - do not remove
//import java.awt.BorderLayout;
//import java.awt.Container;
//import java.awt.Dimension;
//import java.awt.Font;
//import java.awt.GraphicsEnvironment;
//import java.awt.Toolkit;
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.util.ResourceBundle;
//import java.util.StringTokenizer;
//
//import javax.swing.*;
//
//import org.jpedal.PdfDecoder;
//import org.jpedal.Display;
//import org.jpedal.examples.simpleviewer.Commands;
//import org.jpedal.examples.simpleviewer.Values;
//import org.jpedal.fonts.FontMappings;
//import org.jpedal.io.JAIHelper;
//import org.jpedal.examples.simpleviewer.gui.MultiViewTransferHandler;
//import org.jpedal.examples.simpleviewer.gui.SingleViewTransferHandler;
//import org.jpedal.examples.simpleviewer.gui.SwingGUI;
//import org.jpedal.examples.simpleviewer.gui.generic.GUIMouseHandler;
//import org.jpedal.examples.simpleviewer.gui.generic.GUISearchWindow;
//import org.jpedal.examples.simpleviewer.gui.generic.GUIThumbnailPanel;
//import org.jpedal.examples.simpleviewer.gui.popups.TipOfTheDay;
//
//import org.jpedal.examples.simpleviewer.gui.swing.FrameCloser;
//import org.jpedal.examples.simpleviewer.gui.swing.SearchList;
//import org.jpedal.examples.simpleviewer.gui.swing.SwingMouseListener;
//import org.jpedal.examples.simpleviewer.gui.swing.SwingSearchWindow;
//import org.jpedal.examples.simpleviewer.gui.swing.SwingThumbnailPanel;
//import org.jpedal.examples.simpleviewer.utils.Printer;
//import org.jpedal.examples.simpleviewer.utils.PropertiesFile;
//
//import org.jpedal.exception.PdfException;
//import org.jpedal.exception.PdfFontException;
//import org.jpedal.external.Options;
//import org.jpedal.objects.acroforms.actions.ActionHandler;
//import org.jpedal.objects.raw.OutlineObject;
//import org.jpedal.objects.raw.PdfDictionary;
//import org.jpedal.objects.raw.PdfObject;
//import org.jpedal.utils.LogWriter;
//import org.jpedal.utils.Messages;
//import org.w3c.dom.Node;
//
///**
// * Fully featured GUI viewer and demonstration of JPedal's capabilities
// *
// *
// * <br>This class provides the framework for the Viewer and calls other classes
// * which provide the following functions:-
// *
// * <br>Values commonValues - repository for general settings Printer
// * currentPrinter - All printing functions and access methods to see if printing
// * active PdfDecoder decode_pdf - PDF library and panel ThumbnailPanel
// * thumbnails - provides a thumbnail pane down the left side of page -
// * thumbnails can be clicked on to goto page PropertiesFile properties - saved
// * values stored between sessions SwingGUI currentGUI - all Swing GUI functions
// * SearchWindow searchFrame (not GPL) - search Window to search pages and goto
// * references on any page Commands currentCommands - parses and executes all
// * options SwingMouseHandler mouseHandler - handles all mouse and related
// * activity
// */
//public class SimpleViewer {
//
//    /**
//     * control if messages appear
//     */
//    public static boolean showMessages = true;
//    /**
//     * repository for general settings
//     */
//    protected Values commonValues = new Values();
//    /**
//     * All printing functions and access methods to see if printing active
//     */
//    protected Printer currentPrinter = new Printer();
//    /**
//     * PDF library and panel
//     */
//    protected PdfDecoder decode_pdf = new PdfDecoder(true);
//    /**
//     * encapsulates all thumbnail functionality - just ignore if not required
//     */
//    protected GUIThumbnailPanel thumbnails = new SwingThumbnailPanel(commonValues, decode_pdf);
//    /**
//     * values saved on file between sessions
//     */
//    private PropertiesFile properties = new PropertiesFile();
//    /**
//     * general GUI functions
//     */
//    public SwingGUI currentGUI = new SwingGUI(decode_pdf, commonValues, thumbnails, properties);
//    /**
//     * search window and functionality
//     */
//    private GUISearchWindow searchFrame = new SwingSearchWindow(currentGUI);
//    /**
//     * command functions
//     */
//    protected Commands currentCommands = new Commands(commonValues, currentGUI, decode_pdf,
//            thumbnails, properties, searchFrame, currentPrinter);
//    /**
//     * all mouse actions
//     */
////  protected GUIMouseHandler mouseHandler=new SwingMouseHandler(decode_pdf,currentGUI,commonValues,currentCommands);
//    protected GUIMouseHandler mouseHandler = new SwingMouseListener(decode_pdf, currentGUI, commonValues, currentCommands);
//    /**
//     * scaling values which appear onscreen
//     */
//    protected String[] scalingValues;
//    /**
//     * warn user if viewer not setup fully
//     */
//    private boolean isSetup;
//    //private Object[] restrictedMenus;
//    /**
//     * Location of Preferences Files
//     */
//    public final static String PREFERENCES_DEFAULT = "jar:/org/jpedal/examples/simpleviewer/res/preferences/Default.xml";
////  public final static String PREFERENCES_TABLEZONER = "jar:/org/jpedal/examples/simpleviewer/res/preferences/TableZoner.xml";
////  public final static String PREFERENCES_CONTENTEXTRACTOR = "jar:/org/jpedal/examples/simpleviewer/res/preferences/ContentExtractor.xml";
//    public final static String PREFERENCES_NO_GUI = "jar:/org/jpedal/examples/simpleviewer/res/preferences/NoGUI.xml";
//    public final static String PREFERENCES_NO_SIDE_BAR = "jar:/org/jpedal/examples/simpleviewer/res/preferences/NoSideTabOrTopButtons.xml";
//    public final static String PREFERENCES_OPEN_AND_NAV_ONLY = "jar:/org/jpedal/examples/simpleviewer/res/preferences/OpenAndNavOnly.xml";
//    public final static String PREFERENCES_PDFHELP = "jar:/org/jpedal/examples/simpleviewer/res/preferences/PDFHelp.xml";
//    public final static String PREFERENCES_BEAN = "jar:/org/jpedal/examples/simpleviewer/res/preferences/Bean.xml";
//    /**
//     * tell software to exit on close - default is true
//     */
//    public static boolean exitOnClose = true;
//    /**
//     * used internally - please do not use
//     */
//    private static String rawFile = "";
//    public static String file = "";
//    //<start-wrap>
//
//    /**
//     * //<end-wrap>
//     *
//     * public static String message="${titleMessage}";
//     *
//     * private static int count=0;
//     *
//     * // setup and run client with hard-coded file public SimpleViewer() {
//     * //enable error messages which are OFF by default
//     *
//     * PdfDecoder.showErrorMessages=true;
//     *
//     * properties.loadProperties();
//     *
//     * //clean up name String[] seps=new String[]{"/","\\"}; for(int
//     * ii=0;ii<seps.length;ii++){ int id=rawFile.lastIndexOf(seps[ii]);
//     * if(id!=-1) rawFile=rawFile.substring(id+1,rawFile.length()); }
//     *
//     * file=rawFile;
//     *
//     * count++; } /*
//     */
//    /**
//     * setup and run client, loading defaultFile on startup (please do not use)
//     * - use setupViewer();, openDefaultFile(defaultFile)
//     *
//     *
//     */
//    //<start-wrap>
//    /**
//     * //<end-wrap>
//     *
//     * private String defaultFile=null;
//     *
//     * public void setupViewer(String defaultFile) {
//     *
//     * if(count==0) throw new RuntimeException("You cannot use wrapper to open
//     * PDFs");
//     *
//     * this.defaultFile=defaultFile;
//     *
//     * setupViewer();
//     *
//     * openDefaultFile();
//     *
//     * }/*
//     */
//    /**
//     * open the file passed in by user on startup (do not call directly)
//     */
//    public SwingGUI getSwingGUI() {
//        return currentGUI;
//    }
//
//    /**
//     *
//     * @param defaultFile Allow user to open PDF file to display
//     */
//    //<start-wrap>
//    public void openDefaultFile(String defaultFile) {
//        /**
//         * //<end-wrap> public void openDefaultFile() {
//         *
//         * String defaultFile=this.defaultFile; if(defaultFile==null){
//         * defaultFile="jar:/org/jpedal/file.pdf";
//         * currentCommands.inputStream=this.getClass().getResourceAsStream("jar:/org/jpedal/file.pdf");
//         * }
//    /*
//         */
//        //get any user set dpi
//        String hiresFlag = System.getProperty("org.jpedal.hires");
//        if (Commands.hires || hiresFlag != null) {
//            commonValues.setUseHiresImage(true);
//        }
//
//        //get any user set dpi
//        String memFlag = System.getProperty("org.jpedal.memory");
//        if (memFlag != null) {
//            commonValues.setUseHiresImage(false);
//        }
//
//        //reset flag
//        if (thumbnails.isShownOnscreen()) {
//            thumbnails.resetToDefault();
//        }
//
//        commonValues.maxViewY = 0;// ensure reset for any viewport
//
//        /**
//         * open any default file and selected page
//         */
//        if (defaultFile != null) {
//
//            //<start-wrap>
//            File testExists = new File(defaultFile);
//            boolean isURL = false;
//            if (defaultFile.startsWith("http:") || defaultFile.startsWith("jar:") || defaultFile.startsWith("file:")) {
//                LogWriter.writeLog("Opening http connection");
//                isURL = true;
//            }
//
//            if ((!isURL) && (!testExists.exists())) {
//                currentGUI.showMessageDialog(defaultFile + '\n' + Messages.getMessage("PdfViewerdoesNotExist.message"));
//            } else if ((!isURL) && (testExists.isDirectory())) {
//                currentGUI.showMessageDialog(defaultFile + '\n' + Messages.getMessage("PdfViewerFileIsDirectory.message"));
//            } else {
//                commonValues.setFileSize(testExists.length() >> 10);
//
//                //<end-wrap>
//
//                commonValues.setSelectedFile(defaultFile);
//
//                currentGUI.setViewerTitle(null);
//
//                //<start-wrap>
//                /**
//                 * see if user set Page
//                 */
//                String page = System.getProperty("org.jpedal.page");
//                String bookmark = System.getProperty("org.jpedal.bookmark");
//                if (page != null && !isURL) {
//
//                    try {
//                        int pageNum = Integer.parseInt(page);
//
//                        if (pageNum < 1) {
//                            pageNum = -1;
//                            System.err.println(page + " must be 1 or larger. Opening on page 1");
//                            LogWriter.writeLog(page + " must be 1 or larger. Opening on page 1");
//                        }
//
//                        if (pageNum != -1) {
//                            openFile(testExists, pageNum);
//                        }
//
//
//                    } catch (Exception e) {
//                        System.err.println(page + "is not a valid number for a page number. Opening on page 1");
//                        LogWriter.writeLog(page + "is not a valid number for a page number. Opening on page 1");
//                    }
//                } else if (bookmark != null) {
//                    openFile(testExists, bookmark);
//                } else {
//                    //<end-wrap>
//                    try {
//                        currentCommands.openFile(defaultFile);
//                    } catch (PdfException e) {
//                    }
//                    //<start-wrap>
//                }
//            }
//            //<end-wrap>
//        }
//
//        //<start-wrap>
//        /**
//         * //<end-wrap> executeCommand(Commands.SINGLE,null);
//        /*
//         */
//    }
//
//    /**
//     *
//     * @param defaultFile Allow user to open PDF file to display
//     */
//    //<start-wrap>
//    public void openDefaultFileAtPage(String defaultFile, int page) {
//        /**
//         * //<end-wrap> private void openDefaultFileAtPage(String defaultFile,
//         * int page) {
//   /*
//         */
//        //get any user set dpi
//        String hiresFlag = System.getProperty("org.jpedal.hires");
//        if (Commands.hires || hiresFlag != null) {
//            commonValues.setUseHiresImage(true);
//        }
//
//        //get any user set dpi
//        String memFlag = System.getProperty("org.jpedal.memory");
//        if (memFlag != null) {
//            commonValues.setUseHiresImage(false);
//        }
//
//        //reset flag
//        if (thumbnails.isShownOnscreen()) {
//            thumbnails.resetToDefault();
//        }
//
//        commonValues.maxViewY = 0;// ensure reset for any viewport
//
//        /**
//         * open any default file and selected page
//         */
//        if (defaultFile != null) {
//
//            File testExists = new File(defaultFile);
//            boolean isURL = false;
//            if (defaultFile.startsWith("http:") || defaultFile.startsWith("jar:")) {
//                LogWriter.writeLog("Opening http connection");
//                isURL = true;
//            }
//
//            if ((!isURL) && (!testExists.exists())) {
//                currentGUI.showMessageDialog(defaultFile + '\n' + Messages.getMessage("PdfViewerdoesNotExist.message"));
//            } else if ((!isURL) && (testExists.isDirectory())) {
//                currentGUI.showMessageDialog(defaultFile + '\n' + Messages.getMessage("PdfViewerFileIsDirectory.message"));
//            } else {
//
//                commonValues.setSelectedFile(defaultFile);
//                //<start-wrap>
//                commonValues.setFileSize(testExists.length() >> 10);
//                //<end-wrap>
//                currentGUI.setViewerTitle(null);
//
//                openFile(testExists, page);
//
//            }
//        }
//    }
//
//    //<start-wrap>
//    /**
//     * setup and run client
//     */
//    public SimpleViewer() {
//        //enable error messages which are OFF by default
//        PdfDecoder.showErrorMessages = true;
//
//        String prefFile = System.getProperty("org.jpedal.SimpleViewer.Prefs");
//        if (prefFile != null) {
//            properties.loadProperties(prefFile);
//        } else {
//            properties.loadProperties();
//        }
//
//        //properties.loadProperties();
//
//    }
//    //<end-wrap>
//    //<start-wXXrap> //here when other files removed
//
//    /**
//     * setup and run client passing in paramter to show if running as applet,
//     * webstart or JSP (only applet has any effect at present)
//     */
//    public SimpleViewer(int modeOfOperation) {
//
//        //<start-wrap>
//        /**
//         * //<end-wrap>
//         *
//         * //clean up name String[] seps=new String[]{"/","\\"}; for(int
//         * ii=0;ii<seps.length;ii++){ int id=rawFile.lastIndexOf(seps[ii]);
//         * if(id!=-1) rawFile=rawFile.substring(id+1,rawFile.length()); }
//         *
//         * file=rawFile;
//        /*
//         */
//        //enable error messages which are OFF by default
//        PdfDecoder.showErrorMessages = true;
//
//        String prefFile = System.getProperty("org.jpedal.SimpleViewer.Prefs");
//        if (prefFile != null) {
//            properties.loadProperties(prefFile);
//        } else {
//            properties.loadProperties();
//        }
//
//        commonValues.setModeOfOperation(modeOfOperation);
//
//
//    }
//
//    /**
//     * setup and run client passing in paramter that points to the preferences
//     * file we should use.
//     */
//    public SimpleViewer(String prefs) {
//
//        //enable error messages which are OFF by default
//        PdfDecoder.showErrorMessages = true;
//
////    Example preference file can be found here. You will know when it's working
////    String p = "org/jpedal/examples/simpleviewer/res/preferences/NoGUI.xml";
//
////    
////    // p = p.replaceAll("\\.", "/");
////          URL u = Thread.currentThread().getContextClassLoader().getResource(
////                  p);
////          ArrayList retValue = new ArrayList(0);
////          String s = u.toString();
////
////          System.out.println("scanning " + s);
////
////          if (s.startsWith("jar:") && s.endsWith(p)) {
////              int idx = s.lastIndexOf(p);
////              s = s.substring(0, idx); // isolate entry name
////
////              System.out.println("entry= " + s);
////              try{
////                URL url = new URL(s);
////                // Get the jar file
////                JarURLConnection conn = (JarURLConnection) url.openConnection();
////                JarFile jar = conn.getJarFile();
////
////                for (Enumeration e = jar.entries(); e.hasMoreElements();) {
////                  JarEntry entry = (JarEntry) e.nextElement();
////                  if ((!entry.isDirectory())
////                      & (entry.getName().startsWith(p))) { // this
////                    // is how you can match
////                    // to find your fonts.
////                    // System.out.println("Found a match!");
////                    String fontName = entry.getName();
////                    int i = fontName.lastIndexOf('/');
////                    fontName = fontName.substring(i + 1);
////                    retValue.add(fontName);
////                  }
////                }
////              }catch(Exception e){
////                
////              }
////          } else {
////              // Does not start with "jar:"
////              // Dont know - should not happen
////              System.out.println(p);
////              System.exit(1);
////          }
//
//        try {
//            properties.loadProperties(prefs);
//        } catch (Exception e) {
//            System.err.println("Specified Preferrences file not found at " + prefs + ". If this file is within a jar ensure filename has jar: at the begining.");
//            System.exit(1);
//        }
//
//
//    }
//
//    /**
//     * setup and run client passing in parameter that points to the preferences
//     * file we should use.
//     */
//    public SimpleViewer(Container rootContainer, String preferencesPath) {
//
//        //enable error messages which are OFF by default
//        PdfDecoder.showErrorMessages = true;
//
//        if (preferencesPath != null && preferencesPath.length() > 0) {
//            try {
//                properties.loadProperties(preferencesPath);
//            } catch (Exception e) {
//                System.err.println("Specified Preferrences file not found at " + preferencesPath + ". If this file is within a jar ensure filename has jar: at the begining.");
//                System.exit(1);
//            }
//        } else {
//            properties.loadProperties();
//        }
//        setRootContainer(rootContainer);
//
//
//    }
//
//    public void setRootContainer(Container rootContainer) {
//        if (rootContainer == null) {
//            throw new RuntimeException("Null containers not allowed.");
//        }
//
//        Container c = rootContainer;
//
//        if ((rootContainer instanceof JTabbedPane)) {
//            JPanel temp = new JPanel(new BorderLayout());
//            rootContainer.add(temp);
//            c = temp;
//        } else if (rootContainer instanceof JScrollPane) {
//            JPanel temp = new JPanel(new BorderLayout());
//            ((JScrollPane) rootContainer).getViewport().add(temp);
//            c = temp;
//
//        } else if (rootContainer instanceof JSplitPane) {
//            throw new RuntimeException("To add the simpleViewer to a split pane please pass through either JSplitPane.getLeftComponent() or JSplitPane.getRightComponent()");
//        }
//
//        if (!(rootContainer instanceof JFrame)) {
//            c.setLayout(new BorderLayout());
//        }
//
//        //Used to prevent infinite scroll issue as a preferred size has been set
//        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
//        int width = d.width / 2, height = d.height / 2;
//        if (width < 700) {
//            width = 700;
//        }
//
//        //allow user to alter size
//        String customWindowSize = System.getProperty("org.jpedal.startWindowSize");
//        if (customWindowSize != null) {
//
//            StringTokenizer values = new StringTokenizer(customWindowSize, "x");
//
//            System.out.println(values.countTokens());
//            if (values.countTokens() != 2) {
//                throw new RuntimeException("Unable to use value for org.jpedal.startWindowSize=" + customWindowSize + "\nValue should be in format org.jpedal.startWindowSize=200x300");
//            }
//
//            try {
//                width = Integer.parseInt(values.nextToken().trim());
//                height = Integer.parseInt(values.nextToken().trim());
//
//            } catch (Exception ee) {
//                throw new RuntimeException("Unable to use value for org.jpedal.startWindowSize=" + customWindowSize + "\nValue should be in format org.jpedal.startWindowSize=200x300");
//            }
//        }
//
//        c.setPreferredSize(new Dimension(width, height));
//
//        currentGUI.setFrame(c);
//
//        //setupViewer();
//    }
//    //<end-wrap>
//
//    /**
//     * Should be called before setupViewer
//     */
//    public void loadProperties(String props) {
//        properties.loadProperties(props);
//    }
//
//    /**
//     * Should be called before setupViewer
//     */
//    public void loadProperties(InputStream is) {
//        properties.loadProperties(is);
//    }
//
//    /**
//     * initialise and run client (default as Application in own Frame)
//     */
//    public void setupViewer() {
//
//        //also allow messages to be suppressed with JVM option
//        String flag = System.getProperty("org.jpedal.suppressViewerPopups");
//        boolean suppressViewerPopups = false;
//
//        if (flag != null && flag.toLowerCase().equals("true")) {
//            suppressViewerPopups = true;
//        }
//
//
//
//
//        /**
//         * set search window position here to ensure that gui has correct value
//         */
//        String searchType = properties.getValue("searchWindowType");
//        if (searchType != null && searchType.length() != 0) {
//            int type = Integer.parseInt(searchType);
//            searchFrame.setStyle(type);
//        } else {
//            searchFrame.setStyle(SwingSearchWindow.SEARCH_MENU_BAR);
//        }
//
//        //Set search frame here
//        currentGUI.setSearchFrame(searchFrame);
//
//        /**
//         * switch on thumbnails if flag set
//         */
//        String setThumbnail = System.getProperty("org.jpedal.thumbnail");
//        if (setThumbnail != null) {
//            if (setThumbnail.equals("true")) {
//                thumbnails.setThumbnailsEnabled(true);
//            } else if (setThumbnail.equals("true")) {
//                thumbnails.setThumbnailsEnabled(false);
//            }
//        } else //default
//        {
//            thumbnails.setThumbnailsEnabled(true);
//        }
//
//        /**
//         * non-GUI initialisation
//         *
//         */
//        //allow user to override messages
//        /**
//         * allow user to define country and language settings
//         *
//         * you will need a file called messages_XX.properties in
//         * org.jpedal.international.messages where XX is a valid Locale.
//         *
//         * You can also choose an alternative Lovation - see sample code below
//         *
//         * You can manually set Java to use a Locale with this code (also useful
//         * to test)
//         *
//         * Example here is Brazil (note no Locale files present for it)
//         *
//         * If you make and Locale files, we would be delighted to include them
//         * in future versions of the software.
//         *
//         * java.util.Locale aLocale = new java.util.Locale("br", "BR");
//         *
//         * java.util.Locale.setDefault(aLocale);
//         */
//        String customBundle = System.getProperty("org.jpedal.bundleLocation");
//        //customBundle="org.jpedal.international.messages"; //test code
//
//        if (customBundle != null) {
//
//            BufferedReader input_stream = null;
//            ClassLoader loader = Messages.class.getClassLoader();
//            String fileName = customBundle.replaceAll("\\.", "/") + '_' + java.util.Locale.getDefault().getLanguage() + ".properties";
//
//            //also tests if locale file exists and tell user if not
//            try {
//
//                input_stream = new BufferedReader(new InputStreamReader(loader.getResourceAsStream(fileName)));
//
//                input_stream.close();
//
//            } catch (Exception ee) {
//
//
//                java.util.Locale.setDefault(new java.util.Locale("en", "EN"));
//                currentGUI.showMessageDialog("No locale file " + fileName + " has been defined for this Locale - using English as Default"
//                        + "\n Format is path, using '.' as break ie org.jpedal.international.messages");
//
//            }
//
//            ResourceBundle rb = ResourceBundle.getBundle(customBundle);
//            //Messages.setBundle(ResourceBundle.getBundle(customBundle));
//            init(rb);
//
//        } else {
//            init(null);
//        }
//
//
//
//        /**
//         * gui setup, create gui, load properties
//         */
//        currentGUI.init(scalingValues, currentCommands, currentPrinter);
//
//        //now done on first usage
//        //p.createPreferenceWindow(currentGUI);
//
//        mouseHandler.setupMouse();
//
//        if (searchFrame.getStyle() == SwingSearchWindow.SEARCH_TABBED_PANE) {
//            currentGUI.searchInTab(searchFrame);
//        }
//
//        /**
//         * setup window for warning if renderer has problem
//         */
//        decode_pdf.getDynamicRenderer().setMessageFrame(currentGUI.getFrame());
//
//        String propValue = properties.getValue("showfirsttimepopup");
//        boolean showFirstTimePopup = !suppressViewerPopups && propValue.length() > 0 && propValue.equals("true");
//
//        if (showFirstTimePopup) {
//            currentGUI.showFirstTimePopup();
//            properties.setValue("showfirsttimepopup", "false");
//        } else if (!suppressViewerPopups) {
//            propValue = properties.getValue("showrhinomessage");
//        }
//
//
////        if(!suppressViewerPopups && properties != null && (propValue.length()>0 && propValue.equals("true"))){
////
////            //<start-gpl>
////            java.io.InputStream in=null;
////            //add in flag so displays
////            try{
////                in = SimpleViewer.class.getClassLoader().getResourceAsStream("org/mozilla/javascript/Context.class");
////            }catch(Error err){
////            }catch(Exception ex){
////
////            }
////            if (in == null){
////
////                currentGUI.showMessageDialog(Messages.getMessage("Beta release Javascript support\n" +
////                        "If you add Rhino to the classpath, JPedal now offers Javascript support in forms."));
////            }else{
////                currentGUI.showMessageDialog(Messages.getMessage("Beta release Javascript support\n" +
////                        "This is the first release supporting Javascript in Forms\n"+
////                        "We will be adding comprehensive Javascript support to our PDF library\n"+
////                        "Rhino is on the classpath so this code will now support Javascript in Forms"));
////            }  /**
////             //<end-gpl>
////
////             currentGUI.showMessageDialog(Messages.getMessage("Beta release Javascript support\n" +
////             "OS version does not contain Javascript support - please look at full version"));
////
////             /**/
////            properties.setValue("showrhinomessage","false");
////        }
//
//        if (!suppressViewerPopups && JAIHelper.isJAIused()) {
//            propValue = properties.getValue("showddmessage");
//            if (properties != null && (propValue.length() > 0 && propValue.equals("true"))) {
//
//                currentGUI.showMessageDialog(Messages.getMessage("PdfViewer.JAIWarning")
//                        + Messages.getMessage("PdfViewer.JAIWarning1")
//                        + Messages.getMessage("PdfViewer.JAIWarning2")
//                        + Messages.getMessage("PdfViewer.JAIWarning3")
//                        + Messages.getMessage("PdfViewer.JAIWarning4"));
//
//                properties.setValue("showddmessage", "false");
//            }
//        }
//
//        //<start-wrap><start-pdfhelp>
//        /**
//         * check for itext and tell user about benefits
//         */
//        if (!suppressViewerPopups && !commonValues.isContentExtractor()) {
//            propValue = properties.getValue("showitextmessage");
//            boolean showItextMessage = (propValue.length() > 0 && propValue.equals("true"));
//
//            if (!commonValues.isItextOnClasspath() && showItextMessage) {
//
////                currentGUI.showItextPopup();
//
//                properties.setValue("showitextmessage", "false");
//            }
//        }
//        //<end-pdfhelp><end-wrap>
//
//        if (currentGUI.isSingle()) {
//            TransferHandler singleViewTransferHandler = new SingleViewTransferHandler(commonValues, thumbnails, currentGUI, currentCommands);
//            decode_pdf.setTransferHandler(singleViewTransferHandler);
//        } else {
//            TransferHandler multiViewTransferHandler = new MultiViewTransferHandler(commonValues, thumbnails, currentGUI, currentCommands);
//            currentGUI.getMultiViewerFrames().setTransferHandler(multiViewTransferHandler);
//        }
//
////    DefaultTransferHandler dth = new DefaultTransferHandler(commonValues, thumbnails, currentGUI, currentCommands);
////    decode_pdf.setTransferHandler(dth);
//
//        boolean wasUpdateAvailable = false;
//
//        //<start-wrap><start-gpl>
//        propValue = properties.getValue("automaticupdate");
//        if (!suppressViewerPopups && propValue.length() > 0 && propValue.equals("true")) {
//            wasUpdateAvailable = currentCommands.checkForUpdates(false);
//        }
//        //<end-gpl><end-wrap>
//
//        propValue = properties.getValue("displaytipsonstartup");
//        if (!suppressViewerPopups && !wasUpdateAvailable && propValue.length() > 0 && propValue.equals("true")) {
//            SwingUtilities.invokeLater(new Runnable() {
//
//                public void run() {
//                    TipOfTheDay tipOfTheDay = new TipOfTheDay(currentGUI.getFrame(), "/org/jpedal/examples/simpleviewer/res/tips", properties);
//                    tipOfTheDay.setVisible(true);
//                }
//            });
//        }
//
//        //falg so we can warn user if thewy call executeCommand without it setup
//        isSetup = true;
//    }
//
//    /**
//     * setup the viewer
//     */
//    protected void init(ResourceBundle bundle) {
//
//        /**
//         * load correct set of messages
//         */
//        if (bundle == null) {
//
//            //load locale file
//            try {
//                Messages.setBundle(ResourceBundle.getBundle("org.jpedal.international.messages"));
//            } catch (Exception e) {
//                LogWriter.writeLog("Exception " + e + " loading resource bundle.\n"
//                        + "Also check you have a file in org.jpedal.international.messages to support Locale=" + java.util.Locale.getDefault());
//            }
//
//        } else {
//            try {
//                Messages.setBundle(bundle);
//            } catch (Exception ee) {
//                LogWriter.writeLog("Exception with bundle " + bundle);
//                ee.printStackTrace();
//            }
//        }
//        /**
//         * setup scaling values which ar displayed for user to choose
//         */
//        this.scalingValues = new String[]{Messages.getMessage("PdfViewerScaleWindow.text"), Messages.getMessage("PdfViewerScaleHeight.text"),
//            Messages.getMessage("PdfViewerScaleWidth.text"),
//            "25%", "50%", "75%", "100%", "125%", "150%", "200%", "250%", "500%", "750%", "1000%"};
//
//        /**
//         * setup display
//         */
//        if (commonValues.isContentExtractor()) {
//            if (SwingUtilities.isEventDispatchThread()) {
//
//                decode_pdf.setDisplayView(Display.SINGLE_PAGE, Display.DISPLAY_LEFT_ALIGNED);
//
//            } else {
//                final Runnable doPaintComponent = new Runnable() {
//
//                    public void run() {
//                        decode_pdf.setDisplayView(Display.SINGLE_PAGE, Display.DISPLAY_LEFT_ALIGNED);
//                    }
//                };
//                SwingUtilities.invokeLater(doPaintComponent);
//            }
//        } //decode_pdf.setDisplayView(Display.SINGLE_PAGE,Display.DISPLAY_LEFT_ALIGNED);
//        else if (SwingUtilities.isEventDispatchThread()) {
//
//            decode_pdf.setDisplayView(Display.SINGLE_PAGE, Display.DISPLAY_CENTERED);
//
//        } else {
//            final Runnable doPaintComponent = new Runnable() {
//
//                public void run() {
//                    decode_pdf.setDisplayView(Display.SINGLE_PAGE, Display.DISPLAY_CENTERED);
//                }
//            };
//            SwingUtilities.invokeLater(doPaintComponent);
//        }
////    decode_pdf.setDisplayView(Display.SINGLE_PAGE,Display.DISPLAY_CENTERED);
//
//        //pass through GUI for use in multipages and Javascript
//        decode_pdf.addExternalHandler(currentGUI, Options.MultiPageUpdate);
//
//        //used to test ability to replace Javascript with own engine
//        //org.jpedal.objects.javascript.ExpressionEngine marksTest=new TestEngine();
//        //decode_pdf.addExternalHandler(marksTest, Options.ExpressionEngine);
//
//        /**
//         * debugging code to create a log*
//         * LogWriter.setupLogFile(true,1,"","v",false); //LogWriter.log_name =
//         * "/mnt/shared/log.txt";
//         *
//         * /*
//         */
//        //make sure widths in data CRITICAL if we want to split lines correctly!!
//        decode_pdf.init(true);
//
//        /**
//         * ANNOTATIONS code
//         *
//         * replace Annotations with your own custom annotations using paint code
//         *
//         */
//        //decode_pdf.setAnnotationsVisible(false); //disable built-in annotations and use custom versions
//        //code to create a unique iconset
//        //see also org.jpedal.examples.simpleviewer.gui.GUI.createUniqueAnnotationIcons() 
//        //this allows the user to place fonts in the classpath and use these for display, as if embedded
//        //decode_pdf.addSubstituteFonts("org/jpedal/res/fonts/", true);
//        //set to extract all
//        //COMMENT OUT THIS LINE IF USING JUST THE VIEWER
//        decode_pdf.setExtractionMode(0, 72, 1); //values extraction mode,dpi of images, dpi of page as a factor of 72
//
//        //don't extract text and images (we just want the display)
//
//        /*
//         * 
//         */
//        /**
//         * FONT EXAMPLE CODE showing JPedal's functionality to set values for
//         * non-embedded fonts.
//         *
//         * This allows sophisticated substitution of non-embedded fonts.
//         *
//         * Most font mapping is done as the fonts are read, so these calls must
//         * be made BEFORE the openFile() call.
//         */
//        /**
//         * FONT EXAMPLE - Replace global default for non-embedded fonts.
//         *
//         * You can replace Lucida as the standard font used for all non-embedded
//         * and substituted fonts by using is code. Java fonts are case
//         * sensitive, but JPedal resolves currentGUI.frame, so you could use
//         * Webdings, webdings or webDings for Java font Webdings
//         */
//        /**
//         * Removed to save time on startup - uncomment if it causes problems
//         * try{ //choice of example font to stand-out (useful in checking
//         * results to ensure no font missed. //In general use Helvetica or
//         * similar is recommended //
//         * decode_pdf.setDefaultDisplayFont("SansSerif");
//         * }catch(PdfFontException e){ //if its not available catch error and
//         * show valid list
//         *
//         * System.out.println(e.getMessage());
//         *
//         * //get list of fonts you can use String[] fontList
//         * =GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
//         * System.out.println(Messages.getMessage("PdfViewerFontsFound.message"));
//         * System.out.println("=====================\n"); int count =
//         * fontList.length; for (int i = 0; i < count; i++) { Font f=new
//         * Font(fontList[i],1,10); System.out.println(fontList[i]+"
//         * ("+Messages.getMessage("PdfViewerFontsPostscript.message")+ '='
//         * +f.getPSName()+ ')');
//         *
//         * }
//         * System.exit(1);
//         *
//         * }/**
//         */
//        /**
//         * IMPORTANT note on fonts for EXAMPLES
//         *
//         * USEFUL TIP : The SimpleViewer displays a list of fonts used on the
//         * current PDF page with the File > Fonts menu option.
//         *
//         * PDF allows the use of weights for fonts so Arial,Bold is a weight of
//         * Arial. This value is not case sensitive so JPedal would regard
//         * arial,bold and aRiaL,BoLd as the same.
//         *
//         * Java supports a set of Font families internally (which may have
//         * weights), while JPedals substitution facility uses physical True Type
//         * fonts so it is resolving each font weight separately. So mapping
//         * works differently, depending on which is being used.
//         *
//         * If you are using a font, which is named as arial,bold you can use
//         * either arial,bold or arial (and JPedal will then try to select the
//         * bold weight if a Java font is used).
//         *
//         * So for a font such as Arial,Bold JPedal will test for an external
//         * truetype font substitution (ie arialMT.ttf) mapped to Arial,Bold. BUT
//         * if the substitute font is a Java font an additional test will be made
//         * for a match against Arial if there is no match on Arial,Bold.
//         *
//         * If you want to map all Arial to equivalents to a Java font such as
//         * Times New Roman, just map Arial to Times New Roman (only works for
//         * inbuilt java fonts). Note if you map Arial,Bold to a Java font such
//         * as Times New Roman, you will get Times New Roman in a bold weight, if
//         * available. You cannot set a weight for the Java font.
//         *
//         * If you wish to substitute Arial but not Arial,Bold you should
//         * explicitly map Arial,Bold to Arial,Bold as well.
//         *
//         * The reason for the difference is that when using Javas inbuilt fonts
//         * JPedal can resolve the Font Family and will try to work out the
//         * weight internally. When substituting Truetype fonts, these only
//         * contain ONE weight so JPedal is resolving the Font and any weight as
//         * a separate font . Different weights will require separate files.
//         *
//         * Open Source version does not support all font capabilities.
//         */
//        /**
//         * FONT EXAMPLE - Use fonts placed in jar for substitution (1.4 and
//         * above only)
//         *
//         * This allows users to store fonts in the jar and use these for
//         * substitution. Please see javadoc for full description of usage.
//         */
//        //decode_pdf.addSubstituteFonts(fontPath,enforceMapping)
//        /**
//         * FONT EXAMPLE - Use fonts located on machine for substitution
//         *
//         * This code explains how to use JPedal to substitute fonts which are
//         * not embedded using fonts held in any font directory.
//         *
//         * It works as follows:-
//         *
//         * If the -Dorg.jpedal.fontdirs="C:/win/fonts/","/mnt/X11/fonts" is set
//         * to a comma-separated list of directories, any truetype fonts (with
//         * .ttf file ending) will be logged and added to the substitution table.
//         * So arialMT.ttf will be added as arialmt. If arialmt is used in the
//         * PDF but not embedded, JPedal will use this font file to render it.
//         *
//         * If a command line paramter is not appropriate, the call
//         * setFontDirs(String[] fontDirs) will achieve the same.
//         *
//         *
//         * If the name is not an exact match (ie you have arialMT which you wish
//         * to use to display arial, you can use the method
//         * setSubstitutedFontAliases(String[] name, String[] aliases) to convert
//         * it internally - see sample code at bottom of note.
//         *
//         * The Name is not case-sensitive.
//         *
//         * Spaces are important so TimesNewRoman and Times New Roman are
//         * degarded as 2 fonts.
//         *
//         * If you have 2 copies of arialMT.ttf in the scanned directories, the
//         * last one will be used.
//         *
//         * If the file was called arialMT,bold.ttf it is resolved as
//         * ArialMT,bold only.
//         *
//         */
//        //mappings for non-embedded fonts to use
//        FontMappings.setFontReplacements();
//
//        //decode_pdf.setFontDirs(new String[]{"C:/windows/fonts/","C:/winNT/fonts/"});
//        /**
//         * FONT EXAMPLE - Use Standard Java fonts for substitution
//         *
//         * This code tells JPedal to substitute fonts which are not embedded.
//         *
//         * The Name is not case-sensitive.
//         *
//         * Spaces are important so TimesNewRoman and Times New Roman are
//         * degarded as 2 fonts.
//         *
//         * If you have 2 copies of arialMT.ttf in the scanned directories, the
//         * last one will be used.
//         *
//         *
//         * If you wish to use one of Javas fonts for display (for example, Times
//         * New Roman is a close match for myCompanyFont in the PDF, you can the
//         * code below
//         *
//         * String[] aliases={"Times New Roman"};//,"helvetica","arial"};
//         * decode_pdf.setSubstitutedFontAliases("myCompanyFont",aliases);
//         *
//         * Here is is used to map Javas Times New Roman (and all weights) to
//         * TimesNewRoman.
//         *
//         * This can also be done with the command
//         * -org.jpedal.fontmaps="TimesNewRoman=Times New Roman","font2=pdfFont1"
//         */
//        //String[] nameInPDF={"TimesNewRoman"};//,"helvetica","arial"};
//        //decode_pdf.setSubstitutedFontAliases("Times New Roman",nameInPDF);
//        /**
//         * add in external handlers for code - 2 examples supplied
//         *
//         *
//         * //org.jpedal.external.ImageHandler myExampleImageHandler=new
//         * org.jpedal.examples.handlers.ExampleImageDecodeHandler();
//         * org.jpedal.external.ImageHandler myExampleImageHandler=new
//         * org.jpedal.examples.handlers.ExampleImageDrawOnScreenHandler();
//         *
//         * decode_pdf.addExternalHandler(myExampleImageHandler,
//         * Options.ImageHandler);
//         *
//         *
//         * /*
//         */
//        /**
//         * divert all message to our custom code
//         *
//         *
//         * CustomMessageHandler myExampleCustomMessageHandler =new
//         * ExampleCustomMessageHandler();
//         *
//         * decode_pdf.addExternalHandler(myExampleCustomMessageHandler,
//         * Options.CustomMessageOutput);
//         *
//         * /*
//         */
//    }
//
//    /**
//     * private boolean showMenu(String input){ //Check for disabled options
//     *
//     * if(restrictedMenus!=null) for(int i=0; i!=restrictedMenus.length; i++)
//     * if(((String)restrictedMenus[i]).toLowerCase().equals(input.toLowerCase()))
//     * return true; return false;
//  }/*
//     */
//    /**
//     * create items on drop down menus
//     */
//    protected void createSwingMenu(boolean includeAll) {
//        currentGUI.createMainMenu(includeAll);
//    }
//
//    /**
//     * main method to run the software as standalone application
//     */
//    public static void main(String[] args) {
//
//        /**
//         * set the look and feel for the GUI components to be the default for
//         * the system it is running on
//         */
//        try {
//            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//        } catch (Exception e) {
//            LogWriter.writeLog("Exception " + e + " setting look and feel");
//        }
//
////    SimpleViewer current;
////    String prefFile = System.getProperty("org.jpedal.SimpleViewer.Prefs");
////    if(prefFile != null){
////      current = new SimpleViewer(prefFile);
////      current.setupViewer();
////    }else{
//        SimpleViewer current = new SimpleViewer();
//        current.setupViewer();
////    }
//
//        //<start-wrap>
//        if (args.length > 0) {
//            current.openDefaultFile(args[0]);
//
//        } else if (current.properties.getValue("openLastDocument").toLowerCase().equals("true")) {
//            if (current.properties.getRecentDocuments() != null
//                    && current.properties.getRecentDocuments().length > 1) {
//
//                int lastPageViewed = Integer.parseInt(current.properties.getValue("lastDocumentPage"));
//
//                if (lastPageViewed < 0) {
//                    lastPageViewed = 1;
//                }
//
//                current.openDefaultFileAtPage(current.properties.getRecentDocuments()[0], lastPageViewed);
//            }
//
//        }
//        /**
//         * //<end-wrap> current.openDefaultFile();
//    /*
//         */
//    }
//
//    /**
//     * General code to open file at specified boomark - do not call directly
//     *
//     * @param file File the PDF to be decoded
//     * @param bookmark - if not present, exception will be thrown
//     */
//    private void openFile(File file, String bookmark) {
//
//        try {
//
//            boolean fileCanBeOpened = currentCommands.openUpFile(file.getCanonicalPath());
//
//            Object bookmarkPage = null;
//
//            int page = -1;
//
//            //reads tree and populates lookup table
//            if (decode_pdf.getOutlineAsXML() != null) {
//                Node rootNode = decode_pdf.getOutlineAsXML().getFirstChild();
//                if (rootNode != null) {
//                    bookmarkPage = currentGUI.getBookmark(bookmark);
//                }
//
//                if (bookmarkPage != null) {
//                    page = Integer.parseInt((String) bookmarkPage);
//                }
//            }
//
//            //it may be a named destination ( ie bookmark=Test1)
//            if (bookmarkPage == null) {
//                bookmarkPage = decode_pdf.getIO().convertNameToRef(bookmark);
//
//                if (bookmarkPage != null) {
//
//                    //read the object
//                    PdfObject namedDest = new OutlineObject((String) bookmarkPage);
//                    decode_pdf.getIO().readObject(namedDest);
//
//                    //still needed to init viewer
//                    if (fileCanBeOpened) {
//                        currentCommands.processPage();
//                    }
//
//                    //and generic open Dest code
//                    decode_pdf.getFormRenderer().getActionHandler().gotoDest(namedDest, ActionHandler.MOUSECLICKED, PdfDictionary.Dest);
//                }
//            }
//
//            if (bookmarkPage == null) {
//                throw new PdfException("Unknown bookmark " + bookmark);
//            }
//
//
//            if (page > -1) {
//                commonValues.setCurrentPage(page);
//                if (fileCanBeOpened) {
//                    currentCommands.processPage();
//                }
//            }
//        } catch (Exception e) {
//            System.err.println("Exception " + e + " processing file");
//
//
//            commonValues.setProcessing(false);
//        }
//    }
//
//    /**
//     * General code to open file at specified page - do not call directly
//     *
//     * @param file File the PDF to be decoded
//     * @param page int page number to show the user
//     */
//    private void openFile(File file, int page) {
//
//        try {
//            boolean fileCanBeOpened = currentCommands.openUpFile(file.getCanonicalPath());
//
//            commonValues.setCurrentPage(page);
//
//            if (fileCanBeOpened) {
//                currentCommands.processPage();
//            }
//        } catch (Exception e) {
//            System.err.println("Exception " + e + " processing file");
//
//
//            commonValues.setProcessing(false);
//        }
//    }
//
//    /**
//     * Execute Jpedal functionality from outside of the library using this
//     * method. EXAMPLES commandID = Commands.OPENFILE, args =
//     * {"/PDFData/Hand_Test/crbtrader.pdf}" commandID = Commands.OPENFILE, args
//     * = {byte[] = {0,1,1,0,1,1,1,0,0,1}, "/PDFData/Hand_Test/crbtrader.pdf}"
//     * commandID = Commands.ROTATION, args = {"90"} commandID =
//     * Commands.OPENURL, args =
//     * {"http://www.cs.bham.ac.uk/~axj/pub/papers/handy1.pdf"}
//     *
//     * @param commandID :: static int value from Commands to spedify which
//     * command is wanted
//     * @param args :: arguements for the desired command
//     *
//     */
//    public Object executeCommand(int commandID, Object[] args) {
//
//        /**
//         * far too easy to miss this step (I did!) so warn user
//         */
//        if (!isSetup) {
//            throw new RuntimeException("You must call simpleViewer.setupViewer(); before you call any commands");
//        }
//
//        return currentCommands.executeCommand(commandID, args);
//    }
//
//    public SearchList getSearchResults() {
//        return currentCommands.getSearchList();
//    }
//
//    public boolean isProcessing() {
//        return commonValues.isProcessing();
//    }
//
//    /**
//     * Allows external helper classes to be added to JPedal to alter default
//     * functionality. <br><br>If Options.FormsActionHandler is the type then the
//     * <b>newHandler</b> should be of the form
//     * <b>org.jpedal.objects.acroforms.ActionHandler</b> <br><br>If
//     * Options.JPedalActionHandler is the type then the <b>newHandler</b> should
//     * be of the form <b>Map</b> which contains Command Integers, mapped onto
//     * their respective
//     * <b>org.jpedal.examples.simpleviewer.gui.swing.JPedalActionHandler</b>
//     * implementations. For example, to create a custom help action, you would
//     * add to your map, Integer(Commands.HELP) -> JPedalActionHandler. For a
//     * tutorial on creating custom actions in the SimpleViewer, see
//     * <b>http://www.jpedal.org/support.php</b>
//     *
//     * @param newHandler
//     * @param type
//     */
//    public void addExternalHandler(Object newHandler, int type) {
//        decode_pdf.addExternalHandler(newHandler, type);
//    }
//
//    public void dispose() {
//
//
//        commonValues = null;
//
//        currentPrinter = null;
//
//
//        if (thumbnails != null) {
//            thumbnails.dispose();
//        }
//
//        thumbnails = null;
//
//        properties.dispose();
//        properties = null;
//
//        if (currentGUI != null) {
//            currentGUI.dispose();
//        }
//
//        currentGUI = null;
//
//        searchFrame = null;
//
//        currentCommands = null;
//
//        mouseHandler = null;
//
//        scalingValues = null;
//
//        //restrictedMenus=null;
//
//        if (decode_pdf != null) {
//            decode_pdf.dispose();
//        }
//
//        decode_pdf = null;
//
//        Messages.dispose();
//
//
//
//    }
//}