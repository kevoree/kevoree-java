package org.kevoree.library.adaptation;


import org.kevoree.Instance;
import org.kevoree.Model;
import org.kevoree.api.AdaptationPrimitive;

public class AddInstance implements AdaptationPrimitive {
    @Override
    public void run(Model model, Instance instance) {

        System.out.println("AddInstance "+instance.getName());
    }
}
