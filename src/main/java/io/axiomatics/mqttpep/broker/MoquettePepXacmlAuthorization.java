package io.axiomatics.mqttpep.broker;

import io.moquette.broker.security.IAuthorizatorPolicy;
import io.moquette.broker.subscriptions.Topic;
import io.moquette.broker.config.IConfig;
import io.xacml.json.model.Request;
import io.xacml.json.model.Response;
import io.xacml.json.model.Result;
import io.xacml.json.model.PDPDecision;
import io.xacml.pep.json.client.AuthZClient;
import io.xacml.pep.json.client.ClientConfiguration;
import io.xacml.pep.json.client.DefaultClientConfiguration;
import io.xacml.pep.json.client.feign.FeignAuthZClient;
import io.xacml.json.model.Category;
import io.xacml.json.model.Attribute;

public class MoquettePepXacmlAuthorization implements IAuthorizatorPolicy {

    private static final String PDP_URL = "http://192.168.100.7:8081/authorize";  // PDP URL
    private final AuthZClient authZClient;

    // Constructor to initialize the PEP client with PDP configuration
    public MoquettePepXacmlAuthorization(IConfig config) {
        ClientConfiguration clientConfig = DefaultClientConfiguration.builder()
                .authorizationServiceUrl(PDP_URL)
                .username("pdp-user")  // Set the PDP username
                .password("secret")    // Set the PDP password
                .build();
        authZClient = new FeignAuthZClient(clientConfig);  // Use Feign to handle communication with PDP
    }

    // Check if a client is authorized to publish to a topic

    public boolean canWrite(String clientId, String topic) {
        System.out.println("PEP is called onWrite");
        return isActionAllowed(clientId, topic, "publish");  // Check authorization for publishing
    }

    // Check if a client is authorized to subscribe to a topic

    public boolean canRead(String clientId, String topic) {
        System.out.println("PEP is called onRead");
        return isActionAllowed(clientId, topic, "subscribe");  // Check authorization for subscribing
    }

    // Internal method to check if a client action is permitted based on XACML
    private boolean isActionAllowed(String clientId, String resource, String action) {
        try {
            Request request = buildXacmlRequest(clientId, resource, action);  // Build the XACML request
            Response response = authZClient.makeAuthorizationRequest(request);  // Send request to PDP and get the response

            // Process the PDP's response
            for (Result result : response.getResults()) {
                if (result.getDecision().equals(PDPDecision.PERMIT)) {
                    System.out.println("Authorization PERMITTED for action: " + action + " on resource: " + resource);
                    return true;  // Authorization is allowed
                }
            }
            System.out.println("Authorization DENIED for action: " + action + " on resource: " + resource);
        } catch (Exception e) {
            e.printStackTrace();  // Handle any exceptions during authorization
        }
        return false;  // Authorization denied by default if any error occurs
    }

    // Method to build the XACML request based on client ID, resource, and action
    private Request buildXacmlRequest(String clientId, String resource, String action) {
        // Create the subject category for the XACML request
        Category subject = new Category();
        subject.addAttribute(new Attribute("clientId", clientId));  // Add clientId as an attribute
        subject.addAttribute(new Attribute("resource", resource));  // Add resource (topic) as an attribute

        // Create the action category for the XACML request
        Category actionCategory = new Category();
        actionCategory.addAttribute(new Attribute("action", action));  // Add action (publish/subscribe) as an attribute

        // Create the XACML request with subject and action categories
        Request request = new Request();
        request.addAccessSubjectCategory(subject);
        request.addActionCategory(actionCategory);

        return request;  // Return the constructed XACML request
    }

    @Override
    public boolean canWrite(Topic topic, String user, String client) {
        System.out.println("1-PEP is called onWrite");
        return true;
    }

    @Override
    public boolean canRead(Topic topic, String user, String client) {

                System.out.println("1-PEP is called onRead");
                return true;
    }
}
