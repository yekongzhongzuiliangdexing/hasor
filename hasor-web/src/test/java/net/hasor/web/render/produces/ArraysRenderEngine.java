/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.web.render.produces;
import com.alibaba.fastjson.JSON;
import net.hasor.core.AppContext;
import net.hasor.utils.StringUtils;
import net.hasor.web.Invoker;
import net.hasor.web.RenderEngine;
import net.hasor.web.RenderInvoker;
import net.hasor.web.valid.ValidInvoker;

import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;
//
public class ArraysRenderEngine implements RenderEngine {
    private Set<String> templateSet = new HashSet<>();
    public ArraysRenderEngine(List<String> readLines) {
        this.templateSet.addAll(readLines.stream().filter(StringUtils::isNotBlank).collect(Collectors.toList()));
    }
    @Override
    public void initEngine(AppContext appContext) throws Throwable {
    }
    @Override
    public void process(RenderInvoker invoker, Writer writer) throws Throwable {
        //
        Map<String, Object> jsonData = new HashMap<>();
        invoker.keySet().stream().filter(s -> {                     //
            return !Invoker.ROOT_DATA_KEY.equalsIgnoreCase(s) &&    //
                    !ValidInvoker.VALID_DATA_KEY.equalsIgnoreCase(s) &&     //
                    !Invoker.REQUEST_KEY.equalsIgnoreCase(s) &&     //
                    !Invoker.RESPONSE_KEY.equalsIgnoreCase(s);      //
        }).peek(s -> {
            Object valueData = invoker.get(s);
            try {
                jsonData.put(s, JSON.parseObject((valueData == null) ? null : valueData.toString()));
            } catch (Exception e) {
                jsonData.put(s, valueData);
            }
        }).forEach(s -> {
            //
        });
        //
        jsonData.put("engine_renderTo", invoker.renderTo());
        jsonData.put("engine_viewType", invoker.viewType());
        writer.write(JSON.toJSONString(jsonData));
    }
    @Override
    public boolean exist(String template) throws IOException {
        return templateSet.contains(template);
    }
}