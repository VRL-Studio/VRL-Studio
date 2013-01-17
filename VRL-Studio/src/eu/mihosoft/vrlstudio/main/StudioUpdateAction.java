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
import eu.mihosoft.vrlstudio.io.StudioBundleUpdater;
import java.awt.Desktop;
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
    
    public StudioUpdateAction() {
        this.updateApplet = new UpdateNotifierApplet(getCurrentCanvas());
        
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
         File targetFile = null;
        try {
//            targetFile =
//                    new File(
//                    System.getProperty("user.home")
//                    + "/Downloads/" + updateFile.getName());
//            IOUtil.copyFile(updateFile, targetFile);
            
            targetFile =
                    new File(
                    VRL.getPropertyFolderManager().getUpdatesFolder()
                    + "/"+updateFile.getName());
            IOUtil.copyFile(updateFile, targetFile);
            
//            VMessage.info("Update downloaded:",
//                    ">> VRL-Studio " + update.getVersion()
//                    + " has been downloaded to: "
//                    + targetFile.getAbsolutePath());

//            VDialog.showMessageDialog(getCurrentCanvas(), "Update Downloaded",
//                    "<html><div align=\"center\">"
//                    + "<b>VRL-Studio v" + update.getVersion()
//                    + " has been downloaded to:</b><br><br>"
//                    + "" + targetFile.getAbsolutePath() + "<br><br>"
//                    + "<b>How To Use The New Version?</b><br><br>"
//                    + "VRL-Studio will be closed now.<br><br>"
//                    + "<b>Unpack</b> the file shown above and <b>run</b> the new version of <b>VRL-Studio</b>!"
//                    + "</div></html>");
            
//            VSysUtil.openFileInDefaultFileBrowser(targetFile);
            
            StudioBundleUpdater.runStudioUpdate(targetFile);
            
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Studio.class.getName()).
                    log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Studio.class.getName()).
                    log(Level.SEVERE, null, ex);
        } finally {
            getCurrentCanvas().getDock().removeDockApplet(updateApplet);
            updateApplet.setActive(false);
            
            
            
            Studio.THIS.quitApplication();
        }
    }
    
    @Override
    public void updateDownloadStateChanged(Download d) {
        updateApplet.setProgress((int)d.getProgress());
        
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
        }

    }
}
