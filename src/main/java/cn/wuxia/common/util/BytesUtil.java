package cn.wuxia.common.util;

import org.apache.commons.io.IOUtils;

import java.io.*;


public class BytesUtil {
    /**
     * 对象转byte[]
     *
     * @param obj
     * @return
     * @throws IOException
     * @see {@link SerializeUtils#serialize(Object, Class)}
     */
    @Deprecated
    public static byte[] objectToBytes(Object obj) throws IOException {
        if (obj == null) {
            return null;
        }
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        ObjectOutputStream oo = new ObjectOutputStream(bo);
        oo.writeObject(obj);
        byte[] bytes = bo.toByteArray();
        bo.close();
        oo.close();
        return bytes;
    }

    /**
     * byte[]转对象
     *
     * @param bytes
     * @return
     * @throws Exception
     * @see {@link SerializeUtils#deSerialize(byte[], Class)}
     */
    @Deprecated
    public static Object bytesToObject(byte[] bytes) throws IOException, ClassNotFoundException {
        if (bytes == null) {
            return null;
        }
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        ObjectInputStream sIn = new ObjectInputStream(in);
        return sIn.readObject();
    }


    public static ByteArrayOutputStream cloneInputStream(InputStream input) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = input.read(buffer)) > -1) {
                baos.write(buffer, 0, len);
            }
            baos.flush();
            return baos;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            IOUtils.closeQuietly(input);
        }
    }
}
