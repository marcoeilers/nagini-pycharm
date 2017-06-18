package ch.ethz.inf.pm.nagini;

import com.intellij.execution.Executor;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.ToolWindowId;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Created by marco on 18.06.17.
 */
public class NaginiExecutor extends Executor {
    public static final Icon NAGINI_ICON = IconLoader.getIcon("/naginiIcon.png", NaginiExecutor.class); // 16x16
    public static final String EXECUTOR_ID = "Verification";

    @NotNull
    public String getStartActionText() {
        return "Verify";
    }

    @Override
    public String getStartActionText(String configurationName) {
        final String name = configurationName != null ? escapeMnemonicsInConfigurationName(StringUtil.first(configurationName, 30, true)) : null;
        return "Verify" + (StringUtil.isEmpty(name) ? "" :  " '" + name + "'");
    }


    private static String escapeMnemonicsInConfigurationName(String configurationName) {
        return configurationName.replace("_", "__");
    }

    public String getToolWindowId() {
        return ToolWindowId.RUN;
    }

    public Icon getToolWindowIcon() {
        return NAGINI_ICON;
    }

    @NotNull
    public Icon getIcon() {
        return NAGINI_ICON;
    }

    public Icon getDisabledIcon() {
        return null;
    }

    public String getDescription() {
        return "Verify selected module.";
    }

    @NotNull
    public String getActionName() {
        return "Verify";
    }

    @NotNull
    public String getId() {
        return EXECUTOR_ID;
    }

    public String getContextActionId() {
        return "Verify";
    }

    public String getHelpId() {
        return null;//todo
    }
}
