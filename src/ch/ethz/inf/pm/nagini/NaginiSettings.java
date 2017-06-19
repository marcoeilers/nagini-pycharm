package ch.ethz.inf.pm.nagini;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;


/**
 * Created by marco on 19.06.17.
 */
public class NaginiSettings implements SearchableConfigurable, Configurable.NoScroll {
    private NaginiSettingsPanel panel;
    private NaginiSettingsComponent settings;

    public static class NaginiSettingsPanel {
        private JTextField naginiDir;
        private JTextField siliconJar;
        private JTextField carbonJar;
        private JTextField mypyDir;
        private JComboBox verifier;
        private JTextField z3Path;
        private JTextField boogiePath;
        private JPanel panel;

        public NaginiSettingsPanel() {
            //final FileChooserDescriptor descriptor = createSceneBuilderDescriptor();
            //myPathField.addBrowseFolderListener(descriptor.getTitle(), descriptor.getDescription(), null, descriptor);
            verifier.addItem("Silicon");
            verifier.addItem("Carbon");
            verifier.setEditable(false);
        }

        private void reset(NaginiSettingsComponent settings) {
            final String naginiDirS = settings.getNaginiDir();
            final String siliconJarS = settings.getSiliconJar();
            final String carbonJarS = settings.getCarbonJar();
            final String mypyDirS = settings.getMypyDir();
            final String verifierS = settings.getVerifier();
            final String z3PathS = settings.getZ3Path();
            final String boogiePathS = settings.getBoogiePath();
            naginiDir.setText(naginiDirS == null ? "" : naginiDirS);
            siliconJar.setText(siliconJarS == null ? "" : siliconJarS);
            carbonJar.setText(carbonJarS == null ? "" : carbonJarS);
            mypyDir.setText(mypyDirS == null ? "" : mypyDirS);
            z3Path.setText(z3PathS == null ? "" : z3PathS);
            boogiePath.setText(boogiePathS == null ? "" : boogiePathS);
            verifier.setSelectedItem(verifierS == null ? "Silicon" : verifierS);
        }

        private void apply(NaginiSettingsComponent settings) {
            settings.setNaginiDir(naginiDir.getText());
            settings.setSiliconJar(siliconJar.getText());
            settings.setCarbonJar(carbonJar.getText());
            settings.setMypyDir(mypyDir.getText());
            settings.setZ3Path(z3Path.getText());
            settings.setBoogiePath(boogiePath.getText());
            settings.setVerifier((String)verifier.getSelectedItem());
        }

        private boolean isModified(NaginiSettingsComponent settings) {
            return true;
        }
    }

    public NaginiSettings(NaginiSettingsComponent settings) {
        this.settings = settings;
    }

    @NotNull
    @Override
    public String getId() {
        return getHelpTopic();
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Nagini";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return "preferences.Nagini";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        panel = new NaginiSettingsPanel();
        return panel.panel;
    }

    @Override
    public boolean isModified() {
        return panel.isModified(settings);
    }

    @Override
    public void apply() throws ConfigurationException {
        panel.apply(settings);
    }

    @Override
    public void reset() {
        panel.reset(settings);
    }
}
