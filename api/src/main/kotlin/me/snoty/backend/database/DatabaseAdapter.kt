package me.snoty.backend.database

import me.snoty.backend.adapter.Adapter

interface DatabaseAdapter : Adapter {
	companion object {
		const val CONFIG_KEY = "database"
	}
}
