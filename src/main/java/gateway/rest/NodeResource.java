package gateway.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jBeans.NodeInfo;
import gateway.singleton.NodeHandler;

import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Path("/node")
public class NodeResource {
    private static final BlockingQueue<AsyncResponse> suspended = new LinkedBlockingQueue<>();

    @GET
    @Produces("text/plain")
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
    @Path("howmany")
    public String getNetworkSize() {
        return NodeHandler.getInstance().getNodesSize() + "";
    }

    @GET
    @Path("update")
    public void readMessage(@Suspended AsyncResponse ar) throws InterruptedException {
        suspended.put(ar);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setNewNode(NodeInfo node) {
        NodeHandler instance = NodeHandler.getInstance();
        LinkedList<NodeInfo> nodeList = instance.getNodesList();
        boolean isPresent = nodeList.stream().map(NodeInfo::getId).anyMatch(n -> n == node.getId());

        if (!isPresent) {
            instance.addNode(node);

            suspended.forEach(ar -> {
                ar.resume("Node added");
            });

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