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
import com.cloudimpl.domainspec.DomainEventSpecV1;
import com.cloudimpl.domainspec.EntitySpecV1;
import com.cloudimpl.domainspec.EventSpecDecoderV1;
import com.cloudimpl.domainspec.EventSpecV1;
import com.cloudimpl.domainspec.FieldDefRefV1;
import com.cloudimpl.domainspec.FieldSpecV1;
import com.cloudimpl.domainspec.GsonCodec;
import com.cloudimpl.domainspec.Util;
import com.google.gson.JsonObject;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

/**
 *
 * @author nuwan
 */
public class EventV1SpecGenerator extends SpecGenerator {

    static {
        GsonCodec.registerTypeAdaptor(EventSpecV1.class, () -> new EventSpecDecoderV1(), null);
    }
    private final FieldV1SpecGenerator fieldSpec;
    private Map<String, EventSpecV1.Template> templates;
    private DomainEventSpecV1 spec;
    private final Supplier<EntityV1SpecGenerator> entityGen;

    public EventV1SpecGenerator(DomainSpecCodeGenerator generator, FieldV1SpecGenerator fieldSpec, Supplier<EntityV1SpecGenerator> entityGen) {
        super(generator);
        this.fieldSpec = fieldSpec;
        this.entityGen = entityGen;
    }

    @Override
    public void execute() {
//        Map<String, FieldSpecV1.FieldDef> map = spec.getSpec().getTemplates().stream()
//                .flatMap(temp -> temp.getFieldRefs().stream())
//                .map(fr -> fieldSpec.getFieldDef(fr.getName()).orElseThrow().merge(fr))
//                .collect(Collectors.toMap(fd -> fd.getName(), fd -> fd));
        spec.getSpec().getTemplates().stream().map(temp -> createEvent(spec, temp)).forEach(this::generateFile);

    }

    @Override
    public void resolve(JsonObject json) {
        spec = GsonCodec.decode(DomainEventSpecV1.class, json.toString());
        this.templates = spec.getSpec().getTemplates().stream().peek(this::resolveMissingFields).collect(Collectors.toMap(t -> t.getMetadata().getType(), Function.identity()));
    }

    private void resolveMissingFields(EventSpecV1.Template template) {
        EntitySpecV1.Template entityTemplate = this.entityGen.get().getTemplate(template.getMetadata().getOwner()).orElseThrow(() -> Util.$("missing entity:" + template.getMetadata().getOwner() + " for event:" + template.getMetadata().getType()));
        if (!template.hasField(entityTemplate.getMetadata().getId())) {
            template.getFieldRefs().add(entityTemplate.getFieldRef(entityTemplate.getMetadata().getId()).orElseThrow());
        }
        if (!entityTemplate.getMetadata().isRoot()) {
            EntitySpecV1.Template rootTemplate = this.entityGen.get().getTemplate(entityTemplate.getMetadata().rootEntity().orElseThrow()).orElseThrow();
            if (!template.hasField(rootTemplate.getMetadata().getId())) {
                template.getFieldRefs().add(rootTemplate.getFieldRef(rootTemplate.getMetadata().getId()).orElseThrow());
            }
        }

    }

    public DomainEventSpecV1 getSpec() {
        return spec;
    }

    public Optional<EventSpecV1.Template> getTemplate(String name) {
        return Optional.ofNullable(this.templates.get(name));
    }

