package fake.FakeComp;

import org.kevoree.annotations.Component;
import org.kevoree.annotations.Start;
import org.kevoree.annotations.Stop;
import org.kevoree.annotations.Update;
import org.kevoree.annotations.params.Max;
import org.kevoree.annotations.params.Min;
import org.kevoree.annotations.params.Param;
import org.kevoree.annotations.params.Required;

/**
 *
 * Created by leiko on 12/9/15.
 */
@Component(version = 42)
public class FakeComp {

    @Param
    @Required
    private String host;

    @Param
    @Required
    @Min(1)
    @Max(65535)
    private int port;

    @Start
    public void start() {

    }

    @Stop
    public void stop() {

    }

    @Update
    public void update() {

    }
}