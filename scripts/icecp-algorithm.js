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

load('scripts/libraries/js-shims.js');
load('scripts/icecp-channel-observable.js'); // TODO should not start with 'scripts/'
load('scripts/icecp-channel-publisher.js'); // TODO should not start with 'scripts/'

/**
 * @type Object a logger object exposing a log4j-like interface to all algorithm components
 */
var LOGGER = {
    info: function () {
        var a = Array.prototype.slice.call(arguments);
        a.unshift(new Date().getTime() + ' [js] INFO icecp-algorithm:');
        print.apply(this, a);
    },

    error: function () {
        var a = Array.prototype.slice.call(arguments);
        a.unshift(new Date().getTime() + ' [js] ERROR icecp-algorithm:');
        print.apply(this, a);
    }
};

/**
 * Create a new algorithm
 * @param {String} name an identifier for the algorithm
 * @returns {Algorithm}
 */
var Algorithm = (function () {
    // constructor
    function Algorithm(name) {
        this.inputChannels = [];
        this.outputChannels = [];
        this.name = name;
        LOGGER.info('New algorithm:', name);
    }

    /**
     * Add a channel to the set of channels observed by this algorithm
     * @param {String} uri the URI of the channel to subscribe to
     * @returns {IcecpChannelObservable} an RxJS-compatible Observable passing all observed messages
     */
    Algorithm.prototype.addInputChannel = function (uri) {
        LOGGER.info("Adding input channel: ", uri);
        var dco = new IcecpChannelObservable(node, uri);
        this.inputChannels.push(dco);
        return dco.getObservable();
    };

    /**
     * Add a channel to the set of channels published to by this algorithm
     * @param {String} uri the URI of the channel to publish to
     * @returns {IcecpChannelPublisher} a JS helper class for translating and publishing JSON objects
     */
    Algorithm.prototype.addOutputChannel = function (uri) {
        LOGGER.info("Adding output channel: ", uri);
        var dcp = new IcecpChannelPublisher(node, uri);
        this.outputChannels.push(dcp);
        return dcp;
    };

    /**
     * Starts the algorithm execution (opens all channels, performs all subscriptions, etc.) and continues to run until stop is called
     * @returns {undefined} no output
     */
    Algorithm.prototype.start = function () {
        this.inputChannels.forEach(function (c) {
            c.start();
        });
        this.outputChannels.forEach(function (c) {
            c.start();
        });
    };

    /**
     * Stops algorithm execution and cleans up any used resources (ICECP channels, Rx Observables)
     * @returns {undefined}
     */
    Algorithm.prototype.stop = function () {
        this.outputChannels.forEach(function (c) {
            c.stop();
        });
        this.inputChannels.forEach(function (c) {
            c.stop();
        });
    };

    // see http://arjanvandergaag.nl/blog/javascript-class-pattern.html for a description of this pattern
    return Algorithm;
})();


