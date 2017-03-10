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

/**
 * Helper for translating and publishing JSON objects to ICECP channels
 * @param {String} uri the URI of the channel to publish to
 * @returns {IcecpChannelPublisher}
 */
var IcecpChannelPublisher = (function () {
    /**
     * @param {Node} node_
     * @param {String} uri_
     */
    function IcecpChannelPublisher(node_, uri_) {
        this.node = node_;
        this.started = false;
        this.uri = uri_;
    }

    //noinspection JSValidateJSDoc
    /**
     * Publish the given message to a ICECP channel
     * @param {any} jsonMessage a valid JSON entity (not necessarily an object)
     * @returns {undefined}
     */
    IcecpChannelPublisher.prototype.publish = function (jsonMessage) {
        if (!this.started) {
            LOGGER.error("Attempted to publish message in stopped state");
        }
        LOGGER.info("Publishing message [" + jsonMessage + "]");
        var importer = new JavaImporter(Packages.com.intel.icecp.module.jsengine.JsJsonMessage);
        this.channel.publish(importer.JsJsonMessage.makeJsJsonMessage(jsonMessage));
    };

    IcecpChannelPublisher.prototype.start = function () {
        var importer = new JavaImporter(com.intel.icecp.core.metadata, com.intel.icecp.core.messages, com.intel.icecp.core.misc);
        var URI = Java.type("java.net.URI");
        this.channel = node.openChannel(new URI(this.uri), importer.BytesMessage.class, importer.Persistence.DEFAULT);
        this.started = true;
    };

    IcecpChannelPublisher.prototype.stop = function () {
        this.started = false;
    };

    return IcecpChannelPublisher;
})();