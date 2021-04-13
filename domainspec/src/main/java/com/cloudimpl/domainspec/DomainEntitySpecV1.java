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
public class DomainEntitySpecV1 extends DomainSpecV1{
    private EntitySpecV1 spec;

    public DomainEntitySpecV1(EntitySpecV1 spec) {
        this.spec = spec;
    }

    public EntitySpecV1 getSpec() {
        return spec;
    }
    
}
