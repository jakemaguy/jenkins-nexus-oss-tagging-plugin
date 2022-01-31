package io.jenkins.plugins.tagger;


import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import hudson.Extension;
import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;

public class NexusArtifactTaggerStep extends Step {

    private String name;
    private String nexusUrl;
    private List<TagAttribute> attributes;
    private List<TagComponent> components;

    @DataBoundConstructor
    public NexusArtifactTaggerStep(String name, String nexusUrl, List<TagAttribute> attributes, List<TagComponent> components) {
        this.name = name;
        this.nexusUrl = nexusUrl;
        this.attributes = attributes;
        this.components = components;
    }

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new ExecutionImpl(context, this);
    }

    public final String getName() {
        return name;
    }

    @DataBoundSetter
    public void setName(String name) {
        this.name = name;
    }

    public final String getNexusUrl() {
        return nexusUrl;
    }

    @DataBoundSetter
    public void setNexusUrl(String nexusUrl) {
        this.nexusUrl = nexusUrl;
    }

    public List<TagAttribute> getAttributes() {
        return attributes;
    }

    @DataBoundSetter
    public void setAttributes(List<TagAttribute> attributes) {
        this.attributes = attributes;
    }

    public List<TagComponent> getComponents() {
        return components;
    }

    @DataBoundSetter
    public void setComponents(List<TagComponent> components) {
        this.components = components;
    }

    public String getRequestBody() {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("name", name);

        JsonObject requestAttributes = new JsonObject();
        if (this.attributes != null) {
            for (TagAttribute attribute : this.attributes) {
                requestAttributes.addProperty(attribute.getKey(), attribute.getValue());
            }
        }
        requestBody.add("attributes", requestAttributes);

        JsonArray requestComponents = new JsonArray();
        if (this.components != null) {
            for (TagComponent component : this.components) {
                JsonObject requestComponent = new JsonObject();
                requestComponent.addProperty("repository", component.getRepo());
                requestComponent.addProperty("group", component.getGroup());
                requestComponent.addProperty("name", component.getName());
                requestComponent.addProperty("version", component.getVersion());
    
                requestComponents.add(requestComponent);
            }
        }
        requestBody.add("components", requestComponents);
        return requestBody.toString();
    }

    private static class ExecutionImpl extends  SynchronousNonBlockingStepExecution<Void> {
        private static final long serialVersionUID = 1L;
        private final transient NexusArtifactTaggerStep step;

        ExecutionImpl(StepContext context, NexusArtifactTaggerStep step) throws Exception {
            super(context);
            this.step = step;
        }

        @Override public Void run() throws IOException, InterruptedException, ExecutionException {
            TaskListener listener = getContext().get(TaskListener.class);
            FilePath filePath = getContext().get(FilePath.class);
            assert filePath != null;
            VirtualChannel virtualChannel = filePath.getChannel();
            assert virtualChannel != null;

            virtualChannel.callAsync(new NexusArtifactTaggerRunner(listener, step.getName(), step.getRequestBody(), step.getNexusUrl())).get();
            return null;
        }
    }

    @Extension public static class DescriptorImpl extends StepDescriptor {

        @Override
        public String getDisplayName() {
            return "Nexus Artifact Tagger";
        }

        @Override
        public String getFunctionName() {
        return "nexusArtifactTagger";
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet.of(Run.class, TaskListener.class);
        }
    }
}
