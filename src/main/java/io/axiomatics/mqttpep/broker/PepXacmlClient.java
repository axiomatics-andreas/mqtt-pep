package io.axiomatics.mqttpep.broker;

import io.xacml.json.model.*;
import io.xacml.pep.json.client.AuthZClient;
import io.xacml.pep.json.client.ClientConfiguration;
import io.xacml.pep.json.client.DefaultClientConfiguration;
import io.xacml.pep.json.client.feign.FeignAuthZClient;

public class PepXacmlClient {

    private static final String CLIENT_ID = "client1";
    private static final String PDP_URL = "http://192.168.100.7:8081/authorize";

    public static void main(String[] args) {
        // Example usage of the PEP authorization check
        String action = "publish";
        String resource = "test/resource";

        if (isAllowed(action, resource)) {
            System.out.println(action + " action is permitted for resource: " + resource);
        } else {
            System.out.println(action + " action is denied for resource: " + resource);
        }
    }

    // Function to check authorization using Axiomatics PEP
    private static boolean isAllowed(String action, String resource) {
        ClientConfiguration config = DefaultClientConfiguration.builder()
                .authorizationServiceUrl(PDP_URL)
                .username("pdp-user")
                .password("secret")
                .build();

        AuthZClient authZClient = new FeignAuthZClient(config);

        Request request = buildXacmlRequest(action, resource);
        Response response = authZClient.makeAuthorizationRequest(request);

        // Check the decision from PDP
        for (Result result : response.getResults()) {
            if (result.getDecision().equals(PDPDecision.PERMIT)) {
                return true;
            }
        }
        return false;
    }

    // Function to build XACML request
    private static Request buildXacmlRequest(String action, String resource) {
        Category subject = new Category();
        subject.addAttribute(new Attribute("clientId", CLIENT_ID));
        subject.addAttribute(new Attribute("resource", resource));

        Category actionCategory = new Category();
        actionCategory.addAttribute(new Attribute("action", action));

        Request request = new Request();
        request.addAccessSubjectCategory(subject);
        request.addActionCategory(actionCategory);

        return request;
    }
}
