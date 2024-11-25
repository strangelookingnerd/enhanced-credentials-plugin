package io.jenkins.plugins.enhanced.credentials;

import com.cloudbees.plugins.credentials.Credentials;
import com.google.common.util.concurrent.ListenableFuture;
import hudson.AbortException;
import hudson.model.*;
import io.jenkins.plugins.enhanced.credentials.listener.GenericRunListener;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.flow.FlowExecution;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.steps.StepExecution;

import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Logger;

/**
 * Supporter class for credential rules
 */
public class CredentialRuleSupporter {

    private static final Logger LOGGER = Logger.getLogger(CredentialRuleSupporter.class.getName());

    /**
     * Checks if the credential has access for the given Free Style Project
     * @param credentials Credential which will be checked
     * @param project Free Style Project
     * @return True/False
     */
    public Boolean checkProjectHasAccessForCredential(Credentials credentials, FreeStyleProject project) {
        String credentialId = callGetId(credentials);
        String itemName = project.getFullName();
        return checkItemHasAccessForCredential(credentialId, itemName);
    }

    /**
     * Checks if the credential has access for the given Job
     * @param credentials Credential which will be checked
     * @param job Job
     * @return True/False
     */
    public Boolean checkJobHasAccessForCredential(Credentials credentials, Job job) {
        String credentialId = callGetId(credentials);
        String itemName = job.getFullName();
        return checkItemHasAccessForCredential(credentialId, itemName);
    }

    /**
     * Checks the credential rules and returns if the credential has access for the given item (FreeStyle/Job)
     * @param credentialId ID of the credential
     * @param itemName Name of the job/freestyle job
     * @return True/False
     */
    private Boolean checkItemHasAccessForCredential(String credentialId, String itemName) {
        CredentialRules credentialRules = CredentialRuleConfiguration.loadCredentialRules();
        LOGGER.fine(String.format("Checking Access for Credential:%s and Item:%s", credentialId, itemName));

        // Check if there are any rules are defined
        if (credentialRules.getCredentialRuleList().size() == 0) {
            LOGGER.fine("No Credentials Rules are found");
            // If there are no rules, then check the default restriction config
            if (credentialRules.getRestrictNotMatching()) {
                // Means that credential usage must be blocked
                LOGGER.info(String.format("Blocking Credential:%s Access for Item:%s", credentialId, itemName));
                return false;
            } else {
                // Means that credential usage must be allowed
                LOGGER.info(String.format("Allowing Credential:%s Access for Item:%s", credentialId, itemName));
                return true;
            }
        } else {
            // If there are rules defined, loop in them and find if there are any matches
            Boolean isCredentialMatched = false;
            Boolean isItemAllowed = false;
            for (CredentialRule credentialRule : credentialRules.getCredentialRuleList()) {
                String credentialPattern = credentialRule.getCredentialPattern();
                String itemPattern = credentialRule.getItemPattern();
                LOGGER.fine(String.format("Checking access for credentialPattern:%s and itemPattern:%s", credentialPattern,itemPattern));
                // Check if the credential Name matches with the pattern
                if (credentialId.matches(credentialPattern)) {
                    LOGGER.fine(String.format("Credential %s is matched with pattern:%s", credentialId, credentialPattern));
                    isCredentialMatched = true;
                    // Check if item name matches with the pattern
                    if (itemName.matches(itemPattern)) {
                        LOGGER.fine(String.format("Item %s is matched with pattern:%s", itemName, itemPattern));
                        isItemAllowed = true;
                    }
                }
            }
            // If default restriction policy is set to restrict
            // and
            // If the credential is not matched.
            // return true for blocking
            if (credentialRules.getRestrictNotMatching() && !isCredentialMatched) {
                LOGGER.info(String.format("Blocking Credential:%s Access for Item:%s", credentialId, itemName));
                return false;
            }
            // If default restriction policy is set to restrict
            // and
            // If the credential is matched.
            // return isItemAllowed for blocking
            if (credentialRules.getRestrictNotMatching() && isCredentialMatched) {
                if( isItemAllowed)
                    LOGGER.info(String.format("Allowing Credential:%s Access for Item:%s", credentialId, itemName));
                else
                    LOGGER.info(String.format("Blocking Credential:%s Access for Item:%s", credentialId, itemName));
                return isItemAllowed;
            }
            // If default restriction policy is set to allow
            // and
            // If the credential is matched.
            // return isItemAllowed for blocking
            if (!credentialRules.getRestrictNotMatching() && isCredentialMatched) {
                if( isItemAllowed)
                    LOGGER.info(String.format("Allowing Credential:%s Access for Item:%s", credentialId, itemName));
                else
                    LOGGER.info(String.format("Blocking Credential:%s Access for Item:%s", credentialId, itemName));
                return isItemAllowed;
            }
            // If default restriction policy is set to allow
            // and
            // If the credential is not matched.
            // return true for allowing
            if (!credentialRules.getRestrictNotMatching() && !isCredentialMatched) {
                LOGGER.info(String.format("Allowing Credential:%s Access for Item:%s", credentialId, itemName));
                return true;
            }
        }

        // Return false by default where this code shouldn't be reached
        return false;
    }

