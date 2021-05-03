/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.domainspec.old2.v1;

import com.cloudimpl.outstack.runtime.common.GsonCodec;
import com.cloudimpl.outstack.runtime.util.Util;

/**
 *
 * @author nuwan
 */
public abstract class Entity implements IResource {
    private String _tid;
    
    public void setTid(String tid)
    {
        this._tid = tid;
    }

    public String tid() {
        return _tid;
    }
    
    public abstract String id();

    public boolean hasTenant() {
        return this instanceof ITenant;
    }

    public boolean isRoot() {
        return this instanceof RootEntity;
    }

    protected abstract void apply(Event event);
    
    public void applyEvent(Event event)
    {
        if(event.getOwner() != this.getClass())
        {
            throw new DomainEventException("invalid domain event: "+event.getClass().getName());
        }
        apply(event);
    }
    
    public static <T extends Entity> T createEntity(Class<?> type, Event event) {
        if (event.isRootEvent()) {
            if (event.getTenantId() != null) {
                return Util.createObject((Class<T>) type,
                        new Util.VarArg<>(String.class, String.class), new Util.VarArg<>(event.entityId(), event.getTenantId()));
            } else {
                return Util.createObject((Class<T>) type,
                        new Util.VarArg<>(String.class), new Util.VarArg<>(event.entityId()));
            }
        }else
        {
            if(event.getTenantId() != null){
                return Util.createObject((Class<T>) type,
                        new Util.VarArg<>(String.class,String.class, String.class), new Util.VarArg<>(event.rootEntityId(),event.entityId()
                                , event.getTenantId()));
            }else{
                return Util.createObject((Class<T>) type,
                        new Util.VarArg<>(String.class, String.class), new Util.VarArg<>(event.rootEntityId(),event.entityId()));
           
            }
        }
    }
    
    public Entity cloneEntity()
    {
        String json = GsonCodec.encode(this);
        return GsonCodec.decode(this.getClass(), json);
    }
}
