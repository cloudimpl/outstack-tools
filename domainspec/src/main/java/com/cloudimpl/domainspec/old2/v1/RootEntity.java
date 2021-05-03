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
public abstract class RootEntity extends Entity {


    @Override
    public String getRn() {
        if (hasTenant()) { //rrn:restrata:identity:tenant/1234/User/1/Device/12"
            return MessageFormat.format("tenant/{0}/{1}/{2}", ITenant.class.cast(this).getTenantId(), this.getClass().getSimpleName(), tid());
        } else {
            return MessageFormat.format("{1}/{2}", this.getClass().getSimpleName(), tid());
        }
    }

    public static boolean isMyType(Class<? extends Entity> type) {
        return RootEntity.class.isAssignableFrom(type);
    }
}
