/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrlstudio.io;

import eu.mihosoft.vrl.io.IOUtil;
import eu.mihosoft.vrl.io.SynchronizedFileAccess;
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
 * This class is responsible for managing a complete VRL-Studio update.
 *
 * Some methods are called from the Studio process. Others must be called from
 * the updater process (see method documentation for details).
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
public class StudioBundleUpdater {

    /**
     * Java Bean for command-line options.
     */
    private static CmdOptions options;
    /**
     * The update logger.
     */
    private static Logger logger =
            Logger.getLogger(StudioBundleUpdater.class.getName());
    /**
     * The process object of the updater
     */
    private static Process studioUpdaterProcess;
    /**
     * Indicates whether the updater process is running, i.e., if this instance
     * is from the updater process's JVM.
     */
    private static boolean runningUpdater;
    public static final String PREV_VERSION_EXTENSION = "-PREV-VER";

    /**
     * Initializes the updater logger.
     */
    private static void initLogger() {
        try {
            FileHandler fileHandler = new FileHandler("VRL-Studio-Updater.log",
                    1024 * 1024 * 1 /*
                     * MB
                     */, 5);
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

    /**
     * Main method of the update process. It will be delegated from
     * {@link Studio#main(java.lang.String[])} if it has been called with the
     * <code>-updater</code> command-line option (must be the first option).
     *
     * @param args update arguments (see {@link CmdOptions} for possible
     * options)
     */
    public static void main(String[] args) {

        runningUpdater = true;

        initLogger();

        logger.info(">> updater running:");
        options = new CmdOptions();
        CmdLineParser parser = new CmdLineParser(options);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            logger.log(Level.SEVERE, null, e);
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

        if (!options.getPropertyFolder().isDirectory()) {
            String msg = "-property-folder: specified value is no directory: "
                    + options.getPropertyFolder();
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

        while (parentProcessStillRunning()) {
            logger.log(Level.INFO, ">> watching pid: " + options.getPid()
                    + " because of strange Windows file locking we have to wait :( \n"
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

        // if no delta-update, move VRL-Studio folder to VRL-Studio-PREV-VER
        File prevVersion = new File(options.getTargetFolder().getAbsolutePath()
                + PREV_VERSION_EXTENSION);
        // delete prevVersion if it already exists
        IOUtil.deleteDirectory(prevVersion);
        // move current (old) version to PREV-Version
        IOUtil.move(options.getTargetFolder(), prevVersion);

//        IOUtil.deleteDirectory(options.getTargetFolder());

        // if update bundle cannot be copied to final bundle location
        // silently quit
        if (!copyUpdateToFinalBundle()) {
            logger.log(Level.SEVERE, ">> cannot copy update to final bundle");
            return;
        }

        // if running new studio bundle is successful then delete the
        // updater bundle files
        // - we keep them otherwise to allow debugging
        if (runNewStudio()) {

            // delete contents of updates folder
            for (File f : options.getUpdateFolder().listFiles()) {
                IOUtil.deleteTmpFilesOnExit(f);
            }

        }
    }

    /**
     * Determines if the parent (Studio) process is still running.
     *
     * <p><b>Note:</b> only call this from update process.</p>
     *
     * @return <code>true</code> if the Studio process is running;
     * <code>false</code> otherwise
     */
    private static boolean parentProcessStillRunning() {

        throwIfNotCallingFromUpdaterProcess();

        if (!VSysUtil.isWindows()) {
            return VSysUtil.isRunning(options.getPid());
        } else {
            File lockFile = new File(options.getPropertyFolder(), ".lock");
            return SynchronizedFileAccess.isLocked(lockFile);
        }
    }

    /**
     * @throws IllegalStateException if not running from the updater process
     */
    private static void throwIfNotCallingFromUpdaterProcess() {
        if (!isRunningUpdater()) {
            throw new IllegalStateException(
                    "This method must not be called from the Studio process!");
        }
    }

    /**
     * @throws IllegalStateException if not running from the studio process
     */
    private static void throwIfNotCallingFromStudioProcess() {
        if (isRunningUpdater()) {
            throw new IllegalStateException(
                    "This method must not be called from the Updater process!");
        }
    }

    /**
     * Creates the update bundle in a temorary folder.
     *
     * <p><b>Note:</b> only call this from Studio process.</p>
     *
     * @param input the input/source (.zip) file that shall be unzipped
     * @return the tmp folder containing the updater bundle or <code>null</code>
     * if the updater bundle cannot be created
     */
    private static File createUpdaterBundle(File input) {

        throwIfNotCallingFromStudioProcess();

        Studio.logger.info(" --> creating update-bundle-folder");
        File tmpFolder = null;
        try {
            tmpFolder = IOUtil.createTempDir(
                    VRL.getPropertyFolderManager().getUpdatesFolder());
        } catch (IOException ex) {
            Logger.getLogger(StudioBundleUpdater.class.getName()).
                    log(Level.SEVERE, null, ex);
            Studio.logger.severe(
                    " --> StudioBundleUpdater:"
                    + " cannot create tmp dir for update bundle");
            return null;
        }

        if (!VSysUtil.isWindows()) {
            try {
                Studio.logger.info(
                        " --> unzip: " + input + " -> " + tmpFolder);
                IOUtil.copyFile(
                        input, new File(tmpFolder + "/" + input.getName()));

                Process p = Runtime.getRuntime().exec(
                        "unzip " + input.getName(), null, tmpFolder);

                BufferedReader inputS = new BufferedReader(
                        new InputStreamReader(p.getInputStream()));

                String line = null;

                while ((line = inputS.readLine()) != null) {
                    System.out.println(" --> unzip: " + line);
                }

            } catch (IOException ex) {
                Logger.getLogger(StudioBundleUpdater.class.getName()).
                        log(Level.SEVERE, null, ex);
                Studio.logger.severe(
                        " --> StudioBundleUpdater:"
                        + " cannot unpack update bundle");
                return null;
            }
        } else {
            try {
                Studio.logger.info(
                        " --> unzip: " + input + " -> " + tmpFolder);
                IOUtil.copyFile(input,
                        new File(tmpFolder + "/" + input.getName()));
                IOUtil.unzip(input, tmpFolder);
            } catch (IOException ex) {
                Logger.getLogger(
                        StudioBundleUpdater.class.getName()).
                        log(Level.SEVERE, null, ex);
                Studio.logger.severe(
                        " --> StudioBundleUpdater:"
                        + " cannot unpack update bundle");
            }
        }

        Studio.logger.info(" --> update-bundle-folder: " + tmpFolder);

        return tmpFolder;
    }

    /**
     * Runs the updater process that updates the studio.
     *
     * <p><b>Note:</b> only call this from Studio process.</p>
     *
     * @return <code>true</code> if the updater process could be executed;
     * <code>false</code> otherwise
     */
    public static boolean runStudioUpdater(File source) {

        throwIfNotCallingFromStudioProcess();

        Studio.logger.info(">> running studio update");

        File target = Studio.APP_FOLDER;

        File bundleFolder = createUpdaterBundle(source);

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
                    + "-update-folder "
                    + VRL.getPropertyFolderManager().getUpdatesFolder().getAbsolutePath() + " "
                    + "-property-folder "
                    + VRL.getPropertyFolderManager().getPropertyFolder();

            Studio.logger.info(">> final command: " + command);

            try {
                System.out.println(" --> running Unix install");
                studioUpdaterProcess = Runtime.getRuntime().exec(command);

            } catch (IOException ex) {
                Logger.getLogger(StudioBundleUpdater.class.getName()).
                        log(Level.SEVERE, null, ex);
                Studio.logger.severe(">> cannot run update-bundle: "
                        + bundleFolder);
                return false;
            }
        } else {
            String runPath = "updater\\run-update.bat";
            String inPath = "\\VRL-Studio";

            Studio.logger.info(">> updater run path: " + runPath);
            Studio.logger.info(">> updater in path: " + inPath);

            String command = "cmd /C start "
                    + runPath + " "
                    + "-i " + bundleFolder + inPath + " "
                    + "-o " + target.getAbsolutePath() + " "
                    + "-pid " + VSysUtil.getPID() + " "
                    + "-update-folder " + VRL.getPropertyFolderManager().getUpdatesFolder().getAbsolutePath() + " "
                    + "-property-folder " + VRL.getPropertyFolderManager().getPropertyFolder();

            Studio.logger.info(">> final command: " + command);

            try {
                System.out.println(" --> running Unix install");
                studioUpdaterProcess = Runtime.getRuntime().exec(
                        command, null, new File(bundleFolder,
                        "VRL-Studio\\.application"));

            } catch (IOException ex) {

                Studio.logger.severe(">> cannot run update-bundle: "
                        + bundleFolder);

                Studio.logger.log(Level.SEVERE, null, ex);
                return false;
            }
        }

        return true;
    }

    /**
     * Runs the updates Studio process.
     *
     * <p><b>Note:</b> only call this from update process.</p>
     *
     * @return <code>true</code> if the Studio process could be executed;
     * <code>false</code> otherwise
     */
    private static boolean runNewStudio() {

        throwIfNotCallingFromUpdaterProcess();

        logger.info(">> running new studio");

        File bundleFolder = options.getTargetFolder();

        if (VSysUtil.isLinux()) {

            try {
                System.out.println(" --> running Unix install");
                Process p = Runtime.getRuntime().exec("nohup "
                        + bundleFolder.getAbsolutePath()
                        + "/run -updated", null, bundleFolder.getAbsoluteFile());

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
                        + bundleFolder.getAbsolutePath() + " --args -updated");

            } catch (IOException ex) {
                Logger.getLogger(StudioBundleUpdater.class.getName()).
                        log(Level.SEVERE, null, ex);
                logger.severe(">> cannot run studio-bundle: "
                        + bundleFolder);

                return false;
            }
        } else {
            logger.info(">> windows: " + bundleFolder.getAbsolutePath());
            try {
                System.out.println(" --> running Windows install");
                String cmd = "cmd /C start run.bat -updated";
                logger.info(">> windows: " + cmd);
                Process p = Runtime.getRuntime().exec(
                        cmd, null,
                        bundleFolder.getAbsoluteFile());

            } catch (IOException ex) {
                Logger.getLogger(StudioBundleUpdater.class.getName()).
                        log(Level.SEVERE, null, ex);
                logger.severe(">> cannot run studio-bundle: "
                        + bundleFolder);

                return false;
            }
        }

        return true;
    }

    /**
     * Prints a usage message if the updater process has been started with wrong
     * options.
     *
     * @param parser parser used for argument parsing
     */
    private static void printUsage(CmdLineParser parser) {
        System.err.println("Don't call this programm manually!");
        parser.printUsage(System.err);
    }

    /**
     * Copies the update to the final Studio bundle location.
     *
     * <p><b>Note:</b> only call this from update process.</p>
     *
     * @return <code>true</code> if successful; <code>false</code> otherwise
     */
    private static boolean copyUpdateToFinalBundle() {

        throwIfNotCallingFromUpdaterProcess();

        if (!VSysUtil.isWindows()) {
            try {
                logger.info(" --> cp: "
                        + options.getSourceFolder()
                        + " -> " + options.getTargetFolder());

                Process p = Runtime.getRuntime().exec(
                        "cp -rv " + options.getSourceFolder().getAbsolutePath()
                        + " " + options.getTargetFolder().getAbsoluteFile().
                        getParentFile().getAbsolutePath() + "");

                // TODO why does waitFor() hang? (29.01.2013)
//                try {
//                    p.waitFor();
//                } catch (InterruptedException ex) {
//                    Logger.getLogger(StudioBundleUpdater.class.getName()).
//                log(Level.SEVERE, null, ex);
//                }

                BufferedReader input = new BufferedReader(
                        new InputStreamReader(p.getInputStream()));

                String line = null;

                while ((line = input.readLine()) != null) {
                    logger.info(" --> updater: " + line);
                }


            } catch (IOException ex) {
                Logger.getLogger(StudioBundleUpdater.class.getName()).
                        log(Level.SEVERE, null, ex);
                logger.severe("cannot copy update bundle");
                return false;
            }
        } else {
            try {
                IOUtil.copyDirectory(options.getSourceFolder(),
                        options.getTargetFolder().getAbsoluteFile());
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }

        logger.info(
                " --> cp: "
                + options.getSourceFolder()
                + " -> " + options.getTargetFolder());

        return true;
    }

    /**
     * @return <code>true</code> if this instance of StudioBundleUpdater belongs
     * to the updater process; <code>false</code> otherwise
     */
    public static boolean isRunningUpdater() {
        return runningUpdater;
    }
}
