package moore.data;

public class MooreConstants {
    public static final String BASE_BENCHMARK_PATH = "./benchmarks/moore";

    public static final String[] TCP_CLIENT = {
            "/TCP/TCP_FreeBSD_Client.dot",
            "/TCP/TCP_Linux_Client.dot",
            "/TCP/TCP_Windows8_Client.dot",
    };

    public static final String[] TCP_SERVER = {
            "/TCP/TCP_FreeBSD_Server.dot",
            "/TCP/TCP_Linux_Server.dot",
            "/TCP/TCP_Windows8_Server.dot",
    };

    public static final String[] MQTT_INVALID = {
            "/MQTT/hbmqtt/invalid.dot",
            "/MQTT/emqtt/invalid.dot",
            "/MQTT/mosquitto/invalid.dot",
            "/MQTT/VerneMQ/invalid.dot",
            "/MQTT/ActiveMQ/invalid.dot",
    };


    public static final String[] MQTT_NONE_CLEAN = {
            "/MQTT/hbmqtt/none_clean.dot",
            "/MQTT/emqtt/none_clean.dot",
            "/MQTT/mosquitto/none_clean.dot",
            "/MQTT/VerneMQ/none_clean.dot",
            "/MQTT/ActiveMQ/none_clean.dot",
    };

    public static final String[] MQTT_SIMPLE = {
            "/MQTT/hbmqtt/simple.dot",
            "/MQTT/emqtt/simple.dot",
            "/MQTT/mosquitto/simple.dot",
            "/MQTT/VerneMQ/simple.dot",
            "/MQTT/ActiveMQ/simple.dot",
    };


    public static final String[] MQTT_SINGLE_CLIENT = {
            "/MQTT/hbmqtt/single_client.dot",
            "/MQTT/emqtt/single_client.dot",
            "/MQTT/mosquitto/single_client.dot",
            "/MQTT/VerneMQ/single_client.dot",
            "/MQTT/ActiveMQ/single_client.dot"
    };


    public static final String[] MQTT_TWO_CLIENT_WILL_RETAIN = {
            "/MQTT/hbmqtt/two_client_will_retain.dot",
            "/MQTT/emqtt/two_client_will_retain.dot",
            "/MQTT/mosquitto/two_client_will_retain.dot",
            "/MQTT/VerneMQ/two_client_will_retain.dot",
            "/MQTT/ActiveMQ/two_client_will_retain.dot"
    };
}
