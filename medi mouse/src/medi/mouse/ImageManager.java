package medi.mouse;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
 
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.ByteArrayBuffer;
 
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
/**
 * ImageManager
 * @author will
 * not used (yet)
 */
public class ImageManager {
 
        private final String PATH = "/data/medi.mouse/";  //put the downloaded file here
       
 
        public static void DownloadFromUrl(String imageURL, String fileName) {  //this is the downloader method
                try {
                		System.setProperty("http.keepAlive", "false");
                		Log.d("ImageManager",URLEncoder.encode(imageURL,"UTF-8"));
                        URL url = new URL(imageURL); //you can write here any link
                        
                        File file = new File(fileName);
 
                        long startTime = System.currentTimeMillis();
                        Log.d("ImageManager", "download begining");
                        Log.d("ImageManager", "download url:" + url);
                        Log.d("ImageManager", "downloaded file name:" + fileName);
                        /* Open a connection to that URL. */
                        URLConnection ucon = url.openConnection();
                        HttpGet httpRequest = null;

                        try {
							httpRequest = new HttpGet(url.toURI());
						} catch (URISyntaxException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

                        HttpClient httpclient = new DefaultHttpClient();
                        HttpResponse response = (HttpResponse) httpclient.execute(httpRequest);
                        
                        HttpEntity entity = response.getEntity();
                        BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);
                        InputStream is = bufHttpEntity.getContent();
                        
                        
                        
                        Bitmap bm = BitmapFactory.decodeStream(is,null,null);
                        int size = bm.getWidth() * bm.getHeight();
                        bufHttpEntity.consumeContent();
                        
                        ByteArrayOutputStream out = new ByteArrayOutputStream(size);
                        bm.compress(Bitmap.CompressFormat.JPEG, 100, out);

                        byte[] ret = out.toByteArray();
                        bm.recycle();
                        
                        /* Convert the Bytes read to a String. */
                        File workdir = new File("/sdcard/data/medi.mouse/");
                        workdir.mkdirs();
                        FileOutputStream fos = new FileOutputStream("/sdcard/data/medi.mouse/"+file);
;
                        fos.write(ret);
                        for(int x = 0; x<ret.length ; x++){
                        	System.out.println(String.format("0x%02X", ret[x]));
                        }
                        fos.close();
                        Log.d("ImageManager", "download ready in"
                                        + ((System.currentTimeMillis() - startTime) / 1000)
                                        + " sec");
 
                } catch (IOException e) {
                
                        Log.d("ImageManager", "Error: " + e);
                }
 
        }
}