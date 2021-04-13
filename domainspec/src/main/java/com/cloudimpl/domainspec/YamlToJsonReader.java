/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.domainspec;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author nuwan
 */
public class YamlToJsonReader {
    public static String convertYamlToJson(String yamlPath) {
        try {
            ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
            Object obj = yamlReader.readValue(new File(yamlPath), Object.class);
            
            ObjectMapper jsonWriter = new ObjectMapper();
            return jsonWriter.writeValueAsString(obj);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
}
    
    public static void main(String[] args) throws IOException {
        GsonCodec.registerTypeAdaptor(FieldSpecV1.class, ()->new FieldSpecDecoderV1(), null);
        DomainFieldSpecV1 spec = GsonCodec.decode(DomainFieldSpecV1.class, convertYamlToJson("/Users/nuwan/Documents/yaml/FieldSpec.yaml"));
       // System.out.println(GsonCodec.decode(DomainSpecV1.class,convertYamlToJson("/Users/nuwan/Documents/yaml/Fields.yaml")));
        System.out.println(spec);
    }
}
