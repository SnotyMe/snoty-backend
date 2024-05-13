package me.snoty.integration.common

/**
 * Unique ID representing a 3rd party service instance.
 * Used as a namespace to avoid ID conflicts between different service instances, e.g. self-hosted moodle instances.
 */
typealias InstanceId = Int

/**
 * Creates a somewhat unique instance ID from a string.
 * The returned value is guaranteed to be positive.
 */
val String.instanceId: InstanceId
	get() = this.let {
		var hash = 7
		for (i in this.chars()) {
			hash = hash * 31 + i
		}
		return hash
	}
