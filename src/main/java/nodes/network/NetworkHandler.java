package nodes.network;

import jBeans.NodeInfo;
import nodes.network.messages.GreetingMessage;
import nodes.network.messages.MessageType;
import nodes.network.messages.NetworkMessage;
import nodes.network.messages.Token;

import java.util.*;

public class NetworkHandler implements Runnable{
    private final NodeInfo node;
    private volatile NodeState nodeState;

    private final LinkedList<NodeInfo> nodes;
    private Transmitter transmitter;
    private Receiver receiver;

    private final Object networkReferenceLock;
    private NodeInfo next;

    private final Object tokenLock;
    private Token token;

    private final ArrayList<NodeInfo> greetingNodes;

    public NetworkHandler(NodeInfo nodeInfo) {
        nodeState = NodeState.STARTING;
        node = nodeInfo;
        nodes = new LinkedList<>();
        greetingNodes = new ArrayList<>();

        networkReferenceLock = new Object();
        tokenLock = new Object();
    }

    // If this is not the only node, start a token transmitter thread (gRPC client)
    public void init(LinkedList<NodeInfo> nodeList, Transmitter transmitter, Receiver receiver) {
        this.nodes.addAll(nodeList);

        this.receiver = receiver;
        this.transmitter = transmitter;

        if (nodes.size() > 1) {
            next = getNextNodeInNetwork();
            transmitter.greeting();
            nodeState = NodeState.RUNNING;
        } else {
            next = null;
            token = new Token(
                MessageType.TOKEN ,
                new LinkedList<>(),
                new LinkedList<>(),
                node,0);
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                synchronized (this) {
                    this.wait();
                }

                ArrayList<NetworkMessage> messageQueue = receiver.getMessagesQueue();
                messageQueue.forEach(msg -> {
                    switch (msg.getType()) {
                        case GREETING:
                            insertGreetingNodeToNetwork((GreetingMessage) msg);
                            break;
                        case EXIT:
                            break;
                    }
                });

                ArrayList<NetworkMessage> tokenQueue = receiver.getTokenQueue();
                if (tokenQueue.size() > 0) {
                    computeToken((Token) tokenQueue.get(tokenQueue.size() - 1));
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void computeToken(Token receivedToken) {
        //TODO handle token measurements

        ArrayList<NodeInfo> tokenNodesToAdd = new ArrayList<>();
        ArrayList<NodeInfo> tokenNodesToRemove = new ArrayList<>();

        //If there are nodes to add, make a call to the network handler to add the new nodes
        if (receivedToken.getToAdd().size() > 0) {
            receivedToken.getToAdd().forEach(nodeInfo -> {
                // If the node to add its the same that is updating its list that means that the token made
                // a full loop. The node addition is complete.
                if (nodeInfo.getId() != node.getId()) {
                    sortedAdd(nodes, nodeInfo);
                    System.out.println(nodes);
                    tokenNodesToAdd.add(nodeInfo);
                }
            });
            //update gRPC references
            updateNetworkReference();
        }

        tokenNodesToAdd.addAll(new ArrayList<>(greetingNodes));

        greetingNodes.clear();

        token = new Token(MessageType.TOKEN, tokenNodesToAdd, tokenNodesToRemove, node, receivedToken.getLoop() + 1);

        tokenReady();
    }

    // The entry token has been handled and is ready to be sent to the next node
    private void tokenReady() {
        synchronized (transmitter) {
            transmitter.notify();
        }
    }

    // Updates the node reference inside the network. This means set the new target node that will receive the token.
    private void updateNetworkReference() {
        synchronized (networkReferenceLock) {
            NodeInfo newNext = getNextNodeInNetwork();
            if (next != null && newNext.getId() != next.getId()) {
                next = newNext;
            } else if (next == null) {
                // If next is null this means this was the first node entering the network and now is updating its references
                // due to an other node greeting to it. Now the network contains two nodes and the token loop can start.
                next = newNext;
                nodeState = NodeState.RUNNING;
                tokenReady();
            }
        }
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

    private void insertGreetingNodeToNetwork(GreetingMessage message) {
        NodeInfo nodeInfo = message.getNodeInfo();
        sortedAdd(nodes, nodeInfo);
        System.out.println(nodes);
        greetingNodes.add(nodeInfo);
        updateNetworkReference();
    }

    public NodeInfo getTarget() {
        synchronized (networkReferenceLock) {
            return new NodeInfo(next.getId(), next.getIp(), next.getPort());
        }
    }

    public Token getToken() {
        synchronized (tokenLock) {
            return token;
        }
    }

    public NodeInfo getNode() {
        return node;
    }

    public NodeState getNodeState() {
        synchronized (nodeState) {
            return nodeState;
        }
    }

    public enum NodeState {
        STARTING,
        RUNNING,
        QUITTING
    }
 }


