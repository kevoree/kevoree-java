package org.kevoree.tool;


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
import org.kevoree.annotations.params.Param;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.memory.manager.DataManagerBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Mojo(name = "gen-model", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class ModelGeneratorMojo extends AbstractMojo {

    private static final int UNIVERSE = 0;
    private static final int TIME = 0;

    private KevoreeModel kModel = new KevoreeModel(DataManagerBuilder.buildDefault());

    @Parameter(required = true)
    private String namespace;

    @Parameter(defaultValue = "${project.version}", readonly = true)
    private String deployUnitVersion;

    @Parameter(defaultValue = "${project.groupId}:${project.actifactId}", readonly = true)
    private String deployUnitName;

    @Parameter(defaultValue = "${project.build.outputDirectory}", readonly = true)
    private File sourcesDir;

    @Parameter(defaultValue = "${project.build.directory}")
    private File modelDir;

    private CountDownLatch latch = new CountDownLatch(1);

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().debug("Kevoree Model Generator - Reading project...");

        System.out.println(">>>>>>>>>>>>>>>>><< "+Thread.currentThread().getName());
        kModel.connect(o -> {
            System.out.println("*********---------******** "+Thread.currentThread().getName());
            try {
                Model model = kModel.createModel(UNIVERSE, TIME);
                Namespace ns = createNamespace();
                model.addNamespaces(ns);
                getLog().debug("Namespace:       "+ns);

                Class<?> tdefClass = findTypeDefinitionClass();
                TypeDefinition tdef = createTypeDefinition(tdefClass);
                getLog().debug("TypeDefinition:  "+tdef.getName()+"/"+tdef.getVersion());

                DeployUnit du = createDeployUnit();
                tdef.addDeployUnits(du);
                getLog().debug("DeployUnit:      "+du.getName()+":"+du.getVersion()+":"+du.getPlatform());

                saveModel(model);
                getLog().debug("Kevoree Model Generator - Done");
                latch.countDown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        try {
            latch.await(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void saveModel(Model model) throws IOException {
        File modelFile = new File(modelDir + File.separator + "lib.json");
        if (modelFile.exists()) {
            modelFile.delete();
        }
        modelFile.createNewFile();
        kModel.universe(UNIVERSE).time(TIME).json().save(model, modelStr -> {
            try {
                FileUtils.writeStringToFile(modelFile, modelStr);
                getLog().debug("Model:           "+modelFile.toString());
            } catch (IOException e) {
                e.printStackTrace();
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

    private Class<?> findTypeDefinitionClass() throws Exception {
        if (sourcesDir.exists()) {
            getLog().info("Trying to find a TypeDefinition in:");
            getLog().info("  "+sourcesDir.getPath());
            final Set<Class<?>> tdefs = new HashSet<>();
            final URLClassLoader classLoader = new URLClassLoader(new URL[] { sourcesDir.toURI().toURL() });
            final String srcDir = sourcesDir.getPath();

            Files.walkFileTree(sourcesDir.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String targetPath = file.toString();
                    String fqn = targetPath
                            .substring(srcDir.length()+1, targetPath.length() - ".class".length())
                            .replaceAll("/", ".");
                    try {
                        Class<?> clazz = classLoader.loadClass(fqn);
                        if (ReflectUtils.hasAnnotation(clazz, Component.class, Node.class, Group.class, Channel.class)) {
                            tdefs.add(clazz);
                        }
                    } catch (ClassNotFoundException e) {
                        getLog().warn("Unable to load class "+fqn+" from "+ sourcesDir);
                    }
                    return super.visitFile(file, attrs);
                }
            });

            if (tdefs.size() == 1) {
                return tdefs.iterator().next();
            } else if (tdefs.isEmpty()) {
                throw new Exception("No TypeDefinition found");
            } else {
                throw new Exception("Multiple TypeDefinitions found");
            }
        } else {
            throw new Exception("No class found in "+ sourcesDir);
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
}