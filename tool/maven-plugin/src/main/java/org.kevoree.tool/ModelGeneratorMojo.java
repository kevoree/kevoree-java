package org.kevoree.tool;


import javassist.ClassPool;
import javassist.CtClass;
import org.KevoreeModel;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.kevoree.DeployUnit;
import org.kevoree.Model;
import org.kevoree.Namespace;
import org.kevoree.TypeDefinition;
import org.kevoree.annotations.Channel;
import org.kevoree.annotations.Component;
import org.kevoree.annotations.Group;
import org.kevoree.annotations.Node;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.memory.manager.DataManagerBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Mojo(name = "gen-model", requiresDependencyResolution = ResolutionScope.RUNTIME)
public class ModelGeneratorMojo extends AbstractMojo {

    private static final int UNIVERSE = 0;
    private static final int TIME = 0;

    private KevoreeModel kModel = new KevoreeModel(DataManagerBuilder.buildDefault());

    @Parameter(defaultValue = "${project.version}", readonly = true)
    private String deployUnitVersion;

    @Parameter(defaultValue = "${project.groupId}:${project.actifactId}", readonly = true)
    private String deployUnitName;

    @Parameter(defaultValue = "${project.build.outputDirectory}", readonly = true)
    private File sourcesDir;

    @Parameter(defaultValue = "${project.build.directory}")
    private File modelDir;

    @Parameter(required = true)
    private String namespace;

    private CountDownLatch latch = new CountDownLatch(1);

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().debug("Kevoree Model Generator - Reading project...");

        try {
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
                getLog().debug("Namespace:       "+ns);

                for (CtClass tdefClass : findTypeDefinitionClasses()) {
                    TypeDefinition tdef = createTypeDefinition(tdefClass);
                    getLog().debug("TypeDefinition:  "+tdef.getName()+"/"+tdef.getVersion());

                    DeployUnit du = createDeployUnit();
                    tdef.addDeployUnits(du);
                    getLog().debug("DeployUnit:      "+du.getName()+":"+du.getVersion()+":"+du.getPlatform());

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

    private void saveModel(Model model) throws IOException {
        final File modelFile = new File(modelDir + File.separator + "lib.json");
        if (modelFile.exists()) {
            modelFile.delete();
        }
        modelFile.createNewFile();
        kModel.universe(UNIVERSE).time(TIME).json().save(model, new KCallback<String>() {
            @Override
            public void on(String modelStr) {
                try {
                    FileUtils.writeStringToFile(modelFile, modelStr);
                    getLog().debug("Model:           "+modelFile.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
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

    private Set<CtClass> findTypeDefinitionClasses() throws Exception {
        if (sourcesDir.exists()) {
            getLog().info("Trying to find a TypeDefinition in:");
            getLog().info("  "+sourcesDir.getPath());
            final Set<CtClass> tdefs = new HashSet<>();

            Files.walkFileTree(sourcesDir.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    FileInputStream fis = new FileInputStream(file.toFile());
                    ClassPool classPool = ClassPool.getDefault();
                    CtClass clazz = classPool.makeClass(fis);
                    if (clazz.hasAnnotation(Component.class) ||
                            clazz.hasAnnotation(Channel.class) ||
                            clazz.hasAnnotation(Node.class) ||
                            clazz.hasAnnotation(Group.class)) {
                        tdefs.add(clazz);
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

    private TypeDefinition createTypeDefinition(CtClass clazz) throws Exception {
        TypeDefinition tdef;

        if (clazz.hasAnnotation(Component.class)) {
            tdef = kModel.createComponentType(UNIVERSE, TIME);
            Component ca = (Component) clazz.getAnnotation(Component.class);
            tdef.setVersion(ca.version());
            tdef.setDescription(ca.description());

        } else if (clazz.hasAnnotation(Node.class)) {
            tdef = kModel.createNodeType(UNIVERSE, TIME);
            Node ca = (Node) clazz.getAnnotation(Node.class);
            tdef.setVersion(ca.version());
            tdef.setDescription(ca.description());

        } else if (clazz.hasAnnotation(Group.class)) {
            tdef = kModel.createGroupType(UNIVERSE, TIME);
            Group ca = (Group) clazz.getAnnotation(Group.class);
            tdef.setVersion(ca.version());
            tdef.setDescription(ca.description());

        } else if (clazz.hasAnnotation(Channel.class)) {
            tdef = kModel.createChannelType(UNIVERSE, TIME);
            Channel ca = (Channel) clazz.getAnnotation(Channel.class);
            tdef.setVersion(ca.version());
            tdef.setDescription(ca.description());
        } else {
            // this should never happen (unless a new type is added and the check in findTypeDefinitionClasses() visitor
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
}