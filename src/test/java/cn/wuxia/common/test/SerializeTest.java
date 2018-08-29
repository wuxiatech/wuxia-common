package cn.wuxia.common.test;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

import cn.wuxia.common.util.SerializeUtils;
import com.baidu.bjf.remoting.protobuf.Codec;
import com.baidu.bjf.remoting.protobuf.ProtobufProxy;
import com.google.common.collect.Lists;

import cn.wuxia.common.hibernate.query.Pages;
import cn.wuxia.common.util.DateUtil;
import com.google.common.collect.Maps;
import io.protostuff.*;
import io.protostuff.WireFormat.FieldType;
import io.protostuff.runtime.DefaultIdStrategy;
import io.protostuff.runtime.Delegate;
import io.protostuff.runtime.RuntimeEnv;
import io.protostuff.runtime.RuntimeSchema;

public class SerializeTest {



    public static void main(String[] args) {
        TestSerializeBean testSerialize = new TestSerializeBean();
        testSerialize.setA("abc");
        testSerialize.setDate(new Date());
        testSerialize.setTestEnum(TestSerializeBean.TestEnum.a);
        testSerialize.setTimestamp(DateUtil.newInstanceDate());
        Map<String, Object> m = Maps.newHashMap();
        m.put("saldfkj",23423);
        testSerialize.setList(Lists.newArrayList(m));
        Pages pages = new Pages();
        pages.setResult(Lists.newArrayList(testSerialize));
        byte[] seria = SerializeUtils.serialize(pages);

        Pages page = SerializeUtils.deSerialize(seria, Pages.class);
        System.out.println(page.getResult().get(0));

//        Codec<TestSerializeBean> simpleTypeCodec = ProtobufProxy.create(TestSerializeBean.class);
//
//        try {
//            byte[] code = simpleTypeCodec.encode(testSerialize);
//            TestSerializeBean newser = simpleTypeCodec.decode(code);
//            System.out.println(newser.getTimestamp());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }



}
