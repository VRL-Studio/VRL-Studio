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
import eu.mihosoft.vrl.system.VMessage;
import eu.mihosoft.vrl.system.VRL;
import eu.mihosoft.vrl.system.VRLUpdateActionBase;
import eu.mihosoft.vrl.system.VRLUpdater;
import eu.mihosoft.vrl.visual.Canvas;
import eu.mihosoft.vrl.visual.CanvasActionListener;
import eu.mihosoft.vrl.visual.UpdateNotifierApplet;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
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
                    
                    StudioUpdateAction.super.updateAvailable(
                            StudioUpdateAction.this.currentUpdater,
                            currentDownload,
                            currentURL,
                            currentUpdate);
                }
            }
        });
        
        VRL.getCurrentProjectController().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (ae.getActionCommand().equals(VProjectController.ON_CANVAS_OPEN)) {
                    if (updateApplet.isActive()) {
                        VRL.getCurrentProjectController().getCurrentCanvas()
                                .getDock().addDockApplet(updateApplet);
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
        canvas.getDock().addDockApplet(updateApplet);
        
        
        
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
        try {
            File targetFile =
                    new File(
                    System.getProperty("user.home")
                    + "/Downloads/" + updateFile.getName());
            IOUtil.copyFile(updateFile, targetFile);
            VMessage.info("Update downloaded:",
                    ">> VRL-Studio " + update.getVersion()
                    + " has been downloaded to: "
                    + targetFile.getAbsolutePath());
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
}
