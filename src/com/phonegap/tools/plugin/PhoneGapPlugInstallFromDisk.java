package com.phonegap.tools.plugin;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anis on 9/21/16.
 */
public class PhoneGapPlugInstallFromDisk extends PhoneGapPlugInstall {

    @Override
    public void install(Project project) {
        FileChooserDescriptor fcd = new FileChooserDescriptor(false, true, false, false, false, false);
        fcd.setDescription("Please select PhoneGap/Cordova plugin folder");
        fcd.setTitle("Select PhoneGap Plugin");
        VirtualFile pluginDir = FileChooser.chooseFile(fcd, project, null);
        if (pluginDir != null) {
            Notification info = new Notification("PlugmanInstall", "Installing plugin ",
                    "Please be patient…", NotificationType.INFORMATION);
            info.expire();
            Notifications.Bus.notify(info);
            new Thread(() -> {
                List<String> cmd = new ArrayList<>();
                cmd.add("plugman");
                cmd.add("install");
                cmd.add("--platform");
                cmd.add("android");
                cmd.add("--plugin");
                cmd.add(pluginDir.getPath());
                cmd.add("--project");
                cmd.add(project.getBasePath());
                int exitCode = runProcess(cmd);
                Notification postInstall;
                if(exitCode != 0) {
                    postInstall = new Notification("PluginstallError", "Error!", "Error installing plugin", NotificationType.ERROR);
                } else {
                    postInstall = new Notification("PluginstallSucess", "Success!", "Plugin installed successfully!", NotificationType.INFORMATION);
                }
                Notifications.Bus.notify(postInstall);
                project.getBaseDir().refresh(false, true);
            }).start();
        }
    }
}
