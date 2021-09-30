/*
 *  Copyright 2015-2018 DataVens, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.b3log.symphony.util;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * <p> Qi niu util </p>
 *
 * @author changming.Y <changming.yang.ah@gmail.com>
 * @since 2019-07-31 10:58
 */
public class QiniuUtil {

    private static Logger logger = Logger.getLogger(QiniuUtil.class);
    private static String ACCESS_KEY = Symphonys.get("qiniu.accessKey");
    private static String SECRET_KEY = Symphonys.get("qiniu.secretKey");
    private static String bucketName = Symphonys.get("qiniu.bucket");
    private static Auth auth = Auth.create(ACCESS_KEY, SECRET_KEY);
    private static String templateId = "1175988694327234560";

    /**
     * build upload token for app
     * 1. 文件上传后返回格式如： {"key":"qiniu.jpg","hash":"Ftgm-CkWePC9fzMBTRNmPMhGBcSV","bucket":"if-bc","fsize":39335}
     * 2. 文件名设定为 文件hash值 + 源文件扩展名
     * @return
     */
    public static String buildUploadToken(){
        if (auth==null){
            auth = Auth.create(ACCESS_KEY, SECRET_KEY);
        }
        StringMap putPolicy = new StringMap();
        putPolicy.put("returnBody", "{\"key\":\"$(key)\",\"hash\":\"$(etag)\",\"bucket\":\"$(bucket)\",\"fsize\":$(fsize)}");
        putPolicy.put("saveKey", "${etag}${ext}");

//        putPolicy.put("callbackUrl", "http://api.example.com/qiniu/upload/callback");
//        putPolicy.put("callbackBody", "{\"key\":\"$(key)\",\"hash\":\"$(etag)\",\"bucket\":\"$(bucket)\",\"fsize\":$(fsize)}");
//        putPolicy.put("callbackBodyType", "application/json");

        long expireSeconds = 3600;
        return auth.uploadToken(bucketName, null, expireSeconds, putPolicy);
    }


    /**
     * build verify code
     *
     * @param telephone
     * @return
     */
    public static String buildCode(String telephone){
        if (telephone==null || "".equals(telephone)){
            telephone = "13984860374";
        }
        return RandomStringUtils.random(4, telephone);
    }

    /**
     * send verify code
     *
     * @param telephone
     * @return
     */
//    public static String sendVerifyCode(String telephone){
//        if (telephone==null || "".equals(telephone)){
//            return null;
//        }
//        SmsManager smsManager = new SmsManager(auth);
//        String verifyCode = buildCode(telephone);
//        try {
//            Map<String, String> map = new HashMap<>();
//            map.put("code", verifyCode);
//            Response resp = smsManager.sendMessage(templateId, new String[]{telephone}, map);
//            return (resp.bodyString()!=null && !"".equals(resp.bodyString())) ? verifyCode : "" ;
//        } catch (QiniuException e) {
//            logger.error("Send verify code failure." + e.getMessage());
//        }
//        return "";
//    }

    /**
     * 上传本地文件到七牛云
     * 1. 统一采用文件hash值作为文件名
     * 2. 云端文件名：文件hash值 + 源文件扩展名
     * 3. 同样的文件不会被反复上传，云端只有一份
     * 4. 返回 {"key":"FgVd-bfLV0SujlXvqTy-ROxf2g1v.png","hash":"FgVd-bfLV0SujlXvqTy-ROxf2g1v","bucket":"runnf","fsize":137478}
     *
     * @param localFilePath
     * @return
     */
    public static String upload4LocalFile(String localFilePath){
        Configuration cfg = new Configuration(Region.huanan());
        UploadManager uploadManager = new UploadManager(cfg);
        try {
            Response response = uploadManager.put(localFilePath, null, buildUploadToken());
            DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
            return response.bodyString();
        } catch (QiniuException ex) {
            Response r = ex.response;
            logger.error(r.toString());
        }
        return null;
    }

    /**
     * 上传网络资源文件至七牛云
     *
     * @param absolutePath http://xsfsfsfs.jpg
     * @return
     */
    public static String upload4AbsolutePathFile(String absolutePath){
        Configuration cfg = new Configuration(Region.huanan());
        UploadManager uploadManager = new UploadManager(cfg);
        try {
            byte[] data = urlToByteArr(absolutePath);
            Response response = uploadManager.put(data, null, buildUploadToken());
            DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
            return response.bodyString();
        } catch (QiniuException ex) {
            Response r = ex.response;
            logger.error(r.toString());
        } catch (IOException ex){
            logger.error(ex.getCause().getMessage());
        }
        return null;
    }

    public static String upload4AbsolutePathFile(byte[] data) {
        Configuration cfg = new Configuration(Region.huanan());
        cfg.useHttpsDomains = false;
        UploadManager uploadManager = new UploadManager(cfg);
        try {
            Response response = uploadManager.put(data, null, buildUploadToken());
            DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
            return response.bodyString();
        } catch (QiniuException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            logger.error(ex.getCause().getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 网络资源转为byte数组
     *
     * @param resourceURL
     * @return
     */
    public static byte[] urlToByteArr(String resourceURL){
        try {
            URL url = new URL(resourceURL);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5 * 1000);
            InputStream inStream = conn.getInputStream();
            byte[] data = readInputStream(inStream);
            return data;
        } catch (MalformedURLException e) {
            logger.error(e.getCause().getMessage());
        } catch (ProtocolException ex){
            logger.error(ex.getCause().getMessage());
        } catch (IOException ex){
            logger.error(ex.getCause().getMessage());
        }
        return null;
    }


    public static byte[] readInputStream(InputStream inStream) throws IOException{
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while( (len=inStream.read(buffer)) != -1 ){
            outStream.write(buffer, 0, len);
        }
        inStream.close();
        return outStream.toByteArray();
    }

    public static void main(String[] args){
    }
}
