package io.jenkins.plugins.tagger;
import java.io.IOException;
import java.util.logging.Logger;

import org.apache.commons.httpclient.HttpException;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import hudson.model.TaskListener;
import jenkins.security.MasterToSlaveCallable;

public class NexusArtifactTaggerRunner extends MasterToSlaveCallable<Boolean, Throwable> {
    private static final long serialVersionUID = 1L;
    private final TaskListener listener;
    private final String tagName;
    private final String requestBody;
    private final String nexusUrl;


    public NexusArtifactTaggerRunner(TaskListener listener, String tagName, String requestBody, String nexusUrl) {
        this.listener = listener;
        this.tagName = tagName;
        this.requestBody = requestBody;
        this.nexusUrl = nexusUrl;
    }


    private int doesTagExist() throws IOException {
        int exitCode;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpGet doesTagExist = new HttpGet(nexusUrl + "/service/rest/v1/tags/" + tagName);
            CloseableHttpResponse response = httpclient.execute(doesTagExist);
            try {
                exitCode = response.getStatusLine().getStatusCode();
            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }
        return exitCode;
    }

    @Override
    public Boolean call() throws Throwable {
        listener.getLogger().println("Creating Tag: " + tagName);
        int exitCode = doesTagExist();
        
        
        int requestExitCode; // exit code for the PUT/POST request
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            CloseableHttpResponse response;
            // if tag does not exist you'll get 404 status code
            if (exitCode != 200) {
                // create the tag
                HttpPost postRequest = new HttpPost(nexusUrl + "/service/rest/v1/tags");
                HttpEntity stringEntity = new StringEntity(requestBody,ContentType.APPLICATION_JSON);
                postRequest.setEntity(stringEntity);
                response = httpclient.execute(postRequest);
            } else {
                // tag already exists, update it with PUT
                HttpPut putRequest = new HttpPut(nexusUrl + "/service/rest/v1/tags/" + tagName);
                HttpEntity stringEntity = new StringEntity(requestBody,ContentType.APPLICATION_JSON);
                putRequest.setEntity(stringEntity);
                response = httpclient.execute(putRequest);
            }
            requestExitCode = response.getStatusLine().getStatusCode();
            if (requestExitCode != 200) {
                listener.getLogger().println("\nTag Request Failed with Exit Code: " + requestExitCode);
                listener.getLogger().println("FAILURE REASON: " + response.getStatusLine().getReasonPhrase());
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity);
                throw new HttpException("Unexpected response to CONNECT request\n" + result);
            }
        }
        this.listener.getLogger().println("Tag Created Successfully " + requestExitCode);
        return true;
    }
}
