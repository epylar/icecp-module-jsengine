/*
 * ******************************************************************************
 *
 * INTEL CONFIDENTIAL
 *
 * Copyright 2013 - 2016 Intel Corporation All Rights Reserved.
 *
 * The source code contained or described herein and all documents related to
 * the source code ("Material") are owned by Intel Corporation or its suppliers
 * or licensors. Title to the Material remains with Intel Corporation or its
 * suppliers and licensors. The Material contains trade secrets and proprietary
 * and confidential information of Intel or its suppliers and licensors. The
 * Material is protected by worldwide copyright and trade secret laws and treaty
 * provisions. No part of the Material may be used, copied, reproduced,
 * modified, published, uploaded, posted, transmitted, distributed, or disclosed
 * in any way without Intel's prior express written permission.
 *
 * No license under any patent, copyright, trade secret or other intellectual
 * property right is granted to or conferred upon you by disclosure or delivery
 * of the Materials, either expressly, by implication, inducement, estoppel or
 * otherwise. Any license under such intellectual property rights must be
 * express and approved by Intel in writing.
 *
 * Unless otherwise agreed by Intel in writing, you may not remove or alter this
 * notice or any other notice embedded in Materials by Intel or Intel's
 * suppliers or licensors in any way.
 *
 * ******************************************************************************
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
