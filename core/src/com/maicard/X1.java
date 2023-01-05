package com.maicard;

import com.google.common.base.CaseFormat;
import com.maicard.base.CriteriaMap;
import com.maicard.site.entity.Node;
import com.maicard.utils.ClassUtils;
import com.maicard.utils.JsonUtils;
import com.maicard.utils.SecurityUtils;
import com.maicard.utils.StringTools;
import org.checkerframework.checker.units.qual.C;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Arrays;

public class X1 {
    public static void main(String[] argv){

        String src = "TestAbAAA";

         System.out.println(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,src));

    }
}
