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

import javax.servlet.ServletException;
import java.io.IOException;

@Extension
public class CredentialRoleConfiguration extends ManagementLink {

    private final static String url = "CredentialRoles";
    private CredentialRoles itemCredentialRoles;

    public CredentialRoleConfiguration() {
        this.itemCredentialRoles = loadItemCredentialRoles();
    }

    public CredentialRoles getItemCredentialRoles() {
        this.itemCredentialRoles = loadItemCredentialRoles();
        return this.itemCredentialRoles;
    }

    @Override
    public String getIconFileName() {
        return "secure.png";
    }

    @Override
    public String getDisplayName() {
        return "Manage Credential Roles";
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
        return "Manage roles for accessing credentials";
    }

    private static CredentialRoles.CredentialRolesDescriptorImpl getItemCredentialRolesDescriptor(){
        Descriptor descriptor = Jenkins.getActiveInstance().getDescriptorOrDie(CredentialRoles.class);
        return (CredentialRoles.CredentialRolesDescriptorImpl) descriptor;
    }

    public static CredentialRoles loadItemCredentialRoles(){
        getItemCredentialRolesDescriptor().load();
        return getItemCredentialRolesDescriptor().getItemCredentialRoles();
    }

    private CredentialRoles saveItemCredentialRoles(StaplerRequest req, JSONObject submittedForm) throws Descriptor.FormException {
        getItemCredentialRolesDescriptor().configure(req,submittedForm);
        return getItemCredentialRolesDescriptor().getItemCredentialRoles();
    }

    public void doUpdate(StaplerRequest req, StaplerResponse rsp) throws ServletException, IOException, Descriptor.FormException {
        JSONObject submittedForm = req.getSubmittedForm();
        this.saveItemCredentialRoles(req,submittedForm);
        FormApply.success(req.getContextPath() + "/" + this.getUrlName()).generateResponse(req, rsp, null);
    }


}
