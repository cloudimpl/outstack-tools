/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.domainspec.v1;

import com.cloudimpl.outstack.runtime.common.GsonCodec;
import com.google.gson.JsonObject;

/**
 *
 * @author nuwan
 */
public abstract class Entity implements IResource {

    private String _id;

    void setId(String id) {
        this._id = id;
    }

    public String id() {
        return _id;
    }

    public abstract String entityId();

    public boolean hasTenant() {
        return this instanceof ITenant;
    }

    public boolean isRoot() {
        return this instanceof RootEntity;
    }

    protected abstract void apply(Event event);

    public void applyEvent(Event event) {
        if (event.getOwner() != this.getClass()) {
            throw new DomainEventException("invalid domain event: " + event.getClass().getName());
        }
        apply(event);
    }

    public <T extends Entity> T cloneEntity() {
        String json = GsonCodec.encode(this);
        return GsonCodec.decode((Class<T>) this.getClass(), json);
    }

    public <T extends Entity> T rename(String newId)
    {
        JsonObject json = GsonCodec.encodeToJson(this).getAsJsonObject();
        json.addProperty(entityIdFieldName(), newId);
        return GsonCodec.decode((Class<T>) this.getClass(), json.toString());
    }
    
    public abstract String entityIdFieldName();
    
    public static void checkTenantEligibility(Class<? extends Entity> type,String tenantId) {
        if (EntityHelper.hasTenant(type) && tenantId == null) {
            throw new DomainEventException("tenantId is null for entity creation");
        } else if (!EntityHelper.hasTenant(type) && tenantId != null) {
            throw new DomainEventException("tenantId is not applicable for entity creation");
        }
    }
    
     public static  boolean hasTenant(Class<? extends Entity> entityType) {
        return ITenant.class.isAssignableFrom(entityType);
    }

}
