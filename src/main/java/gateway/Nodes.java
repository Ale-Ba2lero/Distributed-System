package gateway;

import nodes.Node;

import javax.ws.rs.*;
import java.util.List;

@Path("/nodes")
// The Java class will be hosted at the URI path "/helloworld"
public class Nodes {
    @GET
    @Produces("text/plain")
    public String printNodes() {
        List<Node> nodes = Gateway.getInstance().getNodesList();
        String nodesPrint = new String();
        for (Node n : nodes) {
            nodesPrint += " " + n.getId();
        }
        return "Nodes: " + nodesPrint;
    }

    @POST
    @Consumes("text/plain")
    public void setNewNode(String id) {
        Gateway.getInstance().addNode(new Node("" + id));
    }
}