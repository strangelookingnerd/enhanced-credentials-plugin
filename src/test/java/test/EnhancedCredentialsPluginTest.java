package test;

import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import hudson.model.FreeStyleProject;
import hudson.model.Label;
import io.jenkins.plugins.enhanced.credentials.CredentialRole;
import io.jenkins.plugins.enhanced.credentials.CredentialUsage;
import io.jenkins.plugins.enhanced.credentials.CredentialUsages;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.ArrayList;
import java.util.Arrays;

public class EnhancedCredentialsPluginTest extends BaseTest{

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    private UsernamePasswordCredentialsImpl credential1;
    private UsernamePasswordCredentialsImpl credential2;
    private Label nodeLabel;


    @Before
    public void setup() throws Exception {
        this.setJenkinsRule(jenkinsRule);
        this.credential1 = this.addSecretTextCredential("credential1");
        this.credential2 = this.addSecretTextCredential("credential2");
        this.nodeLabel = Label.parseExpression("node1");
        this.jenkinsRule.createOnlineSlave(this.nodeLabel);
    }

    @Test
    public void testRestrictWithNoRulesWithFreeStyle() throws Exception {
        this.configure(this.jenkinsRule, true, new ArrayList<>());
        FreeStyleProject freeStyleProject1 = this.createFreeStyle("testRestrictWithNoRulesWithFreeStyle1", credential1);
        FreeStyleProject freeStyleProject2 = this.createFreeStyle("testRestrictWithNoRulesWithFreeStyle2", credential1);
        shouldFail(freeStyleProject1);
        shouldFail(freeStyleProject2);
    }

    @Test
    public void testRestrictWithNoRulesWithFreeStyleOnNode() throws Exception {
        this.configure(this.jenkinsRule, true, new ArrayList<>());
        FreeStyleProject freeStyleProject1 = this.createFreeStyle("testRestrictWithNoRulesWithFreeStyle1", credential1, this.nodeLabel);
        FreeStyleProject freeStyleProject2 = this.createFreeStyle("testRestrictWithNoRulesWithFreeStyle2", credential1, this.nodeLabel);
        shouldFail(freeStyleProject1);
        shouldFail(freeStyleProject2);
    }

    @Test
    public void testNoRestrictWithNoRulesWithFreeStyle() throws Exception {
        this.configure(this.jenkinsRule, false, new ArrayList<>());
        FreeStyleProject freeStyleProject1 = this.createFreeStyle("testNoRestrictWithNoRulesWithFreeStyle1", credential1);
        FreeStyleProject freeStyleProject2 = this.createFreeStyle("testNoRestrictWithNoRulesWithFreeStyle2", credential1);
        shouldSuccess(freeStyleProject1);
        shouldSuccess(freeStyleProject2);
    }

    @Test
    public void testNoRestrictWithNoRulesWithFreeStyleOnNode() throws Exception {
        this.configure(this.jenkinsRule, false, new ArrayList<>());
        FreeStyleProject freeStyleProject1 = this.createFreeStyle("testNoRestrictWithNoRulesWithFreeStyle1", credential1, this.nodeLabel);
        FreeStyleProject freeStyleProject2 = this.createFreeStyle("testNoRestrictWithNoRulesWithFreeStyle2", credential1, this.nodeLabel);
        shouldSuccess(freeStyleProject1);
        shouldSuccess(freeStyleProject2);
    }

    @Test
    public void testRestrictWithNoRulesWithPipeline() throws Exception {
        this.configure(this.jenkinsRule, true, new ArrayList<>());
        WorkflowJob workflowJobSandbox1 = this.createPipeline("testRestrictWithNoRulesWithPipelineSandbox1", credential1,true);
        WorkflowJob workflowJobSandbox2 = this.createPipeline("testRestrictWithNoRulesWithPipelineSandbox2", credential1,true);
        WorkflowJob workflowJobNoSandbox1 = this.createPipeline("testRestrictWithNoRulesWithPipelineNoSandbox1", credential1,false);
        WorkflowJob workflowJobNoSandbox2 = this.createPipeline("testRestrictWithNoRulesWithPipelineNoSandbox2", credential1,false);
        shouldFail(workflowJobSandbox1);
        shouldFail(workflowJobSandbox2);
        shouldFail(workflowJobNoSandbox1);
        shouldFail(workflowJobNoSandbox2);
    }

