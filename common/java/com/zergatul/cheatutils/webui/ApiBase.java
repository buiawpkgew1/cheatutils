package com.zergatul.cheatutils.webui;

import com.google.gson.Gson;
import com.zergatul.cheatutils.configs.ConfigStore;
import org.apache.http.HttpException;
import org.apache.http.MethodNotSupportedException;

public abstract class ApiBase {

    protected Gson gson = ConfigStore.instance.gson;

    public abstract String getRoute();

    public String get() throws HttpException {
        throw new MethodNotSupportedException("不支持该方法");
    }

    public String get(String id) throws HttpException {
        throw new MethodNotSupportedException("不支持该方法");
    }

    public String put(String id, String body) throws HttpException {
        throw new MethodNotSupportedException("不支持该方法");
    }

    public String post(String body) throws HttpException {
        throw new MethodNotSupportedException("不支持该方法");
    }

    public String delete(String id) throws HttpException {
        throw new MethodNotSupportedException("不支持该方法");
    }
}