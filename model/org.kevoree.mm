class org.kevoree.Model {
    rel nodes: org.kevoree.Node
    rel types: org.kevoree.TypeDefinition
}

class org.kevoree.Instance {
    rel instances: org.kevoree.Instance
    att name: String
    att type: String
    rel metrics: org.kevoree.Metric
}

class org.kevoree.Node extends org.kevoree.Instance {
}

class org.kevoree.Metric {
    att name: String
    att value: Continuous
}

class org.kevoree.TypeDefinition {
    att name: String
    att kind: String
    rel dependencies: org.kevoree.DeployUnit
}

class org.kevoree.DeployUnit {
    att uri: String
    att kind: String
    att code: String
    rel dependencies: org.kevoree.DeployUnit
}
