/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.domainspec;

import java.util.Optional;

/**
 *
 * @author nuwan
 */
public class DomainSpecV1 {
    private String apiVersion;
    private Kind kind;
    private Metadata metadata;
    
    public Optional<String> getApiVersion() {
        return Optional.ofNullable(apiVersion);
    }

    public Optional<Kind> getKind() {
        return Optional.ofNullable(kind);
    }

    public Optional<Metadata> getMetaData() {
        return Optional.ofNullable(metadata);
    }
    
    public String getTemplateNamespace(TemplateMetadata metadata)
    {
        return metadata.getModule().map(s->this.metadata.namespace+"."+s).orElse(this.metadata.namespace);
    }
    
    public static final class Metadata
    {
        private final String name;
        private final String plural;
        private final String namespace;

        public Metadata(String name, String plural, String namespace) {
            this.name = name;
            this.plural = plural;
            this.namespace = namespace;
        }

        public Optional<String> getName() {
            return Optional.ofNullable(name);
        }

        public Optional<String> getNamespace() {
            return Optional.ofNullable(namespace);
        }

        public Optional<String> getPlural() {
            return Optional.ofNullable(plural);
        }

        @Override
        public String toString() {
            return "Metadata{" + "name=" + name + ", plural=" + plural + ", namespace=" + namespace + '}';
        }
        
        
    }

    @Override
    public String toString() {
        return "DomainSpecV1{" + "apiVersion=" + apiVersion + ", kind=" + kind + ", metaData=" + metadata + '}';
    }
    
    public static interface TemplateMetadata
    {
        Optional<String> getModule();
        String getType();
    }
}
