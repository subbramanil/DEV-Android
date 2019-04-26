package to.dev.dev_android.view.main.view;

import okhttp3.*;

import java.io.File;
import java.io.IOException;

public class FileUploader {

    public static void uploadProfilePic(String filePath) throws IOException {
        final OkHttpClient client = new OkHttpClient();
        File file = new File(filePath);

        Request request = new Request.Builder()
                .url("https://api.github.com/markdown/raw")
                .post(RequestBody.create(MEDIA_TYPE_MARKDOWN, file))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            } else {
                System.out.println(response.code());
            }

        }
    }

    public static final MediaType MEDIA_TYPE_MARKDOWN= MediaType.parse("text/x-markdown; charset=utf-8");


/*
    public void run() throws Exception {
        File file = new File("README.md");

        Request request = new Request.Builder()
                .url("https://api.github.com/markdown/raw")
                .post(RequestBody.create(MEDIA_TYPE_MARKDOWN, file))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            System.out.println(response.body().string());
        }
    }*/
}
