package ch.ethz.inf.pm.nagini;

import com.intellij.openapi.util.TextRange;

/**
 * Created by marco on 19.06.17.
 */
public class NaginiAnnotation {
    public TextRange range;
    public String message;

    public NaginiAnnotation(TextRange range, String message){
        this.range = range;
        this.message = message;
    }
}
