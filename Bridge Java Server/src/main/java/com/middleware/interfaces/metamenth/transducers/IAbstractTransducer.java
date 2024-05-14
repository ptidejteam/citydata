package com.middleware.interfaces.metamenth.transducers;

import java.util.Map;

import com.middleware.interfaces.metamenth.datatypes.IAbstractMeasure;

public interface IAbstractTransducer {
    String getUID();
    String getName();
    void setName(String name);
    String getRegistryId();
    void setRegistryId(String registryId);
    IAbstractMeasure getSetPoint();
    void setTransducerSetPoint(IAbstractMeasure setpoint, String measure);
    Map<String, Object> getMetaData();
    void addData(Object data);
    boolean removeData(Object data);
    void addMetaData(String key, Object value);
    boolean removeMetaData(String key);
    String toString();
}
