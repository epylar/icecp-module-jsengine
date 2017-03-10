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


