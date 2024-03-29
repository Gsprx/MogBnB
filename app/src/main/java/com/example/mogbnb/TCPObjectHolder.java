package com.example.mogbnb;

import java.io.Serializable;

/**
 * TEMP...MAYBE...
 */

public class TCPObjectHolder implements Serializable {
    public int code;
    public Object obj;

    public TCPObjectHolder(int code, Object obj) {
        this.code = code;
        this.obj = obj;
    }
}
