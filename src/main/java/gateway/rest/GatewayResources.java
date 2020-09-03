package gateway.rest;

import client.Pair;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gateway.singleton.DataHandler;
import jBeans.NodeInfo;
import gateway.singleton.NodeHandler;
import nodes.sensor.Measurement;
import nodes.sensor.MixInMeasurement;

import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Path("/resources")
public class GatewayResources {
    private static final BlockingQueue<AsyncResponse> suspended = new LinkedBlockingQueue<>();

    @GET
    @Path("update")
    public void readMessage(@Suspended AsyncResponse ar) throws InterruptedException {
        suspended.put(ar);
    }

    @GET
    @Produces("text/plain")
    @Path("/node")
    public String getNodesList() {
        List<NodeInfo> nodeInfos = NodeHandler.getInstance().getNodesList();
        ObjectMapper mapper = new ObjectMapper();

        try {
            return mapper.writeValueAsString(nodeInfos);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "Error";
        }
    }

    @GET
    @Produces("text/plain")
    @Path("node/howmany")
    public String getNetworkSize() {
        return NodeHandler.getInstance().getNodesSize() + "";
    }

    @POST
    @Path("/node")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setNewNode(NodeInfo node) {
        NodeHandler instance = NodeHandler.getInstance();
        LinkedList<NodeInfo> nodeList = instance.getNodesList();
        boolean isPresent = nodeList.stream().map(NodeInfo::getId).anyMatch(n -> n == node.getId());

        if (!isPresent) {
            instance.addNode(node);

            suspended.forEach(ar -> {
                ar.resume("Node added\nId:" + node.getId() + "\nIp:" + node.getIp() + "\nPort:" + node.getPort());
            });

            return Response.ok(instance.getNodesList()).build();
        } else {
            return Response.status(Response.Status.CONFLICT).entity("Node id already in use").build();
        }
    }

    @DELETE
    @Path("node/{id}")
    public Response deleteNode(@PathParam("id") int nodeId) {

        NodeHandler instance = NodeHandler.getInstance();
        LinkedList<NodeInfo> nodesList = instance.getNodesList();

        for (NodeInfo nodeInfo : nodesList) {
            if (nodeInfo.getId() == nodeId) {
                if (instance.deleteNode(nodeInfo)) {
                    suspended.forEach(ar -> {
                        ar.resume("Node removed"
                                + "\nId:" + nodeInfo.getId()
                                + "\nIp:" + nodeInfo.getIp()
                                + "\nPort:" + nodeInfo.getPort());
                    });
                    return Response.ok().entity("Node correctly removed from the list").build();
                } else {
                    return Response.status(Response.Status.NOT_FOUND).entity("Node id not present").build();
                }
            }
        }
        return Response.status(Response.Status.NOT_FOUND).entity("Node id not present").build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/data")
    public Response addMeasurement(String jsonMeasurementString) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(Measurement.class, MixInMeasurement.class);

        try {
            Measurement m = mapper.readValue(jsonMeasurementString, Measurement.class);
            DataHandler instance = DataHandler.getInstance();
            instance.addData(m);

            suspended.forEach(ar -> {
                ar.resume("New Measurement: " + m.getValue() + " " + m.getTimestamp());
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Response.ok().build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("data/measurements/{n}")
    public String getLastMeasurement(@PathParam("n") int n) {
        ArrayList<Measurement> m = DataHandler.getInstance().getLastNMeasurements(n);
        ObjectMapper mapper = new ObjectMapper();
        String measurementJsonString;
        try {
            measurementJsonString = mapper.writeValueAsString(m);
        } catch (JsonProcessingException e) {
            measurementJsonString = null;
            e.printStackTrace();
        }

        return  measurementJsonString;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("data/stats/{n}")
    public String getStats(@PathParam("n") int n) {
        ObjectMapper mapper = new ObjectMapper();
        Pair<Double> stats =  DataHandler.getInstance().getStats(n);
        String statsJsonString;
        try {
            statsJsonString = mapper.writeValueAsString(stats);
        } catch (JsonProcessingException e) {
            statsJsonString = "Error";
            e.printStackTrace();
        }

        return statsJsonString;
    }
}