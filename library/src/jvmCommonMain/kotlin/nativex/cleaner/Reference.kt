/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 10/10/19.
 * Copyright (c) 2019 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex.cleaner

import java.lang.ref.PhantomReference
import java.lang.ref.ReferenceQueue
import java.util.*
import java.util.concurrent.ConcurrentHashMap

internal class Reference private constructor(
        queue: ReferenceQueue<Any>,
        referent: Any,
        private val runnable: Runnable
) : PhantomReference<Any>(referent, queue), Runnable {
    override fun run() {
        if (REFS.remove(this)) {
            runnable.run()
        }
    }

    companion object {
        fun create(queue: ReferenceQueue<Any>, referent: Any, runnable: Runnable) {
            REFS.add(Reference(queue, referent, runnable))
        }

        private val REFS = Collections.newSetFromMap(ConcurrentHashMap<Reference, Boolean>())
    }
}