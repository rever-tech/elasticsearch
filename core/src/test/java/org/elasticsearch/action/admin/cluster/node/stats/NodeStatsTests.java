/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.action.admin.cluster.node.stats;

import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.io.stream.BytesStreamOutput;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.discovery.DiscoveryStats;
import org.elasticsearch.discovery.zen.PendingClusterStateStats;
import org.elasticsearch.http.HttpStats;
import org.elasticsearch.indices.breaker.AllCircuitBreakerStats;
import org.elasticsearch.indices.breaker.CircuitBreakerStats;
import org.elasticsearch.ingest.IngestStats;
import org.elasticsearch.monitor.fs.FsInfo;
import org.elasticsearch.monitor.jvm.JvmStats;
import org.elasticsearch.monitor.os.OsStats;
import org.elasticsearch.monitor.process.ProcessStats;
import org.elasticsearch.script.ScriptStats;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.test.VersionUtils;
import org.elasticsearch.threadpool.ThreadPoolStats;
import org.elasticsearch.transport.TransportStats;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;

public class NodeStatsTests extends ESTestCase {

    public void testSerialization() throws IOException {
        NodeStats nodeStats = createNodeStats();
        try (BytesStreamOutput out = new BytesStreamOutput()) {
            nodeStats.writeTo(out);
            try (StreamInput in = out.bytes().streamInput()) {
                NodeStats deserializedNodeStats = NodeStats.readNodeStats(in);
                assertEquals(nodeStats.getNode(), deserializedNodeStats.getNode());
                assertEquals(nodeStats.getTimestamp(), deserializedNodeStats.getTimestamp());
                if (nodeStats.getOs() == null) {
                    assertNull(deserializedNodeStats.getOs());
                } else {
                    assertEquals(nodeStats.getOs().getTimestamp(), deserializedNodeStats.getOs().getTimestamp());
                    assertEquals(nodeStats.getOs().getSwap().getFree(), deserializedNodeStats.getOs().getSwap().getFree());
                    assertEquals(nodeStats.getOs().getSwap().getTotal(), deserializedNodeStats.getOs().getSwap().getTotal());
                    assertEquals(nodeStats.getOs().getSwap().getUsed(), deserializedNodeStats.getOs().getSwap().getUsed());
                    assertEquals(nodeStats.getOs().getMem().getFree(), deserializedNodeStats.getOs().getMem().getFree());
                    assertEquals(nodeStats.getOs().getMem().getTotal(), deserializedNodeStats.getOs().getMem().getTotal());
                    assertEquals(nodeStats.getOs().getMem().getUsed(), deserializedNodeStats.getOs().getMem().getUsed());
                    assertEquals(nodeStats.getOs().getMem().getFreePercent(), deserializedNodeStats.getOs().getMem().getFreePercent());
                    assertEquals(nodeStats.getOs().getMem().getUsedPercent(), deserializedNodeStats.getOs().getMem().getUsedPercent());
                    assertEquals(nodeStats.getOs().getCpu().getPercent(), deserializedNodeStats.getOs().getCpu().getPercent());
                    assertEquals(
                        nodeStats.getOs().getCgroup().getCpuAcctControlGroup(),
                        deserializedNodeStats.getOs().getCgroup().getCpuAcctControlGroup());
                    assertEquals(
                        nodeStats.getOs().getCgroup().getCpuAcctUsageNanos(),
                        deserializedNodeStats.getOs().getCgroup().getCpuAcctUsageNanos());
                    assertEquals(
                        nodeStats.getOs().getCgroup().getCpuControlGroup(),
                        deserializedNodeStats.getOs().getCgroup().getCpuControlGroup());
                    assertEquals(
                        nodeStats.getOs().getCgroup().getCpuCfsPeriodMicros(),
                        deserializedNodeStats.getOs().getCgroup().getCpuCfsPeriodMicros());
                    assertEquals(
                        nodeStats.getOs().getCgroup().getCpuCfsQuotaMicros(),
                        deserializedNodeStats.getOs().getCgroup().getCpuCfsQuotaMicros());
                    assertEquals(
                        nodeStats.getOs().getCgroup().getCpuStat().getNumberOfElapsedPeriods(),
                        deserializedNodeStats.getOs().getCgroup().getCpuStat().getNumberOfElapsedPeriods());
                    assertEquals(
                        nodeStats.getOs().getCgroup().getCpuStat().getNumberOfTimesThrottled(),
                        deserializedNodeStats.getOs().getCgroup().getCpuStat().getNumberOfTimesThrottled());
                    assertEquals(
                        nodeStats.getOs().getCgroup().getCpuStat().getTimeThrottledNanos(),
                        deserializedNodeStats.getOs().getCgroup().getCpuStat().getTimeThrottledNanos());
                    assertArrayEquals(nodeStats.getOs().getCpu().getLoadAverage(),
                            deserializedNodeStats.getOs().getCpu().getLoadAverage(), 0);
                }
                if (nodeStats.getProcess() == null) {
                    assertNull(deserializedNodeStats.getProcess());
                } else {
                    assertEquals(nodeStats.getProcess().getTimestamp(), deserializedNodeStats.getProcess().getTimestamp());
                    assertEquals(nodeStats.getProcess().getCpu().getTotal(), deserializedNodeStats.getProcess().getCpu().getTotal());
                    assertEquals(nodeStats.getProcess().getCpu().getPercent(), deserializedNodeStats.getProcess().getCpu().getPercent());
                    assertEquals(nodeStats.getProcess().getMem().getTotalVirtual(),
                            deserializedNodeStats.getProcess().getMem().getTotalVirtual());
                    assertEquals(nodeStats.getProcess().getMaxFileDescriptors(),
                            deserializedNodeStats.getProcess().getMaxFileDescriptors());
                    assertEquals(nodeStats.getProcess().getOpenFileDescriptors(),
                            deserializedNodeStats.getProcess().getOpenFileDescriptors());
                }
                JvmStats jvm = nodeStats.getJvm();
                JvmStats deserializedJvm = deserializedNodeStats.getJvm();
                if (jvm == null) {
                    assertNull(deserializedJvm);
                } else {
                    JvmStats.Mem mem = jvm.getMem();
                    JvmStats.Mem deserializedMem = deserializedJvm.getMem();
                    assertEquals(jvm.getTimestamp(), deserializedJvm.getTimestamp());
                    assertEquals(mem.getHeapUsedPercent(), deserializedMem.getHeapUsedPercent());
                    assertEquals(mem.getHeapUsed(), deserializedMem.getHeapUsed());
                    assertEquals(mem.getHeapCommitted(), deserializedMem.getHeapCommitted());
                    assertEquals(mem.getNonHeapCommitted(), deserializedMem.getNonHeapCommitted());
                    assertEquals(mem.getNonHeapUsed(), deserializedMem.getNonHeapUsed());
                    assertEquals(mem.getHeapMax(), deserializedMem.getHeapMax());
                    JvmStats.Classes classes = jvm.getClasses();
                    assertEquals(classes.getLoadedClassCount(), deserializedJvm.getClasses().getLoadedClassCount());
                    assertEquals(classes.getTotalLoadedClassCount(), deserializedJvm.getClasses().getTotalLoadedClassCount());
                    assertEquals(classes.getUnloadedClassCount(), deserializedJvm.getClasses().getUnloadedClassCount());
                    assertEquals(jvm.getGc().getCollectors().length, deserializedJvm.getGc().getCollectors().length);
                    for (int i = 0; i < jvm.getGc().getCollectors().length; i++) {
                        JvmStats.GarbageCollector garbageCollector = jvm.getGc().getCollectors()[i];
                        JvmStats.GarbageCollector deserializedGarbageCollector = deserializedJvm.getGc().getCollectors()[i];
                        assertEquals(garbageCollector.getName(), deserializedGarbageCollector.getName());
                        assertEquals(garbageCollector.getCollectionCount(), deserializedGarbageCollector.getCollectionCount());
                        assertEquals(garbageCollector.getCollectionTime(), deserializedGarbageCollector.getCollectionTime());
                    }
                    assertEquals(jvm.getThreads().getCount(), deserializedJvm.getThreads().getCount());
                    assertEquals(jvm.getThreads().getPeakCount(), deserializedJvm.getThreads().getPeakCount());
                    assertEquals(jvm.getUptime(), deserializedJvm.getUptime());
                    if (jvm.getBufferPools() == null) {
                        assertNull(deserializedJvm.getBufferPools());
                    } else {
                        assertEquals(jvm.getBufferPools().size(), deserializedJvm.getBufferPools().size());
                        for (int i = 0; i < jvm.getBufferPools().size(); i++) {
                            JvmStats.BufferPool bufferPool = jvm.getBufferPools().get(i);
                            JvmStats.BufferPool deserializedBufferPool = deserializedJvm.getBufferPools().get(i);
                            assertEquals(bufferPool.getName(), deserializedBufferPool.getName());
                            assertEquals(bufferPool.getCount(), deserializedBufferPool.getCount());
                            assertEquals(bufferPool.getTotalCapacity(), deserializedBufferPool.getTotalCapacity());
                            assertEquals(bufferPool.getUsed(), deserializedBufferPool.getUsed());
                        }
                    }
                }
                if (nodeStats.getThreadPool() == null) {
                    assertNull(deserializedNodeStats.getThreadPool());
                } else {
                    Iterator<ThreadPoolStats.Stats> threadPoolIterator = nodeStats.getThreadPool().iterator();
                    Iterator<ThreadPoolStats.Stats> deserializedThreadPoolIterator = deserializedNodeStats.getThreadPool().iterator();
                    while (threadPoolIterator.hasNext()) {
                        ThreadPoolStats.Stats stats = threadPoolIterator.next();
                        ThreadPoolStats.Stats deserializedStats = deserializedThreadPoolIterator.next();
                        assertEquals(stats.getName(), deserializedStats.getName());
                        assertEquals(stats.getThreads(), deserializedStats.getThreads());
                        assertEquals(stats.getActive(), deserializedStats.getActive());
                        assertEquals(stats.getLargest(), deserializedStats.getLargest());
                        assertEquals(stats.getCompleted(), deserializedStats.getCompleted());
                        assertEquals(stats.getQueue(), deserializedStats.getQueue());
                        assertEquals(stats.getRejected(), deserializedStats.getRejected());
                    }
                }
                FsInfo fs = nodeStats.getFs();
                FsInfo deserializedFs = deserializedNodeStats.getFs();
                if (fs == null) {
                    assertNull(deserializedFs);
                } else {
                    assertEquals(fs.getTimestamp(), deserializedFs.getTimestamp());
                    assertEquals(fs.getTotal().getAvailable(), deserializedFs.getTotal().getAvailable());
                    assertEquals(fs.getTotal().getTotal(), deserializedFs.getTotal().getTotal());
                    assertEquals(fs.getTotal().getFree(), deserializedFs.getTotal().getFree());
                    assertEquals(fs.getTotal().getMount(), deserializedFs.getTotal().getMount());
                    assertEquals(fs.getTotal().getPath(), deserializedFs.getTotal().getPath());
                    assertEquals(fs.getTotal().getSpins(), deserializedFs.getTotal().getSpins());
                    assertEquals(fs.getTotal().getType(), deserializedFs.getTotal().getType());
                    FsInfo.IoStats ioStats = fs.getIoStats();
                    FsInfo.IoStats deserializedIoStats = deserializedFs.getIoStats();
                    assertEquals(ioStats.getTotalOperations(), deserializedIoStats.getTotalOperations());
                    assertEquals(ioStats.getTotalReadKilobytes(), deserializedIoStats.getTotalReadKilobytes());
                    assertEquals(ioStats.getTotalReadOperations(), deserializedIoStats.getTotalReadOperations());
                    assertEquals(ioStats.getTotalWriteKilobytes(), deserializedIoStats.getTotalWriteKilobytes());
                    assertEquals(ioStats.getTotalWriteOperations(), deserializedIoStats.getTotalWriteOperations());
                    assertEquals(ioStats.getDevicesStats().length, deserializedIoStats.getDevicesStats().length);
                    for (int i = 0; i < ioStats.getDevicesStats().length; i++) {
                        FsInfo.DeviceStats deviceStats = ioStats.getDevicesStats()[i];
                        FsInfo.DeviceStats deserializedDeviceStats = deserializedIoStats.getDevicesStats()[i];
                        assertEquals(deviceStats.operations(), deserializedDeviceStats.operations());
                        assertEquals(deviceStats.readKilobytes(), deserializedDeviceStats.readKilobytes());
                        assertEquals(deviceStats.readOperations(), deserializedDeviceStats.readOperations());
                        assertEquals(deviceStats.writeKilobytes(), deserializedDeviceStats.writeKilobytes());
                        assertEquals(deviceStats.writeOperations(), deserializedDeviceStats.writeOperations());
                    }
                }
                if (nodeStats.getTransport() == null) {
                    assertNull(deserializedNodeStats.getTransport());
                } else {
                    assertEquals(nodeStats.getTransport().getRxCount(), deserializedNodeStats.getTransport().getRxCount());
                    assertEquals(nodeStats.getTransport().getRxSize(), deserializedNodeStats.getTransport().getRxSize());
                    assertEquals(nodeStats.getTransport().getServerOpen(), deserializedNodeStats.getTransport().getServerOpen());
                    assertEquals(nodeStats.getTransport().getTxCount(), deserializedNodeStats.getTransport().getTxCount());
                    assertEquals(nodeStats.getTransport().getTxSize(), deserializedNodeStats.getTransport().getTxSize());
                }
                if (nodeStats.getHttp() == null) {
                    assertNull(deserializedNodeStats.getHttp());
                } else {
                    assertEquals(nodeStats.getHttp().getServerOpen(), deserializedNodeStats.getHttp().getServerOpen());
                    assertEquals(nodeStats.getHttp().getTotalOpen(), deserializedNodeStats.getHttp().getTotalOpen());
                }
                if (nodeStats.getBreaker() == null) {
                    assertNull(deserializedNodeStats.getBreaker());
                } else {
                    assertEquals(nodeStats.getBreaker().getAllStats().length, deserializedNodeStats.getBreaker().getAllStats().length);
                    for (int i = 0; i < nodeStats.getBreaker().getAllStats().length; i++) {
                        CircuitBreakerStats circuitBreakerStats = nodeStats.getBreaker().getAllStats()[i];
                        CircuitBreakerStats deserializedCircuitBreakerStats = deserializedNodeStats.getBreaker().getAllStats()[i];
                        assertEquals(circuitBreakerStats.getEstimated(), deserializedCircuitBreakerStats.getEstimated());
                        assertEquals(circuitBreakerStats.getLimit(), deserializedCircuitBreakerStats.getLimit());
                        assertEquals(circuitBreakerStats.getName(), deserializedCircuitBreakerStats.getName());
                        assertEquals(circuitBreakerStats.getOverhead(), deserializedCircuitBreakerStats.getOverhead(), 0);
                        assertEquals(circuitBreakerStats.getTrippedCount(), deserializedCircuitBreakerStats.getTrippedCount(), 0);
                    }
                }
                ScriptStats scriptStats = nodeStats.getScriptStats();
                if (scriptStats == null) {
                    assertNull(deserializedNodeStats.getScriptStats());
                } else {
                    assertEquals(scriptStats.getCacheEvictions(), deserializedNodeStats.getScriptStats().getCacheEvictions());
                    assertEquals(scriptStats.getCompilations(), deserializedNodeStats.getScriptStats().getCompilations());
                }
                DiscoveryStats discoveryStats = nodeStats.getDiscoveryStats();
                DiscoveryStats deserializedDiscoveryStats = deserializedNodeStats.getDiscoveryStats();
                if (discoveryStats == null) {
                    assertNull(deserializedDiscoveryStats);
                } else {
                    PendingClusterStateStats queueStats = discoveryStats.getQueueStats();
                    if (queueStats == null) {
                        assertNull(deserializedDiscoveryStats.getQueueStats());
                    } else {
                        assertEquals(queueStats.getCommitted(), deserializedDiscoveryStats.getQueueStats().getCommitted());
                        assertEquals(queueStats.getTotal(), deserializedDiscoveryStats.getQueueStats().getTotal());
                        assertEquals(queueStats.getPending(), deserializedDiscoveryStats.getQueueStats().getPending());
                    }
                }
                IngestStats ingestStats = nodeStats.getIngestStats();
                IngestStats deserializedIngestStats = deserializedNodeStats.getIngestStats();
                if (ingestStats == null) {
                    assertNull(deserializedIngestStats);
                } else {
                    IngestStats.Stats totalStats = ingestStats.getTotalStats();
                    assertEquals(totalStats.getIngestCount(), deserializedIngestStats.getTotalStats().getIngestCount());
                    assertEquals(totalStats.getIngestCurrent(), deserializedIngestStats.getTotalStats().getIngestCurrent());
                    assertEquals(totalStats.getIngestFailedCount(), deserializedIngestStats.getTotalStats().getIngestFailedCount());
                    assertEquals(totalStats.getIngestTimeInMillis(), deserializedIngestStats.getTotalStats().getIngestTimeInMillis());
                    assertEquals(ingestStats.getStatsPerPipeline().size(), deserializedIngestStats.getStatsPerPipeline().size());
                    for (Map.Entry<String, IngestStats.Stats> entry : ingestStats.getStatsPerPipeline().entrySet()) {
                        IngestStats.Stats stats = entry.getValue();
                        IngestStats.Stats deserializedStats = deserializedIngestStats.getStatsPerPipeline().get(entry.getKey());
                        assertEquals(stats.getIngestFailedCount(), deserializedStats.getIngestFailedCount());
                        assertEquals(stats.getIngestTimeInMillis(), deserializedStats.getIngestTimeInMillis());
                        assertEquals(stats.getIngestCurrent(), deserializedStats.getIngestCurrent());
                        assertEquals(stats.getIngestCount(), deserializedStats.getIngestCount());
                    }
                }
            }
        }
    }

