package generic.modelLearning;

public class ModelLearningInfo {
    public long MQCount;
    public long EQCount;
    public int NumState;
    public int NumAlphabet;
    public int distance;
    public String algorithm;
    public int id;
    public long duration;


    public ModelLearningInfo(
            long MQCount,
            long EQCount,
            int numState,
            int numAlphabet,
            int distance,
            String algorithm,
            int id,
            long duration
    ) {
        this.MQCount = MQCount;
        this.EQCount = EQCount;
        this.NumState = numState;
        this.NumAlphabet = numAlphabet;
        this.distance = distance;
        this.algorithm = algorithm;
        this.id = id;
        this.duration = duration;
    }
}