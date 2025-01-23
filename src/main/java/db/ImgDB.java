package db;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;

import msg.CallbackEven;
import msg.MsgObj;

public class ImgDB {
    private static Set<String> IMG_EXISCTS;
    /**
     * 用来标记本次启动发送过的需要缓存的图片服务器发回来的标识，主要用来去重
     */
    private static Set<String> SENDED_IMG;
    private static Map<String, String> IMG_CHACH;
    // private static Map<String, MsgObj> CHANGE_MSGS;
    private static Class START_CLASS;

    static {

    }

    /**
     * 初始化
     * 
     * @param starClass 启动类的位置
     */
    public static void init(Class starClass) {
        // 为什么要这样读，因为如果写在jar文件里就只能读流，要遍历jar内文件夹太麻烦,没办法了，只能这样声明还读得快

        String imgInit;
        START_CLASS = starClass;
        try (InputStream inputStream = START_CLASS.getResourceAsStream("/resources/list.txt")) {
            imgInit = new String(inputStream.readAllBytes(), "UTF-8"); // 记得规定编码，要不在windows就不是utf-8
        } catch (Exception e) {
            e.printStackTrace();
            imgInit = "";
        }
        String[] imgs = imgInit.split(",");

        IMG_CHACH = new ConcurrentHashMap<>();
        // CHANGE_MSGS = new ConcurrentHashMap<>();
        IMG_EXISCTS = ConcurrentHashMap.newKeySet();
        SENDED_IMG = ConcurrentHashMap.newKeySet();
        for (String img : imgs) {
            IMG_EXISCTS.add(img.trim());
        }

        // System.out.println(IMG_EXISCTS);
    };

    /**
     * 清除缓存图片
     * 
     * @param img_name
     */
    public static void clearImgChache(String img_name) {
        Database.del(img_name);
        // CHANGE_MSGS.remove(img_name);
        IMG_CHACH.remove(img_name);

    }

    /**
     * 设置图片
     * 
     * @param msgObj
     * @param img_name
     *                 这里图片文字的路径 角色|动作
     *                 例子：
     *                 董卓|hurt
     *                 AI[董卓]|hurt
     */
    public static void setImg(MsgObj msgObj, String img_path) {
        // --------本地图片都没有的，直接返回空
        String[] imgPaths = img_path.split("\\|");
        if (imgPaths.length == 0) {
            return;
        }

        String imgName = imgPaths[0];
        // System.out.println("img_path="+img_path);
        if (imgName.startsWith("AI")) {
            imgName = imgName.substring(3, imgName.length() - 1);
        }

        // System.out.println("imgName="+imgName);
        // System.out.println(IMG_EXISCTS);
        if (!IMG_EXISCTS.contains(imgName)) {
            return;
        }
        // System.out.println("imgName="+imgName);

        String realPath = imgPaths.length > 1 ? imgName + "|" + imgPaths[1] : imgName;
        msgObj.imgName.set(realPath);
        String result = getImg(realPath);

        if (result != null) {
            msgObj.localImgFileId = result;
        } else {
            //System.out.println(realPath + "  生成新图片");
            msgObj.localImgFileId = null; // 为了防止把之前的图片发送出去了
            msgObj.localImgFileStream = imgPaths.length > 1 ? getImgFromStream(imgName, imgPaths[1])
                    : getImgFromStream(imgName);
            synchronized (msgObj) {
                if (msgObj.imgWatingCallBack.get() == null) {// 如果有别人在等就放弃
                    msgObj.imgWatingCallBack.set(new CallbackEven() {
                        @Override
                        public void sendImgMsgDone(String imgFileId, String uid) {
                
                            if (SENDED_IMG.contains(imgFileId)) {
                                //System.out.println("realPath="+realPath+" SENDED_IMG 已存在 "+imgFileId);
                                return;
                            }
                            if (!realPath.equals(msgObj.imgName.get())) {// 如果等的不是这张图
                                //System.out.println("realPath="+realPath+" 不等于"+msgObj.imgName.get());
                                return;
                            }
                            if (imgFileId != null) {
                                //System.out.println("存图 realPath="+realPath + " imgFileId=" + imgFileId);
                                SENDED_IMG.add(imgFileId);
                                IMG_CHACH.put(realPath, imgFileId);
                                Database.put(realPath, imgFileId);
                                msgObj.imgWatingCallBack.set(null);

                            }
                        }
                    });
                }

            }

        }

    }

    /*
     * 可能会为空
     */
    public static String getImg(String imgRealPath) {

        // ----------没有就更改图片的请求就找本地缓存--
        String result = IMG_CHACH.get(imgRealPath);
        //System.out.println(imgRealPath + " chache缓存图片id ：" + result);
        if (result != null) {
            return result;
        }

        // ----------如果本地也没有就进数据库找-----------
        result = Database.get(imgRealPath);
        if (result != null) {
            IMG_CHACH.put(imgRealPath, result);
            SENDED_IMG.add(result);
        }
        //System.out.println(imgRealPath + " 数据库图片id ：" + result);
        return result;

    }

    /**
     * 获取本地图片
     * 
     * @param img_name
     * @return
     */
    public static InputStream getImgFromStream(String img_name) {
        // System.out.println( ImgDB.class.getResource("./img/" + img_name +
        // ".jpg").getPath());
        return START_CLASS.getResourceAsStream("/resources/img/" + img_name + ".jpg");
    }

