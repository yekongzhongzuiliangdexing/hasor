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
package net.hasor.core.context;
import net.hasor.core.ApiBinder;
import net.hasor.core.Environment;
import net.hasor.core.container.BeanContainer;
import net.hasor.core.context.beans.ContextShutdownListenerBean;
import net.hasor.core.context.beans.ContextStartListenerBean;
import net.hasor.core.environment.StandardEnvironment;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
public class ContextListenerTest {
    private TemplateAppContext appContext;
    @Before
    public void testBefore() throws IOException {
        final StandardEnvironment env = new StandardEnvironment();
        final BeanContainer container = new BeanContainer();
        this.appContext = new TemplateAppContext() {
            @Override
            protected BeanContainer getContainer() {
                return container;
            }
            @Override
            public Environment getEnvironment() {
                return env;
            }
        };
    }
    //
    @Test
    public void builderTest1() throws Throwable {
        //
        ContextStartListenerBean startListener = new ContextStartListenerBean();
        ContextShutdownListenerBean shutdownListener = new ContextShutdownListenerBean();
        //
        ApiBinder apiBinder = appContext.newApiBinder();
        apiBinder.bindType(ContextStartListener.class).toInstance(startListener);
        apiBinder.bindType(ContextShutdownListener.class).toInstance(shutdownListener);
        //
        assert startListener.getI() == 0;
        appContext.doStart();
        assert startListener.getI() == 1;
        appContext.doStartCompleted();
        assert startListener.getI() == 2;
        //
        //
        assert shutdownListener.getI() == 0;
        appContext.doShutdown();
        assert shutdownListener.getI() == 1;
        appContext.doShutdownCompleted();
        assert shutdownListener.getI() == 2;
    }
    //
    @Test
    public void builderTest2() throws Throwable {
        appContext.start();
        appContext.start();
        //
        appContext.shutdown();
        appContext.shutdown();
    }
}