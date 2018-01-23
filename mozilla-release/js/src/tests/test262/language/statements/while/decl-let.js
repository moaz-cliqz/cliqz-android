// |reftest| error:SyntaxError
// Copyright (C) 2016 the V8 project authors. All rights reserved.
// This code is governed by the BSD license found in the LICENSE file.
/*---
description: Lexical declaration (let) not allowed in statement position
esid: sec-while-statement
es6id: 13.7.3
negative:
  phase: early
  type: SyntaxError
---*/

throw "Test262: This statement should not be evaluated.";

while (false) let x;