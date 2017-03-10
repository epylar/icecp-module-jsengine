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
import com.intel.icecp.core.Message;
import com.intel.icecp.core.Node;
import com.intel.icecp.core.channels.Token;
import com.intel.icecp.core.messages.BytesMessage;
import com.intel.icecp.core.misc.ChannelIOException;
import com.intel.icecp.core.misc.ChannelLifetimeException;
import com.intel.icecp.core.mock.MockChannels;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import javax.script.ScriptException;
import java.io.StringWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.intel.icecp.core.metadata.Persistence.DEFAULT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IcecpAlgorithmTest {
    private static final Logger LOGGER = LogManager.getLogger();
    private ScriptRunner instance;
    private Node node;
    private MockChannels mockChannels = new MockChannels();
    private Channel<IntegerMessage> integerInput1;
    private Channel<IntegerMessage> integerInput2;
    private Channel<IntegerMessage> integerOutput1;

    @Before
    public void before() throws Exception {
        node = mock(Node.class);
        instance = new ScriptRunner(node);
        Token<IntegerMessage> integerMessage = Token.of(IntegerMessage.class);
        integerInput1 = makeMockChannelFromUri(new URI("icecp:/integer/input/1"), integerMessage);
        integerInput2 = makeMockChannelFromUri(new URI("icecp:/integer/input/2"), integerMessage);
        integerOutput1 = makeMockChannelFromUri(new URI("icecp:/integer/output/1"), integerMessage);
    }


    @Test
    public void loadLibrary() throws Exception {
        StringWriter printCaptor = new StringWriter();
        String javascript = new String(Files.readAllBytes(Paths.get("scripts/icecp-algorithm.js")));
        instance.run(node, "node", javascript, printCaptor);
        LOGGER.info("Output of command: \n{}", printCaptor.toString());
    }

    @Test
    public void readBytesMessage() throws Exception {
        String input = "2vkb1chheohtvoitm4jakshiku";
        BytesMessage msg = new BytesMessage(input.getBytes());
        String expected = "\"MnZrYjFjaGhlb2h0dm9pdG00amFrc2hpa3U=\"";
        String javascript = new String(Files.readAllBytes(Paths.get("scripts/icecp-algorithm-example.js")));

        checkMessageFidelity(new URI("icecp:/my/input/channel"), expected, msg, Token.of(BytesMessage.class), javascript);
    }


    @Test
    public void testIcecpAlgorithmMultiplyIntegers() throws Exception {
        IntegerMessage a = new IntegerMessage(1), b = new IntegerMessage(2),
                c = new IntegerMessage(3), d = new IntegerMessage(4);
        CountDownLatch expectedMessageCount = new CountDownLatch(2);
        List<IntegerMessage> outputMessages = listenForMessages(integerOutput1, expectedMessageCount);

        StringWriter scriptOutput = runAndCaptureOutput(
                "load('scripts/icecp-algorithm-example-multiply-integers.js')");
        // calculate a * b
        integerInput1.publish(a);
        integerInput2.publish(b);
        // calculate c * d
        integerInput1.publish(c);
        integerInput2.publish(d);

        assertTrue("Timed out waiting for calculation output", expectedMessageCount.await(1, TimeUnit.SECONDS));
        LOGGER.info("Output of command: \n{}", scriptOutput.toString());
        assertEquals("Expected two output messages", 2, outputMessages.size());
        assertEquals("Expected first output message to be equal to " + a.getValue() + " * " + b.getValue(),
                a.getValue() * b.getValue(),
                outputMessages.get(0).getValue());
        assertEquals("Expected second output message to be equal to " + c.getValue() + " * " + d.getValue(),
                c.getValue() * d.getValue(),
                outputMessages.get(1).getValue());
    }

    private StringWriter runAndCaptureOutput(String javascript) throws ScriptException {
        StringWriter printCaptor = new StringWriter();
        instance.run(node, "node", javascript, printCaptor);
        return printCaptor;
    }

    private <T extends Message> List<T> listenForMessages(Channel<T> channel, CountDownLatch waitForOutput) throws ChannelIOException {
        List<T> messages = new ArrayList<>();

        channel.subscribe(msg -> {
            messages.add(msg);
            waitForOutput.countDown();
        });

        return messages;
    }

    private <T extends Message> Channel<T> makeMockChannelFromUri(URI uri, Token<T> messageType) throws ChannelLifetimeException {
        Channel<T> channel = mockChannels.openChannel(uri, messageType, DEFAULT);
        when(node.openChannel(eq(uri), any(), any())).thenReturn((Channel)channel);
        return channel;
    }

    private <T extends Message> void checkMessageFidelity(URI uri, String expected, T msg, Token<T> type, String javascript) throws Exception {
        // set up channels, script, message
        StringWriter printCaptor = new StringWriter();
        Channel<T> commChannel = makeMockChannelFromUri(uri, type);

        // run js/send message
        instance.run(node, "node", javascript, printCaptor);
        commChannel.publish(msg);

        // check results
        String contains = "Received message:  " + expected;
        assertTrue("[" + printCaptor.toString() + "]" + " does not contain [" + contains + "]", printCaptor.toString().contains(contains));
        LOGGER.info("Output of command: \n{}", printCaptor.toString());
    }
}
