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
package org.b3log.symphony.service;

import org.b3log.latke.logging.Logger;
import org.b3log.latke.service.annotation.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * <p> Redis service implements </p>
 *
 * @author changming.Y <changming.yang.ah@gmail.com>
 * @since 2019-08-11 18:50
 */
@Service
public class RedisService {

    private Logger logger = Logger.getLogger(RedisService.class);

    /**
     * 默认缓存时间 1天
     */
    private int defaultExpTime = 1 * 24 * 3600;

    public int getDefaultExpTime() {
        return defaultExpTime;
    }

    public void setDefaultExpTime(int defaultExpTime) {
        this.defaultExpTime = defaultExpTime;
    }

    @Autowired
    private RedisFacade redisFacade;

    public RedisFacade getRedisFacade() {
        if (redisFacade==null){
           Object obj =  BeanFactoryAware.getBean("redisFacade");
           if (obj!=null && obj instanceof RedisFacade){
               return (RedisFacade) obj;
           }else {
               throw new CacheException("No RedisFacade instance be found.");
           }
        }
        return redisFacade;
    }

    public void setRedisFacade(RedisFacade redisFacade) {
        this.redisFacade = redisFacade;
    }


    @Override
    public boolean isSupportKeyPatter() {
        return true;
    }

    @Override
    public <T> void setCache(String key, T value, int exp, TimeUnit unit) throws CacheException {
        try {
            if(exp <= 0){
                if (unit.compareTo(TimeUnit.SECONDS) == 0){
                    exp = defaultExpTime;
                }else{
                    throw new IllegalArgumentException("cache's expire setting: exp not allowed <=0 ");
                }
            }
            redisFacade.getValueCache(key).set(value, exp, unit);
            logger.info("put cache: " + key + ":" + value);
        }catch (Exception ex){
            logger.warn("setCache error: ", ex);
            throw new CacheException("cache error: " + ex.getMessage(), ex);
        }
    }

    @Override
    public <T> void setCache(String key, T value, int exp) throws CacheException {
        setCache(key, value, exp, TimeUnit.SECONDS);
    }

    @Override
    public <T> void setCache(String key, T value) throws CacheException {
        setCache(key, value, defaultExpTime);
    }

    @Override
    public <T> T getCache(String key) throws CacheException {
        try{
            return (T) redisFacade.getValueCache(key).get();
        }catch (Exception ex){
            logger.warn("getCache error: ", ex);
            throw new CacheException("cache error:" + ex.getMessage(), ex);
        }
    }

    @Override
    public void removeCache(String key) throws CacheException {
        try {
            redisFacade.getValueCache(key).expire(3, TimeUnit.MILLISECONDS);
        }catch (Exception ex){
            logger.warn("removeCache error: ", ex);
            throw new CacheException("cache error:" + ex.getMessage(), ex);
        }
    }

    @Override
    public boolean hasCache(String key) throws CacheException {
        try {
            return redisFacade.getValueCache(key).get() != null;
        }catch (Exception ex){
            logger.warn("hasCache error: ", ex);
            throw new CacheException("cache error:" + ex.getMessage(), ex);
        }
    }

    @Override
    public <T> List<T> getCacheList(String keyPatter) throws CacheException {
        List<T> list = null;
        try{
            Set<String> keys = redisFacade.getKeys(keyPatter);
            if (keys != null && keys.size()>0) {
                list = new ArrayList<>();
                for (String key : keys ) {
                    list.add((T) redisFacade.getValueCache(key).get());
                }
            }
        }catch (Exception ex){
            logger.warn("getCacheList error: ", ex);
            throw new CacheException("cache error:" + ex.getMessage(), ex);
        }
        return list;
    }

    @Override
    public Collection<String> getKeys(String keyPatter) {
        try{
            Set<String> keys = redisFacade.getKeys(keyPatter);
            return keys;
        }catch (Exception ex){
            logger.warn("getKeys error: ", ex);
            throw new CacheException("cache error:" + ex.getMessage(), ex);
        }
    }

    @Override
    public boolean hasKey(String key) throws CacheException {
        try{
            return redisFacade.getTemplate().hasKey(key);
        }catch (Exception ex){
            logger.warn("hasKey error: ", ex);
            throw new CacheException("cache error:" + ex.getMessage(), ex);
        }
    }
}
