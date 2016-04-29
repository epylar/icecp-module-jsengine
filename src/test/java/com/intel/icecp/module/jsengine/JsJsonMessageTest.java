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

import static org.junit.Assert.assertEquals;


import com.intel.icecp.core.messages.BytesMessage;
import com.intel.icecp.node.messages.UndefinedMessage;
import org.junit.Test;

import java.io.IOException;

public class JsJsonMessageTest {
    @Test
    public void checkBytesArray() throws Exception {
        JsJsonMessage jsJsonMessage = JsJsonMessage.makeJsJsonMessage("qrstuvwxyz".getBytes());

        assertEquals("\"cXJzdHV2d3h5eg==\"", jsJsonMessage.toJson());
    }

    @Test
    public void checkBytesMessage() throws Exception {
        JsJsonMessage jsJsonMessage = JsJsonMessage.makeJsJsonMessage(new BytesMessage("hlstuvwxyz".getBytes()));

        assertEquals("\"aGxzdHV2d3h5eg==\"", jsJsonMessage.toJson());
    }


    @Test
    public void checkString() throws Exception {
        String contentsString = "\"abcdefg\"";
        JsJsonMessage jsJsonMessage = JsJsonMessage.makeJsJsonMessage(contentsString);

        assertEquals(contentsString, jsJsonMessage.toJson());

        JsJsonMessage msgConvertedBack = JsJsonMessage.makeJsJsonMessage(jsJsonMessage.toJson());
        assertEquals(contentsString, msgConvertedBack.toJson());
    }

    @Test
    public void checkStringMessage() throws Exception {
        String contentsString = "abcdefg";
        JsJsonMessage jsJsonMessage = JsJsonMessage.makeJsJsonMessage(new StringMessage(contentsString));

        assertEquals("\"" + contentsString + "\"", jsJsonMessage.toJson());
    }


    @Test
    public void checkInteger() throws Exception {
        int contents = 1235124;
        String contentsString = Integer.toString(contents);
        JsJsonMessage jsJsonMessage = JsJsonMessage.makeJsJsonMessage(contentsString);

        assertEquals(contentsString, jsJsonMessage.toJson());

        JsJsonMessage msgConvertedBack = JsJsonMessage.makeJsJsonMessage(jsJsonMessage.toJson());
        assertEquals(contentsString, msgConvertedBack.toJson());
    }

    @Test
    public void checkIntegerMessage() throws Exception {
        int contents = 28743;
        JsJsonMessage jsJsonMessage = JsJsonMessage.makeJsJsonMessage(new IntegerMessage(contents));

        assertEquals(Integer.toString(contents), jsJsonMessage.toJson());
    }

    @Test(expected = IOException.class)
    public void checkUnknownTypeOfMessage() throws Exception {
        JsJsonMessage.makeJsJsonMessage(new UndefinedMessage());
    }
}