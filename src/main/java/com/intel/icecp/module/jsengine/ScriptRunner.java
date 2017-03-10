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
import com.intel.icecp.core.misc.ChannelIOException;
import com.intel.icecp.core.misc.ChannelLifetimeException;
import com.intel.icecp.core.misc.OnPublish;
import com.intel.icecp.module.jsengine.message.JSEngineMessage;
import com.intel.icecp.node.utils.ChannelUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.Writer;
import java.net.URI;

/**
 * This class runs the scripts sent to this module; it implements the OnPublish API so that it can respond directly
 * to ICECP messages.
 */
class ScriptRunner implements OnPublish<JSEngineMessage> {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Node node;
    private final ScriptEngine engine;

    ScriptRunner(Node node) {
        this.node = node;
        ScriptEngineManager factory = new ScriptEngineManager();
        this.engine = factory.getEngineByName("nashorn");
    }

    /**
     * Send a reply back to the caller; includes all of the fields of the original request for execution along with some
     * extra execution results (TODO probably should split into request message and response message); also, this will
     * publish the message under the node's namespace (TODO this may need to be more flexible to allow for absolute
     * names)
     *
     * @param node the node to use for creating the return channel
     * @param returnMessage the message to send
     */
    private static void sendReply(Node node, JSEngineMessage returnMessage) {
        URI uri = ChannelUtils.join(node.getDefaultUri(), returnMessage.returnChannelName);
        try (Channel<JSEngineMessage> channel = node.openChannel(uri, JSEngineMessage.class, Persistence.DEFAULT)) {
            LOGGER.info("Sending reply message to: {}", uri);
            channel.publish(returnMessage);
            Thread.sleep(1000); // TODO remove this; need to verify that the opened channel will remain open and respond to latest().get() requests
        } catch (ChannelLifetimeException | ChannelIOException e) {
            LOGGER.error("Failed to send reply message", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void onPublish(JSEngineMessage message) {
        // Make a copy of the message to return
        JSEngineMessage returnMsg = message.copy();

        // Ask the engine to evaluate the script.
        try {
            run(node, message.nodeVarName, message.javaScriptToExec, null);
            returnMsg.ranSuccessful = true;
            returnMsg.exceptionMsg = null;
            sendReply(node, returnMsg);
        } catch (ScriptException se) {
            LOGGER.info("Failed to execute the script.", se);
            returnMsg.ranSuccessful = false;
            returnMsg.exceptionMsg = se.getMessage();
            sendReply(node, returnMsg);
        }
    }

    /**
     * Run the script with the given code. It uses {@link ScriptEngine#put(String, Object)} to add a variable to the
     * engine so the script can reference it. A typical scenario, is that the script needs a {@code node}, so we give it
     * the one that this module is running on. By default, the {@code node} variable name in the script (described by
     * the {@code message.nodeVarName}) is {@code myNode}.
     *
     * @param node the node instance to pass to the script
     * @param nodeName the variable name of the node
     * @param javascript the code to execute
     * @param printCaptor a writer for the script to print its output to; pass null to use the default Nashorn writer
     * @throws ScriptException if the script fails during execution
     */
    void run(Node node, String nodeName, String javascript, Writer printCaptor) throws ScriptException {
        if (printCaptor != null) {
            engine.getContext().setWriter(printCaptor);
        }
        engine.put(nodeName, node);
        engine.eval(javascript);
    }
}
