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
            "/Nordsec16/server_097c.dot",
            "/Nordsec16/server_097e.dot",
            "/Nordsec16/server_098l.dot",
            "/Nordsec16/server_098m.dot",
            "/Nordsec16/server_098s.dot",
            "/Nordsec16/server_098u.dot",
            "/Nordsec16/server_098za.dot",
            "/Nordsec16/server_100.dot",
            "/Nordsec16/server_101.dot",
            "/Nordsec16/server_101k.dot",
            "/Nordsec16/server_102.dot",
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


}
