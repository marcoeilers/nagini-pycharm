package ch.ethz.inf.pm.nagini;


import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.codeInsight.daemon.impl.UpdateHighlightersUtil;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.util.ThrowableRunnable;
import net.sf.saxon.trans.Err;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
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
    private static final Pattern Error1 = Pattern.compile("^(.*)\\((.*)@(\\d+)\\.(\\d+)\\)$");
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
                    //System.out.println(allText.toString());

                    BufferedReader bufReader = new BufferedReader(new StringReader(allText.toString()));
                    try {
                        String current = bufReader.readLine();
                        while (current != null){
                            Matcher m = Error1.matcher(current);
                            if (m.matches()){
                                String message = m.group(1);
                                int line = Integer.parseInt(m.group(3)) - 1;
                                TextRange range = new TextRange(e.getDocument().getLineStartOffset(line), e.getDocument().getLineEndOffset(line));
                                HighlightInfo hi = HighlightInfo.newHighlightInfo(HighlightInfoType.ERROR).range(range).descriptionAndTooltip(message).needsUpdateOnTyping(false).create();
                                highlights.add(hi);
                            }

                            current = bufReader.readLine();
                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }


                    ApplicationManager.getApplication().invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            UpdateHighlightersUtil.setHighlightersToEditor(e.getProject(), e.getDocument(), 0, e.getDocument().getTextLength(), highlights, e.getColorsScheme(), Pass.EXTERNAL_TOOLS);

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
