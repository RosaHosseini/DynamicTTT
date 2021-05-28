package generic.data;

import java.io.File;

public interface SULReader<A> {
    public A parseModelFromDot(File file) throws Exception;
}
