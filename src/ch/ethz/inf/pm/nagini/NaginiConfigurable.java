package ch.ethz.inf.pm.nagini;

import com.intellij.application.options.ModuleAwareProjectConfigurable;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.UnnamedConfigurable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Created by marco on 18.06.17.
 */
public class NaginiConfigurable extends ModuleAwareProjectConfigurable {
    public NaginiConfigurable(@NotNull Project project, String displayName, String helpTopic) {
        super(project, displayName, helpTopic);
    }

    @NotNull
    @Override
    protected UnnamedConfigurable createModuleConfigurable(Module module) {
        return null;
    }
}
