package com.phonegap.tools.plugin;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anis on 9/16/16.
 */
public class PhoneGapPlugInstallFromNpm extends PhoneGapPlugInstall {

    @Override
    public void install(Project project) {
        String pluginName = Messages.showInputDialog(project, "Enter plugin name (select from http://cordova.apache.org/plugins/)",
                "Which Plugin ?", Messages.getQuestionIcon());
        if(pluginName != null) {
            Notification info = new Notification("PlugmanInstall", "Installing plugin ",
                    "Please be patient…", NotificationType.INFORMATION);
            info.expire();
            Notifications.Bus.notify(info);
            new Thread(() -> {
                List<String> cmd = new ArrayList<String>();
                cmd.add("plugman");
                cmd.add("install");
                cmd.add("--platform");
                cmd.add("android");
                cmd.add("--plugin");
                cmd.add(pluginName);
                cmd.add("--project");
                cmd.add(project.getBasePath());
                int exitCode = runProcess(cmd);
                Notification postInstall;
                if(exitCode != 0) {
                    postInstall = new Notification("PluginstallError", "Error!", "Error installing plugin", NotificationType.ERROR);
                } else {
                    postInstall = new Notification("PluginstallSucess", "Success!", "Plugin Installed successfully!", NotificationType.INFORMATION);
                }
                Notifications.Bus.notify(postInstall);
                project.getBaseDir().refresh(false, true);
            }).start();
        }
    }
}
