package moore.data;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MooreConstants {
    public static final String BASE_BENCHMARK_PATH = "./benchmarks/moore";

    public static final String[] OPEN_SSL_CLIENT = {
            "/Nordsec16/client_097e.dot",
            "/Nordsec16/client_098f.dot",
            "/Nordsec16/client_098j.dot",
            "/Nordsec16/client_098l.dot",
            "/Nordsec16/client_098m.dot",
            "/Nordsec16/client_098za.dot",
            "/Nordsec16/client_100m.dot",
            "/Nordsec16/client_101.dot",
            "/Nordsec16/client_101h.dot",
            "/Nordsec16/client_102.dot",
            "/Nordsec16/client_110-pre1.dot"
    };

    public static final Map<String, String> OPEN_SSL_CLIENT_MAP = Stream.of(new String[][]{
            {"/Nordsec16/client_097e.dot", "/Nordsec16/client_097.dot"},
            {"/Nordsec16/client_098f.dot", "/Nordsec16/client_097e.dot"},
            {"/Nordsec16/client_098j.dot", "/Nordsec16/client_098f.dot"},
            {"/Nordsec16/client_098l.dot", "/Nordsec16/client_098j.dot"},
            {"/Nordsec16/client_098m.dot", "/Nordsec16/client_098l.dot"},
            {"/Nordsec16/client_098za.dot", "/Nordsec16/client_098m.dot"},
            {"/Nordsec16/client_100m.dot", "/Nordsec16/client_098m.dot"},
            {"/Nordsec16/client_101.dot", "/Nordsec16/client_098m.dot"},
            {"/Nordsec16/client_101h.dot", "/Nordsec16/client_101.dot"},
            {"/Nordsec16/client_102.dot", "/Nordsec16/client_101h.dot"},
            {"/Nordsec16/client_110-pre1.dot", "/Nordsec16/client_102.dot"},
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));


    public static final String[] OPEN_SSL_SERVER = {
//            "/Nordsec16/server_097c.dot",
//            "/Nordsec16/server_097e.dot",
//            "/Nordsec16/server_098l.dot",
//            "/Nordsec16/server_098m.dot",
//            "/Nordsec16/server_098s.dot",
//            "/Nordsec16/server_098u.dot",
//            "/Nordsec16/server_098za.dot",
//            "/Nordsec16/server_100.dot",
//            "/Nordsec16/server_101.dot",
//            "/Nordsec16/server_101k.dot",
//            "/Nordsec16/server_102.dot",
            "/Nordsec16/server_110pre1.dot",
    };


    public static final Map<String, String> OPEN_SSL_SERVER_MAP = Stream.of(new String[][]{
            {"/Nordsec16/server_097c.dot", "/Nordsec16/server_097.dot"},
            {"/Nordsec16/server_097e.dot", "/Nordsec16/server_097c.dot"},
            {"/Nordsec16/server_098l.dot", "/Nordsec16/server_097e.dot"},
            {"/Nordsec16/server_098m.dot", "/Nordsec16/server_098l.dot"},
            {"/Nordsec16/server_098s.dot", "/Nordsec16/server_098m.dot"},
            {"/Nordsec16/server_098u.dot", "/Nordsec16/server_098s.dot"},
            {"/Nordsec16/server_098za.dot", "/Nordsec16/server_098u.dot"},
            {"/Nordsec16/server_100.dot", "/Nordsec16/server_098m.dot"},
            {"/Nordsec16/server_101.dot", "/Nordsec16/server_100.dot"},
            {"/Nordsec16/server_101k.dot", "/Nordsec16/server_101.dot"},
            {"/Nordsec16/server_102.dot", "/Nordsec16/server_101k.dot"},
            {"/Nordsec16/server_110pre1.dot", "/Nordsec16/server_102.dot"},
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));


    public static final String[] BRP = {
            "/BenchmarkBRP/BRP_mutant2.flat_0_2.dot",
            "/BenchmarkBRP/BRP_mutant2.flat_0_3.dot",
            "/BenchmarkBRP/BRP_mutant2.flat_0_4.dot",
            "/BenchmarkBRP/BRP_mutant2.flat_0_5.dot",

    };
    public static final Map<String, String> BRP_MAP = Stream.of(new String[][]{
            {"/BenchmarkBRP/BRP_mutant2.flat_0_2.dot", "/BenchmarkBRP/BRP_ref.flat_0_1.dot"},
            {"/BenchmarkBRP/BRP_mutant2.flat_0_3.dot", "/BenchmarkBRP/BRP_ref.flat_0_2.dot"},
            {"/BenchmarkBRP/BRP_mutant2.flat_0_4.dot", "/BenchmarkBRP/BRP_ref.flat_0_3.dot"},
            {"/BenchmarkBRP/BRP_mutant2.flat_0_5.dot", "/BenchmarkBRP/BRP_ref.flat_0_4.dot"},

    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));


    public static final String[] MQTT = {
            "/BenchmarkEdentifier2/learnresult_new_Rand_500_10-15_MC_fix.dot",
    };

    public static final Map<String, String> MQTT_MAPPER = Stream.of(new String[][]{
            {"/BenchmarkEdentifier2/learnresult_new_Rand_500_10-15_MC_fix.dot","/BenchmarkEdentifier2/learnresult_old_500_10-15_fix.dot"},
            {"/BenchmarkMQTT/VerneMQ__two_client_will_retain.dot", "/BenchmarkMQTT/ActiveMQ__two_client_will_retain.dot"},
            {"/BenchmarkMQTT/mosquitto__two_client_will_retain.dot", "/BenchmarkMQTT/VerneMQ__two_client_will_retain.dot"},
            {"/BenchmarkMQTT/hbmqtt__two_client_will_retain.dot", "/BenchmarkMQTT/mosquitto__two_client_will_retain.dot"},
            {"/BenchmarkMQTT/emqtt__two_client_will_retain.dot", "/BenchmarkMQTT/hbmqtt__two_client_will_retain.dot"}
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));


}
