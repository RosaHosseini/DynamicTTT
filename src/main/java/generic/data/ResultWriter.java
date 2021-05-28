package generic.data;

import generic.modelLearning.ModelLearningInfo;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;

public class ResultWriter {

    public void toCSV(List<ModelLearningInfo> results, String fileName) throws Exception {

        File csvOutputFile = new File(fileName);
        csvOutputFile.getParentFile().mkdirs();// if file already exists will do nothing
        csvOutputFile.createNewFile();
        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            String CSVHeader = "algorithm,id,num_states,num_alphabet,eq_count,mq_count,distance,duration";
            pw.println(CSVHeader);
            results.stream()
                    .map(this::covertToCSVRow)
                    .forEach(pw::println);
        }
        assert (csvOutputFile.exists());
    }

    private String covertToCSVRow(ModelLearningInfo data) {
        return "" + data.algorithm +
                "," + data.id +
                "," + data.NumState +
                "," + data.NumAlphabet +
                "," + data.EQCount +
                "," + data.MQCount +
                "," + data.distance +
                "," + data.duration;
    }
}
