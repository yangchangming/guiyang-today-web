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

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <p> Date and times utilities </p>
 *
 * @author changming.Y <changming.yang.ah@gmail.com>
 * @since 2021-08-26 08:58
 */
public final class Dates {

    private static final Logger LOGGER = Logger.getLogger(Dates.class);

    public final static SimpleDateFormat SampleTimeFormat = new SimpleDateFormat("yyyyMMddHHmmss");

    /**
     * 将时间对象转换为yyyyMMddHHmmss格式用的字符串时间
     *
     * @param date
     * @return
     */
    public static String sampleTimeFormat(Date date){
        return SampleTimeFormat.format(date);
    }

}
