import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by anis on 9/16/16.
 */
public abstract class PhoneGapPlugInstall extends AnAction {

    private final static Logger LOGGER = Logger.getLogger(PhoneGapInit.class.getName());
    private static String OS = null;

    public static String getOsName() {
        if (OS == null) {
            OS = System.getProperty("os.name");
        }
        return OS;
    }

    protected int runProcess(List<String> command) {
        int exitCode = 69;
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            BufferedReader br = null;
            String line;
            try {
                br = new BufferedReader(new InputStreamReader(process.getInputStream()));
                while((line = br.readLine()) != null) {
                    output.append(line + System.getProperty("line.separator"));
                }
            } finally {
                br.close();
            }

            exitCode = process.waitFor();
            Notification result;
            if(exitCode != 0) {
                result = new Notification("ExecuteCmdError", "Error!", "Command Output "+output.toString()+" ExitCode: "+exitCode, NotificationType.ERROR);
            } else {
                result = new Notification("ExecuteCmdSuccess", "Success!", "Command Output "+output.toString()+" ExitCode: "+exitCode, NotificationType.INFORMATION);
            }
            result.expire();
            Notifications.Bus.notify(result);


        } catch(Exception e) {
            e.printStackTrace();
        }
        return exitCode;

    }

    protected boolean checkEnvironment() {

        List<String> nodeCmd = new ArrayList<>(),
                plugmanCmd = new ArrayList<>();

        if (getOsName().startsWith("Windows")) {
            nodeCmd.add("where");
            plugmanCmd.add("where");
        } else {
            nodeCmd.add("which");
            plugmanCmd.add("which");
        }
        nodeCmd.add("node");
        plugmanCmd.add("plugman");
        int exitCode = runProcess(nodeCmd);
        if (exitCode != 0) {
            LOGGER.severe("Node not found!");
            return false;
        }

        exitCode = runProcess(plugmanCmd);
        if (exitCode != 0) {
            LOGGER.severe("plugman not found!");
            return false;
        }
        return true;
    }

    // used to disable Install Plugin
    public void update(AnActionEvent event) {

        super.update(event);

        event.getPresentation().setVisible(false);

        Project project = event.getProject();

        ModuleManager moduleManager = ModuleManager.getInstance(project);
        Module appModule = moduleManager.findModuleByName("app");

        File moduleFile = new File(appModule.getModuleFilePath());
        File appDir = moduleFile.getParentFile();

        try {
            File buildFile = new File(appDir + "/build.gradle");

            GradleDependencyUpdater updater = new GradleDependencyUpdater(buildFile);

            List<GradleDependency> allDependencies = updater.getAllDependencies();

            for(GradleDependency dependency : allDependencies) {
                if(dependency.getGroup() != null && dependency.getGroup().compareTo("org.apache.cordova") == 0) {
                    event.getPresentation().setVisible(true);
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public abstract void install(Project project);

    public void actionPerformed(AnActionEvent event) {
        Project project = event.getProject();
        if (checkEnvironment() == false) {
            LOGGER.fine("checking environment");
            Messages.showMessageDialog(project, "You need to install NodeJS and plugman in order to be able to use plugins. If you are on Mac OS X make sure you run the following: `sudo launchctl config user path $PATH`", "Error", Messages.getErrorIcon());
            return;
        }
        install(project);
    }
}
