package ch.ethz.inf.pm.nagini;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by marco on 21.06.17.
 */
public class NaginiServer {
    private static Process currentProcess = null;
    private static String lastConfig = null;
    private static boolean startedBefore = false;
    public static void startProcess(String pythonPath, String args){
        if (currentProcess != null){
            currentProcess.destroyForcibly();
            currentProcess = null;
        }
        NaginiSettingsComponent settings = NaginiSettingsComponent.getInstance();
        String naginiMainPath = settings.getNaginiDir() + File.separator + NaginiRunner.NAGINI_MAIN_LOC;
        String workingDir = settings.getNaginiDir() + File.separator + "src";
        String[] splitArgs = args.split(" ");
        String[] allArgs = new String[splitArgs.length + 2];
        allArgs[0] = pythonPath;
        allArgs[1] = naginiMainPath;
        System.arraycopy(splitArgs, 0, allArgs, 2, splitArgs.length);
        ProcessBuilder pb = new ProcessBuilder(allArgs);
        pb.directory(new File(workingDir));
        Map<String, String> envs = pb.environment();
        envs.put("MYPYDIR", settings.getMypyDir());
        //envs.put("MYPYPATH", "");
        envs.put("PYTHONPATH", workingDir);
        envs.put("Z3_EXE", settings.getZ3Path());
        envs.put("BOOGIE_EXE", settings.getBoogiePath());
        pb.redirectError(new File(System.getProperty("user.home") + "/servererror.txt"));
        pb.redirectOutput(new File(System.getProperty("user.home") + "/serverout.txt"));
        try {
            Process p = pb.start();
            currentProcess = p;
            if (!startedBefore){
                startedBefore = true;
                Thread closeChildThread = new Thread() {
                    public void run() {
                        if (currentProcess != null) {
                            currentProcess.destroyForcibly();
                        }
                    }
                };

                Runtime.getRuntime().addShutdownHook(closeChildThread);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void startIfNeeded(String pythonPath){
        NaginiSettingsComponent settings = NaginiSettingsComponent.getInstance();

        String verifier = settings.getVerifier();
        String args = "--verifier " + verifier.toLowerCase() + " --viper-jar-path " + (verifier.equals("Silicon") ? settings.getSiliconJar() : settings.getCarbonJar()) + " --server dummy.py";
        String newConfig = args + settings.getBoogiePath() + settings.getZ3Path() + settings.getMypyDir() + settings.getNaginiDir() + pythonPath;
        if (!newConfig.equals(lastConfig)){
            startProcess(pythonPath, args);
            lastConfig = newConfig;
        }
    }
}
