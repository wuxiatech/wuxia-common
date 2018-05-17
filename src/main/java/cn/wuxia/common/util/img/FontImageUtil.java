package cn.wuxia.common.util.img;

import cn.wuxia.common.util.DateUtil;
import cn.wuxia.common.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;

public class FontImageUtil {
    protected static transient final Logger logger = LoggerFactory.getLogger(FontImageUtil.class);

    public static void main(String[] args) throws Exception {
        //        createImage("请A1003到3号窗口", new Font("宋体", Font.BOLD, 30), new File("/app/a.png"), 4096, 64);
        buildCharImage("李", "/app/李.png");
        //        createImage("请A1001到1号窗口", new Font("黑体", Font.PLAIN, 40), new File("/app/a2.png"), 4096, 64);

    }

    /**
     * 注意将字体加到linux中
     * @param str
     * @param filepath
     * @throws Exception
     */
    public static void buildCharImage(String str, String filepath) throws Exception {
        File file = new File(filepath);
        createImage(str, new Font("微软雅黑", Font.BOLD, 64), file, 100, 100);
    }

    // 根据str,font的样式以及输出文件目录
    public static void createImage(String str, Font font, File outFile, Integer width, Integer height) throws Exception {
        logger.info("生成文字图片：{},{}", str, outFile.getPath());
        // 创建图片
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
        Graphics g = image.getGraphics();
        g.setClip(0, 0, width, height);
        g.setColor(Color.black);
        g.fillRect(0, 0, width, height);// 先用黑色填充整张图片,也就是背景
        g.setColor(Color.white);// 在换成黑色
        g.setFont(font);// 设置画笔字体
        /** 用于获得垂直居中y */

        Rectangle clip = g.getClipBounds();
        FontMetrics fm = g.getFontMetrics(font);

        int stringWidth = fm.stringWidth(str);
        int x = (clip.width - stringWidth) / 2;

        int ascent = fm.getAscent();
        int descent = fm.getDescent();
        int y = (clip.height - (ascent + descent)) / 2 + ascent;
        g.drawString(str, x, y);// 画出字符串

        g.dispose();
        FileUtil.forceMkdirParent(outFile);
        ImageIO.write(image, "png", outFile);// 输出png图片
    }

}
