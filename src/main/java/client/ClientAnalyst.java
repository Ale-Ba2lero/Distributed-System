package client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import client.Pair;
import nodes.sensor.Measurement;
import nodes.sensor.MixInMeasurement;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ClientAnalyst {

    private static final String URI = "http://localhost:8080/sdp_project_red_war_exploded/resources";

    public static void main(String[] args) throws InterruptedException {
        boolean run = true;
        InputStreamReader streamReader = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(streamReader);

        while (run) {
            System.out.println(
                "1: (GET) Get number of nodes in network.\n" +
                "2: (GET) Get last 'n' measurements.\n" +
                "3: (GET) Get mean and standard deviation.\n" +
                "4: (GET) Subscribe to updates\n" +
                "0: Quit.\n"
            );

            try {
                switch (bufferedReader.readLine()) {
                    case "1":
                        System.out.println(GETNetworkSize());
                        break;
                    case "2":
                        GETDataMeasurement();
                        break;
                    case "3":
                        GETStats();
                        break;
                    case "4":
                        GETUpdates();
                        break;
                    case "0":
                        System.out.println("Quit!");
                        run = false;
                        break;
                    default:
                        System.out.println("Wrong input.\n");
                        break;
                }

            } catch (IOException | NumberFormatException e) {
                e.printStackTrace();
            } finally{
                Thread.sleep(1000);
            }
        }
    }

    private static String GETNetworkSize() {
        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target(URI).path("node/howmany");
        Invocation.Builder invocationBuilder = webTarget.request(MediaType.TEXT_PLAIN);
        Response response = invocationBuilder.get();

        //System.out.println(response.getStatus());
        return response.readEntity(String.class);
    }

    private static void GETDataMeasurement() {
        int n = consoleValueRequest("Get last X measurements.\nX=");

        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target(URI).path("data/measurements/" + n);
        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
        Response response = invocationBuilder.get();

        ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(Measurement.class, MixInMeasurement.class);
        ArrayList<Measurement> measurements;
        try {
              measurements = mapper.readValue(response.readEntity(String.class),  new TypeReference<ArrayList<Measurement>>(){});
              measurements.forEach(m -> System.out.println("Value: " + m.getValue() + " Timestamp: " + m.getTimestamp()));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error!!");
        }
    }

    private static void GETStats() {
        int n = consoleValueRequest("Get stats for last X measurements.\nX=");

        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target(URI).path("data/stats/" + n);
        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
        Response response = invocationBuilder.get();

        if (response.getStatus() == 200) {
            ObjectMapper mapper = new ObjectMapper();
            Pair<Double> stats;
            try {
                stats = mapper.readValue(response.readEntity(String.class), new TypeReference<Pair<Double>>(){});
                System.out.println("Average: " + stats.getKey() + " Standard Deviation: " + stats.getValue());
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error!!");
            }
        } else {
            System.out.println("Not enough data");
        }
    }

    private static void GETUpdates() {
        Thread t = new Thread(() -> {
            while (true) {
                Client client = ClientBuilder.newClient();
                WebTarget webTarget = client.target(URI).path("update");
                Invocation.Builder invocationBuilder = webTarget.request();
                Response response = invocationBuilder.get();

                //System.out.println(response.getStatus());
                System.out.println(response.readEntity(String.class));
            }
        });
        t.start();
    }

    private static int consoleValueRequest(String textRequest) {
        InputStreamReader streamReader = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(streamReader);
        int id = 0;
        try {
            System.out.print(textRequest);
            id = Integer.parseInt(bufferedReader.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return id;
    }

}
