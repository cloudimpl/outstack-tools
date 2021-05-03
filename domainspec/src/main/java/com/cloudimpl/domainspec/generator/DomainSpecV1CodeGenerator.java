/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.domainspec.generator;

import com.google.gson.JsonObject;

/**
 *
 * @author nuwan
 */
public class DomainSpecV1CodeGenerator implements CodeGenerator{
    private final FieldV1SpecGenerator fieldGen;
    private final EntityV1SpecGenerator entityGen;
    private final CommandV1SpecGenerator commandGen;
    private final EventV1SpecGenerator eventGen;
    private final TypeV1SpecGenerator typeGen;
    public DomainSpecV1CodeGenerator(DomainSpecCodeGenerator gen) {
        this.fieldGen = new FieldV1SpecGenerator(gen,this::getTypeGen);
        this.entityGen = new EntityV1SpecGenerator(gen, fieldGen,this::getEventGen);
        this.eventGen = new EventV1SpecGenerator(gen, fieldGen,this::getEntityGen);
        this.typeGen = new TypeV1SpecGenerator(gen, fieldGen);
        this.commandGen = new CommandV1SpecGenerator(gen, fieldGen);
    }
    
    @Override
    public void resolveFieldGen(JsonObject json)
    {
        fieldGen.resolve(json);
    }
    
    @Override
    public void resolveEntityGen(JsonObject json)
    {
        this.entityGen.resolve(json);
    }
    
     @Override
    public void resolveCommandGen(JsonObject json)
    {
        this.commandGen.resolve(json);
    }
    
    @Override
    public void resolveEventGen(JsonObject json)
    {
        this.eventGen.resolve(json);
    }
    
    @Override
    public void resolveTypeGen(JsonObject json)
    {
        this.typeGen.resolve(json);
    }

    @Override
    public void execute() {
        this.typeGen.execute();
        this.fieldGen.execute();
        this.eventGen.execute();
        this.entityGen.execute();
        this.commandGen.execute();
    }
    
    public FieldV1SpecGenerator getFieldGen() {
        return fieldGen;
    }

    public EntityV1SpecGenerator getEntityGen() {
        return entityGen;
    }

    public EventV1SpecGenerator getEventGen() {
        return eventGen;
    }
    
     public CommandV1SpecGenerator getCommandGen() {
        return commandGen;
    }

    public TypeV1SpecGenerator getTypeGen() {
        return typeGen;
    }
    
    
}