    /**
     * 获取带动作的合成图片
     * 
     * @param img_name
     * @return
     */
    public static InputStream getImgFromStream(String img_name, String actionName) {

        switch (actionName) {
            case "hurt":
                return overlayAndCenter(img_name, "hurt", false, false);
            case "help_me":
                return overlayAndCenter(img_name, "help_me", true, false);
            case "dead":
                return overlayAndCenter(img_name, "dead", true, true);
            default:
                return getImgFromStream(img_name);
        }
    }

    /**
     * 合并两张图片，将第二张图片按比例缩放并居中叠加在第一张图片上。
     * 
     * @param img_name1 第一张图片的文件名（JPEG格式）
     * @param img_name2 第二张图片的文件名（PNG格式，带透明）
     * @param charShow  是否第二张图是展示文字
     * @param gray      是否是灰度图
     * @return 合并后的图片输入流
     */
    private static InputStream overlayAndCenter(String img_name1, String img_name2, boolean charShow, boolean gray) {
        //System.out.println("合成图片 " + img_name1 + "," + img_name2 + "," + charShow + "," + gray);
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        BufferedImage image1 = null;
        BufferedImage image2 = null;
        BufferedImage combinedImage = null;
        try {
            // 读取图片只能读流1（JPEG）
            //System.out.println("/resources/img/" + img_name1 + ".jpg");
            image1 = ImageIO.read(START_CLASS.getResourceAsStream("/resources/img/" + img_name1 + ".jpg"));
            // 读取图片只能读流2（带透明的PNG）
            image2 = ImageIO.read(START_CLASS.getResourceAsStream("/resources/img/effect/" + img_name2 + ".png"));

            // 如果gray为真，将图片1去色
            if (gray) {
                BufferedImage grayImage = new BufferedImage(image1.getWidth(), image1.getHeight(),
                        BufferedImage.TYPE_BYTE_GRAY);
                Graphics g = grayImage.getGraphics();
                g.drawImage(image1, 0, 0, null);
                g.dispose();
                image1.flush(); // 释放原始图片1
                image1 = grayImage;
            }

            // 获取图片1的宽高
            int width = image1.getWidth();
            int height = image1.getHeight();

            // 计算图片2的缩放比例（按比例缩放，保持不超过图片1的范围）
            double scale;
            if (charShow) {
                // 计算图片2的缩放比例（宽度缩放为图片1的1/3）
                scale = (double) width / 2.5 / image2.getWidth();
            } else {
                double scaleX = (double) width / image2.getWidth();
                double scaleY = (double) height / image2.getHeight();
                scale = Math.min(scaleX, scaleY); // 选择较小的缩放比例，保证图片2不超出图片1
            }

            // 缩放后的宽高
            int newWidth = (int) (image2.getWidth() * scale);
            int newHeight = (int) (image2.getHeight() * scale);

            // 计算居中的偏移量
            int xOffset = (width - newWidth) / 2;
            int yOffset = (height - newHeight) / 2;

            // 创建一个新的 BufferedImage，与图片1大小相同
            combinedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            // 使用 Graphics2D 处理合并
            Graphics2D g = combinedImage.createGraphics();

            // 启用抗锯齿
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            // 绘制图片1（作为背景）
            g.drawImage(image1, 0, 0, null);

            // 绘制图片2（按比例缩放并居中）
            g.drawImage(image2, xOffset, yOffset, newWidth, newHeight, null);

            // 释放 Graphics2D 对象
            g.dispose();

            // 将合并后的图片写入 ByteArrayOutputStream
            ImageIO.write(combinedImage, "PNG", bs);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (image1 != null) {
                image1.flush(); // 释放图片1
            }
            if (image2 != null) {
                image2.flush(); // 释放图片2
            }
            if (combinedImage != null) {
                combinedImage.flush(); // 释放合并后的图片
            }
        }

        return new ByteArrayInputStream(bs.toByteArray());
    }

    // /**
    // * 更新缓存图片
    // *
    // * @param img_name
    // * @param msgObj
    // */
    // public static void renewImg(String img_name, MsgObj msgObj) {
    // // 这里要起线程，不然就阻塞了
    // final int editCount = msgObj.editCount.get();
    // new Thread(new Runnable() {

    // @Override
    // public void run() {

    // try {
    // Thread.sleep(2000);
    // String fileId = (String) msgObj.getReturnAttr(ReturnType.ImgFileId, 10);
    // if (msgObj.editCount.get() != editCount + 1) {// 避免中途被改过
    // System.out.println(msgObj.editCount.get() + "不等于 editCount+1：" + (editCount +
    // 1));
    // return;
    // }
    // System.out.println(img_name + "返回的fileId：" + fileId);

    // if (!img_name.equals(msgObj.imgWatingCallBack.get())) {// 避免中途被改过
    // System.out.println(img_name + "不等于 msgObj.imgWating.get()：" +
    // msgObj.imgWatingCallBack.get());
    // return;
    // }
    // if (fileId != null) {
    // IMG_CHACH.put(img_name, fileId);
    // Database.put(img_name, fileId);

    // }

    // } catch (Exception e) {
    // e.printStackTrace();
    // } finally {
    // msgObj.imgWatingCallBack.set(null);
    // }
    // }

    // }).start();

    // }

}
