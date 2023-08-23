package io.jenkins.plugins.enhanced.credentials;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import io.jenkins.plugins.casc.Attribute;
import io.jenkins.plugins.casc.ConfigurationContext;
import io.jenkins.plugins.casc.ConfiguratorException;
import io.jenkins.plugins.casc.RootElementConfigurator;
import io.jenkins.plugins.casc.model.CNode;
import io.jenkins.plugins.casc.model.Mapping;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

import java.util.*;
import java.util.logging.Logger;

public class CredentialRules extends AbstractDescribableImpl<CredentialRules> {

    private static final Logger LOGGER = Logger.getLogger(CredentialRules.class.getName());

    private List<CredentialRule> credentialRuleList;

    private Boolean restrictNotMatching = false;

    @DataBoundConstructor
    public CredentialRules(List<CredentialRule> credentialRuleList, Boolean restrictNotMatching) {
        this.credentialRuleList = credentialRuleList;
        this.restrictNotMatching = restrictNotMatching;
    }

    public List<CredentialRule> getCredentialRuleList() {
        return credentialRuleList;
    }

    @DataBoundSetter
    public void setCredentialRuleList(List<CredentialRule> credentialRuleList) {
        this.credentialRuleList = credentialRuleList;
    }

    public Boolean getRestrictNotMatching() {
        return restrictNotMatching;
    }

    @DataBoundSetter
    public void setRestrictNotMatching(Boolean restrictNotMatching) {
        this.restrictNotMatching = restrictNotMatching;
    }

    public List<Descriptor<CredentialRule>> getCredentialRuleListDescriptors() {
        return Jenkins.get().getDescriptorList(CredentialRule.class);
    }

    @Extension
    public static final class CredentialRulesDescriptorImpl extends Descriptor<CredentialRules> implements RootElementConfigurator<CredentialRules> {

        public List<CredentialRule> credentialRuleList = new ArrayList<>();

        public Boolean restrictNotMatching = false;

        public CredentialRules getCredentialRules() {
            return new CredentialRules(this.credentialRuleList, this.restrictNotMatching);
        }

        private CredentialRules processCasc(CNode config) throws ConfiguratorException {
            LOGGER.fine("Processing Casc");
            Boolean restrictNotMatching = false;
            List<CredentialRule> credentialRuleList = new ArrayList<>();
            Mapping credentialRulesMapping = config.asMapping();
            for (Map.Entry<String, CNode> entry : credentialRulesMapping.entrySet()) {
                LOGGER.fine(String.format("Processing Cnode Entry for %s", entry.getKey()));
                String key = entry.getKey();
                if (key.equals("restrictNotMatching")) {
                    restrictNotMatching = Boolean.valueOf(entry.getValue().toString());
                    LOGGER.fine(String.format("Setting restrictNotMatching from Casc with value:%s", restrictNotMatching));
                } else {
                    Mapping credentialRuleMapping = entry.getValue().asMapping();
                    if (!(credentialRuleMapping.containsKey("credentialPattern") && credentialRuleMapping.containsKey("itemPattern"))) {
                        LOGGER.fine("Keys credentialPattern/itemPattern are missing from Casc");
                        throw new ConfiguratorException(String.format("Missing credentialPattern/itemPattern key for %s", key));
                    }
                    String credentialPattern = credentialRuleMapping.get("credentialPattern").toString();
                    String itemPattern = credentialRuleMapping.get("itemPattern").toString();
                    LOGGER.fine(String.format("Adding Credential Rule with values %s %s %s", key, credentialPattern, itemPattern));
                    credentialRuleList.add(new CredentialRule(key, credentialPattern, itemPattern));
                }
            }
            return new CredentialRules(credentialRuleList, restrictNotMatching);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return "Credential Rules";
        }

        @Override
        public Class<CredentialRules> getTarget() {
            return CredentialRules.class;
        }

        @NonNull
        @Override
        public Set<Attribute<CredentialRules, ?>> describe() {
            Set<Attribute<CredentialRules, ?>> credentialRulesDefinitions = new HashSet<>();
            CredentialRules credentialRules = CredentialRuleConfiguration.loadCredentialRules();
            credentialRulesDefinitions.add(
                    new Attribute<CredentialRules, Boolean>("restrictNotMatching", Boolean.class)
                            .getter(target -> {
                                return credentialRules.getRestrictNotMatching();
                            })
            );
            for (CredentialRule credentialRule : credentialRules.getCredentialRuleList()) {
                Attribute<CredentialRules, ?> attribute = new Attribute<CredentialRules, CascCredentialRule>(credentialRule.getName(), CascCredentialRule.class).getter(target -> {
                    return new CascCredentialRule(credentialRule.getItemPattern(), credentialRule.getCredentialPattern());
                });
                credentialRulesDefinitions.add(attribute);
            }

            return credentialRulesDefinitions;
        }

        @NonNull
        @Override
        public CredentialRules configure(CNode config, ConfigurationContext context) throws ConfiguratorException {
            CredentialRules credentialRules = this.processCasc(config);
            this.credentialRuleList = credentialRules.getCredentialRuleList();
            this.restrictNotMatching = credentialRules.getRestrictNotMatching();
            this.save();
            return credentialRules;
        }

        @Override
        public CredentialRules check(CNode config, ConfigurationContext context) throws ConfiguratorException {
            CredentialRules credentialRules = this.processCasc(config);
            return credentialRules;
        }


        @Override
        public boolean configure(StaplerRequest req, JSONObject submittedForm) throws FormException {
            LOGGER.fine("Configuring Credentials Rules");
            List<CredentialRule> submittedcredentialRuleList = new ArrayList<>();
            Boolean submittedRestrictNotMatching = submittedForm.getBoolean("restrictNotMatching");
            LOGGER.fine(String.format("Found submitted restrictNotMatching with value:%s", submittedRestrictNotMatching));
            JSONArray definedCredentialRules = JSONArray.fromObject(submittedForm.getOrDefault("definedCredentialRules", new JSONArray()));
            for (Object item : definedCredentialRules) {
                JSONObject definedCredentialRule = JSONObject.fromObject(item);
                String definitionName = definedCredentialRule.getString("name");
                if (Util.fixEmptyAndTrim(definitionName) == null)
                    throw new Descriptor.FormException("Name can't be empty", definitionName);
                String definitionPattern = definedCredentialRule.getString("credentialPattern");
                if (Util.fixEmptyAndTrim(definitionPattern) == null)
                    throw new Descriptor.FormException("Credential pattern can't be empty", definitionPattern);
                String definitionRuleName = definedCredentialRule.getString("itemPattern");
                if (Util.fixEmptyAndTrim(definitionRuleName) == null)
                    throw new Descriptor.FormException("Item Pattern can't be empty", definitionRuleName);
                LOGGER.fine(String.format("Adding Credential Rule with values %s %s %s", definitionName, definitionPattern, definitionRuleName));
                submittedcredentialRuleList.add(new CredentialRule(definitionName, definitionPattern, definitionRuleName));
            }
            this.credentialRuleList = submittedcredentialRuleList;
            this.restrictNotMatching = submittedRestrictNotMatching;
            this.save();
            return super.configure(req, submittedForm);
        }

        @Override
        public CredentialRules getTargetComponent(ConfigurationContext context) {
            return CredentialRuleConfiguration.loadCredentialRules();
        }
    }
}
