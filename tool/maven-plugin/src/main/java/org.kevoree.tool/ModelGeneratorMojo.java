package org.kevoree.tool;


import org.KevoreeModel;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.kevoree.*;
import org.kevoree.annotations.*;
import org.kevoree.annotations.Channel;
import org.kevoree.annotations.Component;
import org.kevoree.annotations.Group;
import org.kevoree.annotations.Node;
import org.kevoree.annotations.params.*;
import org.kevoree.annotations.params.Param;
import org.kevoree.api.OutputPort;
import org.kevoree.meta.MetaNumberType;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.memory.manager.DataManagerBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Mojo(name = "gen-model", requiresDependencyResolution = ResolutionScope.RUNTIME)
public class ModelGeneratorMojo extends AbstractMojo {

    private static final int UNIVERSE = 0;
    private static final int TIME = 0;

    private KevoreeModel kModel = new KevoreeModel(DataManagerBuilder.buildDefault());

    @Parameter(defaultValue = "${project.version}", readonly = true)
    private String deployUnitVersion;

    @Parameter(defaultValue = "${project.groupId}:${project.artifactId}", readonly = true)
    private String deployUnitName;

    @Parameter(defaultValue = "${project.build.outputDirectory}", readonly = true)
    private File sourcesDir;

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Parameter(defaultValue = "${project.build.directory}")
    private File modelDir;

    @Parameter(required = true)
    private String namespace;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().debug("Kevoree Model Generator - Reading project...");

        try {
            final CountDownLatch latch = new CountDownLatch(1);
            kModel.connect(new KCallback() {
                @Override
                public void on(Object o) {
                    latch.countDown();
                }
            });
            latch.await(1000, TimeUnit.MILLISECONDS);
            try {
                Model model = kModel.createModel(UNIVERSE, TIME);
                Namespace ns = createNamespace();
                model.addNamespaces(ns);
                getLog().info("Namespace:       "+ns.getName());

                for (Class<?> tdefClass : findTypeDefinitionClasses()) {
                    TypeDefinition tdef = createTypeDefinition(tdefClass);
                    getLog().info("TypeDefinition:  "+tdef.getName()+"/"+tdef.getVersion());

                    DictionaryType dictionaryType = createDictionaryType(tdefClass);
                    tdef.addDictionary(dictionaryType);

                    if (tdef instanceof ComponentType) {
                        List<PortType> inputs = createInputPortTypes(tdefClass);
                        StringBuilder inputLogs = new StringBuilder("Input ports:     ");
                        if (!inputs.isEmpty()) {
                            Iterator<PortType> it = inputs.iterator();
                            while (it.hasNext()) {
                                PortType portType = it.next();
                                ((ComponentType) tdef).addInputTypes(portType);
                                inputLogs.append(portType.getName());
                                if (it.hasNext()) {
                                    inputLogs.append(", ");
                                }
                            }
                        } else {
                            inputLogs.append("<none>");
                        }
                        getLog().info(inputLogs.toString());

                        List<PortType> outputs = createOutputPortTypes(tdefClass);
                        StringBuilder outputsLog = new StringBuilder("Output ports:    ");
                        if (!outputs.isEmpty()) {
                            Iterator<PortType> it = outputs.iterator();
                            while (it.hasNext()) {
                                PortType portType = it.next();
                                outputsLog.append(portType.getName());
                                ((ComponentType) tdef).addOutputTypes(portType);
                                if (it.hasNext()) {
                                    outputsLog.append(", ");
                                }
                            }
                        } else {
                            outputsLog.append("<none>");
                        }
                        getLog().info(outputsLog.toString());
                    }

                    DeployUnit du = createDeployUnit();
                    tdef.addDeployUnits(du);
                    getLog().info("DeployUnit:      "+du.getName()+"/"+du.getVersion());

                    ns.addTypeDefinitions(tdef);
                }

                saveModel(model);
                getLog().debug("Kevoree Model Generator - Done");
                latch.countDown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private List<PortType> createOutputPortTypes(Class<?> clazz) {
        List<PortType> portTypes = new ArrayList<>();
        for (Field f : clazz.getDeclaredFields()) {
            if (f.isAnnotationPresent(Output.class) && f.getType().equals(OutputPort.class)) {
                PortType portType = kModel.createPortType(UNIVERSE, TIME);
                portType.setName(f.getName());
                portTypes.add(portType);
            }
        }
        return portTypes;
    }

    private List<PortType> createInputPortTypes(Class<?> clazz) {
        List<PortType> portTypes = new ArrayList<>();
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.isAnnotationPresent(Input.class)) {
                PortType portType = kModel.createPortType(UNIVERSE, TIME);
                portType.setName(m.getName());
                portTypes.add(portType);
            }
        }
        return portTypes;
    }

