package io.jenkins.plugins.enhanced.credentials;

import lombok.Getter;
import lombok.Setter;
import org.kohsuke.stapler.DataBoundConstructor;

@Getter
@Setter
public class CascCredentialRule {

    private String itemPattern;
    private String credentialPattern;

    @DataBoundConstructor
    public CascCredentialRule(String itemPattern, String credentialPattern) {
        this.itemPattern = itemPattern;
        this.credentialPattern = credentialPattern;
    }

}
