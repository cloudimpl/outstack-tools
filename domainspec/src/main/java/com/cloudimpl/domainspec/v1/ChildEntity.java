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
 * @param <T>
 */
public abstract class ChildEntity<T extends RootEntity> extends Entity {

    private String _rootId;

    public void setRootId(String rootId) {
        this._rootId = rootId;
    }

    public String rootId() {
        return this._rootId;
    }

    public abstract String rootEntityId();

    public abstract Class<T> rootType();

    @Override
    public String getTRN() {
        if (hasTenant()) { //rrn:restrata:identity:tenant/1234/User/1/Device/12"
            return MessageFormat.format("tenant/{0}/{1}/{2}/{3}/{4}/{5}", ITenant.class.cast(this).getTenantId(), rootType().getSimpleName(), rootId(), getClass().getSimpleName(), id());
        } else {
            return MessageFormat.format("{0}/{1}/{2}/{3}", rootType().getSimpleName(), rootId(), getClass().getSimpleName(), id());
        }
    }
    
    @Override
    public String getRN() {
        if (hasTenant()) { //rrn:restrata:identity:tenant/1234/User/1/Device/12"
            return MessageFormat.format("tenant/{0}/{1}/{2}/{3}/{4}/{5}", ITenant.class.cast(this).getTenantId(), rootType().getSimpleName(),rootId(), getClass().getSimpleName(), entityId());
        } else {
            return MessageFormat.format("{0}/{1}/{2}/{3}", rootType().getSimpleName(), rootId(), getClass().getSimpleName(), entityId());
        }
    }
    
    public static <R extends RootEntity,T extends ChildEntity<R>> String makeRN(Class<R> rootType,String rootId,Class<T> childType,String entityId, String tenantId)
    {
        if (tenantId != null) { //rrn:restrata:identity:tenant/1234/User/1/Device/12"
            return MessageFormat.format("tenant/{0}/{1}/{2}/{3}/{4}", tenantId, rootType.getSimpleName(), rootId,childType.getSimpleName(),entityId);
        } else {
            return MessageFormat.format("{1}/{2}/{3}/{4}", rootType.getSimpleName(), rootId,childType.getSimpleName(),entityId);
        }
    }
    
    public static <R extends RootEntity,T extends ChildEntity<R>> String makeTRN(Class<R> rootType,String rootId,Class<T> childType,String id, String tenantTid)
    {
        if (tenantTid != null) { //rrn:restrata:identity:tenant/1234/User/1/Device/12"
            return MessageFormat.format("tenant/{0}/{1}/{2}/{3}/{4}", tenantTid, rootType.getSimpleName(), rootId,childType.getSimpleName(),id);
        } else {
            return MessageFormat.format("{1}/{2}/{3}/{4}", rootType.getSimpleName(), rootId,childType.getSimpleName(),id);
        }
    }
    
    public static final ChildEntity DELETED = new ChildEntity() {
        @Override
        public String rootEntityId() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Class rootType() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String entityId() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        protected void apply(Event event) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String entityIdFieldName() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    };
}
