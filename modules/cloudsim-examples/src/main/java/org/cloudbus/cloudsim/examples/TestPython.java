package org.cloudbus.cloudsim.examples;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpHeaders;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestPython {
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    public static void startTraining() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:5000/train"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Training started: " + response.body());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean getProgress() {
    	boolean completed = false;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:5000/progress"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
         // Parse the JSON response body using Jackson
            String jsonResponse = response.body();
            JsonNode jsonNode = objectMapper.readTree(jsonResponse);

            // Extract the 'status' field from the JSON
            String status = jsonNode.path("status").asText();
            System.out.println("Training Status: " + status);
            
            System.out.println("Progress: " + response.body());
            if(status.equals("Completed")) {
            	completed = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return completed;
    }
    
    public static void main(String[] args) throws InterruptedException {
        startTraining();
        boolean completed = false;
        // Poll progress every 2 seconds
        while (completed!=true) {
            completed = getProgress();
            TimeUnit.SECONDS.sleep(2);
        }
    }
}

