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

    public FieldMetaDataV1(String desc, int maxLen) {
        this.desc = desc;
        this.maxLen = maxLen;
    }

    public String getDesc() {
        return desc;
    }

    public int getMaxLen() {
        return maxLen;
    }

    public FieldMetaDataV1 merge(Optional<FieldMetaDataV1> other) {

        FieldMetaDataV1 newMeta = clone();
        if (other.isEmpty()) {
            return newMeta;
        }
        newMeta.desc = other.get().desc;
        newMeta.maxLen = other.get().maxLen;
        return newMeta;
    }

    @Override
    public FieldMetaDataV1 clone() {
        return new FieldMetaDataV1(desc, maxLen);
    }

    @Override
    public String toString() {
        return "MetaData{" + "desc=" + desc + ", maxLen=" + maxLen + '}';
    }

}
