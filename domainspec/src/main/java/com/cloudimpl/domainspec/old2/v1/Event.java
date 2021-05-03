/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.domainspec.old2.v1;

import java.text.MessageFormat;

/**
 *
 * @author nuwan
 */
public abstract class Event implements IResource {

    private String _tenantId;
    private long _seqNum;
    private String _tid;
    private String _rootTid;
    
    public void setTenantId(String tenantId) {
        this._tenantId = tenantId;
    }

    public void setSeqNum(long seq) {
        this._seqNum = seq;
    }

    public long getSeqNum() {
        return _seqNum;
    }

    public void setRootTid(String _rootTid) {
        this._rootTid = _rootTid;
    }

    public void setTid(String _tid) {
        this._tid = _tid;
    }

    public String getTenantId() {
        return _tenantId;
    }

    public String tid()
    {
        return _tid;
    }
    
    public String rootTid()
    {
        return _rootTid;
    }
    
    public abstract Class<? extends Entity> getOwner();

    public abstract Class<? extends RootEntity> getRootOwner();

    public abstract String entityId();

    public abstract String rootEntityId();

    @Override
    public String getRn() {
        if (RootEntity.isMyType(getOwner())) {
            if (getTenantId() != null) {
                return MessageFormat.format("tenant/{0}/{1}/{2}/{3}/{4}",
                        getTenantId(), getOwner().getSimpleName(), tid(), getClass().getSimpleName(), getSeqNum());
            } else {
                return MessageFormat.format("{0}/{1}/{2}/{3}",
                        getOwner().getSimpleName(), tid(), getClass().getSimpleName(), getSeqNum());
            }
        } else {
            if (getTenantId() != null) {
                return MessageFormat.format("tenant/{0}/{1}/{2}/{3}/{4}/{5}/{6}",
                        getTenantId(), getRootOwner().getSimpleName(), rootTid(), getOwner().getSimpleName(), tid(), getClass().getSimpleName(), getSeqNum());

            } else {
                return MessageFormat.format("{0}/{1}/{2}/{3}/{4}/{5}",
                        getRootOwner().getSimpleName(), rootTid(), getOwner().getSimpleName(), tid(), getClass().getSimpleName(), getSeqNum());

            }
        }
    }

    public String getEntityRn() {
        if (RootEntity.isMyType(getOwner())) {
            if (getTenantId() != null) {
                return MessageFormat.format("tenant/{0}/{1}/{2}",
                        getTenantId(), getOwner().getSimpleName(), tid());
            } else {
                return MessageFormat.format("{1}/{2}",
                        getOwner().getSimpleName(), tid());
            }
        } else {
            if (getTenantId() != null) {
                return MessageFormat.format("tenant/{0}/{1}/{2}/{3}/{4}",
                        getTenantId(), getRootOwner().getSimpleName(), rootTid(), getOwner().getSimpleName(), tid());
            } else {
                return MessageFormat.format("{0}/{1}/{2}/{3}",
                        getRootOwner().getSimpleName(), rootTid(), getOwner().getSimpleName(), tid());
            }
        }
    }

    public String getRootEntityRn() {
        if (getTenantId() != null) {
            return MessageFormat.format("tenant/{0}/{1}/{2}",
                    getTenantId(), getRootOwner().getSimpleName(), rootTid());
        } else {
            return MessageFormat.format("{1}/{2}",
                    getRootOwner().getSimpleName(), rootTid());
        }
    }

    public boolean isRootEvent() {
        return getRootOwner() == getOwner();
    }

}
