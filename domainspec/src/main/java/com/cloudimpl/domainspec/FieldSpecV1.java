/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.domainspec;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author nuwan
 */
public class FieldSpecV1 implements Spec {

    private final List<TypeGroup> types = new LinkedList<>();

    public static FieldSpecV1 loadFrom(JsonObject jsonObj) {
        FieldSpecV1 spec = new FieldSpecV1();
        JsonArray arr = jsonObj.getAsJsonArray("types");
        arr.forEach(el -> loadEl(spec, el));
        return spec;
    }

    public static void loadEl(FieldSpecV1 spec, JsonElement el) {
        TypeGroup typeGroup = TypeGroup.loadFrom(el.getAsJsonObject());
        spec.types.add(typeGroup);
    }

    public List<TypeGroup> getTypeGroups() {
        return types;
    }

    @Override
    public String toString() {
        return "FieldSpec{" + "types=" + types + '}';
    }

    public static final class TypeGroup {

        private String type;
        private FieldGroup fieldGroup;

        public FieldGroup getFieldGroup() {
            return fieldGroup;
        }

        public static TypeGroup loadFrom(JsonObject el) {
            TypeGroup group = new TypeGroup();
            group.type = Util.getElStr("type", el);
            JsonArray arr = Util.getElAsArray("fields", el).orElseThrow();
            group.fieldGroup = FieldGroup.loadFrom(arr, group.type);
            return group;
        }

        @Override
        public String toString() {
            return "TypeGroup{" + "type=" + type + ", fieldGroup=" + fieldGroup + '}';
        }

    }

    public static final class FieldGroup {

        private final List<FieldDef> fieldDefs = new LinkedList<>();

        public List<FieldDef> getFieldDefs() {
            return fieldDefs;
        }

        public static FieldGroup loadFrom(JsonArray arr, String type) {
            FieldGroup fgroup = new FieldGroup();
            arr.forEach(el -> loadEl(fgroup, el, type));
            return fgroup;
        }

        private static void loadEl(FieldGroup group, JsonElement el, String type) {
            if (el instanceof JsonPrimitive) {
                group.fieldDefs.add(new FieldDef(el.getAsString(), type, null,null));
            } else if (el instanceof JsonObject) {
                JsonObject jsonObj = el.getAsJsonObject();
                String name = Util.getElStr("name", jsonObj);
                Optional<JsonObject> metadata = Util.getElAsObj("metadata", jsonObj);
                FieldMetaDataV1 meta = null;
                if (metadata.isPresent()) {
                    meta = GsonCodec.decode(FieldMetaDataV1.class, metadata.get().toString());
                }
                group.fieldDefs.add(new FieldDef(name, type, meta,null));
            }
        }

        @Override
        public String toString() {
            return "FieldGroup{" + "fieldDefs=" + fieldDefs + '}';
        }

    }

    public static final class FieldDef {

        private String name;
        private FieldMetaDataV1 metadata;
        private String namespace;
        private String type;

        public FieldDef(String name, String type, FieldMetaDataV1 metadata,String namespace) {
            this.name = name;
            this.type = type;
            this.metadata = metadata == null ? new FieldMetaDataV1(null, 0, false):metadata;
            this.namespace = namespace;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type.equals("string") ? "String" : type;
        }

        public void setNamespace(String namespace)
        {
            this.namespace = namespace;
        }
        
        public Optional<String> getNamespace()
        {
            return Optional.ofNullable(this.namespace);
        }

        public void setMetadata(FieldMetaDataV1 metadata) {
            this.metadata = metadata;
        }
        
        
        public boolean isCustomType() {
            switch (type) {
                case "string":
                case "int":
                case "long":
                case "short":            
                case "boolean":
                case "float":
                case "double": {
                    return false;
                }
                default: {
                    return true;
                }
            }
        }

        public Optional<FieldMetaDataV1> metadata() {
            return Optional.ofNullable(metadata);
        }

        public FieldDef merge(FieldDefRefV1 ref) {
            FieldDef def = clone();
            
            def.metadata().map(meta -> meta.merge(ref.metadata())).ifPresent(meta->def.setMetadata(meta));
            return def;
        }

        public FieldDef clone() {
            return new FieldDef(name, type, metadata,namespace);
        }

        @Override
        public String toString() {
            return "FieldDef{" + "name=" + name + ", metadata=" + metadata + ", namespace=" + namespace + ", type=" + type + '}';
        }

       

    }
}
