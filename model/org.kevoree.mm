with version "6.0.0-SNAPSHOT"

class org.kevoree.Element {
  rel metaData: org.kevoree.Value
}

class org.kevoree.Model extends org.kevoree.Element {
  rel nodes: org.kevoree.Node
  rel channels: org.kevoree.Channel
  rel namespaces: org.kevoree.Namespace
}

class org.kevoree.Instance extends org.kevoree.Element {
  with instantiation "false"

  att name: String with index
  att started: Bool

  rel typeDefinition: org.kevoree.TypeDefinition with maxBound 1
  rel dictionary: org.kevoree.Dictionary with maxBound 1
}

class org.kevoree.Node extends org.kevoree.Instance {
  rel host: org.kevoree.Node with maxBound 1
  rel components: org.kevoree.Component with opposite "host"
  rel modelConnector: org.kevoree.ModelConnector with maxBound 1
                                                 with opposite "node"
}

class org.kevoree.Channel extends org.kevoree.Instance {
  rel inputs: org.kevoree.InputPort with opposite "channels"
  rel outputs: org.kevoree.OutputPort with opposite "channels"
  rel fragmentDictionary: org.kevoree.FragmentDictionary
}

class org.kevoree.Component extends org.kevoree.Instance {
  rel host: org.kevoree.Node with maxBound 1
                             with opposite "components"
  rel inputs: org.kevoree.InputPort with opposite "components"
  rel outputs: org.kevoree.OutputPort with opposite "components"
}

class org.kevoree.ModelConnector extends org.kevoree.Instance {
  rel node: org.kevoree.Node with maxBound 1
                             with opposite "modelConnector"
}

class org.kevoree.Port extends org.kevoree.Element {
  att name: String with index

  rel type: org.kevoree.PortType
  rel channels: org.kevoree.Channel with opposite "inputs"
                                    with opposite "outputs"
  rel components: org.kevoree.Component with opposite "inputs"
                                        with opposite "outputs"
                                        with maxBound 1
}

class org.kevoree.InputPort extends org.kevoree.Port {}

class org.kevoree.OutputPort extends org.kevoree.Port {}

class org.kevoree.Namespace extends org.kevoree.Element {
  att name: String with index

  rel typeDefinitions: org.kevoree.TypeDefinition
}

class org.kevoree.TypeDefinition extends org.kevoree.Element {
  with instantiation "false"

  att name: String with index
  att version: Int with index
  att description: String

  rel dictionary: org.kevoree.DictionaryType with maxBound 1
  rel deployUnits: org.kevoree.DeployUnit
  rel preferedVersions: org.kevoree.PreferedVersion
}

class org.kevoree.PreferedVersion extends org.kevoree.Element {
  att platform: String
  att name: String
  att version: String
}

class org.kevoree.NodeType extends org.kevoree.TypeDefinition {
  att adaptationBehavior: org.kevoree.AdaptationBehavior
}

class org.kevoree.ModelConnectorType extends org.kevoree.TypeDefinition {}

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
  att description: String

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

  rel type: org.kevoree.ParamType
}

class org.kevoree.StringParam extends org.kevoree.Param {
  att value: String
}

class org.kevoree.NumberParam extends org.kevoree.Param {
  att value: String
}

class org.kevoree.BooleanParam extends org.kevoree.Param {
  att value: Bool
}

class org.kevoree.ListParam extends org.kevoree.Param {
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
  att default: String

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

class org.kevoree.MultilineConstraint extends org.kevoree.AbstractConstraint {
  att value: Bool
}

class org.kevoree.LengthConstraint extends org.kevoree.AbstractConstraint {
  att value: Int
}

enum org.kevoree.NumberType {
  SHORT, INT, LONG, FLOAT, DOUBLE
}

enum org.kevoree.AdaptationBehavior {
  ROLLBACK_ON_ERROR, CONTINUE_ON_ERROR // and probably more
}
