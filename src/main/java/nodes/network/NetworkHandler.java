package nodes.network;

import jBeans.NodeInfo;
import nodes.network.messages.GreetingMessage;
import nodes.network.messages.MessageType;
import nodes.network.messages.NetworkMessage;
import nodes.network.messages.Token;

import java.io.IOException;
import java.util.*;

public class NetworkHandler implements Runnable{
    private final NodeInfo node;

    private final LinkedList<NodeInfo> nodes;
    private final Transmitter transmitter;
    private final Receiver receiver;

    private final Object networkReferenceLock;
    private NodeInfo next;

    private final Object tokenLock;
    private Token token;

    private final ArrayList<NodeInfo> greetingNodes;

    public NetworkHandler(NodeInfo nodeInfo) throws IOException {
        node = nodeInfo;
        nodes = new LinkedList<>();
        greetingNodes = new ArrayList<>();

        networkReferenceLock = new Object();
        tokenLock = new Object();

        transmitter = new Transmitter(this, node);
        receiver = new Receiver(this, node);
    }

    // If this is not the only node, start a token transmitter thread (gRPC client)
    public void init(LinkedList<NodeInfo> nodeList) {
        this.nodes.addAll(nodeList);

        Thread transmitterThread = new Thread(transmitter);

        if (nodes.size() > 1) {
            next = getNextNodeInNetwork();
            transmitter.greeting();
        } else {
            next = null;
            token = new Token(
                MessageType.TOKEN ,
                new LinkedList<>(),
                new LinkedList<>(),
                node);
        }

        transmitterThread.start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                synchronized (this) {
                    this.wait();
                }

                ArrayList<NetworkMessage> queue = receiver.getMessagesQueue();
                queue.forEach(msg -> {
                    switch (msg.getType()) {
                        case TOKEN:
                            computeToken((Token) msg);
                            break;
                        case GREETING:
                            insertGreetingNodeToNetwork((GreetingMessage) msg);
                            break;
                        case EXIT:
                            break;
                    }
                });

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void computeToken(Token receivedToken) {
        //TODO handle token measurements

        ArrayList<NodeInfo> tokenNodesToAdd = new ArrayList<>();
        ArrayList<NodeInfo> tokenNodesToRemove = new ArrayList<>();

        //If there are nodes to add, make a call to the network handler to add the new nodes
        if (receivedToken.getToAdd().size() > 0) {
            receivedToken.getToAdd().forEach(nodeInfo -> {
                // If the node to add its the same that is updating its list that means that the token made
                // a full loop. The node addition is complete.
                if (nodeInfo.getId() != node.getId()) {
                    System.out.println("Adding " + nodeInfo.getId());
                    sortedAdd(nodes, nodeInfo);
                    tokenNodesToAdd.add(nodeInfo);
                }
            });
            //update gRPC references
            updateNetworkReference();
        }

        tokenNodesToAdd.addAll(new ArrayList<>(greetingNodes));

        greetingNodes.clear();

        token = new Token(MessageType.TOKEN, tokenNodesToAdd, tokenNodesToRemove, node);

        tokenReady();

        //If there are nodes to remove, make a call to the network handler to remove the new nodes
        /*
        if (toRemove.size() > 0) {
            removeNodesFromList(toRemove);
        }*/
    }

    // The entry token has been handled and is ready to be sent to the next node
    private void tokenReady() {
        synchronized (transmitter) {
            transmitter.notify();
        }
    }

    // Updates the node reference inside the network. This means set the new target node that will receive the token.
    private void updateNetworkReference() {
        NodeInfo newNext = getNextNodeInNetwork();
        synchronized (networkReferenceLock) {
            if (next != null && newNext.getId() != next.getId()) {
                next = newNext;
            } else if (next == null) {
                // If next is null this means this was the first node entering the network and now is updating its references
                // due to an other node greeting to it. Now the network contains two nodes and the token loop can start.
                next = newNext;
                tokenReady();
            }
        }
    }

    private NodeInfo getNextNodeInNetwork() {
        synchronized (nodes) {
            NodeInfo next = nodes.get((getIndex(nodes, node) + 1) % nodes.size());
            return new NodeInfo(next.getId(),next.getIp(), next.getPort());
        }
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

    public synchronized void insertGreetingNodeToNetwork(GreetingMessage message) {
        NodeInfo nodeInfo = message.getNodeInfo();
        sortedAdd(nodes, nodeInfo);
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
 }
