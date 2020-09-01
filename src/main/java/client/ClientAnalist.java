package client;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ClientAnalist {

    private static String URI = "http://localhost:8080/sdp_project_red_war_exploded/";

    public static void main(String[] args) throws IOException {
        InputStreamReader streamReader = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(streamReader);

        while (true) {
            System.out.println(
                "1: (GET) Get number of nodes in network.\n" +
                "2: (GET) Get last 'n' measurements.\n" +
                "3: (GET) Get mean and standard deviation .\n"
            );

            try {
                int userInput = Integer.parseInt(bufferedReader.readLine());

                switch (userInput) {
                    case 1:
                        System.out.println(GETNetworkSize());
                        break;
                    case 2:
                        System.out.println(GETDataMeasurement());
                        break;
                    case 3:
                        System.out.println(GETStats());
                        break;
                    default:
                        System.out.println("Wrong input.\n");
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                System.out.println("\nPress any key to continue");
                bufferedReader.readLine();
            }
        }
    }

    private static String GETNetworkSize() {
        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target(URI).path("node/howmany");
        Invocation.Builder invocationBuilder = webTarget.request(MediaType.TEXT_PLAIN);
        Response response = invocationBuilder.get();

        System.out.println(response.getStatus());
        return response.readEntity(String.class);
    }

    private static String GETDataMeasurement() {
        Client client = ClientBuilder.newClient();
        int n = consoleValueRequest("Get last X measurements.\nX=");
        WebTarget webTarget = client.target(URI).path("data/measurements/" + n);
        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
        Response response = invocationBuilder.get();

        System.out.println(response.getStatus());
        return response.readEntity(String.class);
    }

    private static String GETStats() {
        Client client = ClientBuilder.newClient();
        int n = consoleValueRequest("Get stats for last X measurements.\nX=");
        WebTarget webTarget = client.target(URI).path("data/stats/" + n);
        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
        Response response = invocationBuilder.get();

        System.out.println(response.getStatus());
        return response.readEntity(String.class);
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
