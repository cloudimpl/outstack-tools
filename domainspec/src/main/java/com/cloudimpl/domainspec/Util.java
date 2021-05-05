/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.domainspec;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Optional;

/**
 *
 * @author nuwan
 */
public class Util {
    public static String getElStr(String elName,JsonObject json)
    {
        JsonElement el = json.get(elName);
        if(el == null)
            throw new ElementNotFound(elName);
        return el.getAsString();
    }
    
    public static Optional<JsonObject> getElAsObj(String elName,JsonObject json)
    {
        JsonElement el = json.get(elName);
        if(el == null)
            return Optional.empty();
        return Optional.of(el.getAsJsonObject());
    }
    
     public static Optional<JsonArray> getElAsArray(String elName,JsonObject json)
    {
        JsonArray el = json.getAsJsonArray(elName);
        if(el == null)
            return Optional.empty();
        return Optional.of(el);
    }
     
     public static RuntimeException $(String error)
     {
         return new RuntimeException(error);
     }
}