    private ClassBlock createEvent(DomainEventSpecV1 spec, EventSpecV1.Template template) {
        Map<String, FieldSpecV1.FieldDef> map = template.getFieldRefs().stream()
                .map(fr -> fieldSpec.getFieldDef(fr.getName()).orElseThrow().merge(fr))
                .collect(Collectors.toMap(fd -> fd.getName(), fd -> fd));

        Set<String> ids = new HashSet<>();
        EntitySpecV1.Template entityTemplate = this.entityGen.get().getTemplate(template.getMetadata().getOwner()).orElseThrow();
        ids.add(entityTemplate.getMetadata().getId());
        if (!entityTemplate.getMetadata().isRoot()) {
            EntitySpecV1.Template rootTemplate = this.entityGen.get().getTemplate(entityTemplate.getMetadata().rootEntity().orElseThrow()).orElseThrow();
            ids.add(rootTemplate.getMetadata().getId());
        }
        ClassBuilder builder = new ClassBuilder();
        ClassBlock cb = builder.createClass(template.getMetadata().getType())
                .withPackageName(spec.getMetaData().orElseThrow().getNamespace().orElseThrow())
                .extend(generator.getEventBaseName() + "<" + entityTemplate.getMetadata().getType() + ">")
                .withImports(generator.getSpecPackageName() + "." + generator.getEventBaseName())
                .withImports(generator.getSpecPackageName() + "." + generator.getEventBaseName())
                .withImports(NotEmpty.class.getName(),NotBlank.class.getName())
                .withAccess(AccessLevel.PUBLIC);

        List<FieldDefRefV1> fieldRefs = template.getFieldRefs();
        fieldRefs.stream().map(ref -> map.get(ref.getName())).filter(fd -> fd.getNamespace().isPresent()).forEach(fd -> cb.withImports(fd.getNamespace().get() + "." + fd.getType()));
        String[] params = fieldRefs.stream().map(ref -> map.get(ref.getName())).map(fd -> fd.getType() + " " + fd.getName()).toArray(String[]::new);
        ConstructorBlock ctor = cb.createConstructor(params).withAccess(AccessLevel.PUBLIC);
        fieldRefs.stream().map(ref -> map.get(ref.getName())).forEach(fd -> {
            Var v = cb.var(fd.getType(), fd.getName()).withAccess(AccessLevel.PRIVATE).withFinal();
            if(ids.contains(fd.getName()))
            {
                v.withAnnotation(NotEmpty.class.getSimpleName()+"(message = \""+fd.getName()+" field cannot be empty or null in "+entityTemplate.getMetadata().getType()+" event\")")
                        .withAnnotation(NotBlank.class.getSimpleName()+"(message = \""+fd.getName()+" field cannot be blank in "+entityTemplate.getMetadata().getType()+" event\")");
            }
            v.end();
            ctor.stmt().append("this." + fd.getName() + " = " + fd.getName()).end();
            cb.createGetter(v);
        });

        FunctionBlock ownerFun = cb.createFunction("getOwner")
                .withReturnType("Class<? extends " + generator.getEntityBaseName() + ">").withAccess(AccessLevel.PUBLIC)
                .withAnnotation(Override.class.getSimpleName());
        ownerFun.withReturnStatment(entityTemplate.getMetadata().getType() + ".class").end();
        cb.withImports(generator.getSpecPackageName() + "." + generator.getEntityBaseName());
        cb.withImports(entityGen.get().getSpec().getTemplateNamespace(entityTemplate.getMetadata()) + "." + entityTemplate.getMetadata().getType());

        FunctionBlock ownerRootFun = cb.createFunction("getRootOwner")
                .withReturnType("Class<? extends " + generator.getRootEntityBaseName() + ">").withAccess(AccessLevel.PUBLIC)
                .withAnnotation(Override.class.getSimpleName());
        ownerRootFun.withReturnStatment(getRootType(cb, template) + ".class").end();
        cb.withImports(generator.getSpecPackageName() + "." + generator.getRootEntityBaseName());

        FunctionBlock rootEntityId = cb.createFunction("rootEntityId")
                .withReturnType("String").withAccess(AccessLevel.PUBLIC)
                .withAnnotation(Override.class.getSimpleName());
        rootEntityId.withReturnStatment(getRootIdField(template)).end();

        FunctionBlock entityidFunc = cb.createFunction("entityId")
                .withReturnType("String").withAccess(AccessLevel.PUBLIC)
                .withAnnotation(Override.class.getSimpleName());
        entityidFunc.withReturnStatment(entityTemplate.getMetadata().getId()).end();
        return cb;
    }

    private String getRootType(ClassBlock cb, EventSpecV1.Template template) {
        EntitySpecV1.Template entityTemplate = entityGen.get().getTemplate(template.getMetadata().getOwner()).orElseThrow();
        if (entityTemplate.getMetadata().isRoot()) {
            return entityTemplate.getMetadata().getType();
        } else {
            EntitySpecV1.Template rootTemplate = entityGen.get().getTemplate(entityTemplate.getMetadata().rootEntity().orElseThrow()).orElseThrow();
            cb.withImports(entityGen.get().getSpec().getTemplateNamespace(rootTemplate.getMetadata()) + "." + rootTemplate.getMetadata().getType());
            return rootTemplate.getMetadata().getType();
        }
    }

    private String getRootIdField(EventSpecV1.Template template) {
        EntitySpecV1.Template entityTemplate = entityGen.get().getTemplate(template.getMetadata().getOwner()).orElseThrow();
        if (entityTemplate.getMetadata().isRoot()) {
            return entityTemplate.getMetadata().getId();
        } else {
            EntitySpecV1.Template rootTemplate = entityGen.get().getTemplate(entityTemplate.getMetadata().rootEntity().orElseThrow()).orElseThrow();
            return rootTemplate.getMetadata().getId();
        }
    }

    private void generateFile(ClassBlock cb) {
        JavaFile file = JavaFile.wrap(cb);
        file.writeTo(new File(generator.getCodeGenFolder()));
    }
}