    private DictionaryType createDictionaryType(Class<?> clazz) throws Exception {
        DictionaryType dic = kModel.createDictionaryType(UNIVERSE, TIME);
        List<Field> fields = ReflectUtils.getAllFieldsWithAnnotation(clazz, Param.class);
        if (!fields.isEmpty()) {
            getLog().info("Dictionary:");
            for (Field field : fields) {
                try {
                    ParamType param = createParamType(clazz, field);
                    if (param != null) {
                        dic.addParams(param);
                    }
                } catch (MojoExecutionException e) {
                    throw new MojoExecutionException("Cannot create dictionary for "+clazz.getName(), e);
                }
            }
        } else {
            getLog().info("Dictionary:      <none>");
        }
        return dic;
    }

    private ParamType createParamType(Class<?> clazz, Field field) throws Exception {
        StringBuilder logParam = new StringBuilder("  ");
        logParam.append(field.getName());
        ParamType param;
        if (field.getType().equals(Integer.class) || field.getType().equals(int.class)) {
            param = kModel.createNumberParamType(UNIVERSE, TIME);
            ((NumberParamType) param).setType(MetaNumberType.INT);
            String defaultVal = String.valueOf(getDefaultValue(clazz, field));
            ((NumberParamType) param).setDefault(defaultVal);
            logParam.append(": int");
            if (defaultVal != null) {
                logParam.append(" = ");
                logParam.append(defaultVal);
            }
            createParamConstraints(field, param, logParam);

        } else if (field.getType().equals(Double.class) || field.getType().equals(double.class)) {
            param = kModel.createNumberParamType(UNIVERSE, TIME);
            ((NumberParamType) param).setType(MetaNumberType.DOUBLE);
            String defaultVal = String.valueOf(getDefaultValue(clazz, field));
            ((NumberParamType) param).setDefault(defaultVal);
            logParam.append(": double");
            if (defaultVal != null) {
                logParam.append(" = ");
                logParam.append(defaultVal);
            }
            createParamConstraints(field, param, logParam);

        } else if (field.getType().equals(Long.class) || field.getType().equals(long.class)) {
            param = kModel.createNumberParamType(UNIVERSE, TIME);
            ((NumberParamType) param).setType(MetaNumberType.LONG);
            String defaultVal = String.valueOf(getDefaultValue(clazz, field));
            ((NumberParamType) param).setDefault(defaultVal);
            logParam.append(": long");
            if (defaultVal != null) {
                logParam.append(" = ");
                logParam.append(defaultVal);
            }
            createParamConstraints(field, param, logParam);

        } else if (field.getType().equals(Float.class) || field.getType().equals(float.class)) {
            param = kModel.createNumberParamType(UNIVERSE, TIME);
            ((NumberParamType) param).setType(MetaNumberType.FLOAT);
            String defaultVal = String.valueOf(getDefaultValue(clazz, field));
            ((NumberParamType) param).setDefault(defaultVal);
            logParam.append(": float");
            if (defaultVal != null) {
                logParam.append(" = ");
                logParam.append(defaultVal);
            }
            createParamConstraints(field, param, logParam);

        } else if (field.getType().equals(Short.class) || field.getType().equals(short.class)) {
            param = kModel.createNumberParamType(UNIVERSE, TIME);
            ((NumberParamType) param).setType(MetaNumberType.SHORT);
            String defaultVal = String.valueOf(getDefaultValue(clazz, field));
            ((NumberParamType) param).setDefault(defaultVal);
            logParam.append(": short");
            if (defaultVal != null) {
                logParam.append(" = ");
                logParam.append(defaultVal);
            }
            createParamConstraints(field, param, logParam);

        } else if (field.getType().equals(Boolean.class) || field.getType().equals(boolean.class)) {
            param = kModel.createBooleanParamType(UNIVERSE, TIME);
            Boolean defaultVal = (Boolean) getDefaultValue(clazz, field);
            ((BooleanParamType) param).setDefault(defaultVal);
            logParam.append(": boolean");
            if (defaultVal != null) {
                logParam.append(" = ");
                logParam.append(defaultVal);
            }

        } else if (field.getType().equals(String.class)) {
            param = kModel.createStringParamType(UNIVERSE, TIME);
            String defaultVal = String.valueOf(getDefaultValue(clazz, field));
            ((StringParamType) param).setDefault(defaultVal);
            logParam.append(": string");
            if (defaultVal != null) {
                logParam.append(" = ");
                logParam.append(defaultVal);
            }
            createParamConstraints(field, param, logParam);

        } else if (field.getType().equals(Character.class)) {
            throw new MojoExecutionException("Character type for @Param is not handled yet (" + field.getName() + ")");

        } else if (field.getType().isEnum()) {
            param = kModel.createChoiceParamType(UNIVERSE, TIME);
            logParam.append(": choice [");
            Object[] enumConstants = field.getType().getEnumConstants();
            for (int i=0; i < enumConstants.length; i++) {
                Item item = kModel.createItem(UNIVERSE, TIME);
                item.setValue(enumConstants[i].toString());
                logParam.append(item.getValue());
                if (i < enumConstants.length - 1) {
                    logParam.append(", ");
                }
                ((ChoiceParamType) param).addChoices(item);
            }
            logParam.append("]");
            String defaultValue = String.valueOf(getDefaultValue(clazz, field));
            ((ChoiceParamType) param).setDefault(defaultValue);
            if (defaultValue != null) {
                logParam.append(" = ");
                logParam.append(defaultValue);
            }

        } else {
            throw new MojoExecutionException("Unknown type \""+field.getType().toString()+"\" for @Param "+field.getName());
        }
        param.setName(field.getName());
        if (field.isAnnotationPresent(Fragment.class)) {
            param.setFragment(true);
            logParam.append(" (frag)");
        } else {
            param.setFragment(false);
        }

        if (field.isAnnotationPresent(Required.class)) {
            param.setRequired(true);
            logParam.append(" (required)");
        } else {
            param.setRequired(false);
        }
        getLog().info(logParam.toString());
        return param;
    }

