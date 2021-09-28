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

import org.nutz.http.Http;
import org.nutz.http.Response;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.zip.GZIPInputStream;

/**
 * <p> Http util </p>
 *
 * @author changming.Y <changming.yang.ah@gmail.com>
 * @since 2019-07-10 14:38
 */
public class HttpUtil {

    public static String zipType = "gzip";

    /**
     * retrieve content from http request body
     *
     * @param request
     * @return
     */
    public static String retrieveBody(HttpServletRequest request){
        if (request==null){
            return null;
        }
        String result;
        InputStream body = null;
        String contentEncoding = request.getHeader("Content-Encoding");
        ByteArrayOutputStream bodyStream = null;
        try {
            if (contentEncoding!=null && contentEncoding.equals(zipType)){
                body = new GZIPInputStream(request.getInputStream());
            }else {
                body = new BufferedInputStream(request.getInputStream());
            }
            bodyStream = new ByteArrayOutputStream();
            byte[] bytes = new byte[512];
            int temp = body.read(bytes) ;
            while (temp != -1) {
                bodyStream.write(bytes,0,temp);
                temp=body.read(bytes) ;
            }
        }catch (IOException e){
            throw new RuntimeException(e.getCause());
        }finally {
            try {
                if (body!=null){
                    body.close();
                }
                if (bodyStream!=null){
                    bodyStream.close();
                }
            }catch (IOException e){
                throw new RuntimeException(e.getCause());
            }
        }
        try {
            result = new String(bodyStream.toByteArray(), "utf-8");
        }catch (UnsupportedEncodingException e){
            throw new RuntimeException(e.getCause());
        }
        return result;
    }

    public static String get(String url){
        Response response = Http.get(url);
        return response.getContent();
    }



}
