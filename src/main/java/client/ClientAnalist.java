package client;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ClientAnalist {

    private static String URI = "http://localhost:8080/sdp_project_red_war_exploded/";

    public static void main(String args[]) throws IOException {
        boolean run = true;
        InputStreamReader streamReader = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(streamReader);

        while (run) {
            System.out.println(
                "1: (GET) Get sensor data."
            );

            try {
                int userInput = Integer.parseInt(bufferedReader.readLine());
                if (userInput == 1) {
                    System.out.print(GETSensorData());
                } else {
                    System.out.println("Wrong input.\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                System.out.println("\nPress any key to continue");
                bufferedReader.readLine();
            }
        }
    }

    private static String GETSensorData() {
        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target(URI).path("data");
        Invocation.Builder invocationBuilder = webTarget.request(MediaType.TEXT_PLAIN);
        Response response = invocationBuilder.get();

        System.out.println(response.getStatus());
        return response.readEntity(String.class);
    }
}
