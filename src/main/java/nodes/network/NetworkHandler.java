package nodes.network;

import com.networking.node.NetworkServiceOuterClass.*;
import jBeans.NodeInfo;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;

public class NetworkHandler {
    private Receiver receiver;
    private Transmitter transmitter;

    private NodeInfo node;
    private NodeInfo next;
    private static LinkedList<NodeInfo> nodes;

    public NetworkHandler(NodeInfo nodeInfo, LinkedList<NodeInfo> nodes) throws IOException, InterruptedException {
        this.node = nodeInfo;
        next = null;

        // Start the token receiver thread (gRPC server)
        receiver = new Receiver(this, nodeInfo);
        receiver.start();

        // Istantiate the token transmitter
        transmitter = new Transmitter();

        // If this is not the only node, start a token transmitter thread (gRPC client)
        if (nodes.size() > 1) {
            next = getNextNodeInNetwork(nodes, node);
            transmitter.init(next);
            transmitter.run();
        }
    }

    public void receiveToken(Token token) {
        //TODO handle token measurements

        //Get information about network changes from the token
        LinkedList<ProtoNodeInfo> toAdd = (LinkedList<ProtoNodeInfo>) token.getToAddList();
        LinkedList<ProtoNodeInfo> toRemove = (LinkedList<ProtoNodeInfo>) token.getToRemoveList();

        //If there are nodes to add, make a call to the network handler to add the new nodes
        if (toAdd.size() > 0) {
            addNodesToList(toAdd);
        }

        //TODO send the token
        tokenReady();

        //If there are nodes to remove, make a call to the network handler to remove the new nodes
        if (toRemove.size() > 0) {
            removeNodesFromList(toRemove);
        }
    }

    public void tokenReady() {
        transmitter.notify();
    }

    //Convert the protoNodeInfo obj into NodeInfo obj and add them to the list
    public synchronized void addNodesToList(LinkedList<ProtoNodeInfo> toAdd) {
        toAdd.forEach((pni -> {
            nodes.add(new NodeInfo(pni.getId(), pni.getIp(), pni.getPort()));
        }));
        Collections.sort(nodes);

        //TODO update gRPC references
        updateNetworkReference();
    }

    //Convert the protoNodeInfo obj into NodeInfo obj and remove them from the list
    public synchronized void removeNodesFromList(LinkedList<ProtoNodeInfo> toRemove) {
        toRemove.forEach((pni -> {
            nodes.add(new NodeInfo(pni.getId(), pni.getIp(), pni.getPort()));
        }));
        updateNetworkReference();
    }

    private void updateNetworkReference() {
        //TODO update gRPC references
        NodeInfo newNext = getNextNodeInNetwork(nodes, this.node);
        if (next != null && newNext.getId() != next.getId()) {
            next = newNext;
            transmitter.init(next);
        }
    }

    private static NodeInfo getNextNodeInNetwork(LinkedList<NodeInfo> nodes, NodeInfo node) {
        return nodes.get((getIndex(nodes, node) + 1) % nodes.size());
    }

    private static NodeInfo getPreviousNodeInNetwork(LinkedList<NodeInfo> nodes, NodeInfo node) {
        return nodes.get((getIndex(nodes, node) - 1) % nodes.size());
    }

    private static int getIndex(LinkedList<NodeInfo> nodes, NodeInfo node) {
        int i;
        for (i = 0; i < nodes.size(); i++) {
            if (nodes.get(i).getId() == node.getId()) {
                return i;
            }
        }
        return -1;
    }
}
