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