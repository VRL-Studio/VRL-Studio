/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrlstudio.io;

import eu.mihosoft.vrl.io.IOUtil;
import eu.mihosoft.vrl.io.TextSaver;
import eu.mihosoft.vrl.lang.VLangUtils;
import eu.mihosoft.vrl.system.VSysUtil;
import eu.mihosoft.vrlstudio.main.Studio;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.Target;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

/**
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
public class StudioBundleUpdater {

    private static CmdOptions options;

    public static void main(String[] args) {
        System.out.println(">> updater running:");
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
            System.err.println("-i: specified value is no dorectory: " + options.getSourceFolder());
            printUsage(parser);
            wrongOptions = true;
        }

        if (!options.getTargetFolder().isDirectory()) {
            System.err.println("-o: specified value is no directory: " + options.getTargetFolder());
            printUsage(parser);
            wrongOptions = true;
        }
        
        while (VSysUtil.isRunning(options.getPid())) {
            System.out.println(">> because of strange Windows file locking we have to wait :( \n"
                             + "   (we are lazy and use the same code on unix as well)");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(StudioBundleUpdater.class.getName()).
                        log(Level.SEVERE, null, ex);
            }
        }
        
        TextSaver saver = new TextSaver();
        
        String msg = "s: " + options.getSourceFolder() + " t: " + options.getTargetFolder();
        
        try {
            IOUtil.copyDirectory(options.getSourceFolder(), options.getTargetFolder());
        } catch (IOException ex) {
            Logger.getLogger(StudioBundleUpdater.class.getName()).
                    log(Level.SEVERE, null, ex);
             System.err.println(" --> cannot copy directory: " + options.getSourceFolder());
             return;
        }
        try {
            saver.saveFile(msg, new File("/home/miho/tmp/out.txt"), ".txt");
        } catch (IOException ex) {
            Logger.getLogger(StudioBundleUpdater.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        runNewStudio();
    }

    private static File createUpdateBundle(File input) {

        System.out.println(" --> creating update-bundle-folder");
        File tmpFolder = null;
        try {
            tmpFolder = IOUtil.createTempDir();
        } catch (IOException ex) {
            Logger.getLogger(StudioBundleUpdater.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println(" --> StudioBundleUpdater: cannot create tmp dir for update bundle");
            return null;
        }

        try {
            System.out.println(" --> unzip: " + input + " -> " + tmpFolder);
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
            System.err.println(" --> StudioBundleUpdater: cannot unpack update bundle");
            return null;
        }

        System.out.println(" --> update-bundle-folder: " + tmpFolder);

        return tmpFolder;
    }

    public static boolean runStudioUpdate(File source) {

        System.out.println(">> running studio update");

        File target = Studio.APP_FOLDER;

        File bundleFolder = createUpdateBundle(source);

        if (bundleFolder == null) {
            System.err.println(" --> no update-bundle");
            return false;
        }

        if (!VSysUtil.isWindows()) {
            try {
                System.out.println(" --> running Unix install");
                Process p = Runtime.getRuntime().exec("nohup " + 
                        bundleFolder.getAbsolutePath()
                        + "/VRL-Studio/.application/updater/run-update "
                        + "-i " + bundleFolder + "/VRL-Studio "
                        + "-o " + target.getAbsolutePath() + " "
                        + "-pid " + VSysUtil.getPID());

                BufferedReader input = new BufferedReader(
                        new InputStreamReader(p.getErrorStream()));

                String line = null;

                while ((line = input.readLine()) != null) {
                    System.err.println(" --> updater: " + line);
                }

            } catch (IOException ex) {
                Logger.getLogger(StudioBundleUpdater.class.getName()).
                        log(Level.SEVERE, null, ex);
                System.err.println(">> cannot run update-bundle: "
                        + bundleFolder);
                return false;
            }
        } else {
            System.out.println(" --> running Windows install: not implemented");
        }

        return true;
    }
    
    private static boolean runNewStudio() {

        System.out.println(">> running studio update");

        File bundleFolder = Studio.APP_FOLDER;
        
        if (!VSysUtil.isWindows()) {
            try {
                System.out.println(" --> running Unix install");
                Process p = Runtime.getRuntime().exec("nohup " + 
                        bundleFolder.getAbsolutePath()
                        + "/VRL-Studio/.application/run");

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
                System.err.println(">> cannot run stidop-bundle: "
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
}
