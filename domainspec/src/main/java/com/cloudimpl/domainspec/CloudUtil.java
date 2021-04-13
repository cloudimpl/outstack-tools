/*
 * To change this license header, choose License Headers in Project Properties. To change this template file, choose
 * Tools | Templates and open the template in the editor.
 */
package com.cloudimpl.domainspec;

import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author nuwansa
 */
public class CloudUtil {

    public static <T> T newInstance(Class<T> classType) {
        try {
            return classType.getConstructor().newInstance();
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new CloudException(ex.getMessage());
        }
    }

    public static <T> Class<T> classForName(String clazz) {
        try {
            return (Class<T>) Class.forName(clazz);
        } catch (ClassNotFoundException ex) {
            throw new CloudException(ex.getMessage());
        }
    }

    public static String getHostIpAddr() {
        try {
            InetAddress localMachine = InetAddress.getLocalHost();

            byte[] addr = localMachine.getAddress();

            // Convert to dot representation
            String ipaddr = "";
            for (int i = 0; i < addr.length; i++) {
                if (i > 0) {
                    ipaddr += ".";
                }
                ipaddr += addr[i] & 0xFF;
            }

            return ipaddr;
        } catch (UnknownHostException ex) {
            throw new CloudException(ex);
        }

    }
}
