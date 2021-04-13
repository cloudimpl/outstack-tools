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
public abstract class SpecGenerator {
    protected final DomainSpecCodeGenerator generator;

    public SpecGenerator(DomainSpecCodeGenerator generator) {
        this.generator = generator;
    }
    
    public abstract void resolve(JsonObject json);
    public abstract void execute();
}
