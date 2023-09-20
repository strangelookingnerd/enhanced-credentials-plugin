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
 * Credential Listener for restriction credentials usage
 */
public class GenericCredentialListener {

    private static final Logger LOGGER = Logger.getLogger(GenericCredentialListener.class.getName());

    static final List<FreeStyleBuild> freeStyleBuildsToStop = new ArrayList<>();

    @Extension
    public static class GenericCredentialsUseListener implements CredentialsUseListener {

        private CredentialRuleSupporter credentialRuleSupporter = new CredentialRuleSupporter();

        private void error(Credentials c, String buildUrl) {
            // Get task listener for the build and print log into the build
            TaskListener taskListener = GenericRunListener.getTaskListener(buildUrl);
            LOGGER.fine(String.format("Found Task Listener:%s for Build Url:%s", taskListener.toString(), buildUrl));
            taskListener.error(String.format("Access to credential is blocked. Failing the build..", credentialRuleSupporter.callGetId(c)));
        }

        @Override
        public void onUse(Credentials c, Run run) {
            // Check if run is from Free Style Build
            if (run instanceof FreeStyleBuild) {
                FreeStyleBuild freeStyleBuild = (FreeStyleBuild) run;
                // Check if parent job has access to the credential
                if (!credentialRuleSupporter.checkProjectHasAccessForCredential(c, freeStyleBuild.getProject())) {
                    LOGGER.fine(String.format("Credential:%s doesn't have access for Project:%s - Stopping build..", CredentialRuleSupporter.callGetId(c),freeStyleBuild.getProject().getUrl()));
                    error(c, freeStyleBuild.getUrl());
                    freeStyleBuildsToStop.add(freeStyleBuild);
                }
            }
            else if (run instanceof WorkflowRun) {
                WorkflowRun workflowRun = (WorkflowRun) run;
                if (!credentialRuleSupporter.checkJobHasAccessForCredential(c, workflowRun.getParent())) {
                    LOGGER.fine(String.format("Credential:%s doesn't have access for Project:%s - Stopping build..", CredentialRuleSupporter.callGetId(c),workflowRun.getParent().getUrl()));
                    error(c, workflowRun.getUrl());
                    credentialRuleSupporter.tryStoppingWorkflowRun(workflowRun);
                }

            } else {
                LOGGER.warning(String.format("No implementation found for Run Class:%s", run.getClass().getName()));
            }
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
