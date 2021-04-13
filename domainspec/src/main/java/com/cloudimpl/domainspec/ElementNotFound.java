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
public class ElementNotFound extends java.lang.RuntimeException{

    public ElementNotFound(String elName) {
        super("element "+elName+" is missing");
    }
    
}
