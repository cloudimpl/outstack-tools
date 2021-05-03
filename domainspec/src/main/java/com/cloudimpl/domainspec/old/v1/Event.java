/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.domainspec.old.v1;

import java.text.MessageFormat;

/**
 *
 * @author nuwan
 */
public interface Event extends IResource{
    
    Class<? extends Entity> getOwner();
    
    Class<? extends RootEntity> getRootOwner();
    
    String entityId();
    
    String rootEntityId();
    
    @Override
    default String getRn() {
        if (RootEntity.isMyType(getOwner())) {
            return MessageFormat.format("{0}/{1}/event/{2}", getOwner().getSimpleName(),entityId(),getClass().getSimpleName());
        }
        else
        {
            return MessageFormat.format("{0}/{1}/{2}/{3}/event/{4}", getRootOwner().getSimpleName(),rootEntityId(),getOwner().getSimpleName(),entityId(),getClass().getSimpleName());
        }
    }
}
