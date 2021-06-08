package generic.modelLearning;


public abstract class ModelLearner<I, O, A> {
    protected final Teacher<I, O, A> teacher;

    public ModelLearner(Teacher<I, O, A> teacher) {
        this.teacher = teacher;
    }

    abstract public A learn() throws Exception;
}
