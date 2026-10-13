/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.alerting.action

import org.opensearch.action.ActionType
import org.opensearch.commons.alerting.action.IndexMonitorResponse

class UpdateMonitorAction private constructor() :
    ActionType<IndexMonitorResponse>(NAME, ::IndexMonitorResponse, setOf(LEGACY_NAME)) {
    companion object {
        val INSTANCE = UpdateMonitorAction()
        const val NAME = "alerting:monitor/update"
        const val LEGACY_NAME = "cluster:admin/opendistro/alerting/monitor/write"
    }
}
