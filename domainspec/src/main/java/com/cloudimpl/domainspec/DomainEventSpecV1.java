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
public class DomainEventSpecV1 extends DomainSpecV1{
   private EventSpecV1 spec;

    public DomainEventSpecV1(EventSpecV1 spec) {
        this.spec = spec;
    }

    public EventSpecV1 getSpec() {
        return spec;
    } 
}
