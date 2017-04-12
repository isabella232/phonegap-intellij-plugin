import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode;
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.sun.istack.internal.NotNull;
import org.jetbrains.plugins.gradle.util.GradleConstants;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by anis on 9/2/16.
 */
public class PhoneGapInit extends AnAction {

    private final static Logger LOGGER = Logger.getLogger(PhoneGapInit.class.getName());
    private static boolean extracting = false;

    public PhoneGapInit() {
        super("Init _Cordova");
    }

    // used to disable Init Project
    public void update(AnActionEvent event) {
        super.update(event);

        event.getPresentation().setVisible(!extracting);

        Project project = event.getProject();

        ModuleManager moduleManager = ModuleManager.getInstance(project);
        Module appModule = moduleManager.findModuleByName("app");

        File moduleFile = new File(appModule.getModuleFilePath());
        File appDir = moduleFile.getParentFile();

        try {
            File buildFile = new File(appDir + "/build.gradle");

            GradleDependencyUpdater updater = new GradleDependencyUpdater(buildFile);

            List<GradleDependency> allDependencies = updater.getAllDependencies();

            for (GradleDependency dependency : allDependencies) {
                if (dependency.getGroup() != null && dependency.getGroup().compareTo("org.apache.cordova") == 0) {
                    System.out.println("found that cordova bitch");
                    event.getPresentation().setVisible(false);
                } else {
                    System.out.println("Name: " + dependency.getName() + " Version: " + dependency.getVersion() + " Group: " + dependency.getGroup());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void actionPerformed(AnActionEvent event) {
        Project project = event.getProject();
        try {
            Path temp = Files.createTempFile("cordova-init-", ".zip");
            Files.copy(this.getClass().getClassLoader().getResourceAsStream("/resources/cordova-init.zip"), temp, StandardCopyOption.REPLACE_EXISTING);
            String destination = project.getBasePath();
            if (project == null) return;

            ProgressManager.getInstance().run(new Task.Backgroundable(project, "PhoneGap") {
                public void onSuccess() {
                    ModuleManager moduleManager = ModuleManager.getInstance(project);
                    Module appModule = moduleManager.findModuleByName("app");

                    File moduleFile = new File(appModule.getModuleFilePath());
                    File appDir = moduleFile.getParentFile();

                    try {
                        File buildFile = new File(appDir + "/build.gradle");

                        GradleDependencyUpdater updater = new GradleDependencyUpdater(buildFile);

                        updater.insertDependency("\tcompile 'org.apache.cordova:framework:6.1.2:release@aar'");

                        Files.write(buildFile.toPath(), updater.getGradleFileContents(), StandardCharsets.UTF_8);

//                            ActionManager am = ActionManager.getInstance().getInstance();
//                            AnAction sync = am.getAction("Android.SyncProject");
//                            sync.actionPerformed(new AnActionEvent(null, DataManager.getInstance().getDataContext(),
//                                    ActionPlaces.UNKNOWN, new Presentation(),
//                                    ActionManager.getInstance(), 0));

                        ExternalSystemUtil.refreshProject(
                                project, GradleConstants.SYSTEM_ID, appDir.getPath(), false,
                                ProgressExecutionMode.IN_BACKGROUND_ASYNC);

                        // refresh project to see changes
                        project.getBaseDir().refresh(false, true);

                        Notification info = new Notification("PhoneGapInit", "You're rocking PhoneGap!", "PhoneGap was successfully added to your Android project", NotificationType.INFORMATION);
                        Notifications.Bus.notify(info);
                        extracting = false;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                public void run(@NotNull ProgressIndicator progressIndicator) {
                    try {
                        Notification info = new Notification("PhoneGapInit", "Initializing PhoneGap", "Please be patient…", NotificationType.INFORMATION);
                        info.expire();
                        Notifications.Bus.notify(info);
                        extracting = true;
                        ZipUtils zipUtils = new ZipUtils();
                        zipUtils.unzip(temp.toFile(), destination, progressIndicator);
                    } catch (IOException e) {
                        LOGGER.severe("Can't unzip cordova-init for unknown reasons");
                    }
                    Notification info = new Notification("PhoneGapInit", "Initializing PhoneGap", "Please be patient…", NotificationType.INFORMATION);
                    info.expire();
                    Notifications.Bus.notify(info);
                    extracting = true;
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
