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

import com.intel.icecp.core.Channel;
import com.intel.icecp.core.Node;
import com.intel.icecp.core.metadata.Persistence;
import com.intel.icecp.module.jsengine.message.JSEngineMessage;
import com.intel.icecp.node.NodeFactory;
import com.intel.icecp.node.utils.ChannelUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.StringWriter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.*;


public class ScriptRunnerTest {
    private ScriptRunner instance = new ScriptRunner(null);
    private Node node;

    @Before
    public void before() throws Exception {
        node = NodeFactory.buildMockNode();
        instance = new ScriptRunner(node);
    }

    @Test
    public void run() throws Exception {
        StringWriter printCaptor = new StringWriter();

        instance.run(null, "node", "print('ok')", printCaptor);

        assertEquals(String.format("ok%n"), printCaptor.toString());
    }

    @Test
    public void successfulOnPublish() throws Exception {
        JSEngineMessage message = new JSEngineMessage("node", "/return/channel", "print('ok')");
        Channel<JSEngineMessage> channel = node.openChannel(ChannelUtils.join(node.getDefaultUri(), "/return/channel"), JSEngineMessage.class, Persistence.DEFAULT);
        CountDownLatch latch = new CountDownLatch(1);
        channel.subscribe(m -> {
            latch.countDown();
            assertTrue(m.ranSuccessful);
            assertNull(m.exceptionMsg);
        });

        instance.onPublish(message);

        latch.await(10, TimeUnit.SECONDS);
        assertEquals(0, latch.getCount());
    }

    @Test
    public void failedOnPublish() throws Exception {
        JSEngineMessage message = new JSEngineMessage("node", "/return/channel", "not real code 1234");
        Channel<JSEngineMessage> channel = node.openChannel(ChannelUtils.join(node.getDefaultUri(), "/return/channel"), JSEngineMessage.class, Persistence.DEFAULT);
        CountDownLatch latch = new CountDownLatch(1);
        channel.subscribe(m -> {
            latch.countDown();
            assertFalse(m.ranSuccessful);
            assertNotNull(m.exceptionMsg);
        });

        instance.onPublish(message);

        latch.await(10, TimeUnit.SECONDS);
        assertEquals(0, latch.getCount());
    }
}