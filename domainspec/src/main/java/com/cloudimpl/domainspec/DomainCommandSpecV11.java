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
public class DomainCommandSpecV11 extends DomainSpecV1{
    private CommandSpecV1 spec;

    public DomainCommandSpecV11(CommandSpecV1 spec) {
        this.spec = spec;
    }

    public CommandSpecV1 getSpec() {
        return spec;
    }
    
}
