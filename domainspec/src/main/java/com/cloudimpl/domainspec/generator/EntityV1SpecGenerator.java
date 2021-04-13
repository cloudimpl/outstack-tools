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
import com.cloudimpl.codegen4j.FunctionBlock;
import com.cloudimpl.codegen4j.JavaFile;
import com.cloudimpl.codegen4j.Var;
import com.cloudimpl.domainspec.DomainEntitySpecV1;
import com.cloudimpl.domainspec.EntitySpecDecoderV1;
import com.cloudimpl.domainspec.EntitySpecV1;
import com.cloudimpl.domainspec.FieldDefRefV1;
import com.cloudimpl.domainspec.FieldSpecV1;
import com.cloudimpl.domainspec.GsonCodec;
import com.google.gson.JsonObject;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 *
 * @author nuwan
 */
public class EntityV1SpecGenerator extends SpecGenerator {

    static {
        GsonCodec.registerTypeAdaptor(EntitySpecV1.class, () -> new EntitySpecDecoderV1(), null);
    }
    private final FieldV1SpecGenerator fieldSpec;
    private final Supplier<EventV1SpecGenerator> eventSpec;
    private DomainEntitySpecV1 spec;
    private Map<String, EntitySpecV1.Template> templates;

    public EntityV1SpecGenerator(DomainSpecCodeGenerator generator, FieldV1SpecGenerator fieldSpec, Supplier<EventV1SpecGenerator> eventSpec) {
        super(generator);
        this.fieldSpec = fieldSpec;
        this.eventSpec = eventSpec;
    }

    @Override
    public void resolve(JsonObject json) {
        spec = GsonCodec.decode(DomainEntitySpecV1.class, json.toString());
        templates = spec.getSpec().getTemplates().stream().peek(this::resolveMissingFields).collect(Collectors.toMap(t -> t.getMetadata().getType(), Function.identity()));
    }

    private void resolveMissingFields(EntitySpecV1.Template template) {
        template.getFieldRefs().stream().filter(def -> def.getName().equals(template.getMetadata().getId())).findAny().ifPresentOrElse(f -> {
        }, () -> template.getFieldRefs().add(new FieldDefRefV1(template.getMetadata().getId(), null)));
    }

    public Optional<EntitySpecV1.Template> getTemplate(String type) {
        return Optional.ofNullable(templates.get(type));
    }

    public DomainEntitySpecV1 getSpec() {
        return spec;
    }

    @Override
    public void execute() {
//        Map<String, FieldSpecV1.FieldDef> map = spec.getSpec().getTemplates().stream()
//                .flatMap(temp -> temp.getFieldRefs().stream())
//                .map(fr -> fieldSpec.getFieldDef(fr.getName()).orElseThrow().merge(fr))
//                .collect(Collectors.toMap(fd -> fd.getName(), fd -> fd));

        spec.getSpec().getTemplates().stream().map(temp -> createEntity(spec, temp)).forEach(this::generateFile);

    }

    private ClassBlock createEntity(DomainEntitySpecV1 spec, EntitySpecV1.Template template) {

        Map<String, FieldSpecV1.FieldDef> map = template.getFieldRefs().stream()
                .map(fr -> fieldSpec.getFieldDef(fr.getName()).orElseThrow().merge(fr))
                .collect(Collectors.toMap(fd -> fd.getName(), fd -> fd));
        if (!map.containsKey(template.getMetadata().getId())) {
            map.put(template.getMetadata().getId(), fieldSpec.getFieldDef(template.getMetadata().getId()).orElseThrow());
        }
        EntitySpecV1.Template rootTemplate = null;
        if (!template.getMetadata().isRoot() && !map.containsKey(template.getMetadata().rootEntity().orElseThrow())) {
            rootTemplate = getTemplate(template.getMetadata().rootEntity().orElseThrow()).orElseThrow();
            map.put(rootTemplate.getMetadata().getId(), fieldSpec.getFieldDef(rootTemplate.getMetadata().getId()).orElseThrow());
        }
        ClassBuilder builder = new ClassBuilder();
        ClassBlock cb = builder.createClass(template.getMetadata().getType())
                .withPackageName(spec.getMetaData().orElseThrow().getNamespace().orElseThrow() + template.getMetadata().getModule().map(s -> "." + s).orElse(""))
                .implement(template.getMetadata().isRoot() ? generator.getRootEntityBaseName() : generator.getChildEntityBaseName())
                .withImports(template.getMetadata().isRoot() ? generator.getSpecPackageName() + "." + generator.getRootEntityBaseName() : generator.getSpecPackageName() + "." + generator.getChildEntityBaseName())
                .withAccess(AccessLevel.PUBLIC);
        createConstructor(cb, template);
        if (template.getMetadata().isTenant()) {
            cb.implement(generator.getTenantBaseName()).withImports(generator.getSpecPackageName() + "." + generator.getTenantBaseName());
            generateTenantIdFunction(cb);

        }
        map.values().stream().filter(fd -> fd.getNamespace().isPresent()).forEach(fd -> cb.withImports(fd.getNamespace().get() + "." + fd.getType()));
        EntitySpecV1.Template rootTemp = rootTemplate;
        map.values().stream().forEach(fd -> {
            Var v = cb.var(fd.getType(), fd.getName()).withAccess(AccessLevel.PRIVATE);
            if (fd.getName().equals(template.getMetadata().getId())) {
                v.withFinal();
            }
            if(rootTemp != null && fd.getName().equals(rootTemp.getMetadata().getId()))
            {
                v.withFinal();
            }
            v.end();
            cb.createGetter(v);
            if (fd.getName().equals(template.getMetadata().getId())) {
                generateIdFunction(cb, template.getMetadata().getId());
            }
        });
        if (rootTemplate != null) {
            generateRootIdFunction(cb, rootTemplate.getMetadata().getId());
            generateRootTypeFunction(cb, rootTemplate.getMetadata().getType());
        }
        template.getLogic().forEach(apply -> generateApplyFunction(cb, apply));
        return cb;
    }

