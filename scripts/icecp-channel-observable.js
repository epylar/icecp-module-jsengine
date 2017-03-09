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

load("scripts/libraries/rx.all.js"); // TODO should not start with 'scripts/'

/**
 * Proxy for converting ICECP channels to RxJS Observables; also, more info at 
 * https://medium.com/@benlesh/hot-vs-cold-observables-f8094ed53339#.phh8roa5a
 * @param {String} uri the URI of the channel to subscribe to
 * @returns {IcecpChannelObservable}
 */
var IcecpChannelObservable = (function () {
    "use strict";
    /**
     * @param {Node} node_
     * @param {String} uri_
     * @returns {IcecpChannelObservable}
     */
    function IcecpChannelObservable(node_, uri_) {
        this.observers = [];
        this.node = node_;
        var obs = this.observers;
        this.observable = Rx.Observable.create(function (o) {
            obs.push(o);

            return function () {
                LOGGER.info('Closed: ' + uri_);
            };
        });

        // public fields
        this.uri = uri_;
    }
    
    /**
     * @returns {Rx.Observable} the underlying RxJS observable; will not publish messages until start is called
     */
    IcecpChannelObservable.prototype.getObservable = function () {
        return this.observable;
    };

    /**
     * Binds the channel data to the RxJS observable; opens a new ICECP channel subscription
     * @returns {Rx.Observable} the underlying RxJS observable
     */
    IcecpChannelObservable.prototype.start = function () {
        var importer = new JavaImporter(com.intel.icecp.core.metadata,
            com.intel.icecp.core.messages,
            com.intel.icecp.core.misc,
            com.intel.icecp.module.jsengine);
        var URI = Java.type("java.net.URI");
        this.channel = node.openChannel(new URI(this.uri), importer.BytesMessage.class, importer.Persistence.DEFAULT);
        var dco = this;
        this.channel.subscribe(function (message) {
            var json = JSON.parse(importer.JsJsonMessage.makeJsJsonMessage(message).toJson());

            dco.observers.forEach(function (o) {
                o.onNext(json);
            });
        });

        return this.observable;
    };


    /**
     * Unbinds the ICECP channel subscription from the RxJS observable
     * @returns {undefined}
     */
    IcecpChannelObservable.prototype.stop = function () {
        this.observers.forEach(function(o){ o.complete(o); });
        this.channel.close();
    };

    return IcecpChannelObservable;
})();


