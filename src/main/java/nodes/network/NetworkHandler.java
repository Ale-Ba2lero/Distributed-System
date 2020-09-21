package nodes.network;

import jBeans.NodeInfo;
import nodes.MeasurementsBuffer;
import nodes.ServerHandler;
import nodes.sensor.Measurement;

import javax.ws.rs.core.Response;
import java.util.*;

public class NetworkHandler implements Runnable{
    private final NodeInfo node;
    private volatile NodeState nodeState;

    private final LinkedList<NodeInfo> nodes;
    private Transmitter transmitter;
    private Receiver receiver;
    private MeasurementsBuffer buffer;

    private NodeInfo target;

    private Token token;

    private final ArrayList<NodeInfo> greetingNodes;

    public NetworkHandler(NodeInfo nodeInfo) {
        nodeState = NodeState.STARTING;
        node = nodeInfo;
        nodes = new LinkedList<>();
        greetingNodes = new ArrayList<>();
    }

    // If this is not the only node, start a token transmitter thread (gRPC client)
    public void init(LinkedList<NodeInfo> nodeList, Transmitter transmitter, Receiver receiver, MeasurementsBuffer buffer) {
        this.nodes.addAll(nodeList);

        this.receiver = receiver;
        this.transmitter = transmitter;

        this.buffer = buffer;

        if (nodes.size() > 1) {
            target = getNextNodeInNetwork();
            transmitter.greeting(node);
            nodeState = NodeState.RUNNING;
        } else {
            target = null;
            token = null;
        }
    }

    @Override
    public void run() {
        while (nodeState != NodeState.DONE) {
            try {
                synchronized (this) {
                    this.wait();
                }

                ArrayList<NodeInfo> greetingsQueue = receiver.getGreetingsQueue();
                greetingsQueue.forEach(nodeInfo -> {
                    sortedAdd(nodes, nodeInfo);
                    greetingNodes.add(nodeInfo);
                    updateNetworkTarget();
                });

                Token receivedToken = receiver.getToken();
                if (receivedToken != null) {
                    computeToken(receivedToken);
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void computeToken(Token receivedToken) {
        ArrayList<Measurement> measurements = receivedToken.getMeasurements();

        // Check whether the measurement of this node is already present in the token...
        boolean isPresent =
                measurements
                        .stream()
                        .map(Measurement::getId)
                        .anyMatch(v -> Integer.parseInt(v) == node.getId());

        // ... if there are no measurements associated with the node id,
        // and if a new measurement is ready, add the measurement to the token.
        if (!isPresent) {
            Measurement m = buffer.pop();
            if (m != null) {
                measurements.add(m);
            }
        }

        // If all nodes inserted the measurement into tho token,
        // calculate the average and send the value to the gateway
        if (measurements.size() == nodes.size()) {
            Measurement m = new Measurement(
                node.getId() + "",
                measurements.get(measurements.size() - 1).getType(),
                measurements.stream().mapToDouble(Measurement::getValue).average().getAsDouble(),
                measurements.get(measurements.size() - 1).getTimestamp());

            System.out.println("Send to geateway: " + m );

            Response response = ServerHandler.POSTMeasurement(m);
            //System.out.println(response.getStatus());
            //System.out.println(response.readEntity(String.class));

            measurements = new ArrayList<>();
        }

        ArrayList<NodeInfo> tokenNodesToAdd = new ArrayList<>();
        ArrayList<NodeInfo> tokenNodesToRemove = new ArrayList<>();

        // Add the nodes to the network list and retransmit them to the next node
        if (receivedToken.getToAdd().size() > 0) {
            receivedToken.getToAdd().forEach(nodeInfo -> {
                // If the node to add is the same that is the current one that means that the token made
                // a full loop.
                // The addition of the node to the network is complete.
                if (nodeInfo.getId() != node.getId()) {
                    sortedAdd(nodes, nodeInfo);
                    tokenNodesToAdd.add(nodeInfo);
                }
            });

            //update gRPC references
            updateNetworkTarget();
        }

        // The target node is the one that is currently the successor in the network
        NodeInfo targetNode = target;

        // Remove the nodes to the network list and retransmit them to the next node
        if (receivedToken.getToRemove().size() > 0) {
            receivedToken.getToRemove().forEach(nodeInfo -> {
                if (nodeInfo.getId() != node.getId()) {
                    for (int i = 0; i < nodes.size(); i++) {
                        if (nodes.get(i).getId() == nodeInfo.getId()) {
                            nodes.remove(nodes.get(i));
                            i = nodes.size();
                        }
                    }
                    tokenNodesToRemove.add(nodeInfo);
                } else {
                    // If the node to remove is the current one that means that the token made
                    // a full loop through the network.
                    // The removal of the node from the network is complete.
                    quit();
                }
            });

            //update gRPC references
            updateNetworkTarget();
        }

        tokenNodesToAdd.addAll(greetingNodes);
        greetingNodes.clear();
        if (nodeState == NodeState.QUITTING) tokenNodesToRemove.add(node);

        if (targetNode.getId() == node.getId()) {
            System.out.println("Last node");
            target = null;
            token = null;
            nodeState = NodeState.STARTING;
        } else {
            token = new Token(
                tokenNodesToAdd,
                tokenNodesToRemove,
                node,
                targetNode,
                measurements);

            tokenReady();
        }
    }

    // The entry token has been handled and is ready to be sent to the next node
    private void tokenReady() {
        synchronized (transmitter) {
            transmitter.notify();
        }
    }

    // Updates the node reference inside the network. This means set the new target node that will receive the token.
    private void updateNetworkTarget() {
        NodeInfo newNext = getNextNodeInNetwork();
        if (target != null && newNext.getId() != target.getId()) {
            target = newNext;
        } else if (target == null) {
            // If next is null this means this was the first node entering the network and now is updating its references
            // due to an other node greeting to it. Now the network contains two nodes and the token loop can start.
            target = newNext;
            nodeState = NodeState.RUNNING;
            token = new Token(new ArrayList<>(),
                    new ArrayList<>(),
                    node,
                    target,
                    new ArrayList<>());
            tokenReady();
        }

        System.out.println(nodes);
    }

    private NodeInfo getNextNodeInNetwork() {
        NodeInfo next = nodes.get((getIndex(nodes, node) + 1) % nodes.size());
        return new NodeInfo(next.getId(),next.getIp(), next.getPort());
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

    private void sortedAdd(List<NodeInfo> list, NodeInfo element) {
        int index = Collections.binarySearch(list, element, Comparator.comparing(NodeInfo::getId));
        if (index < 0) {
            index = -index - 1;
        }
        list.add(index, element);
    }

    private void quit() {
        nodeState = NodeState.DONE;
        System.out.println("Node removed");
        transmitter.quit();
    }

    public NodeInfo getTarget() {
        return new NodeInfo(target.getId(), target.getIp(), target.getPort());
    }

    public Token getToken() {
        return token;
    }

    public NodeInfo getNode() {
        return node;
    }

    public NodeState getNodeState() {
        return nodeState;
    }

    public void removeNodeFromNetwork() {
        nodeState = NodeState.QUITTING;
    }

    public enum NodeState {
        STARTING,
        RUNNING,
        QUITTING,
        DONE
    }
 }


