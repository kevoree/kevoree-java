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
    att name: String

    rel typeDefinition: org.kevoree.TypeDefinition with maxBound 1
    rel metrics: org.kevoree.Metric
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
}

class org.kevoree.Group extends org.kevoree.Instance {
    rel nodes: org.kevoree.Node with opposite "groups"
}

class org.kevoree.Component extends org.kevoree.Instance {
    rel host: org.kevoree.Node with maxBound 1

    rel inputs: org.kevoree.InputPort with opposite "channels"
    rel outputs: org.kevoree.OutputPort with opposite "channels"
}

class org.kevoree.Port extends org.kevoree.Element {
    att name: String

    rel type: org.kevoree.PortType
    rel channels: org.kevoree.Channel
            with opposite "inputs"
            with opposite "outputs"
}

class org.kevoree.InputPort extends org.kevoree.Port {}
class org.kevoree.OutputPort extends org.kevoree.Port {}

class org.kevoree.Namespace extends org.kevoree.Element {
    att name: String

    rel typeDefinitions: org.kevoree.TypeDefinition
}

class org.kevoree.Metric extends org.kevoree.Element {
    att name: String
    att value: Continuous
}

class org.kevoree.TypeDefinition extends org.kevoree.Element {
    att name: String
    att version: String

    rel dictionary: org.kevoree.DictionaryType with maxBound 1
    rel deployUnits: org.kevoree.DeployUnit
}

class org.kevoree.NodeType extends org.kevoree.TypeDefinition {}

class org.kevoree.GroupType extends org.kevoree.TypeDefinition {
    att remote: Bool

    rel fragDictionaries: org.kevoree.FragmentDictionaryType
}

class org.kevoree.ChannelType extends org.kevoree.TypeDefinition {
    att remote: Bool
    att fragmentable: Bool

    rel fragDictionaries: org.kevoree.FragmentDictionaryType
}

class org.kevoree.ComponentType extends org.kevoree.TypeDefinition {
    att remote: Bool

    rel inputTypes: org.kevoree.PortType
    rel outputTypes: org.kevoree.PortType
}

class org.kevoree.DeployUnit extends org.kevoree.Element {
    att name: String
    att platform: String
    att version: String

    rel dependencies: org.kevoree.DeployUnit
}

class org.kevoree.DictionaryType extends org.kevoree.Element {
    rel attributes: org.kevoree.AttributeType
}

class org.kevoree.FragmentDictionaryType extends org.kevoree.Dictionary {
    rel fragment: org.kevoree.Node
}

class org.kevoree.AttributeType extends org.kevoree.Element {
    att optional: Bool

    rel datatype: org.kevoree.DataType with maxBound 1
}

class org.kevoree.PortType extends org.kevoree.Element {
    att name: String

    rel protocol: org.kevoree.MsgProtocol
}

class org.kevoree.Value extends org.kevoree.Element {
    att name: String
    att value: String
}

class org.kevoree.MsgProtocol extends org.kevoree.Element {
    att name: String
}

class org.kevoree.DataType extends org.kevoree.Element {}
class org.kevoree.StringDataType extends org.kevoree.DataType {
    att multiline: Bool
    att default: String
}
class org.kevoree.DoubleDataType extends org.kevoree.DataType {
    att min: Double
    att max: Double
    att default: Double
}
class org.kevoree.IntDataType extends org.kevoree.DataType {
    att min: Int
    att max: Int
    att default: Int
}
class org.kevoree.LongDataType extends org.kevoree.DataType {
    att min: Long
    att max: Long
    att default: Long
}
class org.kevoree.BooleanDataType extends org.kevoree.DataType {
    att default: Bool
}
class org.kevoree.ChoiceDataType extends org.kevoree.DataType {
    rel default: org.kevoree.Choice with maxBound 1
    rel choices: org.kevoree.Choice
}

class org.kevoree.Choice {
    att value: String
}