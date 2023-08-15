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

public class CredentialRoles extends AbstractDescribableImpl<CredentialRoles> {

    private static final Logger LOGGER = Logger.getLogger(CredentialRoles.class.getName());

    private List<CredentialRole> itemCredentialRoleList;

    private Boolean restrictNotMatching = false;

    @DataBoundConstructor
    public CredentialRoles(List<CredentialRole> itemCredentialRoleList, Boolean restrictNotMatching) {
        this.itemCredentialRoleList = itemCredentialRoleList;
        this.restrictNotMatching = restrictNotMatching;
    }

    public List<CredentialRole> getItemCredentialRoleList() {
        return itemCredentialRoleList;
    }

    @DataBoundSetter
    public void setItemCredentialRoleList(List<CredentialRole> itemCredentialRoleList) {
        this.itemCredentialRoleList = itemCredentialRoleList;
    }

    public Boolean getRestrictNotMatching() {
        return restrictNotMatching;
    }

    @DataBoundSetter
    public void setRestrictNotMatching(Boolean restrictNotMatching) {
        this.restrictNotMatching = restrictNotMatching;
    }

    public List<Descriptor<CredentialRole>> getItemCredentialRoleListDescriptors() {
        return Jenkins.getActiveInstance().getDescriptorList(CredentialRole.class);
    }

    @Extension
    public static final class CredentialRolesDescriptorImpl extends Descriptor<CredentialRoles> implements RootElementConfigurator<CredentialRoles> {

        public List<CredentialRole> credentialRoleList = new ArrayList<>();

        public Boolean restrictNotMatching = false;

        public CredentialRoles getItemCredentialRoles() {
            return new CredentialRoles(this.credentialRoleList, this.restrictNotMatching);
        }

        private CredentialRoles processCasc(CNode config) throws ConfiguratorException {
            LOGGER.fine("Processing Casc");
            Boolean restrictNotMatching = false;
            List<CredentialRole> credentialRoleList = new ArrayList<>();
            Mapping credentialRolesMapping = config.asMapping();
            for (Map.Entry<String, CNode> entry : credentialRolesMapping.entrySet()) {
                LOGGER.fine(String.format("Processing Cnode Entry for %s", entry.getKey()));
                String key = entry.getKey();
                if (key.equals("restrictNotMatching")) {
                    restrictNotMatching = Boolean.valueOf(entry.getValue().toString());
                    LOGGER.fine(String.format("Setting restrictNotMatching from Casc with value:%s", restrictNotMatching));
                } else {
                    Mapping credentialRoleMapping = entry.getValue().asMapping();
                    if (!(credentialRoleMapping.containsKey("credentialPattern") && credentialRoleMapping.containsKey("itemPattern"))) {
                        LOGGER.fine("Keys credentialPattern/itemPattern are missing from Casc");
                        throw new ConfiguratorException(String.format("Missing credentialPattern/itemPattern key for %s", key));
                    }
                    String credentialPattern = credentialRoleMapping.get("credentialPattern").toString();
                    String itemPattern = credentialRoleMapping.get("itemPattern").toString();
                    LOGGER.fine(String.format("Adding Credential Role with values %s %s %s", key, credentialPattern, itemPattern));
                    credentialRoleList.add(new CredentialRole(key, credentialPattern, itemPattern));
                }
            }
            return new CredentialRoles(credentialRoleList, restrictNotMatching);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return "Credential Roles";
        }

        @Override
        public Class<CredentialRoles> getTarget() {
            return CredentialRoles.class;
        }

        @NonNull
        @Override
        public Set<Attribute<CredentialRoles, ?>> describe() {
            Set<Attribute<CredentialRoles, ?>> credentialRolesDefinitions = new HashSet<>();
            CredentialRoles credentialRoles = CredentialRoleConfiguration.loadItemCredentialRoles();
            credentialRolesDefinitions.add(
                    new Attribute<CredentialRoles, Boolean>("restrictNotMatching", Boolean.class)
                            .getter(target -> {
                                return credentialRoles.getRestrictNotMatching();
                            })
            );
            for (CredentialRole credentialRole : credentialRoles.getItemCredentialRoleList()) {
                Attribute<CredentialRoles, ?> attribute = new Attribute<CredentialRoles, CascCredentialRule>(credentialRole.getName(), CascCredentialRule.class).getter(target -> {
                    return new CascCredentialRule(credentialRole.getItemPattern(), credentialRole.getCredentialPattern());
                });
                credentialRolesDefinitions.add(attribute);
            }

            return credentialRolesDefinitions;
        }

        @NonNull
        @Override
        public CredentialRoles configure(CNode config, ConfigurationContext context) throws ConfiguratorException {
            CredentialRoles credentialRoles = this.processCasc(config);
            this.credentialRoleList = credentialRoles.getItemCredentialRoleList();
            this.restrictNotMatching = credentialRoles.getRestrictNotMatching();
            this.save();
            return credentialRoles;
        }

        @Override
        public CredentialRoles check(CNode config, ConfigurationContext context) throws ConfiguratorException {
            CredentialRoles credentialRoles = this.processCasc(config);
            return credentialRoles;
        }


        @Override
        public boolean configure(StaplerRequest req, JSONObject submittedForm) throws FormException {
            LOGGER.fine("Configuring Credentials Roles");
            List<CredentialRole> submittedItemCredentialRoleList = new ArrayList<>();
            Boolean submittedRestrictNotMatching = submittedForm.getBoolean("restrictNotMatching");
            LOGGER.fine(String.format("Found submitted restrictNotMatching with value:%s", submittedRestrictNotMatching));
            JSONArray definedItemCredentialRoles = JSONArray.fromObject(submittedForm.getOrDefault("definedItemCredentialRoles", new JSONArray()));
            for (Object item : definedItemCredentialRoles) {
                JSONObject definedItemCredentialRole = JSONObject.fromObject(item);
                String definitionName = definedItemCredentialRole.getString("name");
                if (Util.fixEmptyAndTrim(definitionName) == null)
                    throw new Descriptor.FormException("Name can't be empty", definitionName);
                String definitionPattern = definedItemCredentialRole.getString("credentialPattern");
                if (Util.fixEmptyAndTrim(definitionPattern) == null)
                    throw new Descriptor.FormException("Credential pattern can't be empty", definitionPattern);
                String definitionRoleName = definedItemCredentialRole.getString("itemPattern");
                if (Util.fixEmptyAndTrim(definitionRoleName) == null)
                    throw new Descriptor.FormException("Item Pattern can't be empty", definitionRoleName);
                LOGGER.fine(String.format("Adding Credential Role with values %s %s %s", definitionName, definitionPattern, definitionRoleName));
                submittedItemCredentialRoleList.add(new CredentialRole(definitionName, definitionPattern, definitionRoleName));
            }
            this.credentialRoleList = submittedItemCredentialRoleList;
            this.restrictNotMatching = submittedRestrictNotMatching;
            this.save();
            return super.configure(req, submittedForm);
        }

        @Override
        public CredentialRoles getTargetComponent(ConfigurationContext context) {
            return CredentialRoleConfiguration.loadItemCredentialRoles();
        }
    }
}
