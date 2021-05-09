/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.domainspec.generator;

import com.cloudimpl.codegen4j.AccessLevel;
import com.cloudimpl.codegen4j.CaseBlock;
import com.cloudimpl.codegen4j.ClassBlock;
import com.cloudimpl.codegen4j.ClassBuilder;
import com.cloudimpl.codegen4j.ConstructorBlock;
import com.cloudimpl.codegen4j.FunctionBlock;
import com.cloudimpl.codegen4j.JavaFile;
import com.cloudimpl.codegen4j.SwitchBlock;
import com.cloudimpl.codegen4j.Var;
import com.cloudimpl.domainspec.*;
import com.google.gson.JsonObject;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

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
        if (template.getMetadata().isRoot() && template.getMetadata().getVersion() == null) {
            throw new RuntimeException("root entity " + template.getMetadata().getType() + " version not defined");
        }
        String entityVersion = template.getMetadata().getVersion();
        EntitySpecV1.Template rootTemplate = null;
//        if (!template.getMetadata().isRoot() && !map.containsKey(template.getMetadata().rootEntity().orElseThrow())) {
//            rootTemplate = getTemplate(template.getMetadata().rootEntity().orElseThrow()).orElseThrow();
//            map.put(rootTemplate.getMetadata().getId(), fieldSpec.getFieldDef(rootTemplate.getMetadata().getId()).orElseThrow());
//        }
        if (!template.getMetadata().isRoot()) {
            rootTemplate = getTemplate(template.getMetadata().rootEntity().orElseThrow()).orElseThrow();
            if (rootTemplate.getMetadata().getVersion() == null) {
                throw new RuntimeException("root entity " + rootTemplate.getMetadata().getType() + " version not defined");
            }
            entityVersion = rootTemplate.getMetadata().getVersion();
        }
        ClassBuilder builder = new ClassBuilder();
        ClassBlock cb = builder.createClass(template.getMetadata().getType())
                .withPackageName(spec.getMetaData().orElseThrow().getNamespace().orElseThrow() + template.getMetadata().getModule().map(s -> "." + s).orElse(""))
                .extend((template.getMetadata().isRoot() ? generator.getRootEntityBaseName() : generator.getChildEntityBaseName()) + ((rootTemplate != null) ? ("<" + rootTemplate.getMetadata().getType() + ">") : ""))
                .withImports(template.getMetadata().isRoot() ? generator.getSpecPackageName() + "." + generator.getRootEntityBaseName() : generator.getSpecPackageName() + "." + generator.getChildEntityBaseName())
                .withImports(generator.getSpecPackageName() + ".EntityMeta")
                .withImports(NotBlank.class.getName(),NotEmpty.class.getName())
                .withAccess(AccessLevel.PUBLIC);
        cb.withAnnotation("EntityMeta(plural=\"" + template.getMetadata().getPlural() + "\",version=\"" + entityVersion + "\")");
        createConstructor(cb, template, rootTemplate);
        if(!template.getMetadata().isRoot()){
            if(template.getMetadata().isTenant() && !rootTemplate.getMetadata().isTenant()){
                throw new RuntimeException("Child  entity '" + template.getMetadata().getType() + "' tenant enable for non tenant root entity '"+rootTemplate.getMetadata().getType() +"'");
            }
        }
        if ((template.getMetadata().isTenant() && template.getMetadata().isRoot()) || (rootTemplate != null && rootTemplate.getMetadata().isTenant())) {
            cb.implement(generator.getTenantBaseName()).withImports(generator.getSpecPackageName() + "." + generator.getTenantBaseName());
            generateTenantIdFunction(cb);

        }
        map.values().stream().filter(fd -> fd.getNamespace().isPresent()).forEach(fd -> cb.withImports(fd.getNamespace().get() + "." + fd.getType()));
        EntitySpecV1.Template rootTemp = rootTemplate;
        map.values().stream().forEach(fd -> {
            Var v = cb.var(fd.getType(), fd.getName()).withAccess(AccessLevel.PRIVATE);
            if (fd.getName().equals(template.getMetadata().getId())) {
                v.withFinal();
                v.withAnnotation(NotEmpty.class.getSimpleName()+"(message = \""+fd.getName()+" field cannot be empty or null in "+template.getMetadata().getType()+" entity\")");
                v.withAnnotation(NotBlank.class.getSimpleName()+"(message = \""+fd.getName()+" field cannot be blank in "+template.getMetadata().getType()+" entity\")");
            }
            if (rootTemp != null && fd.getName().equals(rootTemp.getMetadata().getId())) {
                v.withFinal();
                v.withAnnotation(NotEmpty.class.getSimpleName()+"(message = \""+fd.getName()+" field cannot be empty or null in "+template.getMetadata().getType()+" entity\")");
                v.withAnnotation(NotBlank.class.getSimpleName()+"(message = \""+fd.getName()+" field cannot be blank in "+template.getMetadata().getType()+" entity\")");
            }
            v.end();
            cb.createGetter(v);
            if (fd.getName().equals(template.getMetadata().getId())) {
                generateIdFunction(cb, template.getMetadata().getId());
            }
        });
        if (rootTemplate != null) {
            //  generateRootIdFunction(cb, rootTemplate.getMetadata().getId());
            generateRootTypeFunction(cb, rootTemplate.getMetadata().getType());
        }
        generateIdFieldFunction(cb, template.getMetadata().getId());
        template.getLogic().forEach(apply -> generateApplyEventFunction(cb, template, apply));
        generateApplyFunction(cb, template.getLogic());
        return cb;
    }

    private void createConstructor(ClassBlock cb, EntitySpecV1.Template template, EntitySpecV1.Template rootTemplate) {
        List<String> params = new LinkedList<>();
        template.getMetadata().rootEntity().map(s -> getTemplate(s).orElseThrow()).ifPresent(t -> {
            cb.withImports(getSpec().getTemplateNamespace(t.getMetadata()) + "." + t.getMetadata().getType());
            //params.add("String " + t.getMetadata().getId());
        });
        params.add("String " + template.getMetadata().getId());
        if (template.getMetadata().isTenant() || (rootTemplate != null && rootTemplate.getMetadata().isTenant())) {
            params.add("String tenantId");
        }
        ConstructorBlock ctr = cb.createConstructor(params.stream().toArray(String[]::new)).withAccess(AccessLevel.PUBLIC);
        params.stream().map(s -> s.split(" ")[1]).forEach(s -> {
            ctr.stmt().append("this." + s + " = " + s).end();
        });
    }

    private void generateIdFunction(ClassBlock cb, String idField) {
        FunctionBlock fb = cb.createFunction("entityId").withReturnType("String")
                .withAccess(AccessLevel.PUBLIC)
                .withAnnotation(Override.class.getSimpleName());
        fb.withReturnStatment(idField).end();
    }

