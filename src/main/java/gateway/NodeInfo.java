package gateway;

import javax.xml.bind.annotation.*;

@XmlRootElement
public class NodeInfo {
    private int id;
    private String ip;
    private int port;

    public NodeInfo(int id, String ip, int port) {
        this.id = id;
        this.ip = ip;
        this.port = port;
    }

    public NodeInfo(){
    }

    public int getId() {
        return id;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "NodeInfo{" +
                "id=" + id +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                '}';
    }
}
