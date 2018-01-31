package ch.ethz.inf.pm.nagini;


import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.codeInsight.daemon.impl.UpdateHighlightersUtil;
import com.intellij.codeInsight.problems.ProblemImpl;
//import com.intellij.compiler.CompilerWorkspaceConfiguration;
//import com.intellij.compiler.ProblemsView;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessListener;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindowId;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.problems.WolfTheProblemSolver;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.ThrowableRunnable;
//import net.sf.saxon.trans.Err;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by marco on 19.06.17.
 */
public class NaginiProcessListener implements ProcessListener {
    private StringBuilder allText = new StringBuilder();
    private Project project;
    private String scriptName;
    private static final Pattern ERROR_PATTERN = Pattern.compile("^(.*)\\((.*)@(\\d+)\\.(\\d+)\\)$");
    //private static final Pattern Error2 =Pattern.compile("^(.*)\((.*)@(\d+)\.(\d+)\)$");


    public NaginiProcessListener(Project project, String scriptName){
        this.project = project;
        this.scriptName = scriptName;
    }

    @Override
    public void startNotified(ProcessEvent event) {

    }

    @Override
    public void processTerminated(ProcessEvent event) {
        Editor[] editors = EditorFactory.getInstance().getAllEditors();
        for (Editor e: editors){
            if (e.getProject() == project){
                VirtualFile vf = FileDocumentManager.getInstance().getFile(e.getDocument());
                if (vf != null && vf.toString().contains(scriptName)){

                    List<HighlightInfo> highlights = new ArrayList<HighlightInfo>();
                    List<NaginiAnnotation> nas = new ArrayList<NaginiAnnotation>();

                    BufferedReader bufReader = new BufferedReader(new StringReader(allText.toString()));
                    try {
                        String current = bufReader.readLine();
                        while (current != null){
                            Matcher m = ERROR_PATTERN.matcher(current);
                            if (m.matches()){
                                String message = m.group(1);
                                String errorFile = m.group(2);
                                if (vf.toString().contains(errorFile)){
                                    int line = Integer.parseInt(m.group(3)) - 1;
                                    int lineStart = e.getDocument().getLineStartOffset(line);
                                    int lineEnd = e.getDocument().getLineEndOffset(line);
                                    String text = e.getDocument().getText();
                                    int i = 0;
                                    while (lineStart + i < lineEnd){
                                        if (!Character.isWhitespace(text.charAt(lineStart + i))){
                                            break;
                                        }
                                        i++;
                                    }
                                    int offset = i;
                                    //int offset = Integer.parseInt(m.group(4));
                                    TextRange range = new TextRange(lineStart + offset, lineEnd);
                                    HighlightInfo hi = HighlightInfo.newHighlightInfo(HighlightInfoType.ERROR).range(range).descriptionAndTooltip(message).needsUpdateOnTyping(false).create();
                                    highlights.add(hi);
                                    NaginiAnnotation na = new NaginiAnnotation(range, message);
                                    nas.add(na);
                                    //WolfTheProblemSolver.getInstance(project).weHaveGotProblems(vf, Collections.singletonList(new ProblemImpl(vf, hi, true)));
                                }

                            }

                            current = bufReader.readLine();

                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }

                    NaginiAnnotationHolder holder = new NaginiAnnotationHolder(e.getDocument().getText().hashCode());
                    holder.annotations = nas;

                    ReadAction.run(new ThrowableRunnable() {
                        @Override
                        public void run() throws Throwable {
                            PsiFile file = PsiManager.getInstance(project).findFile(vf);
                            NaginiAnnotator.annotations.put(file, holder);

                            final String statusMessage = highlights.size() > 0 ? "Verification has found errors." : "Verification successful.";
                            final MessageType messageType = highlights.size() > 0 ? MessageType.ERROR : MessageType.INFO;
                            final NotificationType notType = highlights.size() > 0 ? NotificationType.ERROR : NotificationType.INFORMATION;

                            final Notification notification = new Notification("", "Verification complete", statusMessage, notType);

                            notification.notify(project);
                        }
                    });

                    ApplicationManager.getApplication().invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            WriteAction.run(new ThrowableRunnable() {
                                @Override
                                public void run() throws Throwable {
                                    e.getDocument().setText(e.getDocument().getText());
                                }
                            });
                        }
                    });


                }
            }
        }
    }

    @Override
    public void processWillTerminate(ProcessEvent event, boolean willBeDestroyed) {

    }

    @Override
    public void onTextAvailable(ProcessEvent event, Key outputType) {
        allText.append(event.getText());
    }
}
