package com.alicp.jetcache.support;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * fast json 实现
 *
 * @author ironman@tinman.cn Date: 2021/6/18 Time: 1:38 Description: No Description
 */
public class FastjsonValueEncoder extends AbstractValueEncoder {

    public static final FastjsonValueEncoder INSTANCE = new FastjsonValueEncoder(true);

    public FastjsonValueEncoder(boolean useIdentityNumber) {
        super(useIdentityNumber);
    }

    @Override
    public byte[] apply(Object value) {
        return JSON.toJSONBytes(value, SerializerFeature.WriteClassName);
    }
}
