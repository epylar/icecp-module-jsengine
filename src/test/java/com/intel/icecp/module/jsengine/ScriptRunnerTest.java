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