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
import org.kohsuke.stapler.interceptor.RequirePOST;

public class CredentialRule extends AbstractDescribableImpl<CredentialRule> {

    private String name;
    private String credentialPattern;
    private String itemPattern;

    @DataBoundConstructor
    public CredentialRule(String name, String credentialPattern, String itemPattern) {
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
    public Descriptor<CredentialRule> getDescriptor() {
        return super.getDescriptor();
    }

    @Extension
    public static final class CredentialRuleDescriptor extends Descriptor<CredentialRule> {

        @NonNull
        @Override
        public String getDisplayName() {
            return "Credential Rule Definition";
        }

        @RequirePOST
        public FormValidation doCheckName(@QueryParameter String value){
            Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            if(Util.fixEmptyAndTrim(value) == null){
                return FormValidation.error("Name can't be empty.");
            }
            return FormValidation.ok();
        }

        @RequirePOST
        public FormValidation doCheckCredentialPattern(@QueryParameter String value){
            Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            if(Util.fixEmptyAndTrim(value) == null){
                return FormValidation.error("Credential Pattern can't be empty.");
            }
            return FormValidation.ok();
        }

        @RequirePOST
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
