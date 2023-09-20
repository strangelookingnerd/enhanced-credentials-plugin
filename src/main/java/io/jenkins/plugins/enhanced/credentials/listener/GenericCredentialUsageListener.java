package io.jenkins.plugins.enhanced.credentials.listener;

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsUseListener;
import hudson.Extension;
import hudson.model.*;
import io.jenkins.plugins.enhanced.credentials.CredentialRuleSupporter;
import io.jenkins.plugins.enhanced.credentials.CredentialUsages;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Credential Listener for finding credential usage.
 */
public class GenericCredentialUsageListener {

    @Extension
    public static class GenericCredentialsUseListener implements CredentialsUseListener {

        @Override
        public void onUse(Credentials c, Run run) {
            //Increment credential usage
            CredentialUsages.incrementCredentialUsage(c,run);
        }

        @Override
        public void onUse(Credentials c, Node node) {
            //Increment credential usage
            CredentialUsages.incrementCredentialUsage(c,node);
        }

        @Override
        public void onUse(Credentials c, Item item) {
            // Increment credentials usage
            CredentialUsages.incrementCredentialUsage(c,item);
        }
    }


}
