package gateway;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/nodes")
// The Java class will be hosted at the URI path "/helloworld"
public class Nodes {
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
    public Response setNewNode(NodeInfo nodeData) {
        NodeHandler.getInstance().addNode(nodeData);
        return Response.ok(nodeData.toString()).build();
    }
}