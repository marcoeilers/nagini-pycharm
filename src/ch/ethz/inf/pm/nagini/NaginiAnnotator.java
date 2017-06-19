package ch.ethz.inf.pm.nagini;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by marco on 19.06.17.
 */
public class NaginiAnnotator implements Annotator {
    public static final Map<PsiFile, NaginiAnnotationHolder> annotations = new HashMap<>();

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (element instanceof PsiFile) {
            PsiFile file = (PsiFile) element;
            if (annotations.containsKey(file)){
                NaginiAnnotationHolder annotationHolder = annotations.get(file);
                if (file.getText().hashCode() == annotationHolder.modStamp){
                    List<NaginiAnnotation> as = annotationHolder.annotations;
                    for (NaginiAnnotation a: as){
                        holder.createErrorAnnotation(a.range, a.message);
                    }
                }
            }

        }

    }
}
