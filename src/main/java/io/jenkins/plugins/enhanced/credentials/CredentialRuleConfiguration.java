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

/**
 * This class is for creating link in the Managed Jenkins Page {@link ManagementLink}
 */
@Extension
public class CredentialRuleConfiguration extends ManagementLink {

    private final static String url = "CredentialRules";
    private CredentialRules credentialRules;

    public CredentialRuleConfiguration() {
        this.credentialRules = loadCredentialRules();
    }

    /**
     * @return credential rules which are loaded from the disk
     */
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

    /**
     * @return Descriptor of the CredentialRules class
     */
    private static CredentialRules.CredentialRulesDescriptorImpl getCredentialRulesDescriptor(){
        Descriptor descriptor = Jenkins.get().getDescriptorOrDie(CredentialRules.class);
        return (CredentialRules.CredentialRulesDescriptorImpl) descriptor;
    }

    /**
     * @return credential rules which are loaded from the disk
     */
    public static CredentialRules loadCredentialRules(){
        getCredentialRulesDescriptor().load();
        return getCredentialRulesDescriptor().getCredentialRules();
    }

    /**
     * Saves the credential rules to the disk which are set in the configuration page.
     * @param req
     * @param submittedForm
     * @return Credential rules which are saved to the disk.
     * @throws Descriptor.FormException
     */
    private CredentialRules saveCredentialRules(StaplerRequest req, JSONObject submittedForm) throws Descriptor.FormException {
        getCredentialRulesDescriptor().configure(req,submittedForm);
        return getCredentialRulesDescriptor().getCredentialRules();
    }

    /**
     * HTTP Post action for saving the credential rules.
     * @param req
     * @param rsp
     * @throws Exception
     */
    @RequirePOST
    public void doUpdate(StaplerRequest req, StaplerResponse rsp) throws Exception {
        CredentialRuleSupporter.checkAdminPermission();
        // Get the form from the submitted form
        JSONObject submittedForm = req.getSubmittedForm();
        // Save and reload the page.
        this.saveCredentialRules(req,submittedForm);
        FormApply.success(req.getContextPath() + "/" + this.getUrlName()).generateResponse(req, rsp, null);
    }


}
