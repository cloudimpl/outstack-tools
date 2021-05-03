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
import com.cloudimpl.domainspec.CommandSpecDecoderV1;
import com.cloudimpl.domainspec.CommandSpecV1;
import com.cloudimpl.domainspec.DomainCommandSpecV11;
import com.cloudimpl.domainspec.FieldSpecV1;
import com.cloudimpl.domainspec.GsonCodec;
import com.google.gson.JsonObject;
import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @author nuwan
 */
public class CommandV1SpecGenerator extends SpecGenerator {

    static {
        GsonCodec.registerTypeAdaptor(CommandSpecV1.class, () -> new CommandSpecDecoderV1(), null);
    }
    private final FieldV1SpecGenerator fieldSpec;
    private DomainCommandSpecV11 spec;
    private Map<String, CommandSpecV1.Template> templates;

    public CommandV1SpecGenerator(DomainSpecCodeGenerator generator, FieldV1SpecGenerator fieldSpec) {
        super(generator);
        this.fieldSpec = fieldSpec;
    }

    @Override
    public void resolve(JsonObject json) {
        spec = GsonCodec.decode(DomainCommandSpecV11.class, json.toString());
        templates = spec.getSpec().getTemplates().stream().collect(Collectors.toMap(t -> t.getMetadata().getType(), Function.identity()));
    }

    public Optional<CommandSpecV1.Template> getTemplate(String type) {
        return Optional.ofNullable(templates.get(type));
    }

    public DomainCommandSpecV11 getSpec() {
        return spec;
    }

    @Override
    public void execute() {
//        Map<String, FieldSpecV1.FieldDef> map = spec.getSpec().getTemplates().stream()
//                .flatMap(temp -> temp.getFieldRefs().stream())
//                .map(fr -> fieldSpec.getFieldDef(fr.getName()).orElseThrow().merge(fr))
//                .collect(Collectors.toMap(fd -> fd.getName(), fd -> fd));
        if(spec != null)
        spec.getSpec().getTemplates().stream().map(temp -> createCommand(spec, temp)).forEach(this::generateFile);

    }

    private ClassBlock createCommand(DomainCommandSpecV11 spec, CommandSpecV1.Template template) {

        Map<String, FieldSpecV1.FieldDef> map = template.getFieldRefs().stream()
                .map(fr -> fieldSpec.getFieldDef(fr.getName()).orElseThrow().merge(fr))
                .collect(Collectors.toMap(fd -> fd.getName(), fd -> fd));
       
        ClassBuilder builder = new ClassBuilder();
        ClassBlock cb = builder.createClass(template.getMetadata().getType())
                .withPackageName(spec.getMetaData().orElseThrow().getNamespace().orElseThrow() + template.getMetadata().getModule().map(s -> "." + s).orElse(""))
                .extend(generator.getCommandBaseName())
                .withImports(generator.getSpecPackageName() + "." + generator.getCommandBaseName())
                .withAccess(AccessLevel.PUBLIC);
        createConstructor(cb, template,map);

        map.values().stream().filter(fd -> fd.getNamespace().isPresent()).forEach(fd -> cb.withImports(fd.getNamespace().get() + "." + fd.getType()));
        map.values().stream().forEach(fd -> {
            Var v = cb.var(fd.getType(), fd.getName()).withFinal().withAccess(AccessLevel.PRIVATE);
            
            v.end();
            cb.createGetter(v);
        });
        
        FunctionBlock fb = cb.createFunction("builder").withReturnType("Builder").withFinal().withStatic().withAccess(AccessLevel.PUBLIC);
        fb.withReturnStatment("new Builder()").end();
        
        createBuilder(template,cb, map);
        return cb;
    }

    private void createConstructor(ClassBlock cb, CommandSpecV1.Template template,Map<String, FieldSpecV1.FieldDef> map) {
        ConstructorBlock ctr = cb.createConstructor("Builder builder").withAccess(AccessLevel.PRIVATE);
        ctr.stmt().append("super(builder)").end();
        map.values().forEach(fd->{
            ctr.stmt().append2("this.").append(fd.getName()).append("=").append2("builder.").append(fd.getName()).end();
        });
    }

    private void createBuilder(CommandSpecV1.Template template,ClassBlock cb,Map<String, FieldSpecV1.FieldDef> map)
    {
        ClassBlock inner = cb.createClass("Builder").extend("Command.Builder").withStatic().withFinal().withAccess(AccessLevel.PUBLIC);
        map.values().forEach(fd->{
            Var v = inner.var(fd.getType(), fd.getName()).withAccess(AccessLevel.PRIVATE);
            v.end();
           FunctionBlock fn =  inner.createFunction(FunctionBlock.createName("with",fd.getName())).withReturnType("Builder").withAccess(AccessLevel.PUBLIC);
           fn.withArgs(fd.getType()+" "+fd.getName());
           fn.stmt().append("this."+fd.getName()).append("=").append(fd.getName()).end();
           fn.withReturnStatment("this").end();
        }); 
        
         FunctionBlock fn =  inner.createFunction("build").withReturnType(template.getMetadata().getType()).withAccess(AccessLevel.PUBLIC).withAnnotation(Override.class.getSimpleName());
         fn.withReturnStatment("new "+template.getMetadata().getType()+"(this)").end();
         
    }
    
    private void generateFile(ClassBlock cb) {
        JavaFile file = JavaFile.wrap(cb);
        file.writeTo(new File(generator.getCodeGenFolder()));
    }
}
