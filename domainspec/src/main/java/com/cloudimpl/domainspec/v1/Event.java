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
public abstract class Event<T extends Entity> implements IResource ,Input{

    public enum Action {
        CREATE, UPDATE, DELETE, RENAME
    }
    private String _tenantId;
    private long _seqNum;
    private String _id;
    private String _rootId;
    private Action _action;

    public void setTenantId(String tenantId) {
        this._tenantId = tenantId;
    }

    public void setSeqNum(long seq) {
        this._seqNum = seq;
    }

    public void setAction(Action action)
    {
        this._action = action;
    }
    public long getSeqNum() {
        return _seqNum;
    }

    public Action getAction() {
        return _action;
    }

    public boolean isConsumed()
    {
        return this._action != null;
    }
    
    public void setRootId(String rootId) {
        this._rootId = rootId;
    }

    public void setId(String id) {
        this._id = id;
    }

    @Override
    public String tenantId() {
        return _tenantId;
    }

    public String id() {
        return _id;
    }

    public String rootId() {
        return _rootId;
    }

    public abstract Class<? extends Entity> getOwner();

    public abstract Class<? extends RootEntity> getRootOwner();

    public abstract String entityId();

    public abstract String rootEntityId();

    @Override
    public String getTRN() {
        if (RootEntity.isMyType(getOwner())) {
            if (tenantId() != null) {
                return MessageFormat.format("tenant/{0}/{1}/{2}/{3}/{4}",
                        tenantId(), getOwner().getSimpleName(), id(), getClass().getSimpleName(), getSeqNum());
            } else {
                return MessageFormat.format("{0}/{1}/{2}/{3}",
                        getOwner().getSimpleName(), id(), getClass().getSimpleName(), getSeqNum());
            }
        } else {
            if (tenantId() != null) {
                return MessageFormat.format("tenant/{0}/{1}/{2}/{3}/{4}/{5}/{6}",
                        tenantId(), getRootOwner().getSimpleName(), rootId(), getOwner().getSimpleName(), id(), getClass().getSimpleName(), getSeqNum());

            } else {
                return MessageFormat.format("{0}/{1}/{2}/{3}/{4}/{5}",
                        getRootOwner().getSimpleName(), rootId(), getOwner().getSimpleName(), id(), getClass().getSimpleName(), getSeqNum());

            }
        }
    }

    @Override
    public String getRN() {
        if (RootEntity.isMyType(getOwner())) {
            if (tenantId() != null) {
                return MessageFormat.format("tenant/{0}/{1}/{2}/{3}/{4}",
                        tenantId(), getOwner().getSimpleName(), entityId(), getClass().getSimpleName(), getSeqNum());
            } else {
                return MessageFormat.format("{0}/{1}/{2}/{3}",
                        getOwner().getSimpleName(), entityId(), getClass().getSimpleName(), getSeqNum());
            }
        } else {
            if (tenantId() != null) {
                return MessageFormat.format("tenant/{0}/{1}/{2}/{3}/{4}/{5}/{6}",
                        tenantId(), getRootOwner().getSimpleName(), rootId(), getOwner().getSimpleName(), entityId(), getClass().getSimpleName(), getSeqNum());

            } else {
                return MessageFormat.format("{0}/{1}/{2}/{3}/{4}/{5}",
                        getRootOwner().getSimpleName(), rootId(), getOwner().getSimpleName(), entityId(), getClass().getSimpleName(), getSeqNum());

            }
        }
    }

    public String getEntityTRN() {
        if (RootEntity.isMyType(getOwner())) {
            if (tenantId() != null) {
                return MessageFormat.format("tenant/{0}/{1}/{2}",
                        tenantId(), getOwner().getSimpleName(), id());
            } else {
                return MessageFormat.format("{1}/{2}",
                        getOwner().getSimpleName(), id());
            }
        } else {
            if (tenantId() != null) {
                return MessageFormat.format("tenant/{0}/{1}/{2}/{3}/{4}",
                        tenantId(), getRootOwner().getSimpleName(), rootId(), getOwner().getSimpleName(), id());
            } else {
                return MessageFormat.format("{0}/{1}/{2}/{3}",
                        getRootOwner().getSimpleName(), rootId(), getOwner().getSimpleName(), id());
            }
        }
    }

    public String getEntityRN() {
        if (RootEntity.isMyType(getOwner())) {
            if (tenantId() != null) {
                return MessageFormat.format("tenant/{0}/{1}/{2}",
                        tenantId(), getOwner().getSimpleName(), entityId());
            } else {
                return MessageFormat.format("{1}/{2}",
                        getOwner().getSimpleName(), entityId());
            }
        } else {
            if (tenantId() != null) {
                return MessageFormat.format("tenant/{0}/{1}/{2}/{3}/{4}",
                        tenantId(), getRootOwner().getSimpleName(), rootEntityId(), getOwner().getSimpleName(), entityId());
            } else {
                return MessageFormat.format("{0}/{1}/{2}/{3}",
                        getRootOwner().getSimpleName(), rootEntityId(), getOwner().getSimpleName(), entityId());
            }
        }
    }

    public String getRootEntityTRN() {
        if (tenantId() != null) {
            return MessageFormat.format("tenant/{0}/{1}/{2}",
                    tenantId(), getRootOwner().getSimpleName(), rootId());
        } else {
            return MessageFormat.format("{1}/{2}",
                    getRootOwner().getSimpleName(), rootId());
        }
    }

    public String getRootEntityRN() {
        if (tenantId() != null) {
            return MessageFormat.format("tenant/{0}/{1}/{2}",
                    tenantId(), getRootOwner().getSimpleName(), entityId());
        } else {
            return MessageFormat.format("{1}/{2}",
                    getRootOwner().getSimpleName(), entityId());
        }
    }

    public boolean isRootEvent() {
        return getRootOwner() == getOwner();
    }

}
