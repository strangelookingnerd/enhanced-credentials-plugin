package test;

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import hudson.Functions;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Label;
import hudson.model.Result;
import hudson.tasks.BatchFile;
import hudson.tasks.Shell;
import io.jenkins.plugins.enhanced.credentials.CredentialRole;
import io.jenkins.plugins.enhanced.credentials.CredentialRoles;
import io.jenkins.plugins.enhanced.credentials.CredentialUsages;
import org.jenkinsci.plugins.credentialsbinding.impl.SecretBuildWrapper;
import org.jenkinsci.plugins.credentialsbinding.impl.UsernamePasswordBinding;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public abstract class BaseTest {

    private JenkinsRule jenkinsRule;
    private String jenkinsFileAny = "pipeline { agent any; stages{ stage('Test'){ steps{ withCredentials([usernamePassword(credentialsId: '%s', passwordVariable: 'password', usernameVariable: 'username')]) { echo 'Test Pipeline' } } } } }";
    private String jenkinsFileWithNode = "pipeline { agent { label '%s' }; stages{ stage('Test'){ steps{ withCredentials([usernamePassword(credentialsId: '%s', passwordVariable: 'password', usernameVariable: 'username')]) { echo 'Test Pipeline' } } } } }";

    public void setJenkinsRule(JenkinsRule jenkinsRule) {
        this.jenkinsRule = jenkinsRule;
    }

    protected UsernamePasswordCredentialsImpl addSecretTextCredential(String credentialId) throws IOException {
        UsernamePasswordCredentialsImpl usernamePasswordCredentials = new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, credentialId, "", "username", "password");
        SystemCredentialsProvider.getInstance().getCredentials().add(usernamePasswordCredentials);
        SystemCredentialsProvider.getInstance().save();
        return usernamePasswordCredentials;
    }

    protected FreeStyleProject createFreeStyle(String name, UsernamePasswordCredentialsImpl credential) throws IOException {
        SecretBuildWrapper secretBuildWrapper = new SecretBuildWrapper(Collections.singletonList(new UsernamePasswordBinding("VARIABLE", credential.getId())));
        FreeStyleProject freeStyleProject = this.jenkinsRule.createFreeStyleProject(name);
        freeStyleProject.getBuildWrappersList().add(secretBuildWrapper);
        freeStyleProject.getBuildersList().add(Functions.isWindows() ? new BatchFile("echo PASSES") : new Shell("echo PASSES"));
        freeStyleProject.save();
        return freeStyleProject;
    }

    protected FreeStyleProject createFreeStyle(String name, UsernamePasswordCredentialsImpl credential, Label label) throws IOException {
        SecretBuildWrapper secretBuildWrapper = new SecretBuildWrapper(Collections.singletonList(new UsernamePasswordBinding("VARIABLE", credential.getId())));
        FreeStyleProject freeStyleProject = this.jenkinsRule.createFreeStyleProject(name);
        freeStyleProject.getBuildWrappersList().add(secretBuildWrapper);
        freeStyleProject.setAssignedLabel(label);
        freeStyleProject.getBuildersList().add(Functions.isWindows() ? new BatchFile("echo PASSES") : new Shell("echo PASSES"));
        freeStyleProject.save();
        return freeStyleProject;
    }

    protected WorkflowJob createPipeline(String name, UsernamePasswordCredentialsImpl credential, Boolean sandbox) throws IOException {
        CpsFlowDefinition cpsFlowDefinition = new CpsFlowDefinition(String.format(jenkinsFileAny, credential.getId()),sandbox);
        WorkflowJob workflowJob = this.jenkinsRule.createProject(WorkflowJob.class, name);
        workflowJob.setDefinition(cpsFlowDefinition);
        workflowJob.save();
        return workflowJob;
    }
    protected WorkflowJob createPipeline(String name, UsernamePasswordCredentialsImpl credential, Boolean sandbox, Label label) throws IOException {
        CpsFlowDefinition cpsFlowDefinition = new CpsFlowDefinition(String.format(jenkinsFileWithNode, label.getName(), credential.getId()),sandbox);
        WorkflowJob workflowJob = this.jenkinsRule.createProject(WorkflowJob.class, name);
        workflowJob.setDefinition(cpsFlowDefinition);
        workflowJob.save();
        return workflowJob;
    }

    protected void configure(JenkinsRule jenkinsRule, Boolean restrictNotMatching, List<CredentialRole> credentialRoleList) {
        CredentialRoles.CredentialRolesDescriptorImpl descriptor = (CredentialRoles.CredentialRolesDescriptorImpl) jenkinsRule.jenkins.getDescriptor(CredentialRoles.class);
        descriptor.restrictNotMatching = restrictNotMatching;
        descriptor.credentialRoleList = credentialRoleList;
        descriptor.save();
    }

    protected CredentialUsages getCredentialUsages(JenkinsRule jenkinsRule){
        CredentialUsages.CredentialUsageDescriptor credentialUsageDescriptor = (CredentialUsages.CredentialUsageDescriptor) jenkinsRule.jenkins.getDescriptor(CredentialUsages.class);
        return credentialUsageDescriptor.getCredentialUsageReport();
    }


    protected void shouldFail(WorkflowJob workflowJob) throws Exception {
        WorkflowRun workflowRun = workflowJob.scheduleBuild2(0).waitForStart();
        this.jenkinsRule.waitUntilNoActivity();
        this.jenkinsRule.assertBuildStatus(Result.FAILURE, workflowRun);
        this.jenkinsRule.assertLogContains("Access to credential is blocked.",workflowRun);
    }

     protected void shouldSuccess(WorkflowJob workflowJob) throws Exception {
        WorkflowRun workflowRun = workflowJob.scheduleBuild2(0).waitForStart();
        this.jenkinsRule.waitUntilNoActivity();
        this.jenkinsRule.assertBuildStatus(Result.SUCCESS, workflowRun);
    }

    protected void shouldFail(FreeStyleProject freeStyleProject) throws Exception {
        FreeStyleBuild freeStyleBuild = freeStyleProject.scheduleBuild2(0).waitForStart();
        this.jenkinsRule.waitUntilNoActivity();
        this.jenkinsRule.assertBuildStatus(Result.FAILURE, freeStyleBuild);
        this.jenkinsRule.assertLogContains("Access to credential is blocked.",freeStyleBuild);
    }

    protected void shouldSuccess(FreeStyleProject freeStyleProject) throws Exception {
        FreeStyleBuild freeStyleBuild = freeStyleProject.scheduleBuild2(0).waitForStart();
        this.jenkinsRule.waitUntilNoActivity();
        this.jenkinsRule.assertBuildStatus(Result.SUCCESS, freeStyleBuild);
    }
}
