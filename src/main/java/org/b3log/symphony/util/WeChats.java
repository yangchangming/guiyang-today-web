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

import com.google.gson.JsonObject;
import com.sun.beans.WeakCache;
import org.json.JSONException;
import org.json.JSONObject;

import javax.security.auth.login.AccountException;

/**
 * <p> WeChat utilities </p>
 *
 * @author changming.Y <changming.yang.ah@gmail.com>
 * @since 2021-08-24 14:46
 */
public final class WeChats {

    final private static String appId = "wxaf4bef7a754a2e63";
    final private static String appSecret = "5e30821f8778fb2d8e2693dd70d24c99";

    final private static String WECHAT_CODE_REQUEST_URL = "https://open.weixin.qq.com/connect/qrconnect?appid=APPID&redirect_uri=REDIRECT_URI&response_type=code&scope=SCOPE&state=STATE#wechat_redirect";
    final private static String WECHAT_ACCESS_TOKEN_REQUEST_URL = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="+ appId + "&secret="+ appSecret +"&grant_type=authorization_code&code=";
    final private static String WECHAT_USER_INFO_REQUEST_URL = "https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID";

    /**
     * Constructor
     */
    private WeChats(){
    }

    /**
     * Get code from wechat open platform
     */
    public static void getCode(){
    }

    /**
     * Get access token from wechat open platform
     *
     * @param code
     * @return
     */
    public static AccessTokenModel getAccessToken(String code){
        if (code==null || "".equals(code)){
            return null;
        }
        StringBuffer accessTokenUrl = new StringBuffer(WECHAT_ACCESS_TOKEN_REQUEST_URL);
        accessTokenUrl.append(code);
        String content = HttpUtil.get(accessTokenUrl.toString());
        WeChats.AccessTokenModel accessTokenModel = new WeChats.AccessTokenModel();
        JSONObject result = null;
        try {
            result = new JSONObject(content);
            if (result!=null){
                accessTokenModel.setAccessToken(result.getString("access_token"));
                accessTokenModel.setOpenId(result.getString("openid"));
                accessTokenModel.setExpires(result.getLong("expires_in"));
                accessTokenModel.setRefreshToken(result.getString("refresh_token"));
                accessTokenModel.setUnionId(result.getString("unionid"));
            }
            return accessTokenModel;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return accessTokenModel;
    }

    public static WeChatUserInfoModel getUserInfo(String openId, String accessToken){








        return null;
    }

    public static class AccessTokenModel{
        private String accessToken;
        private long expires;
        private String refreshToken;
        private String openId;
        private String[] scopes;
        private String unionId;

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public long getExpires() {
            return expires;
        }

        public void setExpires(long expires) {
            this.expires = expires;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }

        public String getOpenId() {
            return openId;
        }

        public void setOpenId(String openId) {
            this.openId = openId;
        }

        public String[] getScopes() {
            return scopes;
        }

        public void setScopes(String[] scopes) {
            this.scopes = scopes;
        }

        public String getUnionId() {
            return unionId;
        }

        public void setUnionId(String unionId) {
            this.unionId = unionId;
        }
    }

    public static class WeChatUserInfoModel{
        private String openId;
        private String unionId;
        private String nickName;
        private String sex;
        private String province;
        private String city;
        private String country;
        private String headImgUrl;
        private String[] privileges;

        public String getOpenId() {
            return openId;
        }

        public void setOpenId(String openId) {
            this.openId = openId;
        }

        public String getUnionId() {
            return unionId;
        }

        public void setUnionId(String unionId) {
            this.unionId = unionId;
        }

        public String getNickName() {
            return nickName;
        }

        public void setNickName(String nickName) {
            this.nickName = nickName;
        }

        public String getSex() {
            return sex;
        }

        public void setSex(String sex) {
            this.sex = sex;
        }

        public String getProvince() {
            return province;
        }

        public void setProvince(String province) {
            this.province = province;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getHeadImgUrl() {
            return headImgUrl;
        }

        public void setHeadImgUrl(String headImgUrl) {
            this.headImgUrl = headImgUrl;
        }

        public String[] getPrivileges() {
            return privileges;
        }

        public void setPrivileges(String[] privileges) {
            this.privileges = privileges;
        }
    }
}

