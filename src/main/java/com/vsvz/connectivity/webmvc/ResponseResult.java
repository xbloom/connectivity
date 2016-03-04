package com.vsvz.connectivity.webmvc;

/**
 * Created by liusijin on 16/2/28.
 */
public class ResponseResult<T> {
    public OPERATION op;

    public OPERATION getOp() {
        return op;
    }

    public void setOp(OPERATION op) {
        this.op = op;
    }

    public T result;

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }
}
