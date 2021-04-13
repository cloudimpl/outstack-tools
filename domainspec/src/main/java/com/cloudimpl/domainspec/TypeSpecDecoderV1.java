/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.domainspec;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;

/**
 *
 * @author nuwan
 */
public class TypeSpecDecoderV1 implements JsonDeserializer<TypeSpecV1>{

    @Override
    public TypeSpecV1 deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
        JsonArray json = je.getAsJsonArray();
        return TypeSpecV1.loadFrom(json);
    }
    
}
