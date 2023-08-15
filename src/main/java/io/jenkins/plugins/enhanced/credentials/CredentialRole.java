package io.jenkins.plugins.enhanced.credentials;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

public class CredentialRole extends AbstractDescribableImpl<CredentialRole> {

    private String name;
    private String credentialPattern;
    private String itemPattern;

    @DataBoundConstructor
    public CredentialRole(String name, String credentialPattern, String itemPattern) {
        this.name = name;
        this.credentialPattern = credentialPattern;
        this.itemPattern = itemPattern;
    }

    @DataBoundSetter
    public void setName(String name) {
        this.name = name;
    }

    @DataBoundSetter
    public void setCredentialPattern(String credentialPattern) {
        this.credentialPattern = credentialPattern;
    }

    @DataBoundSetter
    public void setItemPattern(String itemPattern) {
        this.itemPattern = itemPattern;
    }

    public String getName() {
        return name;
    }

    public String getCredentialPattern() {
        return credentialPattern;
    }

    public String getItemPattern() {
        return itemPattern;
    }

    @Override
    public Descriptor<CredentialRole> getDescriptor() {
        return super.getDescriptor();
    }

    @Extension
    public static final class ItemCredentialRoleDescriptor extends Descriptor<CredentialRole> {

        @NonNull
        @Override
        public String getDisplayName() {
            return "Credential Role Definition";
        }

        public FormValidation doCheckName(@QueryParameter String value){
            Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            if(Util.fixEmptyAndTrim(value) == null){
                return FormValidation.error("Name can't be empty.");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckCredentialPattern(@QueryParameter String value){
            Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            if(Util.fixEmptyAndTrim(value) == null){
                return FormValidation.error("Credential Pattern can't be empty.");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckItemPattern(@QueryParameter String value){
            Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            if(Util.fixEmptyAndTrim(value) == null){
                return FormValidation.error("Item Patterncan't be empty.");
            }
            return FormValidation.ok();
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
            return super.configure(req, json);
        }
    }

}
