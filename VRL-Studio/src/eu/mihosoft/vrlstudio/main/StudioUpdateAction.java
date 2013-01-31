/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrlstudio.main;

import eu.mihosoft.vrl.io.Download;
import eu.mihosoft.vrl.io.IOUtil;
import eu.mihosoft.vrl.io.VProjectController;
import eu.mihosoft.vrl.reflection.VisualCanvas;
import eu.mihosoft.vrl.system.RepositoryEntry;
import eu.mihosoft.vrl.system.VRL;
import eu.mihosoft.vrl.system.VRLUpdateActionBase;
import eu.mihosoft.vrl.system.VRLUpdater;
import eu.mihosoft.vrl.system.VSysUtil;
import eu.mihosoft.vrl.visual.CanvasActionListener;
import eu.mihosoft.vrl.visual.UpdateNotifierApplet;
import eu.mihosoft.vrl.visual.VDialog;
import eu.mihosoft.vrl.visual.VSwingUtil;
import eu.mihosoft.vrlstudio.io.StudioBundleUpdater;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
class StudioUpdateAction extends VRLUpdateActionBase {

    private UpdateNotifierApplet updateApplet;
    private VRLUpdater currentUpdater;
    private Download currentDownload;
    private RepositoryEntry currentUpdate;
    private URL currentURL;
    private boolean projectClosed = false;

