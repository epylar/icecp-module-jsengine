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
