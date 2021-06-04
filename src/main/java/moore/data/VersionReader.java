package moore.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class VersionReader {
    public HashMap<String, String> readVersions(String fileName) {
        HashMap<String, String> map = new HashMap<>();
        String line = "";
        String splitBy = ",";

        try {
            //parsing a CSV file into BufferedReader class constructor
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            br.readLine();
            while ((line = br.readLine()) != null)   //returns a Boolean value
            {
                String[] info = line.split(splitBy);    // use comma as separator
                if (info.length >= 2)
                    map.put(info[1], info[0]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    public static void main(String[] args) {
        HashMap<String, String> x = new VersionReader().readVersions("./benchmarks/moore/Nordsec16/client_version_info.csv"
        );
        System.out.println(x);
    }
}