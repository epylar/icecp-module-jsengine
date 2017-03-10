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
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.BinaryNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.intel.icecp.core.Message;
import com.intel.icecp.core.messages.BytesMessage;

import java.io.IOException;

public final class JsJsonMessage implements Message, JsonSerializable {
    private static ObjectMapper om = new ObjectMapper();
    JsonNode jsonNode;

    static {
        om.enable(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS);
    }

    private JsJsonMessage(JsonNode jsonNode) { this.jsonNode = jsonNode; }

    public static JsJsonMessage makeJsJsonMessage(String json) throws IOException {
        return new JsJsonMessage(readJsonNode(json));
    }

    private static JsonNode readJsonNode(String json) throws IOException {
        return om.readTree(json);
    }

    public static JsJsonMessage makeJsJsonMessage(BytesMessage msg) throws IOException {
        return makeJsJsonMessage(msg.getBytes());
    }

    public static JsJsonMessage makeJsJsonMessage(IntegerMessage msg) throws IOException {
        return makeJsJsonMessage(new IntNode(msg.getValue()));
    }

    public static JsJsonMessage makeJsJsonMessage(StringMessage msg) throws IOException {
        return makeJsJsonMessage(new TextNode(msg.getValue()));
    }

    public static JsJsonMessage makeJsJsonMessage(JsonNode jsonNode) {
        return new JsJsonMessage(jsonNode);
    }

    public static JsJsonMessage makeJsJsonMessage(Message msg) throws IOException {
        throw new IOException("Unknown message [" + msg + "] of type [" + msg.getClass() + "]");
    }

    public static JsJsonMessage makeJsJsonMessage(byte[] bytes) {
        BinaryNode binaryNode = new BinaryNode(bytes);
        return new JsJsonMessage(binaryNode);
    }

    public String toJson() throws JsonProcessingException {
        return om.writeValueAsString(jsonNode);
    }

    @Override
    public void serializeWithType(JsonGenerator jsonGenerator, SerializerProvider serializerProvider,
                                  TypeSerializer typeSerializer) throws IOException {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void serialize(JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonNode.serialize(jsonGenerator, serializerProvider);
    }

    // According to SonarQube:
    // Fields in a "Serializable" class should either be transient or serializable
    // The alternative to making all members serializable or transient is to implement special methods
    // which take on the responsibility of properly serializing and de-serializing the object.
    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {
        out.writeObject(this.toJson());
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        StringBuilder inString = new StringBuilder();
        for (int character = in.read(); character != -1; character = in.read()) {
            inString.append((char) character);
        }

        jsonNode = readJsonNode(inString.toString());
    }
}
