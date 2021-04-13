/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.domainspec.generator;

import com.cloudimpl.domainspec.DomainFieldSpecV1;
import com.cloudimpl.domainspec.FieldSpecDecoderV1;
import com.cloudimpl.domainspec.FieldSpecV1;
import com.cloudimpl.domainspec.GsonCodec;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author nuwan
 */
public class FieldV1SpecGenerator extends SpecGenerator {

    static {
        GsonCodec.registerTypeAdaptor(FieldSpecV1.class, () -> new FieldSpecDecoderV1(), null);
    }
    private final Map<String, FieldSpecV1.FieldDef> mapFields = new HashMap<>();
    private final java.util.function.Supplier<TypeV1SpecGenerator> typeGen;
    private DomainFieldSpecV1 spec;
    public FieldV1SpecGenerator(DomainSpecCodeGenerator generator,java.util.function.Supplier<TypeV1SpecGenerator> typeGen) {
        super(generator);
        this.typeGen = typeGen;
    }

    
    @Override
    public void execute() {

        spec.getSpec().getTypeGroups().stream().map(tg -> tg.getFieldGroup())
                .flatMap(tg -> tg.getFieldDefs().stream())
                .forEach(f -> mapFields.put(f.getName(), f));
    }

    public Optional<FieldSpecV1.FieldDef> getFieldDef(String name) {
        return Optional.ofNullable(Optional.ofNullable(this.mapFields.get(name)).orElseThrow(()->new RuntimeException("missing element:"+name)));
    }

    @Override
    public void resolve(JsonObject json) {
         spec = GsonCodec.decode(DomainFieldSpecV1.class,
                 json.toString());
          spec.getSpec().getTypeGroups().stream().map(tg -> tg.getFieldGroup())
                .flatMap(tg -> tg.getFieldDefs().stream())
                .filter(fd->fd.isCustomType())
                  .forEach(fd->fd.setNamespace(typeGen.get().getSpec().getTemplateNamespace(typeGen.get().getTemplate(fd.getType()).orElseThrow().getMetdata())));
        spec.getSpec().getTypeGroups().stream().map(tg -> tg.getFieldGroup())
                .flatMap(tg -> tg.getFieldDefs().stream())
                .forEach(f -> mapFields.put(f.getName(), f));
    }
}
