with version "6.0.0-SNAPSHOT"

class org.kevoree.Model extends org.kevoree.Element {
    rel nodes: org.kevoree.Node
    rel channels: org.kevoree.Channel
    rel groups: org.kevoree.Group
    rel namespaces: org.kevoree.Namespace
}

class org.kevoree.Element {
    rel metaData: org.kevoree.Value
}

class org.kevoree.Instance extends org.kevoree.Element {
    with instantiation "false"

    att name: String with index

    rel typeDefinition: org.kevoree.TypeDefinition with maxBound 1
    rel dictionary: org.kevoree.Dictionary
}

class org.kevoree.Node extends org.kevoree.Instance {
    rel host: org.kevoree.Node with maxBound 1
    rel subNodes: org.kevoree.Node
    rel components: org.kevoree.Component
    rel groups: org.kevoree.Group with opposite "nodes"
}

class org.kevoree.Channel extends org.kevoree.Instance {
    rel inputs: org.kevoree.InputPort with opposite "channels"
    rel outputs: org.kevoree.OutputPort with opposite "channels"
    rel fragmentDictionary: org.kevoree.FragmentDictionary
}

class org.kevoree.Group extends org.kevoree.Instance {
    rel nodes: org.kevoree.Node with opposite "groups"
    rel fragmentDictionary: org.kevoree.FragmentDictionary
}

class org.kevoree.Component extends org.kevoree.Instance {
    rel host: org.kevoree.Node with maxBound 1
    rel inputs: org.kevoree.InputPort with opposite "channels"
    rel outputs: org.kevoree.OutputPort with opposite "channels"
}

class org.kevoree.Port extends org.kevoree.Element {
    att name: String with index

    rel type: org.kevoree.PortType
    rel channels: org.kevoree.Channel with opposite "inputs"
                                      with opposite "outputs"
}

class org.kevoree.InputPort extends org.kevoree.Port {
}

class org.kevoree.OutputPort extends org.kevoree.Port {
}

class org.kevoree.Namespace extends org.kevoree.Element {
    att name: String with index

    rel typeDefinitions: org.kevoree.TypeDefinition
}

class org.kevoree.TypeDefinition extends org.kevoree.Element {
    with instantiation "false"

    att name: String with index
    att version: String with index

    rel dictionary: org.kevoree.DictionaryType with maxBound 1
    rel deployUnits: org.kevoree.DeployUnit
}

class org.kevoree.NodeType extends org.kevoree.TypeDefinition {
}

class org.kevoree.GroupType extends org.kevoree.TypeDefinition {
    att remote: Bool
}

class org.kevoree.ChannelType extends org.kevoree.TypeDefinition {
    att remote: Bool
    att fragmentable: Bool
}

class org.kevoree.ComponentType extends org.kevoree.TypeDefinition {
    att remote: Bool

    rel inputTypes: org.kevoree.PortType
    rel outputTypes: org.kevoree.PortType
}

class org.kevoree.DeployUnit extends org.kevoree.Element {
    att name: String with index
    att version: String with index
    att platform: String
}

class org.kevoree.DictionaryType extends org.kevoree.Element {
    rel params: org.kevoree.ParamType
}

class org.kevoree.ParamType extends org.kevoree.Element {
    with instantiation "false"

    att name: String with index
    att required: Bool
    att fragment: Bool

    rel constraints: org.kevoree.AbstractConstraint
}

class org.kevoree.Dictionary extends org.kevoree.Element {
    rel params: org.kevoree.Param
}

class org.kevoree.FragmentDictionary extends org.kevoree.Dictionary {
    att name: String with index
}

class org.kevoree.Param extends org.kevoree.Element {
    with instantiation "false"

    att name: String with index
}

class org.kevoree.NumberParam extends org.kevoree.Element {
    att value: String
    att type: org.kevoree.NumberType
}

class org.kevoree.BooleanParam extends org.kevoree.Element {
    att value: Bool
}

class org.kevoree.ListParam extends org.kevoree.Element {
    rel values: org.kevoree.Item
}

class org.kevoree.PortType extends org.kevoree.Element {
    att name: String with index

    rel protocol: org.kevoree.Value
}

class org.kevoree.Value extends org.kevoree.Element {
    att name: String with index
    att value: String
}

class org.kevoree.StringParamType extends org.kevoree.ParamType {
    att default: String
}

class org.kevoree.NumberParamType extends org.kevoree.ParamType {
    att default: String
    att type: org.kevoree.NumberType
}

class org.kevoree.BooleanParamType extends org.kevoree.ParamType {
    att default: Bool
}

class org.kevoree.ChoiceParamType extends org.kevoree.ParamType {
    att defaultIndex: Int

    rel choices: org.kevoree.Item
}

class org.kevoree.ListParamType extends org.kevoree.ParamType {
    rel default: org.kevoree.Item
}

class org.kevoree.Item {
    att value: String
}

class org.kevoree.AbstractConstraint extends org.kevoree.Element {
    with instantiation "false"
}

class org.kevoree.MinConstraint extends org.kevoree.AbstractConstraint {
    att value: Int
    att exclusive: Bool
}

class org.kevoree.MaxConstraint extends org.kevoree.AbstractConstraint {
    att value: Int
    att exclusive: Bool
}

class org.kevoree.LengthConstraint extends org.kevoree.AbstractConstraint {
    att value: Int
}

class org.kevoree.MultilineConstraint extends org.kevoree.AbstractConstraint {
    att value: Bool
}

enum org.kevoree.NumberType {
    SHORT, INT, LONG, FLOAT, DOUBLE
}