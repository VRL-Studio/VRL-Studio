///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package eu.mihosoft.vrlstudio.io;
//
//import eu.mihosoft.vrl.annotation.*;
//import eu.mihosoft.vrl.io.*;
//import eu.mihosoft.vrl.io.IOUtil;
//import eu.mihosoft.vrl.io.vrlx.VRLXSessionController;
//import eu.mihosoft.vrl.io.vrlx.VRLXReflection;
//import eu.mihosoft.vrl.reflection.VisualCanvas;
//import eu.mihosoft.vrl.reflection.VisualObject;
//import eu.mihosoft.vrl.visual.CanvasWindow;
//import eu.mihosoft.vrl.visual.MessageType;
//import java.io.BufferedReader;
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileReader;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.io.Serializable;
//import java.util.ArrayList;
//
///**
// *
// * @author Michael Hoffer <info@michaelhoffer.de>
// */
//@ComponentInfo(name = "Session Exporter")
//public class SessionExporter implements Serializable {
//
//    private static final long serialVersionUID = 1;
//    private transient VisualCanvas mainCanvas;
//    private static final String SEP = File.separator;
//    private static String SOURCE_FOLDER =
//            "resources" + SEP + "templates" + SEP + "VRL-Player";
////    private static String BUNDLE_FOLDER =
////            "resources" + SEP + "templates" + SEP + "bundles" + SEP +
////            "VRL-Player.app" + SEP + "Contents";
//
//    @MethodInfo(noGUI = true, callOptions = "assign-to-canvas")
//    public void setMainCanvas(VisualCanvas mainCanvas) {
//        this.mainCanvas = mainCanvas;
//    }
//
//    @MethodInfo(valueOptions = "hideConnector=true")
//    public void export(
//            @ParamInfo(name = "mode:",
//            options = "hideConnector=true") String mode,
//            @ParamInfo(name = "resolution:",
//            options = "hideConnector=true") String resolution,
//            @ParamInfo(name = "options:",
//            options = "hideConnector=true") String options,
//            @ParamInfo(name = "destination:",
//            style = "save-dialog") File destination)
//            throws FileNotFoundException, IOException, Exception {
//
//        modifyUnixScript("run-32bit", mode, resolution, options);
//        modifyUnixScript("run-64bit", mode, resolution, options);
//
//        modifyWindowsScript("run-32bit.bat", mode, resolution, options);
//        modifyWindowsScript("run-64bit.bat", mode, resolution, options);
//
////        VisualObject w = mainCanvas.getCanvasWindow(this);
////
////        mainCanvas.getInspector().
////                removeObject(w.getObjectRepresentation().getID());
////
////        w.close();
//
//
//        try {
//            copyStudioLibToTemplate();
//            saveSession();
//
//            zipFolder(SOURCE_FOLDER, destination);
//
//            deleteStudioLibFromTemplate();
//        } catch (Exception ex) {
//            String message = ex.toString();
//
//            if (ex.getCause() != null) {
//                message += " caused by " + ex.getCause().toString();
//            }
//
//            mainCanvas.getMessageBox().addMessage("Can't export session:",
//                    message,
//                    MessageType.ERROR);
//        }
//    }
//
//    private void zipFolder(String folder, File destination) throws Exception {
//        String destCheck = destination.getPath().toLowerCase();
//        String finalDestination = new String(destination.getPath());
//        if (!destCheck.matches(".*\\.zip")) {
//            finalDestination += ".zip";
//        }
//
//        String destFolderName = destination.getName();
//
//        String newFolder = destination.getName();
//
//
//        // remove ".zip"
//        if (newFolder.toLowerCase().endsWith(".zip")) {
//            // ".zip" is of length 4
//            newFolder = newFolder.substring(0, destFolderName.length() - 4);
//        }
//
//        IOUtil.zipFolderWithCustomName(folder, newFolder, finalDestination);
//    }
//
//    private void saveSession()
//            throws FileNotFoundException, Exception {
//        VRLXSessionController sessionController =
//                new VRLXSessionController(
//                VRLXReflection.getVRLXFormat());
//        sessionController.saveSession(mainCanvas, new File(SOURCE_FOLDER
//                + "/bin/resources/player-resources/default.vrlx"));
//    }
//
////    public void createBundle() throws IOException {
////        IOUtil.copyDirectory(new File(BUNDLE_FOLDER),
////                new File(BUNDLE_FOLDER + SEP + "MacOS"));
////        IOUtil.copyDirectory(new File(SOURCE_FOLDER),
////                new File(BUNDLE_FOLDER + SEP + "MacOS"));
////    }
//    private void modifyUnixScript(String scriptName, String mode,
//            String resolution, String options)
//            throws FileNotFoundException, IOException {
//        String runScript = SOURCE_FOLDER + SEP + scriptName;
//
//        File script = new File(runScript);
//
//        String s = script.getAbsolutePath();
//
//        FileReader fileReader = new FileReader(script);
//        BufferedReader reader = new BufferedReader(fileReader);
//
//        ArrayList<String> lines = new ArrayList<String>();
//
//        if (options == null) {
//            options = "";
//        }
//
//        while (reader.ready()) {
//            String line = reader.readLine();
//
//            if (line.matches(".*PLAYERCONF.*=.*")) {
//                line = "PLAYERCONF=\""
//                        + "-mode " + mode + " "
//                        + "-resolution " + resolution + " "
//                        + options + " "
//                        + "resources/player-resources/default.vrlx\"";
//            }
//
//            lines.add(line);
//        }
//
//        reader.close();
//
//        FileWriter fileWriter = new FileWriter(script);
//        BufferedWriter writer = new BufferedWriter(fileWriter);
//
//        for (String line : lines) {
//            writer.write(line + "\n");
//        }
//
//        writer.flush();
//        writer.close();
//    }
//
//    private void modifyWindowsScript(String scriptName, String mode,
//            String resolution, String options)
//            throws FileNotFoundException, IOException {
//        String runScript = SOURCE_FOLDER + SEP + scriptName;
//
//        File script = new File(runScript);
//        FileReader fileReader = new FileReader(script);
//        BufferedReader reader = new BufferedReader(fileReader);
//
//        ArrayList<String> lines = new ArrayList<String>();
//
//        if (options == null) {
//            options = "";
//        }
//
//        while (reader.ready()) {
//            String line = reader.readLine();
//
//            if (line.matches(".*PLAYERCONF.*=.*")) {
//                line = "set PLAYERCONF="
//                        + "-mode " + mode + " "
//                        + "-resolution " + resolution + " "
//                        + options + " "
//                        + "resources\\player-resources\\default.vrlx";
//            }
//
//            lines.add(line);
//        }
//
//        reader.close();
//
//        FileWriter fileWriter = new FileWriter(script);
//        BufferedWriter writer = new BufferedWriter(fileWriter);
//
//        for (String line : lines) {
//            writer.write(line + "\n");
//        }
//
//        writer.flush();
//        writer.close();
//    }
//
//    private void copyStudioLibToTemplate() throws IOException {
//        IOUtil.copyDirectory(new File("lib"),
//                new File(SOURCE_FOLDER + SEP + "bin" + SEP + "lib"));
//        IOUtil.copyDirectory(new File("custom-lib"),
//                new File(SOURCE_FOLDER + SEP + "bin" + SEP + "custom-lib"));
//    }
//
//    private void deleteStudioLibFromTemplate() throws IOException {
//        IOUtil.deleteDirectory(new File(SOURCE_FOLDER + SEP + "bin" + SEP + "lib"));
//        IOUtil.deleteDirectory(new File(SOURCE_FOLDER + SEP + "bin" + SEP + "custom-lib"));
//    }
//}
