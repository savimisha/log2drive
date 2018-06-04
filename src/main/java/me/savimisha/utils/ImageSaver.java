package me.savimisha.utils;

import me.savimisha.Config;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class ImageSaver {
    private static final String TAG = ImageSaver.class.getSimpleName();

    public interface Callback {
        void onSuccess(File file);
        void onFailure(Exception e);
    }

    public static void save(String url, final Callback callback) {
        Temp.check();
        String[] tmp = url.split("/");
        String name = tmp[tmp.length - 1];
        String fileName = Translit.toTranslit(name);
        final String format = ImageFormat.getName(name);
        String encodedName = null;
        if (fileName.equals(name)) {
            encodedName = name;
        } else
            try {
                encodedName = URLEncoder.encode(name, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                DataBase.error(TAG, "Error with encoding photo name.", e);
                callback.onFailure(e);
                return;
            }
        int lastSlash = url.lastIndexOf("/");
        String encodedLink = url.substring(0, lastSlash + 1) + encodedName;
        final String path = Config.TMP_DIR + "/saved_images/" + fileName;
        Request request = new Request.Builder()
                .url(encodedLink)
                .build();
        Http.client().newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                DataBase.error(TAG, "Image loading error.", e);
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream inputStream = response.body().byteStream();
                try {
                    BufferedImage img = ImageIO.read(inputStream);
                    File dir = new File(Config.TMP_DIR + "/saved_images");
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    File file = new File(path);
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    ImageIO.write(img, format, file);

                    response.body().close();

                    callback.onSuccess(file);
                } catch (Exception e) {
                    DataBase.error(TAG,  "Image saving error.", e);
                    callback.onFailure(e);
                }
            }
        });
    }
}
