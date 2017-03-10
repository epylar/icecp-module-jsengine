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

package com.intel.icecp.module.jsengine.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.intel.icecp.core.Message;

/**
 * The first version of the Message sent to the JSEngine Module.
 * At a minimum, need to specify the return channel, and the
 * script to run.
 */
@SuppressWarnings("serial")
public class JSEngineMessage implements Message {

    @JsonIgnore
    public static final String JSENGINE_CHANNEL_NAME = "jsengine-cmd";

    private String messageId;
    public String nodeVarName = "node";
    public String javaScriptToExec;  //default script to run
    public String returnChannelName = "";
    public boolean ranSuccessful = true;
    public String exceptionMsg = null;

    public JSEngineMessage() {
        // necessary for Jackson serialization
    }

    public JSEngineMessage(String nodeName, String returnChannelName, String javaScript) {
        nodeVarName = nodeName;
        this.returnChannelName = returnChannelName;
        javaScriptToExec = javaScript;
    }

    public JSEngineMessage copy() {
        JSEngineMessage dst = new JSEngineMessage();
        dst.messageId = this.messageId;
        dst.nodeVarName = this.nodeVarName;
        dst.javaScriptToExec = this.javaScriptToExec;
        dst.returnChannelName = this.returnChannelName;
        dst.ranSuccessful = this.ranSuccessful;
        dst.exceptionMsg = this.exceptionMsg;
        return dst;
    }
}
