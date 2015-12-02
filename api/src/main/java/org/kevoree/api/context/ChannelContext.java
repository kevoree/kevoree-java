package org.kevoree.api.context;

import org.kevoree.Channel;

/**
 *
 * Created by leiko on 12/2/15.
 */
public interface ChannelContext extends Context {

    String getName();

    Channel getInstance();
}
