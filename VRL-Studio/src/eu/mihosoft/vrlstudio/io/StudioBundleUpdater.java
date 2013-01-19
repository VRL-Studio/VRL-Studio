/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrlstudio.io;

import eu.mihosoft.vrl.io.IOUtil;
import eu.mihosoft.vrl.io.TextSaver;
import eu.mihosoft.vrl.system.VRL;
import eu.mihosoft.vrl.system.VSysUtil;
import eu.mihosoft.vrlstudio.main.Studio;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

/**
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
public class StudioBundleUpdater {

    private static CmdOptions options;
    private static Logger logger = 
            Logger.getLogger(StudioBundleUpdater.class.getName());
    private static Process studioUpdaterProcess;
//    private static File exitFile;
    
    public boolean isUpdaterProcessRunning() {
        
        // process is null and hasn't started yet
        if (studioUpdaterProcess == null) {
            return false;
        }
        
        try {
            // we try to get exit value
            // if this is possible we know that the process has been terminated
            studioUpdaterProcess.exitValue();
            
            return false;
        } catch (IllegalThreadStateException ex) {
            // the process hasn't been terminated. thus, we return true
            return true;
        }
    }

    private static void initLogger() {
        try {
            FileHandler fileHandler = new FileHandler("VRL-Studio-Updater.log",
                    1024 * 1024 * 1 /*MB*/, 5);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
        } catch (IOException ex) {
            Logger.getLogger(StudioBundleUpdater.class.getName()).
                    log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(StudioBundleUpdater.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {
        initLogger();

        logger.info(">> updater running:");
        options = new CmdOptions();
        CmdLineParser parser = new CmdLineParser(options);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            printUsage(parser);
            return;
        }

        boolean wrongOptions = false;

        if (!options.getSourceFolder().isDirectory()) {
            String msg = "-i: specified value is no directory: " 
                    + options.getSourceFolder();
            logger.log(Level.SEVERE, msg);
            printUsage(parser);
            wrongOptions = true;
        }

        if (!options.getTargetFolder().isDirectory()) {
            String msg = "-o: specified value is no directory: " 
                    + options.getTargetFolder();
            logger.log(Level.SEVERE, msg);
            printUsage(parser);
            wrongOptions = true;
        }

        if (!options.getUpdateFolder().isDirectory()) {
            String msg = "-update-folder: specified value is no directory: " 
                    + options.getUpdateFolder();
            logger.log(Level.SEVERE, msg);
            printUsage(parser);
            wrongOptions = true;
        }

        if (wrongOptions) {
            logger.log(Level.SEVERE, "UPDATER CALLED WITH WRONG OPTIONS");
            return;
        }

        System.out.println(">> updater waiting...");
        
//        exitFile = new File(options.getUpdateFolder(),"exit-updater");

        while (VSysUtil.isRunning(options.getPid())) {
            logger.log(Level.INFO, ">> because of strange Windows file locking we have to wait :( \n"
                    + "   (we are lazy and use the same code on unix as well)");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(StudioBundleUpdater.class.getName()).
                        log(Level.SEVERE, null, ex);
            }
            
//            // stop update
//            if (exitFile.exists()) {
//                logger.log(Level.INFO, ">> stopping update (requested by host process)");
//                IOUtil.deleteDirectory(exitFile);
//                return;
//            }
        }

        // if no delta-update, delete VRL-Studio folder
        IOUtil.deleteDirectory(options.getTargetFolder());

        if (!copyUpdateToFinalBundle()) {
            return;
        }

        // delete contents of updates folder
        for (File f : options.getUpdateFolder().listFiles()) {
            IOUtil.deleteTmpFilesOnExit(f);
        }

        runNewStudio();
    }

    private static File createUpdateBundle(File input) {

        Studio.logger.info(" --> creating update-bundle-folder");
        File tmpFolder = null;
        try {
            tmpFolder = IOUtil.createTempDir(VRL.getPropertyFolderManager().getUpdatesFolder());
        } catch (IOException ex) {
            Logger.getLogger(StudioBundleUpdater.class.getName()).log(Level.SEVERE, null, ex);
            Studio.logger.severe(" --> StudioBundleUpdater: cannot create tmp dir for update bundle");
            return null;
        }

        try {
            Studio.logger.info(" --> unzip: " + input + " -> " + tmpFolder);
            IOUtil.copyFile(input, new File(tmpFolder + "/" + input.getName()));

            Process p = Runtime.getRuntime().exec("unzip " + input.getName(), null, tmpFolder);

            BufferedReader inputS = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));

            String line = null;

            while ((line = inputS.readLine()) != null) {
                //System.err.println(" --> unzip: " + line);
            }

        } catch (IOException ex) {
            Logger.getLogger(StudioBundleUpdater.class.getName()).log(Level.SEVERE, null, ex);
            Studio.logger.severe(" --> StudioBundleUpdater: cannot unpack update bundle");
            return null;
        }

        Studio.logger.info(" --> update-bundle-folder: " + tmpFolder);

        return tmpFolder;
    }

    public static boolean runStudioUpdate(File source) {

        Studio.logger.info(">> running studio update");

        File target = Studio.APP_FOLDER;

        File bundleFolder = createUpdateBundle(source);

        if (bundleFolder == null) {
            Studio.logger.severe(" --> no update-bundle");
            return false;
        }


        if (!VSysUtil.isWindows()) {

            String runPath = "/VRL-Studio/.application/updater/run-update";
            String inPath = "/VRL-Studio";

            if (VSysUtil.isMacOSX()) {
                runPath = "/VRL-Studio.app/Contents/Resources/.application/updater/run-update";
                inPath = "/VRL-Studio.app";
            }

            Studio.logger.info(">> updater run path: " + runPath);
            Studio.logger.info(">> updater in path: " + inPath);

            String command = "nohup "
                    + bundleFolder.getAbsolutePath() + runPath + " "
                    + "-i " + bundleFolder + inPath + " "
                    + "-o " + target.getAbsolutePath() + " "
                    + "-pid " + VSysUtil.getPID() + " "
                    + "-update-folder " + VRL.getPropertyFolderManager().getUpdatesFolder().getAbsolutePath();

            Studio.logger.info(">> final command: " + command);

            try {
                System.out.println(" --> running Unix install");
                studioUpdaterProcess = Runtime.getRuntime().exec(command);

//                BufferedReader input = new BufferedReader(
//                        new InputStreamReader(p.getErrorStream()));
//
//                String line = null;
//
//                while ((line = input.readLine()) != null) {
//                    System.err.println(" --> updater: " + line);
//                }

            } catch (IOException ex) {
                Logger.getLogger(StudioBundleUpdater.class.getName()).
                        log(Level.SEVERE, null, ex);
                Studio.logger.severe(">> cannot run update-bundle: "
                        + bundleFolder);
                return false;
            }
        } else {
            System.out.println(" --> running Windows install: not implemented");
        }

        return true;
    }
    
    private static boolean runNewStudio() {

        logger.info(">> running new studio");

        File bundleFolder = options.getTargetFolder();

        if (VSysUtil.isLinux()) {

            try {
                System.out.println(" --> running Unix install");
                Process p = Runtime.getRuntime().exec("nohup "
                        + bundleFolder.getAbsolutePath()
                        + "/run", null, bundleFolder.getAbsoluteFile());

            } catch (IOException ex) {
                Logger.getLogger(StudioBundleUpdater.class.getName()).
                        log(Level.SEVERE, null, ex);
                logger.severe(">> cannot run studio-bundle: "
                        + bundleFolder);

                return false;
            }
        } else if (VSysUtil.isMacOSX()) {

            logger.info(">> osx: " + bundleFolder.getAbsolutePath());
            try {
                System.out.println(" --> running Mac install");
                Process p = Runtime.getRuntime().exec("open "
                        + bundleFolder.getAbsolutePath());

            } catch (IOException ex) {
                Logger.getLogger(StudioBundleUpdater.class.getName()).
                        log(Level.SEVERE, null, ex);
                logger.severe(">> cannot run studio-bundle: "
                        + bundleFolder);

                return false;
            }
        } else {
            System.out.println(" --> running Windows install: not implemented");
        }

        return true;
    }

    private static void printUsage(CmdLineParser parser) {
        System.err.println("Don't call this programm manually!");
        parser.printUsage(System.err);
    }

    private static boolean copyUpdateToFinalBundle() {
        //        try {
        //            IOUtil.copyDirectory(options.getSourceFolder(), options.getTargetFolder());
        //        } catch (IOException ex) {
        //            Logger.getLogger(StudioBundleUpdater.class.getName()).
        //                    log(Level.SEVERE, null, ex);
        //            System.err.println(" --> cannot copy directory: " + options.getSourceFolder());
        //            return;
        //        }

        if (!VSysUtil.isWindows()) {
            try {
                logger.info(" --> cp: " + options.getSourceFolder() + " -> " + options.getTargetFolder());

                Process p = Runtime.getRuntime().exec(
                        "cp -rv " + options.getSourceFolder().getAbsolutePath()
                        + " " + options.getTargetFolder().getAbsoluteFile().getParentFile().getAbsolutePath() + "");
//                try {
//                    p.waitFor();
//                } catch (InterruptedException ex) {
//                    Logger.getLogger(StudioBundleUpdater.class.getName()).log(Level.SEVERE, null, ex);
//                }

                BufferedReader input = new BufferedReader(
                        new InputStreamReader(p.getInputStream()));

                String line = null;

                while ((line = input.readLine()) != null) {
                    logger.info(" --> updater: " + line);
                }


            } catch (IOException ex) {
                Logger.getLogger(StudioBundleUpdater.class.getName()).log(Level.SEVERE, null, ex);
                logger.severe("cannot copy update bundle");
                return false;
            }
        } else {
            //
        }

        logger.info(" --> cp: " + options.getSourceFolder() + " -> " + options.getTargetFolder());

        return true;
    }
}
