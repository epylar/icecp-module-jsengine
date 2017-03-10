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

import com.intel.icecp.core.Message;

public class IntegerMessage implements Message {
    private int value;

    public IntegerMessage() { // if we want to deserialize this with Jackson, we need a public default constructor
    }

    public IntegerMessage(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
