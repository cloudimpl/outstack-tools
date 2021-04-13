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
public class EventSpecV1 implements Spec{
    
    private List<Template> templates = new LinkedList<>();

    public List<Template> getTemplates() {
        return templates;
    }

    public static EventSpecV1 loadFrom(JsonArray json)
    {
        EventSpecV1 spec = new EventSpecV1();
        json.forEach(el->spec.templates.add(Template.loadFrom(el.getAsJsonObject())));
        return spec;
    }
    public static final class Template {

        private MetaData metdata;
        private List<FieldDefRefV1> fieldRefs = new LinkedList<>();

        public static Template loadFrom(JsonObject json) {
            Template template = new Template();
            JsonObject tempJson = Util.getElAsObj("template", json).orElseThrow();
            JsonObject obj = Util.getElAsObj("metadata", tempJson).orElseThrow();
            template.metdata = GsonCodec.decode(MetaData.class, obj.toString());
            JsonArray arr = Util.getElAsArray("fieldRefs", tempJson).orElseThrow();
            arr.forEach(el -> template.fieldRefs.add(FieldDefRefV1.loadFrom(el)));
            return template;
        }

        public MetaData getMetadata() {
            return metdata;
        }

        public List<FieldDefRefV1> getFieldRefs() {
            return fieldRefs;
        }

        public boolean hasField(String fieldName)
        {
            return fieldRefs.stream().filter(f->f.getName().equals(fieldName)).findAny().isPresent();
        }
        
        public static final class MetaData implements DomainSpecV1.TemplateMetadata{

            private String type;
            private String module;
            private String owner;

            @Override
            public String getType() {
                return type;
            }

            public String getOwner() {
                return owner;
            }

            @Override
            public Optional<String> getModule() {
                return Optional.ofNullable(module);
            }
            
            
            
        }

    }
}