    private void createConstructor(ClassBlock cb, EntitySpecV1.Template template) {
        List<String> params = new LinkedList<>();
        template.getMetadata().rootEntity().map(s -> getTemplate(s).orElseThrow()).ifPresent(t -> {
            cb.withImports(getSpec().getTemplateNamespace(t.getMetadata()) + "." + t.getMetadata().getType());
            params.add("String " + t.getMetadata().getId());
        });
        params.add("String " + template.getMetadata().getId());
        if (template.getMetadata().isTenant()) {
            params.add("String tenantId");
        }
        ConstructorBlock ctr = cb.createConstructor(params.stream().toArray(String[]::new)).withAccess(AccessLevel.PUBLIC);
        params.stream().map(s -> s.split(" ")[1]).forEach(s -> {
            ctr.stmt().append("this." + s + " = " + s).end();
        });
    }

    private void generateIdFunction(ClassBlock cb, String idField) {
        FunctionBlock fb = cb.createFunction("id").withReturnType("String")
                .withAccess(AccessLevel.PUBLIC)
                .withAnnotation(Override.class.getSimpleName());
        fb.withReturnStatment(idField).end();
    }

    private void generateRootIdFunction(ClassBlock cb, String idField) {
        FunctionBlock fb = cb.createFunction("rootId").withReturnType("String")
                .withAccess(AccessLevel.PUBLIC)
                .withAnnotation(Override.class.getSimpleName());
        fb.withReturnStatment(idField).end();
    }

    private void generateRootTypeFunction(ClassBlock cb, String rootType) {
        FunctionBlock fb = cb.createFunction("rootType").withReturnType("Class<" + rootType + ">")
                .withAccess(AccessLevel.PUBLIC)
                .withAnnotation(Override.class.getSimpleName());
        fb.withReturnStatment(rootType + ".class").end();
    }

    private void generateApplyFunction(ClassBlock cb, EntitySpecV1.Template.ApplyStmt apply) {
        cb.withImports(eventSpec.get().getSpec().getTemplateNamespace(eventSpec.get().getTemplate(apply.getEvt()).orElseThrow().getMetadata()) + "." + apply.getEvt());
        FunctionBlock fb = cb.createFunction("apply")
                .withArgs(eventSpec.get().getTemplate(apply.getEvt()).orElseThrow().getMetadata().getType() + " evt")
                .withAccess(AccessLevel.PUBLIC);
        for (String stmt : apply.getStmt()) {
            String st = stmt.substring(0, stmt.lastIndexOf(".")) + "." + FunctionBlock.createName("get", stmt.substring(stmt.lastIndexOf(".") + 1)) + "()";
            fb.stmt().append(st).end();
        }
    }

    private void generateTenantIdFunction(ClassBlock cb) {
        Var v = cb.var("String", "tenantId").withAccess(AccessLevel.PRIVATE).withFinal().end();
        cb.createGetter(v).withAnnotation(Override.class.getSimpleName());
    }

    private void generateFile(ClassBlock cb) {
        JavaFile file = JavaFile.wrap(cb);
        file.writeTo(new File(generator.getCodeGenFolder()));
    }
}
