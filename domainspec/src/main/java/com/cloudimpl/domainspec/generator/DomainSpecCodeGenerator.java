/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.domainspec.generator;

import com.cloudimpl.codegen4j.spi.MavenCodeGenSpi;
import com.cloudimpl.domainspec.Util;
import com.cloudimpl.domainspec.YamlToJsonReader;
import com.cloudimpl.domainspec.GsonCodec;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author nuwan
 */
public class DomainSpecCodeGenerator extends MavenCodeGenSpi {

    private final Map<String, CodeGenerator> mapGen = new HashMap<>();

    public DomainSpecCodeGenerator() {
        super("domaingen", "com.cloudimpl.outstack");
        mapGen.put("com.cloudimpl.domainspec/v1", new DomainSpecV1CodeGenerator(this));
    }

    @Override
    public void execute() {

        String resourcesFolder = getProject().getResourcesDir();
        try {
            List<String> yamls = getAllSpecFiles(resourcesFolder);
            Map<String, List<JsonObject>> map = yamls.stream().map(p -> YamlToJsonReader.convertYamlToJson(p)).map(s -> GsonCodec.toJsonObject(s))
                    .filter(s -> Util.getElStr("kind", s) != null)
                    .filter(s -> Util.getElStr("apiVersion", s) != null)
                    .collect(Collectors.groupingBy(s -> s.get("kind").getAsString()));
            map.entrySet().stream().filter(e -> e.getKey().equals("Type")).flatMap(s -> s.getValue().stream()).forEach(json -> getGenerator(Util.getElStr("apiVersion", json)).orElseThrow().resolveTypeGen(json));
            map.entrySet().stream().filter(e -> e.getKey().equals("Field")).flatMap(s -> s.getValue().stream()).forEach(json -> getGenerator(Util.getElStr("apiVersion", json)).orElseThrow().resolveFieldGen(json));
            map.entrySet().stream().filter(e -> e.getKey().equals("Entity")).flatMap(s -> s.getValue().stream()).forEach(json -> getGenerator(Util.getElStr("apiVersion", json)).orElseThrow().resolveEntityGen(json));
            map.entrySet().stream().filter(e -> e.getKey().equals("Event")).flatMap(s -> s.getValue().stream()).forEach(json -> getGenerator(Util.getElStr("apiVersion", json)).orElseThrow().resolveEventGen(json));
            mapGen.values().forEach(gen -> gen.execute());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public String getSpecPackageName() {
        return getProperty("specNamespace", "");
    }
    
    public String getChildEntityBaseName() {
        return getProperty("childEntityBase", "ChildEntity");
    }
    
    public String getEntityBaseName() {
        return getProperty("entityBase", "Entity");
    }

    public String getRootEntityBaseName() {
        return getProperty("rootEntityBase", "RootEntity");
    }
    
    public String getEventBaseName() {
        return getProperty("eventBase", "Event");
    }

    public String getTenantBaseName(){
        return getProperty("tenantBase", "ITenant");
    }
    
    public static List<String> getAllSpecFiles(String path) throws IOException {
        try (Stream<Path> stream = Files.walk(Path.of(path))) {
            return stream.filter(p -> p.toString().endsWith(".yaml")).map(p -> p.toFile().getAbsolutePath()).collect(Collectors.toList());
        }
    }

    private Optional<CodeGenerator> getGenerator(String apiVersion) {
        return Optional.ofNullable(mapGen.get(apiVersion));
    }

    @Override
    public String getCodeGenFolder()
    {
        return getProject().getSourceDir();
    }
}
