/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.domainspec;

/**
 *
 * @author nuwan
 */
public class DomainFieldSpecV1 extends DomainSpecV1{
    private final FieldSpecV1 spec;

    public DomainFieldSpecV1(FieldSpecV1 spec) {
        this.spec = spec;
    }

    public FieldSpecV1 getSpec() {
        return spec;
    }

    @Override
    public String toString() {
        return super.toString() + "DomainFieldSpecV1{" + "spec=" + spec + '}';
    }
    
    
}
