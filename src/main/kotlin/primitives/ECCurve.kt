/*
 * Copyright 2011 Google Inc.
 * Copyright 2014 Andreas Schildbach
 * Copyright 2014-2016 the libsecp256k1 contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package primitives

import org.bouncycastle.crypto.ec.CustomNamedCurves
import org.bouncycastle.crypto.params.ECDomainParameters
import org.bouncycastle.math.ec.FixedPointUtil

// Code forked from bitcoinj ECKey.java
class ECCurve {
  companion object {
    val EC_DOMAIN_PARAMS: ECDomainParameters

    init {
      // The parameters of the secp256k1 curve that Bitcoin uses.
      val EC_PARAMS = CustomNamedCurves.getByName("secp256k1")

      // Tell Bouncy Castle to precompute data that's needed during secp256k1 calculations.
      FixedPointUtil.precompute(EC_PARAMS.g)

      EC_DOMAIN_PARAMS =
          ECDomainParameters(EC_PARAMS.curve, EC_PARAMS.g, EC_PARAMS.n, EC_PARAMS.h)

      // TODO
      // HALF_CURVE_ORDER = EC_PARAMS.getN().shiftRight(1)
    }
  }
}