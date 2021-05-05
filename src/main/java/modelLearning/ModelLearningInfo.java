package modelLearning;

public class ModelLearningInfo {
    public long MQCount;
    public long EQCount;
    public int NumState;
    public int NumAlphabet;
    public int distance;
    public String algorithm;
    public int id;

    public ModelLearningInfo(long MQCount,
                             long EQCount,
                             int numState,
                             int numAlphabet,
                             int distance,
                             String algorithm,
                             int id) {
        this.MQCount = MQCount;
        this.EQCount = EQCount;
        NumState = numState;
        NumAlphabet = numAlphabet;
        this.distance = distance;
        this.algorithm = algorithm;
        this.id = id;
    }
}