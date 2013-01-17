/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrlstudio.io;

import java.io.File;
import org.kohsuke.args4j.Option;

/**
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
public class CmdOptions {

    @Option(name = "-o", usage = "target folder", required = true)
    private File targetFolder;
    @Option(name = "-i", usage = "source folder", required = true)
    private File sourceFolder;
    @Option(name = "-update-folder", usage = "update folder (will be deleted)", required = true)
    private File updateFolder;
    @Option(name = "-pid", usage = "parent process id", required = true)
    private int pid;

    /**
     * @return the targetFolder
     */
    public File getTargetFolder() {
        return targetFolder;
    }

    /**
     * @param targetFolder the targetFolder to set
     */
    public void setTargetFolder(File targetFolder) {
        this.targetFolder = targetFolder;
    }

    /**
     * @return the sourceFile
     */
    public File getSourceFolder() {
        return sourceFolder;
    }

    /**
     * @param sourceFile the sourceFile to set
     */
    public void setSourceFolder(File sourceFile) {
        this.sourceFolder = sourceFile;
    }

    /**
     * @return the pid
     */
    public int getPid() {
        return pid;
    }

    /**
     * @param pid the pid to set
     */
    public void setPid(int pid) {
        this.pid = pid;
    }

    /**
     * @return the updateFolder
     */
    public File getUpdateFolder() {
        return updateFolder;
    }

    /**
     * @param updateFolder the updateFolder to set
     */
    public void setUpdateFolder(File updateFolder) {
        this.updateFolder = updateFolder;
    }
}
