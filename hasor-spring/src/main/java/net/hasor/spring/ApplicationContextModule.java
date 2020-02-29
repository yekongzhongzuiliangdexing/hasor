/*
 * Copyright 2002-2005 the original author or authors.
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
package net.hasor.spring;
import net.hasor.core.ApiBinder;
import net.hasor.core.AppContext;
import net.hasor.core.Module;
import net.hasor.core.provider.InstanceProvider;
import net.hasor.utils.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Hasor 集成 Spring插件
 * @version : 2016年2月15日
 * @author 赵永春 (zyc@hasor.net)
 */
public class ApplicationContextModule implements Module {
    public static   String                       DefaultHasorBeanName = AppContext.class.getName();
    protected final Supplier<ApplicationContext> applicationContext;

    public ApplicationContextModule(ApplicationContext applicationContext) {
        this(InstanceProvider.of(Objects.requireNonNull(applicationContext, "spring applicationContext is null.")));
    }

    public ApplicationContextModule(Supplier<ApplicationContext> applicationContext) {
        this.applicationContext = Objects.requireNonNull(applicationContext, "spring applicationContext is null.");
    }

    @Override
    public final void loadModule(ApiBinder apiBinder) throws Throwable {
        apiBinder.bindType(ApplicationContext.class).toProvider(this.applicationContext);
        ApplicationContext app = this.applicationContext.get();
        String[] names = app.getBeanDefinitionNames();
        //
        List<String> exportList = new ArrayList<>();
        for (String name : names) {
            if (this.isExportBean(name)) {
                exportList.add(name);
            }
        }
        exportList = this.exportBeanNames(exportList);
        if (exportList != null) {
            for (String name : exportList) {
                if (app.containsBean(name)) {
                    Class<?> beanType = Object.class;
                    if (app instanceof ConfigurableApplicationContext) {
                        String className = ((ConfigurableApplicationContext) app).getBeanFactory().getBeanDefinition(name).getBeanClassName();
                        if (StringUtils.isNotBlank(className)) {
                            beanType = app.getClassLoader().loadClass(className);
                        }
                    }
                    apiBinder.bindType(beanType).idWith(name).toProvider(new SpringBeanAgent<>(name, this.applicationContext));
                }
            }
        }
        //
    }

    private static class SpringBeanAgent<T> implements Supplier<T> {
        private String                       beanID;
        private Supplier<ApplicationContext> applicationContext;

        public SpringBeanAgent(String beanID, Supplier<ApplicationContext> applicationContext) {
            this.beanID = beanID;
            this.applicationContext = applicationContext;
        }

        @Override
        public T get() {
            return (T) applicationContext.get().getBean(this.beanID);
        }
    }

    /**
     * 判断该Bean是否可以被导出到Hasor
     * @param beanName 将要被导出的Bean Name
     * @return 是否导出到Hasor容器中。
     */
    protected boolean isExportBean(String beanName) {
        return false;
    }

    /**
     * 确认最终到导出到Hasor中的Bean列表。
     * @param exportList 最终到导出到Hasor中的Bean列表
     */
    protected List<String> exportBeanNames(List<String> exportList) {
        return exportList;
    }
}
