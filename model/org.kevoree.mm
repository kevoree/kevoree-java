with version "6.0.0-SNAPSHOT"

class org.kevoree.Model {
    rel nodes: org.kevoree.Node

    rel namespaces: org.kevoree.Namespace
}

class org.kevoree.Instance {
    att name: String

    rel typeDefinition: org.kevoree.TypeDefinition
    rel metrics: org.kevoree.Metric
}

class org.kevoree.Node extends org.kevoree.Instance {
    rel host: org.kevoree.Node
    rel subNodes: org.kevoree.Node
    rel components: org.kevoree.Component
}

class org.kevoree.Component extends org.kevoree.Instance {
    rel host: org.kevoree.Node
}

class org.kevoree.Namespace {
    att name: String

    rel typeDefinitions: org.kevoree.TypeDefinition
}

class org.kevoree.Metric {
    att name: String
    att value: Continuous
}

class org.kevoree.TypeDefinition {
    att name: String
    att version: String

    rel deployUnits: org.kevoree.DeployUnit
}

class org.kevoree.NodeType extends org.kevoree.TypeDefinition {

}

class org.kevoree.DeployUnit {
    att name: String
    att platform: String
    att version: String

    rel dependencies: org.kevoree.DeployUnit
}
