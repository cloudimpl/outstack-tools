package com.cloudimpl.domainspec;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author nuwan
 */
public class DomainTypeSpecV1 extends DomainSpecV1{
    private TypeSpecV1 spec;

    public DomainTypeSpecV1(TypeSpecV1 spec) {
        this.spec = spec;
    }

    public TypeSpecV1 getSpec() {
        return spec;
    }
    
}
