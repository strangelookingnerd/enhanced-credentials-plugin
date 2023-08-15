package io.jenkins.plugins.enhanced.credentials.listener;

import hudson.Extension;
import hudson.model.*;
import hudson.tasks.BuildStep;
import io.jenkins.plugins.enhanced.credentials.CredentialRuleSupporter;
import lombok.SneakyThrows;

import java.util.logging.Logger;

public class GenericStepListener {

    private static final Logger LOGGER = Logger.getLogger(GenericStepListener.class.getName());

    @Extension
    public static class GenericBuildStepListener extends BuildStepListener{

        CredentialRuleSupporter credentialRuleSupporter = new CredentialRuleSupporter();

        @SneakyThrows
        @Override
        public void started(AbstractBuild build, BuildStep bs, BuildListener listener) {
            if( build instanceof FreeStyleBuild) {
                FreeStyleBuild freeStyleBuild = (FreeStyleBuild) build;
                LOGGER.fine(String.format("Found instance of FreeStyleBuild for %s", freeStyleBuild.getUrl()));
                if(GenericCredentialListener.freeStyleBuildsToStop.contains(freeStyleBuild)){
                    LOGGER.fine(String.format("Stopping Step for FreeStyleBuild:%s", freeStyleBuild.getUrl()));
                    credentialRuleSupporter.tryStoppingFreeStyleBuild(freeStyleBuild);
                }
            }
        }

        @Override
        public void finished(AbstractBuild build, BuildStep bs, BuildListener listener, boolean canContinue) {}
    }


}
