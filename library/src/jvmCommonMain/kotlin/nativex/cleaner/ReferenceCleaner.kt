/*
 * Created by Michael Carrara <michael.carrara@breadwallet.com> on 10/10/19.
 * Copyright (c) 2019 Breadwinner AG.  All right reserved.
 *
 * See the LICENSE file at the project root for license information.
 * See the CONTRIBUTORS file at the project root for a list of contributors.
 */
package com.blockset.walletkit.nativex.cleaner

import java.lang.ref.ReferenceQueue
import java.util.logging.Level
import java.util.logging.Logger

object ReferenceCleaner {

    private val Log = Logger.getLogger(ReferenceCleaner::class.java.name)
    private val queue: ReferenceQueue<Any> = ReferenceQueue()
    @Suppress("unused")
    private val thread = Thread(ReferenceCleanerRunnable(queue)).apply {
        isDaemon = true
        name = javaClass.name
        start()
    }

    private class ReferenceCleanerRunnable(val queue: ReferenceQueue<Any>) : Runnable {
        override fun run() {
            while (true) {
                val ref: Reference = try {
                    queue.remove() as Reference
                } catch (e: Throwable) {
                    Log.log(Level.SEVERE, "Error pumping queue", e)
                    continue
                }
                try {
                    ref.run()
                } catch (t: Throwable) {
                    Log.log(Level.SEVERE, "Error cleaning up", t)
                }
            }
        }
    }

    /**
     * Register a runnable to be executed once all references to `referent`
     * have been dropped.
     *
     * This method provides an alternative to the `finalize` method, which
     * is deprecated as of JDK9.
     */
    public fun register(referent: Any, runnable: Runnable) {
        Reference.create(queue, referent, runnable)
    }
}