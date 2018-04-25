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

import com.intellij.openapi.projectRoots.Sdk;
import com.jetbrains.python.run.PythonRunConfiguration;
import com.jetbrains.python.run.PythonScriptCommandLineState;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yole
 */
public class NaginiRunner extends GenericProgramRunner {
    public static final String NAGINI_MAIN_LOC = "src/nagini_translation/main.py";
    public static final String NAGINI_CLIENT_LOC = "src/nagini_translation/client.py";


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
        NaginiSettingsComponent settings = NaginiSettingsComponent.getInstance();

        ExecutionResult executionResult;
        RunProfile profile = env.getRunProfile();
        PythonRunConfiguration pprofile = (PythonRunConfiguration) profile;
        String oldScriptParameters = pprofile.getScriptParameters();
        String oldScriptName = pprofile.getScriptName();
        Map<String, String> oldEnvs = pprofile.getEnvs();
        String oldWorkingDir = pprofile.getWorkingDirectory();

        Map<String, String> envs = new HashMap<String, String>();
        String workingDir = settings.getNaginiDir() + File.separator + "src";

        boolean useServer = settings.getUseServer();

        if (useServer){
            Sdk sdk = ((PythonScriptCommandLineState)state).getSdk();

            NaginiServer.startIfNeeded(sdk.getHomePath());

            String naginiClientPath = settings.getNaginiDir() + File.separator + NaginiRunner.NAGINI_CLIENT_LOC;

            pprofile.setScriptParameters(pprofile.getScriptName());
            pprofile.setScriptName(naginiClientPath);

        }else{
            String verifier = settings.getVerifier();
            String args = "--verifier " + verifier.toLowerCase() + " --viper-jar-path " + (verifier.equals("Silicon") ? settings.getSiliconJar() : settings.getCarbonJar()) + " ";

            pprofile.setScriptParameters(args + pprofile.getScriptName());
            String naginiMainPath = settings.getNaginiDir() + File.separator + NaginiRunner.NAGINI_MAIN_LOC;
            pprofile.setScriptName(naginiMainPath);
            envs.put("MYPYDIR", settings.getMypyDir());
            //envs.put("MYPYPATH", "");
            envs.put("PYTHONPATH", workingDir);
            envs.put("Z3_EXE", settings.getZ3Path());
            envs.put("BOOGIE_EXE", settings.getBoogiePath());
        }


        pprofile.setEnvs(envs);
        pprofile.setWorkingDirectory(workingDir);


        try {
            executionResult = state.execute(env.getExecutor(), this);
            executionResult.getProcessHandler().addProcessListener(new NaginiProcessListener(pprofile.getProject(), oldScriptName));
        }finally{
            pprofile.setScriptName(oldScriptName);
            pprofile.setScriptParameters(oldScriptParameters);
            pprofile.setEnvs(oldEnvs);
            pprofile.setWorkingDirectory(oldWorkingDir);
        }

        return DefaultProgramRunnerKt.showRunContent(executionResult, env);
    }
}
