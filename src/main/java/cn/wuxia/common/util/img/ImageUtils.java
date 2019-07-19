/*
 * Created on :23 Apr, 2014 Author :songlin Change History Version Date Author
 * Reason <Ver.No> <date> <who modify> <reason>
 */
package cn.wuxia.common.util.img;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.nutz.img.Images;

import com.gif4j.GifDecoder;
import com.gif4j.GifEncoder;
import com.gif4j.GifImage;
import com.gif4j.GifTransformer;
import com.jhlabs.image.CropFilter;
import com.jhlabs.image.RotateFilter;

import cn.wuxia.common.exception.ImgException;

/**
 * 纯Java实现的图像处理
 *
 * @author Winter Lau
 * @date 2010-4-26 上午09:45:56
 */
public class ImageUtils {

    public static void main(String[] args) throws Exception {
        File img = new File("/Users/songlin/Pictures/com.tencent.ScreenCapture/QQ20140424-1@2x.png");
        //ImageUtils.crop(img, new File("/app/uploadfile/Temp/1398245482931_500.png"), 0, 0, 500, 500, 100, 100);
        //ImageUtils.rotate(img, new File("/app/uploadfile/Temp/1398245482931_90.png"), 90);
        //        ImageUtils.scale(img, new File("/app/uploadfile/Temp/380.jpg"), 380);
        //        int[] a =ImageUtils.shrink(img, new File("/app/uploadfile/Temp/380_2.jpg"), 380);
        //        System.out.print(a[0]+" "+a[1]);
        //ImageUtils.scale(img, new File("/app/uploadfile/Temp/1398245482931_200.png"), 200, 120);
        //scale(img, new File("/app/uploadfile/Temp/380.png"));
        ImageUtil.scale2("/Users/songlin/Pictures/com.tencent.ScreenCapture/QQ20140424-1@2x.png", "/app/uploadfile/Temp/380_1.png", 360, 360, false);
        ImageUtil.scale2("/Users/songlin/Pictures/myAcount.png", "/app/uploadfile/Temp/380_2.png", 360, 360, false);
    }

    /**
     * 裁剪为正方形
     *
     * @param source
     * @param dest
     * @throws IOException
     * @author songlin
     */
    public static void scale(File source, File dest) throws IOException {
        BufferedImage bi = (BufferedImage) ImageIO.read(source);
        String ext = FilenameUtils.getExtension(dest.getName()).toLowerCase();
        int w = bi.getWidth();
        int h = bi.getHeight();
        BufferedImage bi_scale = null;
        if (w > h) {
            bi_scale = Images.zoomScale(bi, w, w, Color.WHITE);
        } else {
            bi_scale = Images.zoomScale(bi, h, h, Color.WHITE);
        }

        ImageIO.write(bi_scale, ext.equals("png") ? "png" : "jpeg", dest);
    }

    /**
     * 图像自动根据比例缩小到指定的方框中
     *
     * @param src
     * @param dest
     * @param size
     * @return
     * @throws ImgException
     */
    public static int[] shrink(File src, File dest, int size) throws ImgException {
        try {
            BufferedImage orig_portrait = (BufferedImage) ImageIO.read(src);
            int w = orig_portrait.getWidth();
            int h = orig_portrait.getHeight();
            if (w <= size && h <= size) {
                FileUtils.copyFile(src, dest);
                return new int[]{w, h};
            } else {
                double ratio = (w > h) ? (double) size / w : (double) size / h;
                int w2 = (int) (w * ratio);
                int h2 = (int) (h * ratio);
                scale(src, dest, w2, h2);
                return new int[]{w2, h2};
            }
        } catch (IOException e) {
            throw new ImgException("Exception occur when shrink image.", e);
        }
    }

    /**
     * 进行图像剪裁
     *
     * @param src    源文件
     * @param dest   目标文件
     * @param left   剪裁部分的左上角x轴
     * @param top    剪裁部分的左上角y轴
     * @param width  剪裁部分的宽度
     * @param height 剪裁部分的高度
     * @param w      目标大小宽度
     * @param h      目标大小高度
     * @throws ImgException
     */
    public static void crop(File src, File dest, int left, int top, int width, int height, int w, int h) throws ImgException {
        if (!dest.getParentFile().exists())
            dest.getParentFile().mkdirs();
        try {
            String ext = FilenameUtils.getExtension(dest.getName()).toLowerCase();
            BufferedImage bi = (BufferedImage) ImageIO.read(src);
            height = Math.min(height, bi.getHeight());
            width = Math.min(width, bi.getWidth());
            if (height <= 0)
                height = bi.getHeight();
            if (width <= 0)
                width = bi.getWidth();
            top = Math.min(Math.max(0, top), bi.getHeight() - height);
            left = Math.min(Math.max(0, left), bi.getWidth() - width);

            if (top == 0 && left == 0 && width == bi.getWidth() && height == bi.getHeight()) {
                ImgScaleFilter scale = new ImgScaleFilter(w, h);
                BufferedImage bi_scale = new BufferedImage(w, h, (bi.getType() != 0) ? bi.getType() : BufferedImage.TYPE_INT_RGB);
                scale.filter(bi, bi_scale);
                ImageIO.write(bi_scale, ext.equals("png") ? "png" : "jpeg", dest);
            } else {
                BufferedImage bi_crop = new BufferedImage(width, height, (bi.getType() != 0) ? bi.getType() : BufferedImage.TYPE_INT_RGB);
                new CropFilter(left, top, width, height).filter(bi, bi_crop);
                BufferedImage bi_scale = new BufferedImage(w, h, (bi.getType() != 0) ? bi.getType() : BufferedImage.TYPE_INT_RGB);
                new ImgScaleFilter(w, h).filter(bi_crop, bi_scale);
                ImageIO.write(bi_scale, ext.equals("png") ? "png" : "jpeg", dest);
            }
        } catch (IOException e) {
            throw new ImgException("Exception occur when crop image.", e);
        }
    }

