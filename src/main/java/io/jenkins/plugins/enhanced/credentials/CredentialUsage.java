package io.jenkins.plugins.enhanced.credentials;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CredentialUsage {

    private String credentialId;
    private Integer totalUsageCount;
    private HashMap<String, Integer> itemUsage = new HashMap<>();
    private HashMap<String, Integer> nodeUsage = new HashMap<>();

    public CredentialUsage(String credentialId){
        this.credentialId = credentialId;
        this.totalUsageCount = 0;
    }

    public CredentialUsage incrementItemUsage(String itemName){
        this.itemUsage.put(itemName, (this.itemUsage.getOrDefault(itemName, 0) + 1));
        this.totalUsageCount = this.calculateTotalUsageCount();
        return this;
    }

    public CredentialUsage incrementNodeUsage(String nodeName){
        this.nodeUsage.put(nodeName, (this.nodeUsage.getOrDefault(nodeName, 0) + 1));
        this.totalUsageCount = this.calculateTotalUsageCount();
        return this;
    }

    private Integer calculateTotalUsageCount(){
        Integer totalUsageCount = 0;
        for(Map.Entry<String, Integer> usage : this.itemUsage.entrySet()){
            totalUsageCount = totalUsageCount + usage.getValue();
        }
        for(Map.Entry<String, Integer> usage : this.nodeUsage.entrySet()){
            totalUsageCount = totalUsageCount + usage.getValue();
        }
        return totalUsageCount;
    }
}
