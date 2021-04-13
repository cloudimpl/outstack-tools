/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.domainspec.v1;

import java.text.MessageFormat;

/**
 *
 * @author nuwan
 */
public interface Entity extends IResource{

    String id();

    default boolean hasTenant() {
        return this instanceof ITenant;
    }
}
