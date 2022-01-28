package io.jenkins.plugins.tagger;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;

public class TagAttribute extends AbstractDescribableImpl<TagAttribute> {
    private final String key;
    private final String value;

    @DataBoundConstructor
    public TagAttribute(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<TagAttribute> {

        @Override
        public String getDisplayName() {
            return "";
        }

        public FormValidation doCheckAttributeKey(@QueryParameter String value) {
            if (value.length() == 0) {
                return FormValidation.error("Attribute Key must not be empty");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckAttributeValue(@QueryParameter String value) {
            if (value.length() == 0) {
                return FormValidation.error("Attribute Value must not be empty");
            }
            return FormValidation.ok();
        }
    }
    
}
