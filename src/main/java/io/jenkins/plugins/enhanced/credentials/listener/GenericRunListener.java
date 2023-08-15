package io.jenkins.plugins.enhanced.credentials.listener;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.*;
import hudson.model.listeners.RunListener;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.util.HashMap;
import java.util.logging.Logger;

public class GenericRunListener {

    private static final Logger LOGGER = Logger.getLogger(GenericRunListener.class.getName());

    private final static HashMap<String, TaskListener> taskListenerMap = new HashMap<>();

    public static TaskListener getTaskListener(String url){
        return taskListenerMap.get(url);
    }

    @Extension
    public static class GenericWorkflowListener extends RunListener<WorkflowRun> {
        @Override
        public void onStarted(WorkflowRun workflowRun, TaskListener listener) {
            super.onStarted(workflowRun, listener);
            LOGGER.fine(String.format("Putting Task Listener for %s", workflowRun.getUrl()));
            taskListenerMap.put(workflowRun.getUrl(), listener);
        }

        @Override
        public void onCompleted(WorkflowRun workflowRun, @NonNull TaskListener listener) {
            super.onCompleted(workflowRun, listener);
            LOGGER.fine(String.format("Removing Task Listener for %s", workflowRun.getUrl()));
            taskListenerMap.remove(workflowRun.getUrl());
        }
    }

    @Extension
    public static class GenericFreeStyleBuildListener extends RunListener<FreeStyleBuild> {
        @Override
        public void onStarted(FreeStyleBuild freeStyleBuild, TaskListener listener) {
            super.onStarted(freeStyleBuild, listener);
            LOGGER.fine(String.format("Putting Task Listener for %s", freeStyleBuild.getUrl()));
            taskListenerMap.put(freeStyleBuild.getUrl(), listener);
        }

        @Override
        public void onCompleted(FreeStyleBuild freeStyleBuild, @NonNull TaskListener listener) {
            super.onCompleted(freeStyleBuild, listener);
            LOGGER.fine(String.format("Removing Task Listener for %s", freeStyleBuild.getUrl()));
            taskListenerMap.remove(freeStyleBuild.getUrl());
        }
    }

}
