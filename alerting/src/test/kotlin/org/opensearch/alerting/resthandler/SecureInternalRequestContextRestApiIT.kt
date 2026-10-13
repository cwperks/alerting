/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.alerting.resthandler

import org.junit.Assume.assumeTrue
import org.opensearch.alerting.AlertingRestTestCase
import org.opensearch.alerting.randomQueryLevelMonitor
import org.opensearch.alerting.randomWorkflow
import org.opensearch.commons.alerting.model.SearchInput
import org.opensearch.search.builder.SearchSourceBuilder

class SecureInternalRequestContextRestApiIT : AlertingRestTestCase() {
    fun `test monitor and workflow writes retain internal access`() {
        assumeTrue(securityEnabled())
        enableFilterBy()
        try {
            val index = "baseline-persistence-data"
            createTestIndex(index)
            val roles = listOf("team", "shared")
            val monitor = createMonitorWithClient(
                client(),
                randomQueryLevelMonitor(enabled = false, inputs = listOf(SearchInput(listOf(index), SearchSourceBuilder()))),
                roles
            )
            val workflow = createWorkflowWithClient(client(), randomWorkflow(monitorIds = listOf(monitor.id), enabled = false), roles)
            updateMonitorWithClient(client(), monitor, roles)
            updateWorkflowWithClient(client(), workflow, roles)
        } finally {
            disableFilterBy()
        }
    }
}
