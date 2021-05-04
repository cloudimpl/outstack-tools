/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.domainspec.generator;

import com.cloudimpl.codegen4j.AccessLevel;
import com.cloudimpl.codegen4j.ClassBlock;
import com.cloudimpl.codegen4j.ClassBuilder;
import com.cloudimpl.codegen4j.ConstructorBlock;
import com.cloudimpl.codegen4j.EnumBlock;
import com.cloudimpl.codegen4j.JavaFile;
import com.cloudimpl.codegen4j.Var;
import com.cloudimpl.domainspec.DomainTypeSpecV1;
import com.cloudimpl.domainspec.FieldSpecV1;
import com.cloudimpl.domainspec.TypeSpecDecoderV1;
import com.cloudimpl.domainspec.TypeSpecV1;
import com.cloudimpl.domainspec.GsonCodec;
import com.google.gson.JsonObject;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 * @author nuwan
 */
public class TypeV1SpecGenerator extends SpecGenerator {

    static {
        GsonCodec.registerTypeAdaptor(TypeSpecV1.class, () -> new TypeSpecDecoderV1(), null);
    }
    private final FieldV1SpecGenerator fieldSpec;
    private Map<String, TypeSpecV1.Template> templates;
    private DomainTypeSpecV1 spec;

    public TypeV1SpecGenerator(DomainSpecCodeGenerator generator, FieldV1SpecGenerator fieldSpec) {
        super(generator);
        this.fieldSpec = fieldSpec;
    }

    @Override
    public void execute() {
        if (spec != null) {
            templates = spec.getSpec().getTemplates().stream().collect(Collectors.toMap(t -> t.getMetdata().getName(), t -> t));
            spec.getSpec().getTemplates().stream().map(temp -> createType(spec, temp)).forEach(this::generateFile);
        }

    }

    @Override
    public void resolve(JsonObject json) {
        spec = GsonCodec.decode(DomainTypeSpecV1.class, json.toString());
        templates = spec.getSpec().getTemplates().stream().collect(Collectors.toMap(t -> t.getMetdata().getName(), t -> t));
    }

    public DomainTypeSpecV1 getSpec() {
        return spec;
    }

    public Optional<TypeSpecV1.Template> getTemplate(String name) {
        return Optional.ofNullable(this.templates.get(name));
    }

    private ClassBlock createObject(DomainTypeSpecV1 spec, TypeSpecV1.Template template) {
        Map<String, FieldSpecV1.FieldDef> map = spec.getSpec().getTemplates().stream()
                .filter(temp -> temp.getMetdata().getType().equals("object"))
                .flatMap(temp -> Arrays.asList(temp.getValues()).stream())
                .map(fr -> fieldSpec.getFieldDef(fr).orElseThrow())
                .collect(Collectors.toMap(fd -> fd.getName(), fd -> fd));
        ClassBuilder builder = new ClassBuilder();
        ClassBlock cb = builder.createClass(template.getMetdata().getName())
                .withPackageName(spec.getTemplateNamespace(template.getMetdata()))
                .withAccess(AccessLevel.PUBLIC);
        String[] params = Arrays.asList(template.getValues()).stream().map(ref -> map.get(ref)).map(fd -> fd.getType() + " " + fd.getName()).toArray(String[]::new);
        ConstructorBlock ctor = cb.createConstructor(params).withAccess(AccessLevel.PUBLIC);
        Arrays.asList(template.getValues()).stream().map(ref -> map.get(ref)).forEach(fd -> {
            Var v = cb.var(fd.getType(), fd.getName()).withAccess(AccessLevel.PRIVATE).withFinal().end();
            ctor.stmt().append("this." + fd.getName() + " = " + fd.getName()).end();
            cb.createGetter(v);
        });
        return cb;
    }

    private ClassBlock createType(DomainTypeSpecV1 spec, TypeSpecV1.Template template) {
        if (template.getMetdata().getType().equals("enum")) {
            return createEnum(spec, template);
        } else {
            return createObject(spec, template);
        }
    }

    private ClassBlock createEnum(DomainTypeSpecV1 spec, TypeSpecV1.Template template) {
        ClassBuilder builder = new ClassBuilder();
        EnumBlock eb = builder.createEnum(template.getMetdata().getName())
                .withPackageName(spec.getTemplateNamespace(template.getMetdata()))
                .withAccess(AccessLevel.PUBLIC);
        Arrays.asList(template.getValues()).stream().limit(template.getValues().length - 1).forEach(val -> eb.enumStmt(val).done());
        Arrays.asList(template.getValues()).stream().skip(template.getValues().length - 1).forEach(val -> eb.enumStmt(val).end());
        return eb;
    }

    private void generateFile(ClassBlock cb) {
        JavaFile file = JavaFile.wrap(cb);
        file.writeTo(new File(generator.getCodeGenFolder()));
    }

}
