package org.kevoree.adaptation.operation.util;

/**
 * The list of available action types. Defines the operations priority.
 * Created by mleduc on 16/12/15.
 */
public enum OperationOrder {
    ADD_DEPLOY_UNIT,
    ADD_INSTANCE,
    STOP_INSTANCE,
    UPDATE_INSTANCE,
    REMOVE_INSTANCE,
    START_INSTANCE,
    REMOVE_DEPLOY_UNIT
}
