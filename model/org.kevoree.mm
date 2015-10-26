class org.kevoree.Model {
    rel nodes: org.kevoree.Node
}

class org.kevoree.Instance {
    rel instances: org.kevoree.Instance
    att name: String
    att type: String
}

class org.kevoree.Node extends org.kevoree.Instance {
    att avg_load: Continuous
}

class org.kevoree.DeployUnit {
    att url: String
    rel types: org.kevoree.TypeDefinition
}

class org.kevoree.TypeDefinition {
    att name: String
    att kind: String
}
