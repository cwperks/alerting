/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.alerting

import org.opensearch.alerting.action.UpdateMonitorAction
import org.opensearch.alerting.resthandler.monitorWriteAction
import org.opensearch.alerting.transport.TransportIndexMonitorAction
import org.opensearch.commons.alerting.action.AlertingActions
import org.opensearch.rest.RestRequest
import org.opensearch.test.OpenSearchTestCase

class AlertingActionRegistrationTests : OpenSearchTestCase() {
    fun `test monitor update action retains the legacy write action`() {
        val actions = AlertingPlugin().actions.associateBy { it.action.name() }

        assertEquals(TransportIndexMonitorAction::class.java, actions[UpdateMonitorAction.NAME]?.transportAction)
        assertEquals(setOf(AlertingActions.INDEX_MONITOR_ACTION_NAME), UpdateMonitorAction.INSTANCE.legacyActionNames())
    }

    fun `test resource authorized updates use the resource action`() {
        assertSame(UpdateMonitorAction.INSTANCE, monitorWriteAction(RestRequest.Method.PUT, true))
    }

    fun `test creates and legacy authorization use the legacy action`() {
        assertSame(AlertingActions.INDEX_MONITOR_ACTION_TYPE, monitorWriteAction(RestRequest.Method.POST, true))
        assertSame(AlertingActions.INDEX_MONITOR_ACTION_TYPE, monitorWriteAction(RestRequest.Method.PUT, false))
    }
}
