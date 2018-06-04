package me.savimisha.utils;


public class ImageFormat {
    static String getName(String name){
        String format = null;
        if (name.endsWith("jpg") || name.endsWith("JPG") || name.endsWith("Jpg"))
            format = "jpg";
        if (name.endsWith("jpeg") || name.endsWith("JPEG") || name.endsWith("Jpeg"))
            format = "jpeg";
        if (name.endsWith("png") || name.endsWith("PNG") || name.endsWith("Png"))
            format = "png";
        if (name.endsWith("gif") || name.endsWith("GIF") || name.endsWith("Gif"))
            format = "gif";
        if (name.endsWith("bmp") || name.endsWith("BMP") || name.endsWith("Bmp"))
            format = "bmp";
        if (format == null) {
            DataBase.error(ImageFormat.class.getSimpleName(), "Не удалось определить формат картинки: " + name);
            return null;
        }
        return format;
    }

    public static boolean isImage(String a) {
        return  (a.endsWith(".jpg") || a.endsWith(".png") || a.endsWith(".gif") || a.endsWith(".jpeg") || a.endsWith(".bmp")
                || a.endsWith(".JPG") || a.endsWith(".PNG") || a.endsWith(".GIF") || a.endsWith(".JPEG") || a.endsWith(".BMP")
                || a.endsWith(".Bmp") || a.endsWith(".Jpg") || a.endsWith(".Jpeg") || a.endsWith(".Png") || a.endsWith(".Gif"));
    }
}