    /**
     * 图像旋转
     *
     * @param src     源文件
     * @param dest    目标文件
     * @param degrees 旋转度数
     * @throws ImgException
     */
    public static void rotate(File src, File dest, double degrees) throws ImgException {
        if (!dest.getParentFile().exists())
            dest.getParentFile().mkdirs();

        try {
            String ext = FilenameUtils.getExtension(dest.getName()).toLowerCase();
            if ("gif".equalsIgnoreCase(ext)) {
                GifImage gifImage = GifDecoder.decode(src);
                GifImage newGif = GifTransformer.rotate(gifImage, degrees, false);
                GifEncoder.encode(newGif, dest);
            } else {
                BufferedImage bi = (BufferedImage) ImageIO.read(src);
                RotateFilter Rotate = new RotateFilter((float) Math.toRadians(degrees));
                BufferedImage bi_rotate = new BufferedImage(bi.getHeight(), bi.getWidth(), (bi.getType() != 0) ? bi.getType()
                        : BufferedImage.TYPE_INT_RGB);
                Rotate.filter(bi, bi_rotate);
                ImageIO.write(bi_rotate, ext.equals("png") ? "png" : "jpeg", dest);
            }
        } catch (IOException e) {
            throw new ImgException("Exception occur when scaling image.", e);
        }
    }


    /**
     * 图像旋转
     *
     * @param src          源文件
     * @param outputStream 目标文件
     * @param degrees      旋转度数
     * @throws ImgException
     */
    public static void rotate(InputStream src, String fileType, OutputStream outputStream, double degrees) throws ImgException {

        try {
            BufferedImage bi = (BufferedImage) ImageIO.read(src);
            RotateFilter Rotate = new RotateFilter((float) Math.toRadians(degrees));
            BufferedImage bi_rotate = new BufferedImage(bi.getHeight(), bi.getWidth(), (bi.getType() != 0) ? bi.getType()
                    : BufferedImage.TYPE_INT_RGB);
            Rotate.filter(bi, bi_rotate);

            ImageIO.write(bi_rotate, fileType.equals("png") ? "png" : "jpeg", outputStream);

        } catch (IOException e) {
            throw new ImgException("Exception occur when scaling image.", e);
        }
    }

    /**
     * 图像缩放
     *
     * @param src  源文件
     * @param dest 目标文件
     * @param w    缩放宽度
     * @param h    缩放高度
     * @throws
     */
    public static void scale(File src, File dest, int w, int h) throws ImgException {
        if (!dest.getParentFile().exists())
            dest.getParentFile().mkdirs();
        try {
            if (w <= 0 && h <= 0) {
                FileUtils.copyFile(src, dest);
                return;
            }
            String ext = FilenameUtils.getExtension(dest.getName()).toLowerCase();
            if ("gif".equalsIgnoreCase(ext)) {
                GifImage gifImage = GifDecoder.decode(src);
                GifImage newGif = GifTransformer.resize(gifImage, w, h, false);
                GifEncoder.encode(newGif, dest);
            } else {
                BufferedImage bi = (BufferedImage) ImageIO.read(src);
                ImgScaleFilter scale = new ImgScaleFilter(w, h);
                BufferedImage bi_scale = new BufferedImage(w, h, (bi.getType() != 0) ? bi.getType() : BufferedImage.TYPE_INT_RGB);
                scale.filter(bi, bi_scale);
                ImageIO.write(bi_scale, ext.equals("png") ? "png" : "jpeg", dest);
            }
        } catch (IOException e) {
            throw new ImgException("Exception occur when scaling image.", e);
        }
    }

    /**
     * 将图像缩放到某个正方形框内
     *
     * @param src  源文件
     * @param dest 目标文件
     * @param size 正方形大小
     * @throws
     */
    public static void scale(File src, File dest, int size) throws ImgException {

        if (!dest.getParentFile().exists())
            dest.getParentFile().mkdirs();

        try {
            String ext = FilenameUtils.getExtension(dest.getName()).toLowerCase();
            if ("gif".equalsIgnoreCase(ext)) {
                GifImage gifImage = GifDecoder.decode(src);
                GifImage newGif = GifTransformer.resize(gifImage, size, size, false);
                GifEncoder.encode(newGif, dest);
            } else {
                BufferedImage bi = (BufferedImage) ImageIO.read(src);
                int w = bi.getWidth();
                int h = bi.getHeight();
                int max_size = Math.min(w, h);

                // 剪裁
                BufferedImage bi_crop = newImg(bi, max_size, max_size);
                new CropFilter((w - max_size) / 2, (h - max_size) / 2, max_size, max_size).filter(bi, bi_crop);

                // 缩小
                ImgScaleFilter scale = new ImgScaleFilter(size, size);
                BufferedImage bi_scale = newImg(bi_crop, size, size);
                scale.filter(bi_crop, bi_scale);

                ImageIO.write(bi_scale, ext.equals("png") ? "png" : "jpeg", dest);
            }
        } catch (IOException e) {
            throw new ImgException("Exception occur when scaling image.", e);
        }
    }

    private static BufferedImage newImg(BufferedImage src, int width, int height) {
        ColorModel dstCM = src.getColorModel();
        return new BufferedImage(dstCM, dstCM.createCompatibleWritableRaster(width, height), dstCM.isAlphaPremultiplied(), null);
    }

}
