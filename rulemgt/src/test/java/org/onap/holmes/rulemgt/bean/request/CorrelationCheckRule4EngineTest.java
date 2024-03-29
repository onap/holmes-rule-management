/**
 * Copyright 2017 ZTE Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.holmes.rulemgt.bean.request;

import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class CorrelationCheckRule4EngineTest {
    @Test
    public void getterAndSetter4Content(){
        final String value = "content";
        CorrelationCheckRule4Engine correlationCheckRule4Engine = new CorrelationCheckRule4Engine();
        correlationCheckRule4Engine.setContent(value);
        assertThat(correlationCheckRule4Engine.getContent(), equalTo(value));
    }
}