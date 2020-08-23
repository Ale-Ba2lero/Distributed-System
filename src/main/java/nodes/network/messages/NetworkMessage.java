package nodes.network.messages;

public class NetworkMessage {
    private MessageType type;

    public NetworkMessage(MessageType type) {
        this.type = type;
    }

    public MessageType getType() {
        return type;
    }
}