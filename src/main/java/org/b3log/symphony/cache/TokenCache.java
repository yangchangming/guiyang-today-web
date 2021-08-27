package org.b3log.symphony.cache;


import org.b3log.latke.logging.Logger;
import org.b3log.symphony.util.JSONs;
import org.json.JSONObject;
import org.omg.CosNaming.NamingContextExtPackage.StringNameHelper;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

/**
 * Token Cache about local
 */
@Named
@Singleton
public class TokenCache {

    private static final Logger logger = Logger.getLogger(TokenCache.class);
    private static final Map<String, String> ID_CACHE = new HashMap<>();
    public static final String SESSION_PREFIX = "app:session:";


    public String get(String userId){
        if (userId==null || "".equals(userId)){
            return null;
        }
        return ID_CACHE.get(userId);
    }

    public void put(String userId, String token){
        ID_CACHE.put(userId, token);
    }






}
