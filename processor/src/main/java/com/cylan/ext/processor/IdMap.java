package com.cylan.ext.processor;

import com.cylan.ext.annotations.DpAnnotation;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;

/**
 * Created by cylan-hunt on 17-1-16.
 */

public class IdMap {

    private static final String ID_2_CLASS_MAP = "ID_2_CLASS_MAP";
    private static final String NAME_2_ID_MAP = "NAME_2_ID_MAP";
    private static final String ID_2_NAME_MAP = "ID_2_NAME_MAP";

    /**
     * @param roundEnv
     * @param processingEnv
     */
    public void brewIdMap(RoundEnvironment roundEnv, ProcessingEnvironment processingEnv) {
        Set<? extends Element> set = roundEnv.getElementsAnnotatedWith(DpAnnotation.class);
        //static block
        Map<Integer, String> mapVerify = new HashMap<>();
        Map<String, Integer> mapVerify_ = new HashMap<>();
        Map<String, Integer> fieldList = new HashMap<>();
        CodeBlock.Builder blockNameId = CodeBlock.builder();
        for (Element element : set) {
            DpAnnotation test = element.getAnnotation(DpAnnotation.class);
            final String name = element.getSimpleName().toString().toLowerCase();
            blockNameId.addStatement(NAME_2_ID_MAP + ".put($S,$L)", name, test.msgId());
            if (mapVerify.containsKey(test.msgId())) {
                throw new IllegalArgumentException("err happen.: 相同的key: " + name);
            }
            mapVerify.put(test.msgId(), name);

            if (mapVerify_.containsKey(name)) {
                throw new IllegalArgumentException("err happen.: 相同的value: " + name);
            }
            mapVerify_.put(name, test.msgId());
            fieldList.put(element.getSimpleName().toString(), test.msgId());
        }
        //static block
        CodeBlock.Builder blockId2NameClass = CodeBlock.builder();
        for (Element element : set) {
            DpAnnotation test = element.getAnnotation(DpAnnotation.class);
            final int msgId = test.msgId();
            blockId2NameClass.addStatement(ID_2_NAME_MAP + ".put($L,$S)", msgId, element.getSimpleName().toString().toLowerCase());
        }


        //static block
        CodeBlock.Builder blockIdClass = CodeBlock.builder();
        for (Element element : set) {
            DpAnnotation test = element.getAnnotation(DpAnnotation.class);
            final int msgId = test.msgId();
            TypeMirror clazzType = null;
            try {
                test.clazz();
            } catch (MirroredTypeException mte) {
                clazzType = mte.getTypeMirror();
            }
            blockIdClass.addStatement(ID_2_CLASS_MAP + ".put($L,$L)", msgId, clazzType + ".class");
        }

        TypeSpec.Builder fileBuilder =
                TypeSpec.classBuilder("DpMsgMap")
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addField(getIdClassMap().build())
                        .addField(getNameIdMap().build())
                        .addField(getId2NameMap().build())
                        .addStaticBlock(blockNameId.build())
                        .addStaticBlock(blockId2NameClass.build())
                        .addStaticBlock(blockIdClass.build());
        addFinalStringField(fileBuilder, fieldList);
        addFinalIntField(fileBuilder, fieldList);
        TypeSpec msgIdMap = fileBuilder.build();
//                        .build();
//        package
        JavaFile javaFile = JavaFile.builder("com.cylan.jiafeigou.dp", msgIdMap)
                .build();

        try {
            javaFile.writeTo(processingEnv.getFiler());
        } catch (Exception e) {
//            e.printStackTrace();
        }

    }

    /**
     * Map<String,Integer></>
     *
     * @return
     */
    private FieldSpec.Builder getNameIdMap() {
//        System.out.println("hunt: " + WildcardTypeName.subtypeOf(Object.class).toString());
        ParameterizedTypeName type = ParameterizedTypeName.get(Map.class,
                String.class,
                Integer.class);
        //field
        FieldSpec.Builder field = FieldSpec.builder(type,
                NAME_2_ID_MAP,
                Modifier.PUBLIC,
                Modifier.STATIC,
                Modifier.FINAL);
        field.initializer("new $T<$T,$T>()", HashMap.class, String.class, Integer.class);
        return field;
    }

    /**
     * Map<Integer,Class<?>></>
     *
     * @return
     */
    private FieldSpec.Builder getIdClassMap() {

        ParameterizedTypeName typeName = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(Integer.class),
                ParameterizedTypeName.get(ClassName.get(Class.class), TypeVariableName.get("?")));
        FieldSpec.Builder builder = FieldSpec.builder(typeName,
                ID_2_CLASS_MAP,
                Modifier.PUBLIC,
                Modifier.STATIC, Modifier.FINAL);
        builder.initializer("new $T<$T, $L>()", HashMap.class, Integer.class, "Class<?>");
        return builder;
    }

    private FieldSpec.Builder getId2NameMap() {
        ParameterizedTypeName type = ParameterizedTypeName.get(Map.class,
                Integer.class,
                String.class);
        //field
        FieldSpec.Builder field = FieldSpec.builder(type,
                ID_2_NAME_MAP,
                Modifier.PUBLIC,
                Modifier.STATIC,
                Modifier.FINAL);
        field.initializer("new $T<$T,$T>()", HashMap.class, Integer.class, String.class);
        return field;
    }

    private void addFinalStringField(TypeSpec.Builder typeSpec, Map<String, Integer> fieldMap) {
        Iterator<String> i = fieldMap.keySet().iterator();
        while (i.hasNext()) {
            String name = i.next();
            FieldSpec.Builder field = FieldSpec.builder(String.class,
                    name + "_" + fieldMap.get(name),
                    Modifier.PUBLIC,
                    Modifier.STATIC,
                    Modifier.FINAL);
            field.initializer("$S", name.toLowerCase());
            typeSpec.addField(field.build());
        }
    }

    private void addFinalIntField(TypeSpec.Builder typeSpec, Map<String, Integer> fieldMap) {
        Iterator<String> i = fieldMap.keySet().iterator();
        while (i.hasNext()) {
            String name = i.next();
            FieldSpec.Builder field = FieldSpec.builder(int.class,
                    "ID_" + fieldMap.get(name) + "_" + name,
                    Modifier.PUBLIC,
                    Modifier.STATIC,
                    Modifier.FINAL);
            field.initializer("$L", fieldMap.get(name));
            typeSpec.addField(field.build());
        }
    }
}
