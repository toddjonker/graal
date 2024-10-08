/*
 * Copyright (c) 2024, 2024, Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2024, 2024, Red Hat Inc. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.oracle.svm.core.nmt;

import static com.oracle.svm.core.Uninterruptible.CALLED_FROM_UNINTERRUPTIBLE_CODE;

import org.graalvm.nativeimage.Platform;
import org.graalvm.nativeimage.Platforms;

import com.oracle.svm.core.Uninterruptible;
import com.oracle.svm.core.jdk.UninterruptibleUtils.AtomicLong;

class NmtVirtualMemoryInfo {

    private final AtomicLong peakReservedSize = new AtomicLong(0);
    private final AtomicLong peakCommittedSize = new AtomicLong(0);
    private final AtomicLong reservedSize = new AtomicLong(0);
    private final AtomicLong committedSize = new AtomicLong(0);

    @Platforms(Platform.HOSTED_ONLY.class)
    NmtVirtualMemoryInfo() {
    }

    @Uninterruptible(reason = CALLED_FROM_UNINTERRUPTIBLE_CODE, mayBeInlined = true)
    void trackReserved(long size) {
        long newReservedSize = reservedSize.addAndGet(size);
        updatePeak(newReservedSize, peakReservedSize);
    }

    @Uninterruptible(reason = CALLED_FROM_UNINTERRUPTIBLE_CODE, mayBeInlined = true)
    void trackCommitted(long size) {
        long newCommittedSize = committedSize.addAndGet(size);
        updatePeak(newCommittedSize, peakCommittedSize);
    }

    @Uninterruptible(reason = CALLED_FROM_UNINTERRUPTIBLE_CODE, mayBeInlined = true)
    void trackUncommit(long size) {
        long lastSize = committedSize.addAndGet(-size);
        assert lastSize >= 0;
    }

    @Uninterruptible(reason = CALLED_FROM_UNINTERRUPTIBLE_CODE, mayBeInlined = true)
    void trackFree(long size) {
        long lastSize = reservedSize.addAndGet(-size);
        assert lastSize >= 0;
    }

    @Uninterruptible(reason = CALLED_FROM_UNINTERRUPTIBLE_CODE, mayBeInlined = true)
    private static void updatePeak(long newSize, AtomicLong peakToUpdate) {
        long oldPeak;
        do {
            oldPeak = peakToUpdate.get();
        } while (newSize > oldPeak && !peakToUpdate.compareAndSet(oldPeak, newSize));
    }

    @Uninterruptible(reason = CALLED_FROM_UNINTERRUPTIBLE_CODE, mayBeInlined = true)
    long getReservedSize() {
        return reservedSize.get();
    }

    @Uninterruptible(reason = CALLED_FROM_UNINTERRUPTIBLE_CODE, mayBeInlined = true)
    long getCommittedSize() {
        return committedSize.get();
    }

    @Uninterruptible(reason = CALLED_FROM_UNINTERRUPTIBLE_CODE, mayBeInlined = true)
    long getPeakReservedSize() {
        return peakReservedSize.get();
    }

    @Uninterruptible(reason = CALLED_FROM_UNINTERRUPTIBLE_CODE, mayBeInlined = true)
    long getPeakCommittedSize() {
        return peakCommittedSize.get();
    }
}
