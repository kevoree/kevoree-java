package org.kevoree.api.service;

import org.kevoree.Model;
import org.kevoree.api.callback.DeployCallback;

/**
 *
 * Created by leiko on 12/2/15.
 */
public interface ModelService {

    Model getModel();

    void deploy(Model model, DeployCallback callback);
}
