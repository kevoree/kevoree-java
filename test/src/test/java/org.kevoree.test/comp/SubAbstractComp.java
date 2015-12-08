package org.kevoree.test.comp;

import org.kevoree.annotations.params.Multiline;
import org.kevoree.annotations.params.Param;

/**
 *
 * Created by leiko on 12/1/15.
 */
public class SubAbstractComp extends AbstractComponent {

    @Param
    @Multiline
    private String param1;

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public void update() {}
}
