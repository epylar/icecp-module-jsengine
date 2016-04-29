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
     * @returns {DeapChannelObservable} an RxJS-compatible Observable passing all observed messages
     */
    Algorithm.prototype.addInputChannel = function (uri) {
        LOGGER.info("Adding input channel: ", uri);
        var dco = new DeapChannelObservable(node, uri);
        this.inputChannels.push(dco);
        return dco.getObservable();
    };

    /**
     * Add a channel to the set of channels published to by this algorithm
     * @param {String} uri the URI of the channel to publish to
     * @returns {DeapChannelPublisher} a JS helper class for translating and publishing JSON objects
     */
    Algorithm.prototype.addOutputChannel = function (uri) {
        LOGGER.info("Adding output channel: ", uri);
        var dcp = new DeapChannelPublisher(node, uri);
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


