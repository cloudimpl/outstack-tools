/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.domainspec;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.Objects;
import java.util.Optional;

/**
 *
 * @author nuwan
 */
public class FieldDefRefV1 {

    private final String name;
    private final FieldMetaDataV1 metadata;

    public FieldDefRefV1(String name, FieldMetaDataV1 metadata) {
        this.name = name;
        this.metadata = metadata;
    }

    public String getName() {
        return name;
    }

    public Optional<FieldMetaDataV1> metadata() {
        return Optional.ofNullable(metadata);
    }

    public static final FieldDefRefV1 loadFrom(JsonElement el) {
        if (el instanceof JsonPrimitive) {
            return new FieldDefRefV1(el.getAsString(), null);
        } else {
            JsonObject obj = el.getAsJsonObject();
            FieldMetaDataV1 metdata = GsonCodec.decode(FieldMetaDataV1.class, obj.toString());
            return new FieldDefRefV1(obj.get("name").getAsString(), metdata);
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 43 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FieldDefRefV1 other = (FieldDefRefV1) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "FieldDefRef{" + "name=" + name + ", metadata=" + metadata + '}';
    }

}
