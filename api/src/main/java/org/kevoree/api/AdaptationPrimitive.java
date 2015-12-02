package org.kevoree.api;

import org.kevoree.Instance;
import org.kevoree.Model;

public interface AdaptationPrimitive {

    void run(Model model, Instance instance);
}
