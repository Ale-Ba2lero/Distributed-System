package gateway.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import jBeans.NodeInfo;
import gateway.singleton.NodeHandler;
import nodes.sensor.Measurement;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedList;
import java.util.List;

@Path("/node")
// The Java class will be hosted at the URI path "/helloworld"
public class NodeResource {

    @GET
    @Produces("text/plain")
    public String printNodes() {
        List<NodeInfo> nodeInfos = NodeHandler.getInstance().getNodesList();
        ObjectMapper mapper = new ObjectMapper();

        try {
            String json = mapper.writeValueAsString(nodeInfos);
            return json;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return "Error";
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setNewNode(NodeInfo node) {
        NodeHandler instance = NodeHandler.getInstance();
        //Get a shallow copy of the list to be able to freely iterate on that
        LinkedList<NodeInfo> nodeList = instance.getNodesList();
        boolean isPresent = nodeList.stream().map(NodeInfo::getId).anyMatch(n -> n == node.getId());

        /*
        for (NodeInfo n : nodeList) {
            if (node.getId() == n.getId()) {
                isPresent = true;
            }
        }

        /*
        measurements
                .stream()
                .map(Measurement::getId)
                .anyMatch(v -> Integer.parseInt(v) == node.getId());
        */

        //If the id is not already taken add the node to the list (sync) and return the new list (sync) to the node
        if (!isPresent) {
            instance.addNode(node);
            return Response.ok(instance.getNodesList()).build();
        } else {
            return Response.status(Response.Status.CONFLICT).entity("Node id already in use").build();
        }
    }

    @DELETE
    @Path("{id}")
    public Response deleteNode(@PathParam("id") int nodeId) {

        NodeHandler instance = NodeHandler.getInstance();
        LinkedList<NodeInfo> nodesList = instance.getNodesList();

        for (NodeInfo nodeInfo : nodesList) {
            if (nodeInfo.getId() == nodeId) {
                if (instance.deleteNode(nodeInfo)) {
                    return Response.ok().entity("Node correctly removed from the list").build();
                } else {
                    return Response.status(Response.Status.NOT_FOUND).entity("Node id not present").build();
                }
            }
        }
        return Response.status(Response.Status.NOT_FOUND).entity("Node id not present").build();
    }

}