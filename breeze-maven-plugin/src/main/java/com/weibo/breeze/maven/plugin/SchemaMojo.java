/*
 *
 *   Copyright 2019 Weibo, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.weibo.breeze.maven.plugin;

import com.weibo.breeze.Breeze;
import com.weibo.breeze.BreezeException;
import com.weibo.breeze.SchemaLoader;
import com.weibo.breeze.SchemaUtil;
import com.weibo.breeze.message.Message;
import com.weibo.breeze.message.Schema;
import com.weibo.breeze.serializer.CommonSerializer;
import com.weibo.breeze.serializer.EnumSerializer;
import com.weibo.breeze.serializer.Serializer;
import org.apache.commons.lang3.ClassUtils;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * @author zhanglei28
 * @date 2019/5/16.
 */
@Mojo(
        name = "schema",
        defaultPhase = LifecyclePhase.PREPARE_PACKAGE,
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME
)
public class SchemaMojo extends AbstractMojo {
    private static final String DEFAULT_INCLUDES = "**/*.xml";
    private static final String BREEZE_DIR = "META-INF" + File.separator + "breeze";
    private static final String HEADER_PREFIX = "// auto generated by breeze-maven-plugin (https://github.com/weibreeze/breeze)";

    @Parameter(defaultValue = "${basedir}/src/main/resources")
    private String xmlPath;

    @Parameter(defaultValue = DEFAULT_INCLUDES)
    private String includes;

    @Parameter
    private String excludes;

    /**
     * service interfaces or bean classes want to generate breeze schema
     */
    @Parameter
    private List<String> classes;

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Parameter(defaultValue = "false")
    private Boolean withStaticField;

