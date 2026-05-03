/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.alerting.core.schedule

import org.opensearch.alerting.core.model.MockScheduledJob
import org.opensearch.common.settings.Settings
import org.opensearch.commons.alerting.model.IntervalSchedule
import org.opensearch.threadpool.ThreadPool
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class JobSchedulerStandbyModeTests {
    private val testSettings: Settings = Settings.builder().put("node.name", "node-0").build()
    private val testThreadPool = ThreadPool(testSettings)
    private val jobRunner = MockJobRunner()

    @Test
    fun `schedule skips jobs in standby mode`() {
        val standbyModeEnabled = AtomicBoolean(true)
        val jobScheduler = JobScheduler(testThreadPool, jobRunner, standbyModeEnabled::get)
        val mockScheduledJob = MockScheduledJob(
            "mockScheduledJob-id",
            1L,
            "mockScheduledJob-name",
            "MockScheduledJob",
            true,
            IntervalSchedule(1, ChronoUnit.MINUTES),
            Instant.now(),
            Instant.now()
        )

        assertFalse(jobScheduler.schedule(mockScheduledJob), "We should return false if standby mode is enabled.")
        assertEquals(setOf(), jobScheduler.scheduledJobs(), "List of ScheduledJobs are not the same.")
    }
}
