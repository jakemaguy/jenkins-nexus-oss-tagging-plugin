package io.jenkins.plugins.tagger;
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
    private transient final NexusArtifactTaggerStep step;


    public NexusArtifactTaggerRunner(TaskListener listener, NexusArtifactTaggerStep step) {
        this.listener = listener;
        this.step = step;
    }

    @Override
    public Boolean call() throws Throwable {
        // TaskListener listener = getContext().get(TaskListener.class);

        int exitCode; // exit code from querying if the tag exists already
        try(CloseableHttpClient httpclient = HttpClients.createDefault()) {
            //perform get request to determine if tag already exsists
            HttpGet doesTagExist = new HttpGet(step.getNexusUrl() + "/service/rest/v1/tags/" + step.getName());
            CloseableHttpResponse response = httpclient.execute(doesTagExist);
            exitCode = response.getStatusLine().getStatusCode();

            listener.getLogger().println("Creating Tag: " + step.getName());
            response.close();
        }

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            CloseableHttpResponse response;
            // if tag does not exist you'll get 404 status code
            if (exitCode != 200) {
                // create the tag
                HttpPost postRequest = new HttpPost(step.getNexusUrl() + "/service/rest/v1/tags");
                HttpEntity stringEntity = new StringEntity(step.getRequestBody(),ContentType.APPLICATION_JSON);
                postRequest.setEntity(stringEntity);
                response = httpclient.execute(postRequest);
            } else {
                // tag already exists, update it with PUT
                HttpPut putRequest = new HttpPut(step.getNexusUrl() + "/service/rest/v1/tags/" + step.getName());
                HttpEntity stringEntity = new StringEntity(step.getRequestBody(),ContentType.APPLICATION_JSON);
                putRequest.setEntity(stringEntity);
                response = httpclient.execute(putRequest);
            }
            int requestExitCode = response.getStatusLine().getStatusCode();
            response.close();
            if (requestExitCode != 200) {
                listener.getLogger().println("\nTag Request Failed with Exit Code: " + requestExitCode);
                listener.getLogger().println("FAILURE REASON: " + response.getStatusLine().getReasonPhrase());
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity);
                throw new HttpException("Unexpected response to CONNECT request\n" + result);
            }
            listener.getLogger().println("Tag Created Sucesfully " + requestExitCode);
        }
        return true;
    }
}
