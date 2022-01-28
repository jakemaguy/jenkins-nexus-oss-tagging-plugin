package io.jenkins.plugins.tagger;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;

public class TagComponent extends AbstractDescribableImpl<TagComponent> {
    private final String repo;
    private final String group;
    private final String name;
    private final String version;

    @DataBoundConstructor
    public TagComponent(String repo, String group, String name, String version) {
        this.repo = repo;
        this.group = group;
        this.name = name;
        this.version = version;
    }

    public String getRepo() {
        return repo;
    }

    public String getGroup() {
        return group;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<TagComponent> {

        @Override
        public String getDisplayName() {
            return "";
        }

        public FormValidation doCheckRepo(@QueryParameter String value) {
            if (value.length() == 0) {
                return FormValidation.error("Repo must not be empty");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckGroup(@QueryParameter String value) {
            if (value.length() == 0) {
                return FormValidation.error("Group must not be empty");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckName(@QueryParameter String value) {
            if (value.length() == 0) {
                return FormValidation.error("Name must not be empty");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckVersion(@QueryParameter String value) {
            if (value.length() == 0) {
                return FormValidation.error("Version must not be empty");
            }
            return FormValidation.ok();
        }
    }
    
}
