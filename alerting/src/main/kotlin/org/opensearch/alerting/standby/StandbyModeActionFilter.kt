/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.alerting.standby

import org.apache.logging.log4j.LogManager
import org.opensearch.OpenSearchStatusException
import org.opensearch.action.ActionRequest
import org.opensearch.action.support.ActionFilter
import org.opensearch.action.support.ActionFilterChain
import org.opensearch.action.support.ActionRequestMetadata
import org.opensearch.alerting.action.ExecuteMonitorAction
import org.opensearch.alerting.action.ExecuteWorkflowAction
import org.opensearch.commons.alerting.action.AlertingActions
import org.opensearch.core.action.ActionListener
import org.opensearch.core.action.ActionResponse
import org.opensearch.core.rest.RestStatus
import org.opensearch.tasks.Task
import java.util.function.Supplier

class StandbyModeActionFilter(
    private val standbyModeEnabled: Supplier<Boolean>
) : ActionFilter {
    private val logger = LogManager.getLogger(javaClass)

    override fun order() = Integer.MIN_VALUE

    override fun <Request : ActionRequest, Response : ActionResponse> apply(
        task: Task,
        action: String,
        request: Request,
        actionRequestMetadata: ActionRequestMetadata<Request, Response>,
        listener: ActionListener<Response>,
        chain: ActionFilterChain<Request, Response>
    ) {
        if (standbyModeEnabled.get() && MUTATING_ACTIONS.contains(action)) {
            logger.debug("Alerting standby mode is enabled, rejecting mutating action [{}].", action)
            listener.onFailure(
                OpenSearchStatusException(
                    "Alerting is read-only because this cluster is in standby mode.",
                    RestStatus.FORBIDDEN
                )
            )
            return
        }

        chain.proceed(task, action, request, listener)
    }

    companion object {
        val MUTATING_ACTIONS = setOf(
            AlertingActions.INDEX_MONITOR_ACTION_NAME,
            AlertingActions.DELETE_MONITOR_ACTION_NAME,
            AlertingActions.ACKNOWLEDGE_ALERTS_ACTION_NAME,
            AlertingActions.ACKNOWLEDGE_CHAINED_ALERTS_ACTION_NAME,
            AlertingActions.INDEX_WORKFLOW_ACTION_NAME,
            AlertingActions.DELETE_WORKFLOW_ACTION_NAME,
            AlertingActions.INDEX_COMMENT_ACTION_NAME,
            AlertingActions.DELETE_COMMENT_ACTION_NAME,
            ExecuteMonitorAction.NAME,
            ExecuteWorkflowAction.NAME
        )
    }
}
