package moore.modelLearning;

import generic.modelLearning.ModelLearningInfo;

import java.util.Date;

public class MooreModelLearningInfo extends ModelLearningInfo {
    public String date;
    public MooreModelLearningInfo(
            long MQCount, long EQCount, int numState,
            int numAlphabet, int distance, String algorithm,
            String id, long duration, String date
    ) {
        super(MQCount, EQCount, numState, numAlphabet, distance, algorithm, id, duration);
        this.date = date;
    }
}
