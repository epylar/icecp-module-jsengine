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
