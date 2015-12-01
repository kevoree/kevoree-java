package org.kevoree.test.comp;

import org.kevoree.annotations.params.StringParam;

/**
 *
 * Created by leiko on 12/1/15.
 */
public class SubAbstractComp extends AbstractComponent {

    @StringParam(multiline = true)
    private String param1;

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public void update() {}
}
