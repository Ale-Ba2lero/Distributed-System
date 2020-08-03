package gateway.rest;

import jBeans.NodeInfo;
import gateway.singleton.NodeHandler;

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
        String nodesPrint = new String();
        for (NodeInfo n : nodeInfos) {
            nodesPrint += n.toString() + "\n";
        }
        return "Nodes: " + nodesPrint;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setNewNode(NodeInfo node) {
        NodeHandler instance = NodeHandler.getInstance();
        //Get a shallow copy of the list to be able to freely iterate on that
        LinkedList<NodeInfo> nodeList = instance.getNodesList();
        boolean isPresent = false;

        for (NodeInfo n : nodeList) {
            if (node.getId() == n.getId()) {
                isPresent = true;
            }
        }

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