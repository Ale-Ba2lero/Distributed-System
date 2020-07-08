package gateway;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.util.List;

@Path("/nodes")
// The Java class will be hosted at the URI path "/helloworld"
public class Nodes {
    @GET
    @Produces("text/plain")
    public String printNodes() {
        List<NodeInfo> nodeInfos = Gateway.getInstance().getNodesList();
        String nodesPrint = new String();
        for (NodeInfo n : nodeInfos) {
            nodesPrint += n.toString() + "\n";
        }
        return "Nodes: " + nodesPrint;
    }

    @GET
    @Produces("text/plain")
    @Path("/helloworld")
    public String helloWorld() {
        return "Hello, world!";
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void setNewNode(NodeInfo nodeData) {
        Gateway.getInstance().addNode(nodeData);
    }
}