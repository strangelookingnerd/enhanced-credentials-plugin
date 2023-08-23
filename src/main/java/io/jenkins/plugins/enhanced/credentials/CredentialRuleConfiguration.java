package io.jenkins.plugins.enhanced.credentials;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.ManagementLink;
import hudson.util.FormApply;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.servlet.ServletException;
import java.io.IOException;

@Extension
public class CredentialRuleConfiguration extends ManagementLink {

    private final static String url = "CredentialRules";
    private CredentialRules credentialRules;

    public CredentialRuleConfiguration() {
        this.credentialRules = loadCredentialRules();
    }

    public CredentialRules getCredentialRules() {
        this.credentialRules = loadCredentialRules();
        return this.credentialRules;
    }

    @Override
    public String getIconFileName() {
        return "symbol-lock-closed";
    }

    @Override
    public String getDisplayName() {
        return "Manage Credential Rules";
    }

    @Override
    public String getUrlName() {
        return url;
    }

    @NonNull
    @Override
    public Category getCategory() {
        return Category.SECURITY;
    }

    @Override
    public String getDescription() {
        return "Manage rules for accessing credentials";
    }

    private static CredentialRules.CredentialRulesDescriptorImpl getCredentialRulesDescriptor(){
        Descriptor descriptor = Jenkins.get().getDescriptorOrDie(CredentialRules.class);
        return (CredentialRules.CredentialRulesDescriptorImpl) descriptor;
    }

    public static CredentialRules loadCredentialRules(){
        getCredentialRulesDescriptor().load();
        return getCredentialRulesDescriptor().getCredentialRules();
    }

    private CredentialRules saveCredentialRules(StaplerRequest req, JSONObject submittedForm) throws Descriptor.FormException {
        getCredentialRulesDescriptor().configure(req,submittedForm);
        return getCredentialRulesDescriptor().getCredentialRules();
    }

    @RequirePOST
    public void doUpdate(StaplerRequest req, StaplerResponse rsp) throws Exception {
        CredentialRuleSupporter.checkAdminPermission();
        JSONObject submittedForm = req.getSubmittedForm();
        this.saveCredentialRules(req,submittedForm);
        FormApply.success(req.getContextPath() + "/" + this.getUrlName()).generateResponse(req, rsp, null);
    }


}
