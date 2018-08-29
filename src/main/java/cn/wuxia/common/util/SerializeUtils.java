package cn.wuxia.common.util;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.sql.Timestamp;

import io.protostuff.*;
import io.protostuff.WireFormat.FieldType;
import io.protostuff.runtime.DefaultIdStrategy;
import io.protostuff.runtime.Delegate;
import io.protostuff.runtime.RuntimeEnv;
import io.protostuff.runtime.RuntimeSchema;

public class SerializeUtils {

    /** 时间戳转换Delegate，解决时间戳转换后错误问题 @author jiujie 2016年7月20日 下午1:52:25 */

    private final static DefaultIdStrategy idStrategy = ((DefaultIdStrategy) RuntimeEnv.ID_STRATEGY);

    static {
        idStrategy.registerDelegate(new TimestampDelegate());
    }

    /**
     * 序列化对象
     * @param t
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> byte[] serialize(T t, Class<T> clazz) {
        return ProtobufIOUtil.toByteArray(t, RuntimeSchema.createFrom(clazz, idStrategy), LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
    }

    /**
     * 序列化对象
     * @param t
     * @param <T>
     * @return
     */
    public static <T> byte[] serialize(T t) {
        return serialize(t, (Class<T>) t.getClass());
    }

    /**
     * 反序列化对象
     * @param data
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T deSerialize(byte[] data, Class<T> clazz) {
        RuntimeSchema<T> runtimeSchema = RuntimeSchema.createFrom(clazz, idStrategy);
        T t = runtimeSchema.newMessage();
        ProtobufIOUtil.mergeFrom(data, t, runtimeSchema);
        return t;
    }

    /**
     * protostuff timestamp Delegate
     * @author jiujie
     * @version $Id: TimestampDelegate.java, v 0.1 2016年7月20日 下午2:08:11 jiujie Exp $
     */
    static class TimestampDelegate implements Delegate<Timestamp> {

        public FieldType getFieldType() {
            return FieldType.FIXED64;
        }

        public Class<?> typeClass() {
            return Timestamp.class;
        }

        public Timestamp readFrom(Input input) throws IOException {
            return new Timestamp(input.readFixed64());
        }

        public void writeTo(Output output, int number, Timestamp value, boolean repeated) throws IOException {
            output.writeFixed64(number, value.getTime(), repeated);
        }

        public void transfer(Pipe pipe, Input input, Output output, int number, boolean repeated) throws IOException {
            output.writeFixed64(number, input.readFixed64(), repeated);
        }

    }

}
