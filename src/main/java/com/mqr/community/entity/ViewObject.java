package com.mqr.community.entity;

import java.util.HashMap;
import java.util.Map;

public class ViewObject {

    Map<String, Object> map = new HashMap<>();

    public void setViewObject(String string, Object object) {
        map.put(string, object);
    }

    public Object getViewObject(String string) {
        return map.get(string);
    }

    //必须要有get方法
    public Map<String, Object> getMap() {
        return map;
    }
}
