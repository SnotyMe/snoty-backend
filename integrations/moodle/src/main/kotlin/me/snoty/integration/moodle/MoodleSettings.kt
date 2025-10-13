package me.snoty.integration.moodle

import kotlinx.serialization.Serializable
import me.snoty.backend.wiring.credential.Credential
import me.snoty.backend.wiring.credential.CredentialRef
import me.snoty.backend.wiring.credential.RegisterCredential
import me.snoty.integration.common.model.metadata.*
import me.snoty.integration.common.wiring.node.NodeSettings

@RegisterCredential("Moodle")
@Serializable
data class MoodleCredential(
	@FieldName("Base URL")
	@FieldDescription("Full Base URL of the Moodle URL (Dashboard page)")
	val baseUrl: String,
	val username: String,
	@FieldCensored
	val appSecret: String,
) : Credential()

@Serializable
data class MoodleSettings(
	override val name: String = "Moodle",
	val credentials: CredentialRef<MoodleCredential>,
	@FieldDefaultValue("false")
	@FieldDescription("Whether to emit 'done' assignments (may break auto deletions on assignment completion)")
	val emitDoneAssignments: Boolean = false,
	@FieldDefaultValue("true")
	@FieldDescription("Whether to emit 'closed' assignments (you cannot submit anything)")
	val emitClosedAssignments: Boolean = true,
	@FieldDefaultValue("false")
	@FieldHidden
	@FieldDescription("Fixes a bug where past assignments are emitted again as if they were new. You probably don't want to enable this.")
	val emitPastAssignments: Boolean = false,
) : NodeSettings