    private void createParamConstraints(Field field, ParamType param, StringBuilder logParam) {
        if (field.isAnnotationPresent(Min.class)) {
            Min anno = field.getAnnotation(Min.class);
            MinConstraint min = kModel.createMinConstraint(UNIVERSE, TIME);
            min.setExclusive(anno.exclusive());
            min.setValue(anno.value());
            param.addConstraints(min);
            logParam.append(" (min: ")
                    .append(anno.value())
                    .append(")");
        }
        if (field.isAnnotationPresent(Max.class)) {
            Max anno = field.getAnnotation(Max.class);
            MaxConstraint max = kModel.createMaxConstraint(UNIVERSE, TIME);
            max.setExclusive(anno.exclusive());
            max.setValue(anno.value());
            param.addConstraints(max);
            logParam.append(" (max: ")
                    .append(anno.value())
                    .append(")");
        }
        if (field.isAnnotationPresent(Multiline.class)) {
            Multiline anno = field.getAnnotation(Multiline.class);
            if (anno != null) {
                MultilineConstraint multi = kModel.createMultilineConstraint(UNIVERSE, TIME);
                multi.setValue(true);
                param.addConstraints(multi);
                logParam.append(" (multiline)");
            }
        }
    }

    private void saveModel(Model model) throws IOException, InterruptedException {
        final File modelFile = new File(modelDir + File.separator + "kevoree-model.json");
        if (modelFile.exists()) {
            modelFile.delete();
        }
        modelFile.createNewFile();
        final CountDownLatch latch = new CountDownLatch(1);
        kModel.universe(UNIVERSE).time(TIME).json().save(model, new KCallback<String>() {
            @Override
            public void on(String modelStr) {
                try {
                    FileUtils.writeStringToFile(modelFile, modelStr);
                    getLog().info("Kevoree model generated: "+modelFile.toString());
                    latch.countDown();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        latch.await(1000, TimeUnit.MILLISECONDS);
    }

    private Namespace createNamespace() throws MojoExecutionException {
        if (namespace != null) {
            Namespace ns = kModel.createNamespace(UNIVERSE, TIME);
            ns.setName(namespace);
            return ns;
        } else {
            throw new MojoExecutionException("You must give a namespace value in the Kevoree Model Generator Plugin");
        }
    }

    private Set<Class<?>> findTypeDefinitionClasses() throws Exception {
        if (sourcesDir.exists()) {
            getLog().debug("Trying to find a TypeDefinition in:");
            getLog().debug("  "+sourcesDir.getPath());
            final Set<Class<?>> tdefs = new HashSet<>();
            final Set<URL> urls = new HashSet<>();
            for (Object elem: project.getCompileClasspathElements()) {
                try {
                    urls.add(new File((String) elem).toURI().toURL());
                } catch (Exception ignore) {}
            }
            final ClassLoader classLoader = new URLClassLoader(urls.toArray(new URL[] {}), Thread.currentThread().getContextClassLoader());

            Files.walkFileTree(sourcesDir.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String filePath = file.toString();
                    String fqn = filePath.substring(sourcesDir.toString().length()+1, filePath.length() - ".class".length())
                            .replaceAll("/", ".");
                    Class<?> clazz;
                    try {
                        clazz = classLoader.loadClass(fqn);
                        if (ReflectUtils.hasAnnotation(clazz, Component.class, Node.class, Group.class, Channel.class)) {
                            tdefs.add(clazz);
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    return super.visitFile(file, attrs);
                }
            });

            if (tdefs.isEmpty()) {
                throw new MojoExecutionException("No TypeDefinition found in your project");
            } else {
                return tdefs;
            }
        } else {
            throw new MojoExecutionException("Empty directory: "+ sourcesDir);
        }
    }

    private TypeDefinition createTypeDefinition(Class<?> clazz) throws Exception {
        TypeDefinition tdef;

        if (ReflectUtils.hasAnnotation(clazz, Component.class)) {
            tdef = kModel.createComponentType(UNIVERSE, TIME);
            Component ca = ReflectUtils.findAnnotation(clazz, Component.class);
            tdef.setVersion(ca.version());
            tdef.setDescription(ca.description());

        } else if (ReflectUtils.hasAnnotation(clazz, Node.class)) {
            tdef = kModel.createNodeType(UNIVERSE, TIME);
            Node ca = ReflectUtils.findAnnotation(clazz, Node.class);
            tdef.setVersion(ca.version());
            tdef.setDescription(ca.description());

        } else if (ReflectUtils.hasAnnotation(clazz, Group.class)) {
            tdef = kModel.createGroupType(UNIVERSE, TIME);
            Group ca = ReflectUtils.findAnnotation(clazz, Group.class);
            tdef.setVersion(ca.version());
            tdef.setDescription(ca.description());

        } else if (ReflectUtils.hasAnnotation(clazz, Channel.class)) {
            tdef = kModel.createChannelType(UNIVERSE, TIME);
            Channel ca = ReflectUtils.findAnnotation(clazz, Channel.class);
            tdef.setVersion(ca.version());
            tdef.setDescription(ca.description());
        } else {
            // this should never happen (unless a new type is added and the check in findTypeDefinitionClass() visitor
            // is not updated accordingly)
            throw new Exception("Unable to find the TypeDefinition of "+clazz.getName()+" based on its annotation");
        }

        tdef.setName(clazz.getSimpleName());

        return tdef;
    }

    private DeployUnit createDeployUnit() {
        DeployUnit du = kModel.createDeployUnit(UNIVERSE, TIME);
        du.setName(deployUnitName);
        du.setVersion(deployUnitVersion);
        du.setPlatform("java");
        return du;
    }

    private Object getDefaultValue(Class<?> clazz, Field field) throws Exception {
        Object o = clazz.newInstance();
        field.setAccessible(true);
        return field.get(o);
    }
}