/*
 * @created  2021-01-21 : 13:57
 * @project  MixewayScanner
 * @author   siewer
 */
package io.mixeway.rest.vulnmanage.model;

import io.mixeway.mixewaytesting.utils.Vuln;

import java.util.List;

public class MixewaySecurityGatewayResponse {

    boolean isSecurityPolicyMet;
    String policyResponse;
    List<Vuln> vulnList;

    public boolean isSecurityPolicyMet() {
        return isSecurityPolicyMet;
    }

    public void setSecurityPolicyMet(boolean securityPolicyMet) {
        isSecurityPolicyMet = securityPolicyMet;
    }

    public String getPolicyResponse() {
        return policyResponse;
    }

    public void setPolicyResponse(String policyResponse) {
        this.policyResponse = policyResponse;
    }

    public List<Vuln> getVulnList() {
        return vulnList;
    }

    public void setVulnList(List<Vuln> vulnList) {
        this.vulnList = vulnList;
    }

    public MixewaySecurityGatewayResponse(boolean isSecurityPolicyMet, String policyResponse, List<Vuln> vulnList){
        this.isSecurityPolicyMet = isSecurityPolicyMet;
        this.policyResponse = policyResponse;
        this.vulnList = vulnList;
    }

}
