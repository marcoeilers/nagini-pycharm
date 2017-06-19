package ch.ethz.inf.pm.nagini;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by marco on 19.06.17.
 */
public class NaginiAnnotationHolder {
    public long modStamp;
    public List<NaginiAnnotation> annotations = new ArrayList<>();

    public NaginiAnnotationHolder(long modStamp){
        this.modStamp = modStamp;
    }
}