    private ClassRealm classRealm;
    private Map<String, Class> beanClasses = new HashMap<>();
    private Map<String, Boolean> processResult = new HashMap<>();
    private String srcBreezeDir;
    private String targetBreezeDir;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        //TODO
        // 项目中的类可以根据源文件是否变更，确定是否需要rebuild。间接的看是否可以把版本号写在注释中。
        getLog().info("breeze-maven-plugin:start generating schema");
        try {
            Set<String> classNames = getInterfaces();
            if (classes != null && !classes.isEmpty()) {
                //TODO regexp support
                classNames.addAll(classes);
            }
            addToBeanClasses(classNames);
            if (!beanClasses.isEmpty()) {
                srcBreezeDir = project.getBasedir().getAbsolutePath() + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + BREEZE_DIR;
                targetBreezeDir = project.getBasedir().getAbsolutePath() + File.separator + "target" + File.separator + "classes" + File.separator + BREEZE_DIR;
                FileUtils.mkdir(srcBreezeDir);
                FileUtils.mkdir(targetBreezeDir);
                for (Class clz : beanClasses.values()) {
                    generateSchema(clz);
                }
            }
        } catch (MojoFailureException failException) {
            throw failException;
        } catch (Exception e) {
            getLog().warn("breeze-maven-plugin: generate schema fail.", e);
        }
    }

    private Set<String> getInterfaces() throws IOException, XmlPullParserException {
        Set<String> interfaces = new HashSet<>();
        List<File> list = FileUtils.getFiles(new File(xmlPath), includes, excludes);
        for (File file : list) {
            Xpp3Dom dom = Xpp3DomBuilder.build(new FileInputStream(file), null); // input stream always be closed by builder after build
            Xpp3Dom[] referers = dom.getChildren("motan:referer");
            for (Xpp3Dom ref : referers) {
                if (ref.getAttribute("interface") != null) {
                    interfaces.add(ref.getAttribute("interface"));
                }
                if (ref.getAttribute("serviceInterface") != null) {
                    interfaces.add(ref.getAttribute("serviceInterface"));
                }
            }
        }
        return interfaces;
    }

    private void addToBeanClasses(Set<String> classNames) throws MalformedURLException, DependencyResolutionRequiredException {
        if (!classNames.isEmpty()) {
            initClassRealm();
            for (String className : classNames) {
                try {
                    Class clz = classRealm.loadClass(className);
                    if (clz.isInterface()) {
                        for (Method method : clz.getMethods()) {
                            for (Type param : method.getGenericParameterTypes()) {
                                needProcess(param, beanClasses);
                            }
                            needProcess(method.getGenericReturnType(), beanClasses);
                        }
                        //TODO process breeze service
                    } else {
                        needProcess(clz, beanClasses);
                    }
                } catch (ClassNotFoundException ignore) {
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void needProcess(Type type, Map<String, Class> needProcessClasses) {
        if (type == null) {
            return;
        }
        Class clz;
        if (type instanceof Class) {
            clz = (Class) type;
        } else if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            clz = (Class) pt.getRawType();
            for (Type tp : pt.getActualTypeArguments()) {// check all generic type
                needProcess(tp, needProcessClasses);
            }
        } else {
            return;
        }
        // common type
        if (clz.isInterface() || ClassUtils.isPrimitiveOrWrapper(clz)
                || clz == Object.class || clz == String.class
                || Map.class.isAssignableFrom(clz) || List.class.isAssignableFrom(clz)
                || Set.class.isAssignableFrom(clz)) {
            return;
        }
        if (clz.isArray()) { // check array component type
            needProcess(clz.getComponentType(), needProcessClasses);
            return;
        }

        String name = getCleanName(clz);
        if (beanClasses.containsKey(name) || needProcessClasses.containsKey(name)) {// already add to process.
            return;
        }
        //breeze
        if (Message.class.isAssignableFrom(clz)) {
            return;
        }
        // already has custom serializer
        Serializer serializer = Breeze.DefaultSerializerFactory.getSerializerClassByName(name);
        if (serializer != null) {
            getLog().info(name + " already has custom serializer " + serializer.getClass().getName());
            return;
        }
        serializer = Breeze.getSerializer(name);
        if (serializer != null && !(serializer instanceof CommonSerializer) && !(serializer instanceof EnumSerializer)) {
            getLog().info(name + " already has custom serializer " + serializer.getClass().getName());
            return;
        }
        if (clz.getSuperclass() != null) {
            String superClz = clz.getSuperclass().getName();
            //protobuf
            if ("com.google.protobuf.GeneratedMessage".equals(superClz) || "com.google.protobuf.GeneratedMessageV3".equals(superClz)) {
                getLog().info("skip protobuf message " + clz.getName());
                return;
            }
        }
        // check whether can generate or not
        if (!clz.isEnum()) {
            try {
                new CommonSerializer(clz);
            } catch (BreezeException be) {
                getLog().warn(clz.getName() + " can not create commonSerializer. info:" + be.getMessage());
                return;
            }
        }

        // already has schema in other jar
        try {
            Enumeration<URL> enumeration = classRealm.getResources(SchemaLoader.PATH + name + SchemaLoader.SUFFIX);
            URL elem = null;
            while (enumeration.hasMoreElements()) {
                elem = enumeration.nextElement();
                if ("jar".equals(elem.toURI().getScheme())) {
                    getLog().info(name + " already has schema in jar " + elem.toURI());
                    return;
                }
            }
        } catch (Exception ignore) {
        }
        needProcessClasses.put(name, clz);
    }

    private void generateSchema(Class beanClass) throws MojoFailureException {
        String name = getCleanName(beanClass);
        if (processResult.containsKey(name)) {
            return;
        }
        processResult.put(name, false); //for prevent loop processing, must add class name at begin
        boolean result = false;
        String fileName = srcBreezeDir + File.separator + name + ".breeze";
        Schema oldSchema = null;
        try {
            String jarFileName = getJarFileName(beanClass);
            if (FileUtils.fileExists(fileName)) {
                String content = FileUtils.fileRead(fileName, "UTF-8");
                if (!content.startsWith(HEADER_PREFIX) // not auto generated by plugin
                        || (jarFileName != null && content.contains(jarFileName))) {  // or class in jar with same version
                    processResult.put(name, true);
                    getLog().info("skip update schema for " + name + ", because old schema is not auto generated by plugin, or with same version.");
                    return; // just skip
                }
                oldSchema = SchemaUtil.parseSchema(content);
            }
            Schema schema = buildSchema(beanClass);
            if (oldSchema != null) {
                checkCompatible(schema, oldSchema);
            }
            String header = HEADER_PREFIX;
            if (jarFileName != null) {
                header += " jar:" + jarFileName;
            }
            String content = header + "\n" + SchemaUtil.toFileContent(schema);// add generate comment header
            FileUtils.fileWrite(fileName, "UTF-8", content);
            FileUtils.fileWrite(targetBreezeDir + File.separator + name + ".breeze", "UTF-8", content);
            result = true;
        } catch (MojoFailureException failException) {
            throw failException;
        } catch (Exception e) {
            getLog().warn("breeze-maven-plugin: generate schema fail. class: " + name + ", error: " + e.getMessage());
        }
        processResult.put(name, result);// update process status at finish
        getLog().info("breeze-maven-plugin: generate schema:" + name + ":" + result);
    }

    private String getJarFileName(Class clz) {
        String file = clz.getProtectionDomain().getCodeSource().getLocation().getFile();
        if (file.endsWith(".jar")) {
            return file.substring(file.lastIndexOf(File.separator) + 1);
        }
        return null;
    }

    private void initClassRealm() throws DependencyResolutionRequiredException, MalformedURLException {
        classRealm = ((PluginDescriptor) getPluginContext().get("pluginDescriptor")).getClassRealm();
        Set<String> classpathElements = new HashSet<>();
        classpathElements.add(project.getBuild().getOutputDirectory());
        classpathElements.addAll(project.getCompileClasspathElements());
        classpathElements.addAll(project.getRuntimeClasspathElements());
        for (String cp : classpathElements) {
            classRealm.addURL(new File(cp).toURI().toURL());
        }
    }

    // reserve index for auto compatible
    private Schema buildSchema(Class clz) throws MojoFailureException, BreezeException {
        if (clz.isEnum()) {
            return buildEnumSchema(clz);
        }
        String name = getCleanName(clz);
        Schema schema = Schema.newSchema(name);
        if (clz.getName().contains("$")) {
            schema.setJavaName(clz.getName());
        }
        int index = 1;
        int round = 1;
        Method[] methods = clz.getMethods();
        Field[] fields;
        Set<String> addedFields = new HashSet<>();
        do {
            fields = clz.getDeclaredFields();
            for (Field field : fields) {
                if (checkField(field)) {
                    if (Modifier.isPublic(field.getModifiers())) {
                        addField(schema, field, index++, addedFields);
                    } else if (methods.length > 0) {
                        for (Method method : methods) {// field with getter method
                            if (method.getName().equalsIgnoreCase("get" + field.getName())
                                    || ((field.getType() == boolean.class)
                                    && method.getName().equalsIgnoreCase("is" + field.getName()))) {
                                addField(schema, field, index++, addedFields);
                                break;
                            }
                        }
                    }
                }
            }
            clz = clz.getSuperclass();
            if (fields.length < 100) { // for compatible
                index = round * 100;
            } else {// TODO fields over 100 and has super class fields
                round++;
                index = round * 100;
            }
            round++;
        } while (clz != null && clz != Object.class);
        return schema;
    }

    private Schema buildEnumSchema(Class clz) throws BreezeException, MojoFailureException {
        String name = getCleanName(clz);
        Schema schema = Schema.newSchema(name);
        if (clz.getName().contains("$")) {
            schema.setJavaName(clz.getName());
        }
        List<Field> targetFields = new ArrayList<>();
        for (Field field : clz.getDeclaredFields()) {
            int modifier = field.getModifiers();
            if (Modifier.isPrivate(modifier) && !Modifier.isStatic(modifier) && !"$VALUES".equals(field.getName())) {
                field.setAccessible(true);
                targetFields.add(field);
            }
        }
        Object[] enums = clz.getEnumConstants();
        if (targetFields.isEmpty()) {
            schema.setEnum(true);
            for (Object obj : enums) {
                schema.addEnumValue(((Enum) obj).ordinal(), ((Enum) obj).name());
            }
        } else if (targetFields.size() == 1 && (targetFields.get(0).getType() == int.class || targetFields.get(0).getType() == Integer.class)) {
            schema.setEnum(true);
            for (Object obj : enums) {
                try {
                    schema.addEnumValue((Integer) targetFields.get(0).get(obj), ((Enum) obj).name());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        } else { // as message
            schema.putField(1, "enumValue", "string"); // add enum name
            Set<String> addedFields = new HashSet<>();
            int index = 2;
            for (Field field : targetFields) {
                addField(schema, field, index++, addedFields);
            }
        }
        return schema;
    }

    private void checkCompatible(Schema s1, Schema s2) throws MojoFailureException {
        if (!s1.getName().equals(s2.getName())) {
            throw new MojoFailureException("breeze field name not compatible with old version. schema:" + s1.getName());
        }
        if (s1.isEnum()) {
            Map<Integer, String> values1 = s1.getEnumValues();
            Map<Integer, String> values2 = s2.getEnumValues();
            for (Map.Entry<Integer, String> entry : values1.entrySet()) {
                if (values2.get(entry.getKey()) != null && !values2.get(entry.getKey()).equals(entry.getValue())) {
                    throw new MojoFailureException("breeze enum value not compatible with old version. schema:" + s1.getName() + ", enum:" + entry.getValue() + ", index:" + entry.getKey() + ", old value:" + values2.get(entry.getKey()));
                }
            }

        } else {// message
            for (Schema.Field field : s1.getFields().values()) {
                Schema.Field otherField = s2.getFieldByName(field.getName());
                if (otherField != null && otherField.getIndex() != field.getIndex()) {
                    throw new MojoFailureException("breeze field index not compatible with old version. schema:" + s1.getName() + ", field:" + field.getName() + ", index:" + field.getIndex() + ", old index:" + otherField.getIndex());
                }
            }
        }
    }

    private boolean checkField(Field field) {
        return !Modifier.isFinal(field.getModifiers())
                && (withStaticField || !Modifier.isStatic(field.getModifiers()));
    }

    private void addField(Schema schema, Field field, int index, Set<String> addedFields) throws MojoFailureException {
        if (!addedFields.contains(field.getName())) {// skip duplicate filed, such as super class field.
            Map<String, Class> associateClasses = new HashMap<>();
            needProcess(field.getGenericType(), associateClasses);
            for (Class clz : associateClasses.values()) {
                generateSchema(clz);
            }
            try {
                schema.putField(index, field.getName(), getBreezeType(field.getGenericType(), schema.getName().substring(0, schema.getName().lastIndexOf("."))));
                addedFields.add(field.getName());
            } catch (BreezeException e) {
                getLog().warn("breeze-maven-plugin: add field fail. class:" + schema.getName() + ", field name:" + field.getName() + ", field index:" + index + "， error: " + e.getMessage());
            }
        }
    }

    private String getBreezeType(Type type, String packageName) throws BreezeException {
        if (type == Object.class) {
            throw new BreezeException("can not support Object.class as breeze type.");
        }
        ParameterizedType pt = null;
        Class<?> clz;
        if (type instanceof Class) {
            clz = (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            pt = (ParameterizedType) type;
            clz = (Class<?>) pt.getRawType();
        } else {
            throw new BreezeException("unsupported type :" + type);
        }
        if (clz == String.class || clz == char.class || clz == Character.class || clz == void.class) {
            return "string";
        }

        if (clz == Byte.class || clz == byte.class) {
            return "byte";
        }

        if (clz == Boolean.class || clz == boolean.class) {
            return "bool";
        }

        if (clz == Short.class || clz == short.class) {
            return "int16";
        }

        if (clz == Integer.class || clz == int.class) {
            return "int32";
        }

        if (clz == Long.class || clz == long.class) {
            return "int64";
        }

        if (clz == Float.class || clz == float.class) {
            return "float32";
        }

        if (clz == Double.class || clz == double.class) {
            return "float64";
        }

        if (Map.class.isAssignableFrom(clz)) {
            if (pt == null || pt.getActualTypeArguments().length != 2) {
                throw new BreezeException("class must has two argument generic type when the class is a subclass of map. type:" + type);
            }
            return "map<" + getBreezeType(pt.getActualTypeArguments()[0], packageName) + ", " + getBreezeType(pt.getActualTypeArguments()[1], packageName) + ">";
        }

        if (clz.isArray()) {
            if (clz.getComponentType() == byte.class) {
                return "bytes";
            }
            return "array<" + getBreezeType(clz.getComponentType(), packageName) + ">";
        }

        if (Collection.class.isAssignableFrom(clz)) {
            if (pt == null || pt.getActualTypeArguments().length != 1) {
                throw new BreezeException("class must has a argument generic type when the class is a subclass of Collection. type:" + type);
            }
            return "array<" + getBreezeType(pt.getActualTypeArguments()[0], packageName) + ">";
        }
        // as message
        String name = getCleanName(clz);
        if (name.startsWith(packageName + ".") && !name.substring(packageName.length() + 1).contains(".")) { // same package
            return name.substring(packageName.length() + 1);
        }
        return name;
    }

    private String getCleanName(Class clz) {
        String name = clz.getName();
        if (name.contains("$")) {
            name = name.replaceAll("\\$", "");
        }
        return name;
    }

}