    @Test
    public void testRestrictWithNoRulesWithPipelineOnNode() throws Exception {
        this.configure(this.jenkinsRule, true, new ArrayList<>());
        WorkflowJob workflowJobSandbox1 = this.createPipeline("testRestrictWithNoRulesWithPipelineSandbox1", credential1,true, this.nodeLabel);
        WorkflowJob workflowJobSandbox2 = this.createPipeline("testRestrictWithNoRulesWithPipelineSandbox2", credential1,true, this.nodeLabel);
        WorkflowJob workflowJobNoSandbox1 = this.createPipeline("testRestrictWithNoRulesWithPipelineNoSandbox1", credential1,false, this.nodeLabel);
        WorkflowJob workflowJobNoSandbox2 = this.createPipeline("testRestrictWithNoRulesWithPipelineNoSandbox2", credential1,false, this.nodeLabel);
        shouldFail(workflowJobSandbox1);
        shouldFail(workflowJobSandbox2);
        shouldFail(workflowJobNoSandbox1);
        shouldFail(workflowJobNoSandbox2);
    }

    @Test
    public void testNoRestrictWithNoRulesWithPipeline() throws Exception {
        this.configure(this.jenkinsRule, false, new ArrayList<>());
        WorkflowJob workflowJobSandbox1 = this.createPipeline("testNoRestrictWithNoRulesWithPipelineSandbox1", credential1,true);
        WorkflowJob workflowJobSandbox2 = this.createPipeline("testNoRestrictWithNoRulesWithPipelineSandbox2", credential1,true);
        WorkflowJob workflowJobNoSandbox1 = this.createPipeline("testNoRestrictWithNoRulesWithPipelineNoSandbox1", credential1,false);
        WorkflowJob workflowJobNoSandbox2 = this.createPipeline("testNoRestrictWithNoRulesWithPipelineNoSandbox2", credential1,false);
        shouldSuccess(workflowJobSandbox1);
        shouldSuccess(workflowJobSandbox2);
        shouldSuccess(workflowJobNoSandbox1);
        shouldSuccess(workflowJobNoSandbox2);
    }

    @Test
    public void testNoRestrictWithNoRulesWithPipelineOnNode() throws Exception {
        this.configure(this.jenkinsRule, false, new ArrayList<>());
        WorkflowJob workflowJobSandbox1 = this.createPipeline("testNoRestrictWithNoRulesWithPipelineSandbox1", credential1,true, this.nodeLabel);
        WorkflowJob workflowJobSandbox2 = this.createPipeline("testNoRestrictWithNoRulesWithPipelineSandbox2", credential1,true, this.nodeLabel);
        WorkflowJob workflowJobNoSandbox1 = this.createPipeline("testNoRestrictWithNoRulesWithPipelineNoSandbox1", credential1,false, this.nodeLabel);
        WorkflowJob workflowJobNoSandbox2 = this.createPipeline("testNoRestrictWithNoRulesWithPipelineNoSandbox2", credential1,false, this.nodeLabel);
        shouldSuccess(workflowJobSandbox1);
        shouldSuccess(workflowJobSandbox2);
        shouldSuccess(workflowJobNoSandbox1);
        shouldSuccess(workflowJobNoSandbox2);
    }

    @Test
    public void testMatchingWithRulesWithFreeStyleWithRestrict() throws Exception {
        this.configure(this.jenkinsRule, true, Arrays.asList(new CredentialRole("testMatchingWithRulesWithFreeStyle","credential1.*","testMatchingWithRulesWithFreeStyleWithRestrict.*")));
        FreeStyleProject freeStyleProject = this.createFreeStyle("testMatchingWithRulesWithFreeStyleWithRestrict1", credential1);
        shouldSuccess(freeStyleProject);
        freeStyleProject = this.createFreeStyle("FAILtestMatchingWithRulesWithFreeStyleWithRestrict2", credential1);
        shouldFail(freeStyleProject);
        freeStyleProject = this.createFreeStyle("testMatchingWithRulesWithFreeStyleWithRestrict2", credential2);
        shouldFail(freeStyleProject);
        freeStyleProject = this.createFreeStyle("anotherJob1", credential1);
        shouldFail(freeStyleProject);
        freeStyleProject = this.createFreeStyle("anotherJob2", credential2);
        shouldFail(freeStyleProject);
    }

    @Test
    public void testMatchingWithRulesWithPipelineWithRestrict() throws Exception {
        this.configure(this.jenkinsRule, true, Arrays.asList(new CredentialRole("testMatchingWithRulesWithPipelineWithRestrict","credential1.*","testMatchingWithRulesWithPipelineWithRestrict.*")));
        WorkflowJob workflowJob = this.createPipeline("testMatchingWithRulesWithPipelineWithRestrict1", credential1,true);
        shouldSuccess(workflowJob);
        workflowJob = this.createPipeline("FAILtestMatchingWithRulesWithPipelineWithRestrict2", credential1, true);
        shouldFail(workflowJob);
        workflowJob = this.createPipeline("testMatchingWithRulesWithPipelineWithRestrict2", credential2, true);
        shouldFail(workflowJob);
        workflowJob = this.createPipeline("anotherJob1", credential1, true);
        shouldFail(workflowJob);
        workflowJob = this.createPipeline("anotherJob2", credential2, true);
        shouldFail(workflowJob);
    }

