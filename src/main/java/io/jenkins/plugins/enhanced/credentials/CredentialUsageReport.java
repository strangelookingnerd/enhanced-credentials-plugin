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

/**
 * This class is for creating link in the Managed Jenkins Page {@link ManagementLink}
 */
@Extension
public class CredentialUsageReport extends ManagementLink {

    private final static String url = "credentialUsageReport";
    private HashMap<String, CredentialUsage> credentialUsageMap;

    /**
     * @return map of Credential IDs / CredentialUsage
     */
    public HashMap<String, CredentialUsage> getCredentialUsageMap() {
        this.credentialUsageMap = CredentialUsages.loadCredentialUsageReport().getCredentialUsageMap();
        return this.credentialUsageMap;
    }

    /**
     * @return Returns list of Credential IDs which have credential usage info
     */
    public List<String> getCredentialIds() {
        return this.getCredentialUsageMap().entrySet().stream().map(e -> e.getKey()).collect(Collectors.toList());
    }

    /**
     * @param credentialId ID of the credential
     * @return {@link CredentialUsage} for the given credential Id
     */
    private CredentialUsage getCredentialUsage(String credentialId) {
        return this.getCredentialUsageMap().getOrDefault(credentialId, new CredentialUsage(credentialId));
    }

    /**
     * @return URL of the link
     */
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

    /**
     * Stapler request for showing the usage details of a credential
     * @param staplerRequest
     * @param staplerResponse
     * @throws Exception
     */
    @SuppressWarnings("lgtm[jenkins/csrf]")
    public void doUsageDetails(StaplerRequest staplerRequest, StaplerResponse staplerResponse) throws Exception {
        CredentialRuleSupporter.checkAdminPermission();
        // Get the credential ID from the request
        String credentialId = staplerRequest.getParameter("credentialId");
        // Fill the response with data
        staplerRequest.setAttribute("selectedCredentialId", credentialId);
        staplerRequest.setAttribute("selectedCredentialUsage", this.getCredentialUsage(credentialId));
        staplerRequest.getView(this, "usageDetails.jelly").forward(staplerRequest, staplerResponse);
    }

    /**
     * Cleans the credential usage data
     * @param staplerRequest
     * @param staplerResponse
     * @throws Exception
     */
    @RequirePOST
    public void doClearData(StaplerRequest staplerRequest, StaplerResponse staplerResponse) throws Exception {
        CredentialRuleSupporter.checkAdminPermission();
        CredentialUsages.clearUsageData();
        staplerResponse.forwardToPreviousPage(staplerRequest);
    }


}
