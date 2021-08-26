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

import org.b3log.latke.logging.Logger;

import java.util.Calendar;
import java.util.Map;

/**
 * <p> Authentication util </p>
 *
 * @author changming.Y <changming.yang.ah@gmail.com>
 * @since 2019-07-10 09:54
 */
public class AuthUtil {

    public static Logger logger = Logger.getLogger(AuthUtil.class);
    public static AES aes = AES.getInstance();
    public static String SESSION_PREFIX = "app:session:";
    public static int SESSION_TIMEOUT_SECOND = 30*24*3600; //1个月

    /**
     * build token, and return to client after client login system
     * todo why build token for diff two times?
     *
     * @param userId
     * @return
     */
    public static String buildToken(String userId){
        if (userId==null || "".equals(userId)){
            return "";
        }
        Calendar calendar=Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, (24 * 7));
        String temp = userId + ";" + Dates.sampleTimeFormat(calendar.getTime());
        byte[] data = aes.Encrytor(temp);
        String token= Base64.encodeBase64(data);
        return token;
    }

    /**
     * check in by userId
     *
     * @param token
     * @param userId
     * @return
     */
    public static boolean checkIn(String token, String userId){
        if (token==null || userId==null || "".equals(token) || "".equals(userId)){
            return false;
        }
        return token.toLowerCase().equals(buildToken(userId).toLowerCase());
    }

    /**
     * fetch userId by reverse token
     *
     * @param token
     * @return
     */
    public String reverseToken(String token){
        if (token == null){
           return null;
        }
        byte[] data = Base64.decodeBase64ToByte(token);
        byte[] temp;
        try {
            temp = aes.Decryptor(data);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
        String _temp = new String(temp);
        String userId;
        if (_temp.indexOf(";")>0){
            userId = _temp.substring(0,_temp.indexOf(";") );
        }else{
            throw new RuntimeException();

        }
        return userId;
    }

    public static String buildSignature(Map params){
        return null;
    }
}