    /**
     * This method tries to fail the FreeStyle build
     * @param freeStyleBuild
     * @throws AbortException
     */
    public void tryStoppingFreeStyleBuild(FreeStyleBuild freeStyleBuild) throws AbortException {
        freeStyleBuild.setResult(Result.FAILURE);
        throw new AbortException("Stopping build..");
    }

    /**
     * This method tries to fail the Run
     * @param workflowRun
     * @throws Exception
     */
    public void tryStoppingWorkflowRun(WorkflowRun workflowRun) {
        LOGGER.fine(String.format("Tyring to stop Workflow Run:%s", workflowRun.getUrl()));
        TaskListener taskListener = GenericRunListener.getTaskListener(workflowRun.getUrl());
        try {
            taskListener.error("Stopping build..");
            FlowExecution flowExecution = workflowRun.getExecution();
            if (flowExecution != null) {
                LOGGER.fine(String.format("Found Flow Execution for Workflow Run %s:", workflowRun.getUrl()));
                ListenableFuture<List<StepExecution>> currentExecutions = flowExecution.getCurrentExecutions(true);
                List<StepExecution> stepExecutions = currentExecutions.get();
                for (StepExecution stepExecution : stepExecutions) {
                    LOGGER.fine(String.format("Stopping step %s", stepExecution.toString()));
                    stepExecution.stop(new Throwable("Cancelling step"));
                }
                workflowRun.setResult(Result.FAILURE);
            } else {
                LOGGER.fine(String.format("Flow Execution is null for Workflow Run %s:", workflowRun.getUrl()));
                throw new Exception(String.format("Flow Execution is null for %s:", workflowRun.getUrl()));
            }
        } catch (Exception ex) {
            LOGGER.fine(String.format("Hard-killing Workflow Run:%s", workflowRun.getUrl()));
            taskListener.error(String.format("Failed to stop build with error: %s", ex.getMessage()));
            taskListener.error("Trying Hard Kill!");
            workflowRun.doKill();
        }
    }

    /**
     * This method returns the credential id of the given credential
     * @param credentials Credential Object
     * @return ID of the credential
     */
    public static String callGetId(Credentials credentials) {
        try {
            Method getIdMethod = credentials.getClass().getMethod("getId");
            Object getIdResult = getIdMethod.invoke(credentials);
            return String.valueOf(getIdResult);
        } catch (Exception e) {
            // If it can't get the ID, there is nothing can be done.
            throw new RuntimeException("Runtime Exception");
        }
    }

    /**
     * Checks if the user has Admin permission
     * @throws Exception
     */
    public static void checkAdminPermission() throws Exception {
        Jenkins jenkins = Jenkins.getInstanceOrNull();
        if(jenkins == null){
            throw new Exception("Couldn't get Jenkins Instance");
        }
        jenkins.checkPermission(Jenkins.ADMINISTER);
    }

}
