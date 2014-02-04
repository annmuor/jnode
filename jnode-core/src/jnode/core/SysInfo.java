/*
 * Licensed to the jNode FTN Platform Develpoment Team (jNode Team)
 * under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for 
 * additional information regarding copyright ownership.  
 * The jNode Team licenses this file to you under the 
 * Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package jnode.core;

import com.sun.management.UnixOperatingSystemMXBean;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

/**
 * @author Manjago (kirill@temnenkov.com)
 */
@SuppressWarnings("restriction")
public final class SysInfo {

    public static class ThreadInfo {
        private final int runningThreads;

        public ThreadInfo(int runningThreads) {
            this.runningThreads = runningThreads;
        }

        public int getRunningThreads() {
            return runningThreads;
        }

        @Override
        public String toString() {
            return "ThreadInfo{" + "runningThreads=" + runningThreads + '}';
        }
    }

    public static class MemoryInfo {
        private final int total;
        private final int free;
        private final int max;

        private static int toMB(long value) {
            return Math.round(value / (1024L * 1024L));
        }

        public int getTotal() {
            return total;
        }

        public int getFree() {
            return free;
        }

        public int getMax() {
            return max;
        }

        public MemoryInfo(long total, long free, long max) {
            this.total = toMB(total);
            this.free = toMB(free);
            this.max = toMB(max);
        }

        @Override
        public String toString() {
            return "MemoryInfo{" + "total=" + total + ", free=" + free + ", max=" + max + '}';
        }
    }

    public static class OpenFilesInfo {
        public long getOpenFiles() {
            return openFiles;
        }

        public long getMaxOpenFiles() {
            return maxOpenFiles;
        }

        private final long openFiles;
        private final long maxOpenFiles;

        public OpenFilesInfo(long openFiles, long maxOpenFiles) {
            this.openFiles = openFiles;
            this.maxOpenFiles = maxOpenFiles;
        }

        @Override
        public String toString() {
            return "OpenFilesInfo{" + "openFiles=" + openFiles + ", maxOpenFiles=" + maxOpenFiles + '}';
        }
    }

    private SysInfo() {
    }

    public static OpenFilesInfo openFilesInfo() {
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        if (os instanceof UnixOperatingSystemMXBean) {
            UnixOperatingSystemMXBean unixOperatingSystemMXBean = (UnixOperatingSystemMXBean) os;
            return new OpenFilesInfo(
                    unixOperatingSystemMXBean.getOpenFileDescriptorCount(),
                    unixOperatingSystemMXBean.getMaxFileDescriptorCount());
        } else {
            return null;
        }
    }

    public static MemoryInfo memoryInfo() {
        Runtime runtime = Runtime.getRuntime();
        return new MemoryInfo(runtime.totalMemory(), runtime.freeMemory(), runtime.maxMemory());
    }

    public static ThreadInfo threadInfo(){
        return new ThreadInfo(Thread.activeCount());
    }
}
