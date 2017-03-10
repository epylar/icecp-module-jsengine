/*
 * Copyright (c) 2017 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intel.icecp.module.jsengine;

import com.intel.icecp.core.Module.StopReason;
import com.intel.icecp.core.Node;
import com.intel.icecp.core.attributes.Attributes;
import com.intel.icecp.core.attributes.IdAttribute;
import com.intel.icecp.core.attributes.ModuleStateAttribute;
import com.intel.icecp.core.mock.MockChannels;
import com.intel.icecp.module.jsengine.attributes.CodeAttribute;
import com.intel.icecp.node.AttributesFactory;
import com.intel.icecp.node.NodeFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;

public class JSEngineModuleTest {
    private static final Logger LOGGER = LogManager.getLogger();
    private Node node;
    private JSEngineModule module;
    private Attributes attributes;

    @Before
    public void beforeTest() throws Exception {
        module = new JSEngineModule();
        node = NodeFactory.buildMockNode();
        attributes = AttributesFactory.buildEmptyAttributes(new MockChannels(), URI.create("icecp:/attributes/test"));
        attributes.add(new IdAttribute(99));
        attributes.add(new ModuleStateAttribute());
    }

    @Test
    public void testRun() throws Exception {
        try {
            // count down the latch first so that it will not stuck forever.
            // We expect this to throw an exception since the attributes will not be set in the module
            // TODO fix this latch
            module.stop(StopReason.NODE_SHUTDOWN);
        } catch (Exception e) {
            LOGGER.info("Attributes do not exist");
        }

        String jsscript = "print('this is ok')";
        attributes.add(new CodeAttribute(jsscript));
        module.run(node, attributes);
        module.stop(StopReason.NODE_SHUTDOWN);
    }
}
