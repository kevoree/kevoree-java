package org.kevoree.core.api;

import org.kevoree.Model;
import org.kevoree.Node;

public interface Core {

    Model model();

    Node node();

    Object instance(String name);

    String[] instanceNames();

    void remove(String name);

    void add(String name);

}
