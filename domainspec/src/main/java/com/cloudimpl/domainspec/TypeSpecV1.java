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
public class TypeSpecV1 implements Spec{
    
    private final List<Template> templates = new LinkedList<>();

    public List<Template> getTemplates() {
        return templates;
    }

    public static TypeSpecV1 loadFrom(JsonArray json)
    {
        TypeSpecV1 spec = new TypeSpecV1();
        json.forEach(el->spec.templates.add(Template.loadFrom(el.getAsJsonObject())));
        return spec;
    }
    
    public static   class Template {

        private MetaData metadata;
        private String[] values;
        public static Template loadFrom(JsonObject json) {
            JsonObject tempJson = Util.getElAsObj("template", json).orElseThrow();
            return GsonCodec.decode(Template.class, tempJson.toString());
        }

        public MetaData getMetdata() {
            return metadata;
        }

        public String[] getValues() {
            return values;
        }
        
        public String getType()
        {
            return metadata.getType();
        }
        
        public String getName()
        {
            return metadata.getName();
        }
        
        public static final class MetaData implements DomainSpecV1.TemplateMetadata{

            private String type;
            private String module;
            private String name;

            @Override
            public String getType() {
                return type;
            }

            public String getName() {
                return name;
            }

            @Override
            public Optional<String> getModule() {
                return Optional.ofNullable(module);
            }

           
            
        }
        
        public static final class DataType {
            private String name;
            private String type;
            private String namespace;

            public DataType(String name, String type, String namespace) {
                this.name = name;
                this.type = type;
                this.namespace = namespace;
            }

            

            public String getName() {
                return name;
            }

            public Optional<String> getNamespace() {
                return Optional.ofNullable(namespace);
            }
            
        }

    }
}
