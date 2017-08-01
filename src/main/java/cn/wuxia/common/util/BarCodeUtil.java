package cn.wuxia.common.util;

import org.krysalis.barcode4j.HumanReadablePlacement;
import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 条形码处理工具
 * Created by luodengxiong on 2017/01/31.
 */
public class BarCodeUtil {

    public static void generateCode128Barcode(File file, String code) {
        Code128Bean bean = new Code128Bean();
        final int dpi = 300;
        bean.setFontName("Arial");
        bean.setFontSize(3d);

        OutputStream out = null;

        try {
            out = new FileOutputStream(file);
            BitmapCanvasProvider canvas = new BitmapCanvasProvider(out,
                    "image/jpeg", dpi, BufferedImage.TYPE_BYTE_BINARY, false, 0);
            bean.generateBarcode(canvas, code);
            canvas.finish();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null)
                    out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String code = "UCM0123456789";
        generateCode128Barcode(new File("/opt/logs/code128.jpg"), code);
        System.out.println("生成图片成功:"+code);

    }
}
