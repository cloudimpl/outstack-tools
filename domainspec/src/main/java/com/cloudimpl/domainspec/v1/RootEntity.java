/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.domainspec.v1;

import com.cloudimpl.outstack.runtime.util.Util;
import java.text.MessageFormat;

/**
 *
 * @author nuwan
 */
public abstract class RootEntity extends Entity {
    
    @Override
    public String getRN() {
        if (hasTenant()) { //rrn:restrata:identity:tenant/1234/User/1/Device/12"
            return makeRN(this.getClass(), ITenant.class.cast(this).getTenantId(), entityId());
        } else {
            return makeRN(this.getClass(), null, entityId());
        }
    }
    
    @Override
    public String getTRN() {
        if (hasTenant()) { //rrn:restrata:identity:tenant/1234/User/1/Device/12"
            return makeTRN(this.getClass(), ITenant.class.cast(this).getTenantId(), id());
        } else {
            return makeTRN(this.getClass(), null, id());
        }
    }
    
    public <T extends ChildEntity> T createChildEntity(Class<T> type, String entityId, String id) {
        T t;
        if (hasTenant()) {
            t = Util.createObject(type, new Util.VarArg<>(String.class, String.class, String.class), new Util.VarArg<>(entityId(), entityId, ITenant.class.cast(this).getTenantId()));
        } else {
            t = Util.createObject(type, new Util.VarArg<>(String.class, String.class), new Util.VarArg<>(entityId(), entityId, id));
        }
        EntityHelper.updateId(t, id);
        EntityHelper.updateRootId(t, id());
        return t;
    }
    
    public static <T extends RootEntity> T create(Class<T> type, String entityId, String tenantId, String tid) {
        T root;
        if (hasTenant(type)) {
            root =  Util.createObject(type, new Util.VarArg<>(String.class,String.class), new Util.VarArg<>(entityId,tenantId));
        }
        else
        {
            root =  Util.createObject(type, new Util.VarArg<>(String.class,String.class), new Util.VarArg<>(entityId));
        }
        EntityHelper.updateId(root, tid);
        return root;
    }
    
    public static boolean isMyType(Class<? extends Entity> type) {
        return RootEntity.class.isAssignableFrom(type);
    }
    
    public static String makeRN(Class<? extends RootEntity> type, String entityId, String tenantId) {
        Entity.checkTenantEligibility(type, tenantId);
        if (tenantId != null) { //rrn:restrata:identity:tenant/1234/User/1/Device/12"
            return MessageFormat.format("tenant/{0}/{1}/{2}", tenantId, type.getSimpleName(), entityId);
        } else {
            return MessageFormat.format("{1}/{2}", type.getSimpleName(), entityId);
        }
    }
    
    public static String makeTRN(Class<? extends RootEntity> type, String id, String tenantId) {
        Entity.checkTenantEligibility(type, tenantId);
        if (tenantId != null) { //rrn:restrata:identity:tenant/1234/User/1/Device/12"
            return MessageFormat.format("tenant/{0}/{1}/{2}", tenantId, type.getSimpleName(), id);
        } else {
            return MessageFormat.format("{1}/{2}", type.getSimpleName(), id);
        }
    }
    
    public static final RootEntity DELETED = new RootEntity() {
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