//    private void generateRootIdFunction(ClassBlock cb, String idField) {
//        FunctionBlock fb = cb.createFunction("rootEntityId").withReturnType("String")
//                .withAccess(AccessLevel.PUBLIC)
//                .withAnnotation(Override.class.getSimpleName());
//        fb.withReturnStatment(idField).end();
//    }
    private void generateIdFieldFunction(ClassBlock cb, String idField) {
        FunctionBlock fb = cb.createFunction("idField").withReturnType("String")
                .withAccess(AccessLevel.PUBLIC)
                .withAnnotation(Override.class.getSimpleName());
        fb.withReturnStatment("\"" + idField + "\"").end();
    }

    private void generateRootTypeFunction(ClassBlock cb, String rootType) {
        FunctionBlock fb = cb.createFunction("rootType").withReturnType("Class<" + rootType + ">")
                .withAccess(AccessLevel.PUBLIC)
                .withAnnotation(Override.class.getSimpleName());
        fb.withReturnStatment(rootType + ".class").end();
    }

    private void generateApplyEventFunction(ClassBlock cb, EntitySpecV1.Template template, EntitySpecV1.Template.ApplyStmt apply) {
        EventSpecV1.Template spec = eventSpec.get().getTemplate(apply.getEvt()).orElseThrow(() -> Util.$(apply.getEvt()));
        if (!template.getMetadata().getType().equals(spec.getMetadata().getOwner())) {
            throw new RuntimeException("event " + spec.getMetadata().getType() + " is own by " + spec.getMetadata().getOwner() + ", cannot applied to " + template.getMetadata().getType());
        }
        cb.withImports(eventSpec.get().getSpec().getTemplateNamespace(eventSpec.get().getTemplate(apply.getEvt()).orElseThrow().getMetadata()) + "." + apply.getEvt());
        FunctionBlock fb = cb.createFunction("applyEvent")
                .withArgs(eventSpec.get().getTemplate(apply.getEvt()).orElseThrow().getMetadata().getType() + " evt")
                .withAccess(AccessLevel.PRIVATE);
        if (apply.getStmt() != null) {
            for (String stmt : apply.getStmt()) {
                String st = stmt.substring(0, stmt.lastIndexOf(".")) + "." + FunctionBlock.createName("get", stmt.substring(stmt.lastIndexOf(".") + 1)) + "()";
                fb.stmt().append(st).end();
            }
        }
    }

    private void generateApplyFunction(ClassBlock cb, List<EntitySpecV1.Template.ApplyStmt> applyList) {
        cb.withImports(generator.getSpecPackageName() + "." + generator.getEventBaseName());
        cb.withImports(generator.getSpecPackageName() + ".DomainEventException");
        FunctionBlock fb = cb.createFunction("apply").withArgs("Event event").withAccess(AccessLevel.PUBLIC).withAnnotation(Override.class.getSimpleName());
        SwitchBlock sb = fb.createSwitch("event.getClass().getSimpleName()");
        for (EntitySpecV1.Template.ApplyStmt stmt : applyList) {
            CaseBlock caseBlock = sb.createCase("\"" + stmt.getEvt() + "\"");
            caseBlock.stmt().append("applyEvent((" + stmt.getEvt() + ") event)").end();
            caseBlock.stmt().append("break").end();
        }
        sb.createDefault().stmt().append("throw new DomainEventException(DomainEventException.ErrorCode.UNHANDLED_EVENT,\"unhandled event:\"+event.getClass().getName())").end();
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
