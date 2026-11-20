/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.alerting.util

import org.opensearch.script.Script
import org.opensearch.script.ScriptService
import org.opensearch.script.ScriptType
import org.opensearch.script.TemplateScript

/** Renders Alerting templates through core scripting with a Mustache-only language restriction. */
class MustacheTemplateService(private val scriptService: ScriptService) {
    fun renderTemplate(template: String, params: Map<String, Any>): String {
        return renderScript(Script(ScriptType.INLINE, Script.DEFAULT_TEMPLATE_LANG, template, params), emptyMap())
    }

    fun renderScript(script: Script, additionalParams: Map<String, Any>): String {
        // ScriptService checks the resolved language for stored scripts before compilation.
        return scriptService.compile(script, TemplateScript.CONTEXT, setOf(Script.DEFAULT_TEMPLATE_LANG))
            .newInstance(script.params + additionalParams)
            .execute()
    }
}
