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


/**
 * Helper for translating and publishing JSON objects to ICECP channels
 * @param {String} uri the URI of the channel to publish to
 * @returns {DeapChannelPublisher}
 */
var DeapChannelPublisher = (function () {
    /**
     * @param {Node} node_
     * @param {String} uri_
     */
    function DeapChannelPublisher(node_, uri_) {
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
    DeapChannelPublisher.prototype.publish = function (jsonMessage) {
        if (!this.started) {
            LOGGER.error("Attempted to publish message in stopped state");
        }
        LOGGER.info("Publishing message [" + jsonMessage + "]");
        var importer = new JavaImporter(Packages.com.intel.icecp.module.jsengine.JsJsonMessage);
        this.channel.publish(importer.JsJsonMessage.makeJsJsonMessage(jsonMessage));
    };

    DeapChannelPublisher.prototype.start = function () {
        var importer = new JavaImporter(com.intel.icecp.core.metadata, com.intel.icecp.core.messages, com.intel.icecp.core.misc);
        var URI = Java.type("java.net.URI");
        this.channel = node.openChannel(new URI(this.uri), importer.BytesMessage.class, importer.Persistence.DEFAULT);
        this.started = true;
    };

    DeapChannelPublisher.prototype.stop = function () {
        this.started = false;
    };

    return DeapChannelPublisher;
})();