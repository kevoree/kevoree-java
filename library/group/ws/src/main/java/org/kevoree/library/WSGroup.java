package org.kevoree.library;

import org.kevoree.annotations.Group;
import org.kevoree.annotations.params.Fragment;
import org.kevoree.annotations.params.Param;

/**
 *
 */
@Group(description = "TODO", version = 6)
public class WSGroup {



    @Param
    @Fragment
    private int port = 9000;
}
