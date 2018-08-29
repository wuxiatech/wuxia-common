package cn.wuxia.common.test;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

@ProtobufClass
public class TestSerializeBean {
    Timestamp timestamp;

    String a;

    Date date;

    TestEnum testEnum;

    List<Map> list;

    TestSerializeBean child;

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public TestEnum getTestEnum() {
        return testEnum;
    }

    public void setTestEnum(TestEnum testEnum) {
        this.testEnum = testEnum;
    }

    public List<Map> getList() {
        return list;
    }

    public void setList(List<Map> list) {
        this.list = list;
    }

    public TestSerializeBean getChild() {
        return child;
    }

    public void setChild(TestSerializeBean child) {
        this.child = child;
    }

    enum TestEnum {
        a, b
    }
}