    private static NodeStats createNodeStats() {
        DiscoveryNode node = new DiscoveryNode("test_node", buildNewFakeTransportAddress(),
                emptyMap(), emptySet(), VersionUtils.randomVersion(random()));
        OsStats osStats = null;
        if (frequently()) {
            double loadAverages[] = new double[3];
            for (int i = 0; i < 3; i++) {
                loadAverages[i] = randomBoolean() ? randomDouble() : -1;
            }
            osStats = new OsStats(System.currentTimeMillis(), new OsStats.Cpu(randomShort(), loadAverages),
                    new OsStats.Mem(randomLong(), randomLong()),
                    new OsStats.Swap(randomLong(), randomLong()),
                    new OsStats.Cgroup(
                        randomAsciiOfLength(8),
                        randomPositiveLong(),
                        randomAsciiOfLength(8),
                        randomPositiveLong(),
                        randomPositiveLong(),
                        new OsStats.Cgroup.CpuStat(randomPositiveLong(), randomPositiveLong(), randomPositiveLong())));
        }
        ProcessStats processStats = frequently() ? new ProcessStats(randomPositiveLong(), randomPositiveLong(), randomPositiveLong(),
                new ProcessStats.Cpu(randomShort(), randomPositiveLong()),
                new ProcessStats.Mem(randomPositiveLong())) : null;
        JvmStats jvmStats = null;
        if (frequently()) {
            int numMemoryPools = randomIntBetween(0, 10);
            List<JvmStats.MemoryPool> memoryPools = new ArrayList<>(numMemoryPools);
            for (int i = 0; i < numMemoryPools; i++) {
                memoryPools.add(new JvmStats.MemoryPool(randomAsciiOfLengthBetween(3, 10), randomPositiveLong(),
                        randomPositiveLong(), randomPositiveLong(), randomPositiveLong()));
            }
            JvmStats.Threads threads = new JvmStats.Threads(randomIntBetween(1, 1000), randomIntBetween(1, 1000));
            int numGarbageCollectors = randomIntBetween(0, 10);
            JvmStats.GarbageCollector[] garbageCollectorsArray = new JvmStats.GarbageCollector[numGarbageCollectors];
            for (int i = 0; i < numGarbageCollectors; i++) {
                garbageCollectorsArray[i] = new JvmStats.GarbageCollector(randomAsciiOfLengthBetween(3, 10),
                        randomPositiveLong(), randomPositiveLong());
            }
            JvmStats.GarbageCollectors garbageCollectors = new JvmStats.GarbageCollectors(garbageCollectorsArray);
            int numBufferPools = randomIntBetween(0, 10);
            List<JvmStats.BufferPool> bufferPoolList = new ArrayList<>();
            for (int i = 0; i < numBufferPools; i++) {
                bufferPoolList.add(new JvmStats.BufferPool(randomAsciiOfLengthBetween(3, 10), randomPositiveLong(), randomPositiveLong(),
                        randomPositiveLong()));
            }
            JvmStats.Classes classes = new JvmStats.Classes(randomPositiveLong(), randomPositiveLong(), randomPositiveLong());
            jvmStats = frequently() ? new JvmStats(randomPositiveLong(), randomPositiveLong(), new JvmStats.Mem(randomPositiveLong(),
                    randomPositiveLong(), randomPositiveLong(), randomPositiveLong(), randomPositiveLong(), memoryPools), threads,
                    garbageCollectors, randomBoolean() ? Collections.emptyList() : bufferPoolList, classes) : null;
        }
        ThreadPoolStats threadPoolStats = null;
        if (frequently()) {
            int numThreadPoolStats = randomIntBetween(0, 10);
            List<ThreadPoolStats.Stats> threadPoolStatsList = new ArrayList<>();
            for (int i = 0; i < numThreadPoolStats; i++) {
                threadPoolStatsList.add(new ThreadPoolStats.Stats(randomAsciiOfLengthBetween(3, 10), randomIntBetween(1, 1000),
                        randomIntBetween(1, 1000), randomIntBetween(1, 1000), randomPositiveLong(),
                        randomIntBetween(1, 1000), randomIntBetween(1, 1000)));
            }
            threadPoolStats = new ThreadPoolStats(threadPoolStatsList);
        }
        FsInfo fsInfo = null;
        if (frequently()) {
            int numDeviceStats = randomIntBetween(0, 10);
            FsInfo.DeviceStats[] deviceStatsArray = new FsInfo.DeviceStats[numDeviceStats];
            for (int i = 0; i < numDeviceStats; i++) {
                FsInfo.DeviceStats previousDeviceStats = randomBoolean() ? null :
                        new FsInfo.DeviceStats(randomInt(), randomInt(), randomAsciiOfLengthBetween(3, 10),
                                randomPositiveLong(), randomPositiveLong(), randomPositiveLong(), randomPositiveLong(), null);
                deviceStatsArray[i] = new FsInfo.DeviceStats(randomInt(), randomInt(), randomAsciiOfLengthBetween(3, 10),
                        randomPositiveLong(), randomPositiveLong(), randomPositiveLong(), randomPositiveLong(), previousDeviceStats);
            }
            FsInfo.IoStats ioStats = new FsInfo.IoStats(deviceStatsArray);
            int numPaths = randomIntBetween(0, 10);
            FsInfo.Path[] paths = new FsInfo.Path[numPaths];
            for (int i = 0; i < numPaths; i++) {
                paths[i] = new FsInfo.Path(randomAsciiOfLengthBetween(3, 10), randomBoolean() ? randomAsciiOfLengthBetween(3, 10) : null,
                        randomPositiveLong(), randomPositiveLong(), randomPositiveLong());
            }
            fsInfo = new FsInfo(randomPositiveLong(), ioStats, paths);
        }
        TransportStats transportStats = frequently() ? new TransportStats(randomPositiveLong(), randomPositiveLong(),
                randomPositiveLong(), randomPositiveLong(), randomPositiveLong()) : null;
        HttpStats httpStats = frequently() ? new HttpStats(randomPositiveLong(), randomPositiveLong()) : null;
        AllCircuitBreakerStats allCircuitBreakerStats = null;
        if (frequently()) {
            int numCircuitBreakerStats = randomIntBetween(0, 10);
            CircuitBreakerStats[] circuitBreakerStatsArray = new CircuitBreakerStats[numCircuitBreakerStats];
            for (int i = 0; i < numCircuitBreakerStats; i++) {
                circuitBreakerStatsArray[i] = new CircuitBreakerStats(randomAsciiOfLengthBetween(3, 10), randomPositiveLong(),
                        randomPositiveLong(), randomDouble(), randomPositiveLong());
            }
            allCircuitBreakerStats = new AllCircuitBreakerStats(circuitBreakerStatsArray);
        }
        ScriptStats scriptStats = frequently() ? new ScriptStats(randomPositiveLong(), randomPositiveLong()) : null;
        DiscoveryStats discoveryStats = frequently() ? new DiscoveryStats(randomBoolean() ? new PendingClusterStateStats(randomInt(),
                randomInt(), randomInt()) : null) : null;
        IngestStats ingestStats = null;
        if (frequently()) {
            IngestStats.Stats totalStats = new IngestStats.Stats(randomPositiveLong(), randomPositiveLong(), randomPositiveLong(),
                    randomPositiveLong());

            int numStatsPerPipeline = randomIntBetween(0, 10);
            Map<String, IngestStats.Stats> statsPerPipeline = new HashMap<>();
            for (int i = 0; i < numStatsPerPipeline; i++) {
                statsPerPipeline.put(randomAsciiOfLengthBetween(3, 10), new IngestStats.Stats(randomPositiveLong(),
                        randomPositiveLong(), randomPositiveLong(), randomPositiveLong()));
            }
            ingestStats = new IngestStats(totalStats, statsPerPipeline);
        }
        //TODO NodeIndicesStats are not tested here, way too complicated to create, also they need to be migrated to Writeable yet
        return new NodeStats(node, randomPositiveLong(), null, osStats, processStats, jvmStats, threadPoolStats, fsInfo,
                transportStats, httpStats, allCircuitBreakerStats, scriptStats, discoveryStats, ingestStats);
    }
}
