/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.alerting.util

import org.opensearch.common.settings.Settings
import org.opensearch.script.MockScriptEngine
import org.opensearch.script.Script
import org.opensearch.script.ScriptContext
import org.opensearch.script.ScriptService
import org.opensearch.script.ScriptType
import org.opensearch.script.StoredScriptSource
import org.opensearch.script.TemplateScript
import org.opensearch.script.mustache.MustacheScriptEngine
import org.opensearch.test.OpenSearchTestCase
import java.util.function.Function

class MustacheTemplateServiceTests : OpenSearchTestCase() {
    private fun scriptService(settings: Settings = Settings.EMPTY): ScriptService {
        return object : ScriptService(
            settings,
            mapOf(
                "mustache" to MustacheScriptEngine(),
                "painless" to MockScriptEngine(
                    "painless",
                    mapOf("script" to Function { _: Map<String, Any> -> "painless result" }),
                    emptyMap()
                )
            ),
            mapOf<String, ScriptContext<*>>(TemplateScript.CONTEXT.name to TemplateScript.CONTEXT)
        ) {
            override fun getScriptFromClusterState(id: String): StoredScriptSource {
                return when (id) {
                    "template" -> StoredScriptSource("mustache", "Hello {{name}}", emptyMap())
                    else -> StoredScriptSource("painless", "script", emptyMap())
                }
            }
        }
    }

    fun `test renders inline and stored Mustache templates`() {
        scriptService().use { scripts ->
            val service = MustacheTemplateService(scripts)
            assertEquals("Hello world", service.renderTemplate("Hello {{name}}", mapOf("name" to "world")))
            val stored = Script(ScriptType.STORED, null, "template", mapOf("name" to "original"))
            assertEquals("Hello override", service.renderScript(stored, mapOf("name" to "override")))
        }
    }

    fun `test rejects other languages without restricting other callers`() {
        scriptService().use { scripts ->
            val service = MustacheTemplateService(scripts)
            val inline = Script(ScriptType.INLINE, "painless", "script", emptyMap())
            val stored = Script(ScriptType.STORED, null, "other-script", emptyMap())
            for (script in listOf(inline, stored)) {
                val error = expectThrows(IllegalArgumentException::class.java) { service.renderScript(script, emptyMap()) }
                assertEquals("script language [painless] is not permitted by this caller", error.message)
                assertEquals("painless result", scripts.compile(script, TemplateScript.CONTEXT).newInstance(emptyMap()).execute())
            }
        }
    }

    fun `test respects node script restrictions`() {
        scriptService(Settings.builder().putList("script.allowed_types", "stored").build()).use { scripts ->
            val service = MustacheTemplateService(scripts)
            expectThrows(IllegalArgumentException::class.java) { service.renderTemplate("Hello", emptyMap()) }
            val stored = Script(ScriptType.STORED, null, "template", emptyMap())
            assertEquals("Hello world", service.renderScript(stored, mapOf("name" to "world")))
        }
    }
}
