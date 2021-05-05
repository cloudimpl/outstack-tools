/*
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.domainspec;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author nuwan
 */
public class EntitySpecV1 implements Spec {

    private List<Template> templates = new LinkedList<>();

    public List<Template> getTemplates() {
        return templates;
    }

    public static EntitySpecV1 loadFrom(JsonArray json) {
        EntitySpecV1 spec = new EntitySpecV1();
        json.forEach(el -> spec.templates.add(Template.loadFrom(el.getAsJsonObject())));
        return spec;
    }

    public static final class Template {

        private MetaData metdata;
        private List<FieldDefRefV1> fieldRefs = new LinkedList<>();
        private List<ApplyStmt> logic = new LinkedList<>();

        public static Template loadFrom(JsonObject json) {
            Template template = new Template();
            JsonObject tempJson = Util.getElAsObj("template", json).orElseThrow();
            JsonObject obj = Util.getElAsObj("metadata", tempJson).orElseThrow();
            template.metdata = GsonCodec.decode(MetaData.class, obj.toString());
            JsonArray arr = Util.getElAsArray("fieldRefs", tempJson).orElseThrow();
            arr.forEach(el -> template.fieldRefs.add(FieldDefRefV1.loadFrom(el)));
            if (tempJson.get("logic") != null) {
                JsonArray logicArr = Util.getElAsArray("logic", tempJson).orElseThrow();
                logicArr.forEach(el -> template.logic.add(ApplyStmt.loadEl(el.getAsJsonObject())));
            }

            return template;
        }

        public MetaData getMetadata() {
            return metdata;
        }

        public List<FieldDefRefV1> getFieldRefs() {
            return fieldRefs;
        }

        public List<ApplyStmt> getLogic() {
            return logic;
        }

        public boolean hasField(String name) {
            return getFieldRef(name).isPresent();
        }

        public Optional<FieldDefRefV1> getFieldRef(String name) {
            return fieldRefs.stream().filter(fr -> fr.getName().equals(name)).findFirst();
        }

        public FieldDefRefV1 getIdFieldDef() {
            return getFieldRef(getMetadata().id).orElseThrow();
        }

        public static final class MetaData implements DomainSpecV1.TemplateMetadata {

            private String type;
            private String module;
            private String name;
            private boolean tenant;
            private String plural;
            private String version;
            private String id;
            private String rootEntity;

            @Override
            public String getType() {
                return type;
            }

            public String getName() {
                return name;
            }

            public String getPlural() {
                return plural;
            }

            public String getVersion() {
                return version;
            }

            public String getId() {
                return id;
            }

            public boolean isRoot() {
                return !rootEntity().isPresent();
            }

            public boolean isTenant() {
                return tenant;
            }

            public Optional<String> rootEntity() {
                return Optional.ofNullable(rootEntity);
            }

            @Override
            public Optional<String> getModule() {
                return Optional.ofNullable(module);
            }

        }

        public static final class ApplyStmt {

            private String evt;
            private String[] stmt;

            public String getEvt() {
                return evt;
            }

            public String[] getStmt() {
                return stmt;
            }

            public static ApplyStmt loadEl(JsonObject json) {
                return GsonCodec.decode(ApplyStmt.class, json.get("apply").toString());
            }

        }
    }

}
