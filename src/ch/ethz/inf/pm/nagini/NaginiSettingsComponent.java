package ch.ethz.inf.pm.nagini;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;

/**
 * Created by marco on 19.06.17.
 */
@State(
        name="NaginiSettingsComponent",
        storages = {
                @Storage("other.xml")}
)
public class NaginiSettingsComponent implements PersistentStateComponent<NaginiSettingsComponent> {
    public String naginiDir = null;
    public String siliconJar = null;
    public String carbonJar = null;
    public String mypyDir = null;
    public String z3Path = null;
    public String boogiePath = null;
    public String verifier = null;

    public String getNaginiDir() {
        return naginiDir;
    }

    public void setNaginiDir(String naginiDir) {
        this.naginiDir = naginiDir;
    }

    public String getSiliconJar() {
        return siliconJar;
    }

    public void setSiliconJar(String siliconJar) {
        this.siliconJar = siliconJar;
    }

    public String getCarbonJar() {
        return carbonJar;
    }

    public void setCarbonJar(String carbonJar) {
        this.carbonJar = carbonJar;
    }

    public String getMypyDir() {
        return mypyDir;
    }

    public void setMypyDir(String mypyDir) {
        this.mypyDir = mypyDir;
    }

    public String getZ3Path() {
        return z3Path;
    }

    public void setZ3Path(String z3Path) {
        this.z3Path = z3Path;
    }

    public String getBoogiePath() {
        return boogiePath;
    }

    public void setBoogiePath(String boogiePath) {
        this.boogiePath = boogiePath;
    }

    public String getVerifier() {
        return verifier;
    }

    public void setVerifier(String verifier) {
        this.verifier = verifier;
    }

    public static NaginiSettingsComponent getInstance() {
        return ServiceManager.getService(NaginiSettingsComponent.class);
    }

    public NaginiSettingsComponent getState() {
        return this;
    }

    public void loadState(NaginiSettingsComponent object) {
        XmlSerializerUtil.copyBean(object, this);
    }
}
