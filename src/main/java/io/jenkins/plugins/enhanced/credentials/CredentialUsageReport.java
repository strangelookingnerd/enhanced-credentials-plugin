package io.jenkins.plugins.enhanced.credentials;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.ManagementLink;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Extension
public class CredentialUsageReport extends ManagementLink {

    private final static String url = "credentialUsageReport";

    private HashMap<String, CredentialUsage> credentialUsageMap;

    public HashMap<String, CredentialUsage> getCredentialUsageMap() {
        this.credentialUsageMap = CredentialUsages.loadCredentialUsageReport().getCredentialUsageMap();
        return this.credentialUsageMap;
    }

    public List<String> getCredentialIds(){
        return this.getCredentialUsageMap().entrySet().stream().map(e -> e.getKey()).collect(Collectors.toList());
    }

    private CredentialUsage getCredentialUsage(String credentialId){
        return this.getCredentialUsageMap().getOrDefault(credentialId, new CredentialUsage(credentialId));
    }

    public String getUrl() {
        return url;
    }

    @NonNull
    @Override
    public Category getCategory() {
        return Category.STATUS;
    }

    @Override
    public String getDescription() {
        return "Displays credential usages and details";
    }

    @Override
    public String getIconFileName() {
        return "symbol-analytics";
    }

    @Override
    public String getDisplayName() {
        return "Credentials Usage Report";
    }

    @Override
    public String getUrlName() {
        return url;
    }

    @SuppressWarnings("lgtm[jenkins/csrf]")
    public void doUsageDetails(StaplerRequest staplerRequest, StaplerResponse staplerResponse) throws Exception {
        CredentialRuleSupporter.checkAdminPermission();
        String credentialId = staplerRequest.getParameter("credentialId");
        staplerRequest.setAttribute("selectedCredentialId", credentialId);
        staplerRequest.setAttribute("selectedCredentialUsage", this.getCredentialUsage(credentialId));
        staplerRequest.getView(this, "usageDetails.jelly").forward(staplerRequest,staplerResponse);
    }

    @RequirePOST
    public void doClearData(StaplerRequest staplerRequest, StaplerResponse staplerResponse) throws Exception {
        CredentialRuleSupporter.checkAdminPermission();
        CredentialUsages.clearUsageData();
        staplerResponse.forwardToPreviousPage(staplerRequest);
    }


}
