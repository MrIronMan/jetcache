package com.alicp.jetcache.support;

import com.alibaba.fastjson.JSONObject;

/**
 * fast json 实现
 *
 * @author ironman@tinman.cn Date: 2021/6/18 Time: 0:39 Description: No Description
 */
public class FastjsonValueDecoder extends AbstractValueDecoder {

    public static final FastjsonValueDecoder INSTANCE = new FastjsonValueDecoder(true);

    public FastjsonValueDecoder(boolean useIdentityNumber) {
        super(useIdentityNumber);
    }

    @Override
    protected Object doApply(byte[] buffer) {
        return JSONObject.parseObject(buffer, Object.class);
    }
}
