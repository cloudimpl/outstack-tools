/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.domainspec;

import java.util.Optional;

/**
 *
 * @author nuwan
 */
public class FieldMetaDataV1 {

    private String desc;
    private int maxLen;
    private boolean required;
    public FieldMetaDataV1(String desc, int maxLen,boolean required) {
        this.desc = desc;
        this.required = required;
        this.maxLen = maxLen;
    }

    public String getDesc() {
        return desc;
    }

    public int getMaxLen() {
        return maxLen;
    }

    public boolean isRequired() {
        return required;
    }

    public FieldMetaDataV1 merge(Optional<FieldMetaDataV1> other) {

        FieldMetaDataV1 newMeta = clone();
        if (other.isEmpty()) {
            return newMeta;
        }
        newMeta.desc = other.get().desc;
        newMeta.maxLen = other.get().maxLen;
        newMeta.required = other.get().required;
        return newMeta;
    }

    @Override
    public FieldMetaDataV1 clone() {
        return new FieldMetaDataV1(desc, maxLen,required);
    }

    @Override
    public String toString() {
        return "FieldMetaDataV1{" + "desc=" + desc + ", maxLen=" + maxLen + ", required=" + required + '}';
    }

    

}
