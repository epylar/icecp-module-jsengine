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


