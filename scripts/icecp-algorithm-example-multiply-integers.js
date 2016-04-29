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

load('scripts/icecp-algorithm.js');

var a = new Algorithm('my-algorithm-name');

var integerInput1 = a.addInputChannel('icecp:/integer/input/1'); // this creates a RxJS-compatible Observer
var integerInput2 = a.addInputChannel('icecp:/integer/input/2'); // this creates a RxJS-compatible Observer
var integerOutput1 = a.addOutputChannel('icecp:/integer/output/1');

// join integerInput1 and integerInput2 to perform some calculation
// see https://github.com/Reactive-Extensions/RxJS/blob/master/doc/api/core/operators/bufferwithcount.md
var buffered1 = integerInput1.bufferWithCount(1);
var buffered2 = integerInput2.bufferWithCount(1);

// see https://github.com/Reactive-Extensions/RxJS/blob/master/doc/api/core/operators/zip.md
Rx.Observable.zip(buffered1, buffered2, function (a, b) { return {input1: a, input2: b}; }).
select(function (x, idx, obs) {
    return x.input1 * x.input2;
}).subscribe(function (x) { integerOutput1.publish(x); });

a.start(); // Start listening on input channels and accept published messages on output channels


