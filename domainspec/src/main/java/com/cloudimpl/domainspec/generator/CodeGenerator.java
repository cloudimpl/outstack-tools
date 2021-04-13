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
public interface CodeGenerator {

    void resolveFieldGen(JsonObject json);

    void resolveEntityGen(JsonObject json);

    void resolveEventGen(JsonObject json);
    
    void resolveTypeGen(JsonObject json);
    
    void execute();

}
