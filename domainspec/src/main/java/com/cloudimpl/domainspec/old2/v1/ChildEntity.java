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
public abstract class ChildEntity<T extends RootEntity> extends Entity {
    private String _rootTid;

    public void setRootTid(String _rootTid) {
        this._rootTid = _rootTid;
    }
    
    public String rootTid()
    {
        return this._rootTid;
    }
    
    public abstract String rootId();
    
    public abstract <T extends RootEntity> Class<T> rootType();
    
      @Override
    public String getRn() {
        if (hasTenant()) { //rrn:restrata:identity:tenant/1234/User/1/Device/12"
            return MessageFormat.format("tenant/{0}/{1}/{2}/{3}/{4}/{5}",ITenant.class.cast(this).getTenantId(),rootType().getSimpleName(),rootTid(),getClass().getSimpleName(),tid());
        } else {
            return MessageFormat.format("{0}/{1}/{2}/{3}",rootType().getSimpleName(),rootTid(),getClass().getSimpleName(), tid());
        }
    }
}