    @Test
    public void testMatchingWithRulesWithFreeStyleWithNoRestrict() throws Exception {
        this.configure(this.jenkinsRule, false, Arrays.asList(new CredentialRole("testMatchingWithRulesWithFreeStyle","credential1.*","testMatchingWithRulesWithFreeStyleWithNoRestrict.*")));
        FreeStyleProject freeStyleProject = this.createFreeStyle("testMatchingWithRulesWithFreeStyleWithNoRestrict1", credential1);
        shouldSuccess(freeStyleProject);
        freeStyleProject = this.createFreeStyle("FAILtestMatchingWithRulesWithFreeStyleWithNoRestrict2", credential1);
        shouldFail(freeStyleProject);
        freeStyleProject = this.createFreeStyle("testMatchingWithRulesWithFreeStyleWithNoRestrict2", credential2);
        shouldSuccess(freeStyleProject);
        freeStyleProject = this.createFreeStyle("anotherJob1", credential1);
        shouldFail(freeStyleProject);
        freeStyleProject = this.createFreeStyle("anotherJob2", credential2);
        shouldSuccess(freeStyleProject);
    }

    @Test
    public void testMatchingWithRulesWithPipelineWithNoRestrict() throws Exception {
        this.configure(this.jenkinsRule, false, Arrays.asList(new CredentialRole("testMatchingWithRulesWithPipelineWithNoRestrict","credential1.*","testMatchingWithRulesWithPipelineWithNoRestrict.*")));
        WorkflowJob workflowJob = this.createPipeline("testMatchingWithRulesWithPipelineWithNoRestrict1", credential1,true);
        shouldSuccess(workflowJob);
        workflowJob = this.createPipeline("FAILtestMatchingWithRulesWithPipelineWithNoRestrict2", credential1, true);
        shouldFail(workflowJob);
        workflowJob = this.createPipeline("testMatchingWithRulesWithPipelineWithNoRestrict2", credential2, true);
        shouldSuccess(workflowJob);
        workflowJob = this.createPipeline("anotherJob1", credential1, true);
        shouldFail(workflowJob);
        workflowJob = this.createPipeline("anotherJob2", credential2, true);
        shouldSuccess(workflowJob);
    }

    @Test
    public void testCredentialUsageCount() throws Exception {
        WorkflowJob workflowJob = this.createPipeline("testCredentialUsageCountPipeline", credential1,true);
        shouldSuccess(workflowJob);
        FreeStyleProject freeStyleProject = this.createFreeStyle("testCredentialUsageCountFreeStyle", credential1);
        shouldSuccess(freeStyleProject);
        CredentialUsages credentialUsages  = this.getCredentialUsages(this.jenkinsRule);
        CredentialUsage credentialUsage = credentialUsages.getCredentialUsageMap().get(credential1.getId());
        Assert.assertEquals(2, credentialUsage.getTotalUsageCount().intValue());
        Assert.assertEquals(1, credentialUsage.getItemUsage().get(workflowJob.getUrl()).intValue());
        Assert.assertEquals(1, credentialUsage.getItemUsage().get(freeStyleProject.getUrl()).intValue());
    }

    @Test
    public void testCredentialUsageCountOnNode() throws Exception {
        WorkflowJob workflowJob = this.createPipeline("testCredentialUsageCountPipeline", credential1,true, this.nodeLabel);
        shouldSuccess(workflowJob);
        FreeStyleProject freeStyleProject = this.createFreeStyle("testCredentialUsageCountFreeStyle", credential1, this.nodeLabel);
        shouldSuccess(freeStyleProject);
        CredentialUsages credentialUsages  = this.getCredentialUsages(this.jenkinsRule);
        CredentialUsage credentialUsage = credentialUsages.getCredentialUsageMap().get(credential1.getId());
        Assert.assertEquals(2, credentialUsage.getTotalUsageCount().intValue());
        Assert.assertEquals(1, credentialUsage.getItemUsage().get(workflowJob.getUrl()).intValue());
        Assert.assertEquals(1, credentialUsage.getItemUsage().get(freeStyleProject.getUrl()).intValue());
    }


}
