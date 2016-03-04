package com.vsvz.connectivity.webmvc;

/**
 * Created by liusijin on 16/2/28.
 */
public class OPERATION {
    //TODO design me later
    // XXXYYYZZ x for system,yyy for function, zz for reason
    public static final OPERATION SUCCESS= new OPERATION(10110100,"成功");
    public static final OPERATION FAILED = new OPERATION(10110101,"失败");


    public long code;
    public String message;

    OPERATION(long code, String message) {
        this.code=code;
        this.message = message;
    }

    public void setCode(long code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
