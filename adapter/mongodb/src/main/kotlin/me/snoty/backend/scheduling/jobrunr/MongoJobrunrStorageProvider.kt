package me.snoty.backend.scheduling.jobrunr

import com.mongodb.client.MongoClient
import me.snoty.backend.database.mongo.MONGO_DB_NAME
import org.jobrunr.storage.StorageProvider
import org.jobrunr.storage.StorageProviderUtils
import org.jobrunr.storage.nosql.mongo.MongoDBStorageProvider
import org.koin.core.annotation.Single

const val JOBRUNR_COLLECTION_PREFIX = "jobrunr:"

@Single
fun storageProvider(mongoClient: MongoClient): StorageProvider = MongoDBStorageProvider(
	/* mongoClient = */ mongoClient,
	/* dbName = */ MONGO_DB_NAME,
	/* collectionPrefix = */ JOBRUNR_COLLECTION_PREFIX,
	/* databaseOptions = */ StorageProviderUtils.DatabaseOptions.CREATE
)
