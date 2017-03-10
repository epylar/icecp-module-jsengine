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
import com.intel.icecp.core.Module;
import com.intel.icecp.core.Node;
import com.intel.icecp.core.attributes.AttributeNotFoundException;
import com.intel.icecp.core.attributes.AttributeNotWriteableException;
import com.intel.icecp.core.attributes.Attributes;
import com.intel.icecp.core.attributes.ModuleStateAttribute;
import com.intel.icecp.core.metadata.Persistence;
import com.intel.icecp.core.misc.ChannelIOException;
import com.intel.icecp.core.misc.ChannelLifetimeException;
import com.intel.icecp.core.misc.Configuration;
import com.intel.icecp.core.modules.ModuleProperty;
import com.intel.icecp.module.jsengine.attributes.CodeAttribute;
import com.intel.icecp.module.jsengine.message.JSEngineMessage;
import com.intel.icecp.node.utils.ChannelUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.concurrent.CountDownLatch;

/**
 * This module will execute a Nashorn javascript file inside a ScriptEngine. A
 * command channel is opened, and subscribed to. Send a JSEngineMessage to the
 * channel, and it will execute the specified script.
 * <p>
 * You can test this module by just creating a message, publish it to the
 * command channel, and get the response:
 * <p>
 * JSEngineMessage msg = new JSEngineMessage("MyNode", "return-channel");
 * <p>
 * Channel&lt;JSEngineMessage&gt; channel = node.openChannel(...);
 * <p>
 * channel.publish(msg);
 * <p>
 * msg = channel.latest.get();
 * <p>
 * if (msg.ranSuccessful)
 * <p>
 * //success;
 * <p>
 *
 */
@ModuleProperty(name = "JSEngineModule", attributes = {CodeAttribute.class})
public class JSEngineModule implements Module {
    private static final Logger LOGGER = LogManager.getLogger();
    private final CountDownLatch stopLatch = new CountDownLatch(1);
    private Attributes jsEngineAttributes;

    private static void closeChannel(Channel channel) {
        try {
            channel.close();
        } catch (ChannelLifetimeException e) {
            LOGGER.error("Failed to close channel: {}", channel, e);
        }
    }

    private static void publishJavaScriptMessage(Channel<JSEngineMessage> cmdChannel, JSEngineMessage initialMessage) {
        if (initialMessage.javaScriptToExec != null) {
            // publish message to cmdChannel
            try {
                cmdChannel.publish(initialMessage);
            } catch (ChannelIOException e) {
                LOGGER.error("Failed to publish the initial javascript to channel", e);
            }
        }
    }

    /**
     * A method to create a channel; used by this module since it creates channels.
     *
     * @param node the node to use for creating the channel
     * @param channelSuffix the name of the channel (just the suffix)
     * @return a JSEngineMessage channel with the suffix appended to the node URI
     */
    private static Channel<JSEngineMessage> createChannel(Node node, String channelSuffix) throws ChannelLifetimeException {
        URI fullChannelName = ChannelUtils.join(node.getDefaultUri(), channelSuffix);
        return node.openChannel(fullChannelName, JSEngineMessage.class, Persistence.DEFAULT);
    }

    /**
     * Where everything begins. Create the command channel, create the callback,
     * subscribe.
     */
    @Override
    public void run(Node node, Attributes attributes) {
        this.jsEngineAttributes = attributes;

        // setup command channel
        Channel<JSEngineMessage> cmdChannel;
        try {
            cmdChannel = createChannel(node, JSEngineMessage.JSENGINE_CHANNEL_NAME);
            cmdChannel.subscribe(new ScriptRunner(node));
        } catch (ChannelLifetimeException | ChannelIOException e) {
            LOGGER.error("Failed to subscribe to command channel", e);
            setAttribute(ModuleStateAttribute.NAME, State.ERROR);
            return;
        }

        LOGGER.info("JSEngine ready for commands");

        // run the initial javascript inside configuration message
        try {
            // construct JSEngineMessage
            JSEngineMessage initialMessage = new JSEngineMessage();
            initialMessage.javaScriptToExec = jsEngineAttributes.get(CodeAttribute.class);

            // add comment-out symbols to javascript so that it won't accidentally run as macro on journal log service
            LOGGER.trace("Initial JS code to execute: {}", "// " + initialMessage.javaScriptToExec);

            publishJavaScriptMessage(cmdChannel, initialMessage);

            setAttribute(ModuleStateAttribute.NAME, State.RUNNING);

            LOGGER.info("javascript loaded and ran.");

        } catch (AttributeNotFoundException e) {
            LOGGER.error("Can't find attribute", e);
            setAttribute(ModuleStateAttribute.NAME, State.ERROR);
        }

        waitForTearDown();

        closeChannel(cmdChannel);
    }

    /**
     * Set an attribute with class and value pair with error handling
     *
     * @param attributeClass class of the attribute to be set
     * @param attributeValue value of the attribute to be set
     */
    private void setAttribute(Class attributeClass, Object attributeValue) {
        try {
            jsEngineAttributes.set(attributeClass, attributeValue);
        } catch (AttributeNotFoundException | AttributeNotWriteableException e) {
            LOGGER.error("Attribute {} could not be set", attributeClass.getName(), e);
        }
    }

    /**
     * Stop method of the Module interface. Tell the stopLatch to countdown and
     * the run() method will stop blocking.
     */
    @Override
    public void stop(StopReason reason) {
        LOGGER.info("Stopped because: {}", reason);
        stopLatch.countDown();
        setAttribute(ModuleStateAttribute.class, State.STOPPED);
    }

    /**
     * @param arg0 Node
     * @param arg1 Configuration
     * @param arg2 Channel with State
     * @param arg3 moduleId
     * @deprecated No longer use, please use {@code Attribute} version.
     */
    @Override
    @Deprecated
    public void run(Node arg0, Configuration arg1, Channel<State> arg2, long arg3) {
        throw new UnsupportedOperationException("This method is no longer supported");
    }

    /**
     * Wait for stopLatch to turn to 0 which will indicate the application
     * should terminate. See the tearDown() method.
     */
    private void waitForTearDown() {
        try {
            stopLatch.await();
        } catch (InterruptedException e1) {
            LOGGER.info("Interrupted.");
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Set an attribute
     *
     * @param attributeName name of the attribute to be set
     * @param attributeValue value of the attribute to be set
     */
    private void setAttribute(String attributeName, Object attributeValue) {
        try {
            jsEngineAttributes.set(attributeName, attributeValue);
        } catch (AttributeNotFoundException e) {
            LOGGER.error("Attribute {} not set, please check config file, {}", attributeName, e);
        } catch (AttributeNotWriteableException e) {
            LOGGER.error("Attribute {} not writable, {}", attributeName, e);
        }
    }

}