    public StudioUpdateAction() {


        // create the update notification icon
        this.updateApplet = new UpdateNotifierApplet(getCurrentCanvas());

        // register action: if user clicks on the icon available updates
        // will be installed
        updateApplet.setActionListener(new CanvasActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (updateApplet.isActive()) {

                    // only allow update if currently no update is in progrss
                    if (!currentUpdater.isDownloadingRepository()
                            && !currentUpdater.isDownloadingUpdate()) {

                        StudioUpdateAction.super.updateAvailable(
                                StudioUpdateAction.this.currentUpdater,
                                currentDownload,
                                currentURL,
                                currentUpdate);
                    }
                }
            }
        });

        // if we open a new canvas we add the update icon to it
        VRL.getCurrentProjectController().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (ae.getActionCommand().equals(VProjectController.ON_CANVAS_OPEN)) {
                    if (updateApplet.isActive()) {
                        VisualCanvas canvas = VRL.getCurrentProjectController().
                                getCurrentCanvas();
                        canvas.getDock().addDockAppletAfter(
                                canvas.getMessageBoxApplet(), updateApplet);
                    }
                }
            }
        });

        // ... and remove it from the old canvas
        VRL.getCurrentProjectController().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (ae.getActionCommand().equals(VProjectController.ON_CANVAS_CLOSE)) {
                    VRL.getCurrentProjectController().getCurrentCanvas()
                            .getDock().removeDockApplet(updateApplet);
                }
            }
        });


    }

    @Override
    public void checkForUpdates(VRLUpdater updater, Download d, URL location) {
        //
    }

    private VisualCanvas getCurrentCanvas() {
        return VRL.getCurrentProjectController().getCurrentCanvas();
    }

    @Override
    public void updateAvailable(final VRLUpdater updater, Download d,
            URL location, final RepositoryEntry update) {

        VisualCanvas canvas = getCurrentCanvas();

        canvas.getDock().removeDockApplet(updateApplet);
        canvas.getDock().
                addDockAppletAfter(canvas.getMessageBoxApplet(), updateApplet);

        updateApplet.showApplet();
        updateApplet.setActive(true);

        currentUpdater = updater;
        currentDownload = d;
        currentUpdate = update;
        currentURL = location;
    }

    @Override
    public void installAction(
            VRLUpdater updater,
            RepositoryEntry update, File updateFile) {

        // verify download
        if (updater.isVerificationEnabled()
                && !updater.isVerificationSuccessful()) {
            VDialog.showMessageDialog(getCurrentCanvas(), "VRL-Studio Update Failed",
                    "<html><div align=\"center\">"
                    + "The update verification failed! Please try again later!"
                    + "</div></html>");

            IOUtil.deleteDirectory(updateFile);
            return;
        }

        // verify free disk space
        File appfolder = IOUtil.getRootParent(Studio.APP_FOLDER);
        long freeSpace = IOUtil.getFreeSpaceOnPartition(appfolder);
        long updateSize = IOUtil.getFileSize(updateFile);

        System.out.println(">> free diskspace on " + appfolder.getAbsolutePath()
                + ": " + freeSpace / 1024 / 1024 + " MB");
        System.out.println(">> update size: "
                + updateSize / 1024 / 1024 + " MB");

        // we request 5 times more space than the update size
        // (we temporarily need some space to ensure we can create backup copies
        //  in case something goes wrong)
        if (freeSpace < 5 * IOUtil.getFileSize(updateFile)) {
            VDialog.showMessageDialog(getCurrentCanvas(),
                    "VRL-Studio Update Failed",
                    "<html><div align=\"center\">"
                    + "Not enough space on "
                    + appfolder.getAbsolutePath()
                    + ".<br>"
                    + "Delete unused files and try again."
                    + "</div></html>");
            IOUtil.deleteDirectory(updateFile);
            return;
        }

        File targetFile = null;
        try {

            // check whether we can write to the app folder (for update)
            boolean weHavePrivileges = Studio.APP_FOLDER.canWrite();

            if (weHavePrivileges) {

                targetFile =
                        new File(
                        VRL.getPropertyFolderManager().getUpdatesFolder()
                        + "/" + updateFile.getName());
            } else {
                // if we cannot write to the app folder, we save the update in
                // the download folder
                // - the user has to manually install VRL-Studio
                targetFile =
                        new File(
                        System.getProperty("user.home")
                        + "/Downloads/" + updateFile.getName());
            }

            IOUtil.copyFile(updateFile, targetFile);

            // if we can write to the VRL-Studio folder we can run the automatic
            // install process
            if (weHavePrivileges) {

                VSwingUtil.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            projectClosed = VRL.getCurrentProjectController().
                                    closeProject(true, "Update VRL-Studio");
                        } catch (IOException ex) {
                            Logger.getLogger(StudioUpdateAction.class.getName()).
                                    log(Level.SEVERE, null, ex);
                        }
                    }
                });

                if (projectClosed) {
                    VSwingUtil.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            getCurrentCanvas().getEffectPane().startSpin();
                        }
                    });
                    StudioBundleUpdater.runStudioUpdater(targetFile);
                    VRL.exit(0);
                }
            } else {

                // if we cannot write to the update folder we download the
                // update and ask the user to manually unpack/start the folder

                VDialog.showMessageDialog(getCurrentCanvas(),
                        "Update Downloaded",
                        "<html><div align=\"center\">"
                        + "<b>VRL-Studio v" + update.getVersion()
                        + " has been downloaded to:</b><br><br>"
                        + "" + targetFile.getAbsolutePath() + "<br><br>"
                        + "<b>How To Use The New Version?</b><br><br>"
                        + "VRL-Studio will be closed now.<br><br>"
                        + "<b>Unpack</b> the file shown above and <b>run</b> "
                        + "the new version of <b>VRL-Studio</b>!"
                        + "</div></html>");

                VSysUtil.openFileInDefaultFileBrowser(targetFile);
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Studio.class.getName()).
                    log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Studio.class.getName()).
                    log(Level.SEVERE, null, ex);
        } finally {
            getCurrentCanvas().getDock().removeDockApplet(updateApplet);
            updateApplet.setActive(false);
        }
    }

    @Override
    public void updateDownloadStateChanged(Download d) {
        updateApplet.setProgress((int) d.getProgress());
        updateApplet.setToolTipText("Downloading - " + (int) d.getProgress() + "%");

        if (d.getStatus() == Download.ERROR) {
            updateApplet.setActive(false);
            VDialog.showMessageDialog(getCurrentCanvas(), "Cannot Update",
                    "<html><div align=\"center\">"
                    + "<b>Update cannot be downloaded. Try again later.</b>"
                    + "</div></html>");
            d.cancel();

        }

        if (d.getStatus() == Download.COMPLETE) {
            updateApplet.setProgress(0);
            updateApplet.setToolTipText("Download Complete");
        }
    }

    @Override
    public void startVerification(Download d) {
        getCurrentCanvas().getEffectPane().startSpin();
        updateApplet.setToolTipText("Verifying Download...");
    }

    @Override
    public void stopVerification(Download d, boolean verificationSuccessful) {
        getCurrentCanvas().getEffectPane().stopSpin();
        if (verificationSuccessful) {
            updateApplet.setToolTipText("Verification Successful");
        } else {
            updateApplet.setToolTipText("Verification Failed!");
        }
    }
}
