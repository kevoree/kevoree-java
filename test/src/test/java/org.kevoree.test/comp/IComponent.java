package org.kevoree.test.comp;

import org.kevoree.annotations.Component;
import org.kevoree.annotations.Start;
import org.kevoree.annotations.Stop;
import org.kevoree.annotations.Update;

/**
 *
 * Created by leiko on 12/1/15.
 */
@Component(version = 42)
public interface IComponent {

    @Start void start();

    @Stop void stop();

    @Update void update();
}
