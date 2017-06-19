package ch.ethz.inf.pm.nagini;

import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.impl.AnnotationHolderImpl;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.codeInsight.daemon.impl.UpdateHighlightersUtil;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.DefaultProgramRunnerKt;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.GenericProgramRunner;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.AnnotationSession;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileDocumentManager;

import com.jetbrains.python.run.PythonRunConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yole
 */
public class NaginiRunner extends GenericProgramRunner {
    private static final String NAGINI_MAIN_PATH = "/home/marco/scion/git/nagini/src/nagini_translation/main.py";
    private static final String WORKING_DIR = "/home/marco/scion/git/nagini/src";
    private static final String MYPYDIR = "/usr/local/bin/mypy";
    private static final String NAGINI_ARGS = "--verifier silicon --viper-jar-path /viper/testqp/silicon/target/scala-2.11/silicon.jar ";

    @Override
    @NotNull
    public String getRunnerId() {
        return "NaginiRunner";
    }

    @Override
    public boolean canRun(@NotNull final String executorId, @NotNull final RunProfile profile) {
        boolean result = executorId.equals(NaginiExecutor.EXECUTOR_ID) ;
        result = result && profile.getClass().toString().contains("PythonRunConfiguration");
        return result;
    }

    @Override
    protected RunContentDescriptor doExecute(@NotNull RunProfileState state, @NotNull ExecutionEnvironment env) throws ExecutionException {
        FileDocumentManager.getInstance().saveAllDocuments();

        ExecutionResult executionResult;
        RunProfile profile = env.getRunProfile();
        PythonRunConfiguration pprofile = (PythonRunConfiguration) profile;
        String oldScriptParameters = pprofile.getScriptParameters();
        String oldScriptName = pprofile.getScriptName();
        Map<String, String> oldEnvs = pprofile.getEnvs();
        String oldWorkingDir = pprofile.getWorkingDirectory();

        pprofile.setScriptParameters(NAGINI_ARGS + pprofile.getScriptName());
        pprofile.setScriptName(NAGINI_MAIN_PATH);
        Map<String, String> envs = new HashMap<String, String>();
        envs.put("MYPYDIR", MYPYDIR);
        envs.put("MYPYPATH", "");
        envs.put("PYTHONPATH", WORKING_DIR);
        pprofile.setEnvs(envs);
        pprofile.setWorkingDirectory(WORKING_DIR);

        executionResult = state.execute(env.getExecutor(), this);
        executionResult.getProcessHandler().addProcessListener(new NaginiProcessListener(pprofile.getProject(), oldScriptName));
        pprofile.setScriptName(oldScriptName);
        pprofile.setScriptParameters(oldScriptParameters);
        pprofile.setEnvs(oldEnvs);
        pprofile.setWorkingDirectory(oldWorkingDir);

        return DefaultProgramRunnerKt.showRunContent(executionResult, env);
    }
}
