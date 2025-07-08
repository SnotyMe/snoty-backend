package me.snoty.backend.scheduling.jobrunr

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import me.snoty.backend.database.mongo.MONGO_DB_NAME
import org.bson.Document
import org.jobrunr.jobs.states.StateName
import org.jobrunr.storage.StorageProvider
import org.jobrunr.storage.StorageProviderUtils
import org.jobrunr.storage.StorageProviderUtils.Jobs
import org.jobrunr.storage.nosql.mongo.MongoDBStorageProvider
import org.jobrunr.utils.reflection.ReflectionUtils
import org.koin.core.annotation.Single

const val JOBRUNR_COLLECTION_PREFIX = "jobrunr:"

@Single(binds = [SnotyJobRunrStorageProvider::class, StorageProvider::class])
fun provideStorageProvider(mongoClient: MongoClient) = MongoJobRunrStorageProvider(mongoClient)

class MongoJobRunrStorageProvider(mongoClient: MongoClient) : MongoDBStorageProvider(
	/* mongoClient = */ mongoClient,
	/* dbName = */ MONGO_DB_NAME,
	/* collectionPrefix = */ JOBRUNR_COLLECTION_PREFIX,
	/* databaseOptions = */ StorageProviderUtils.DatabaseOptions.CREATE
), SnotyJobRunrStorageProvider {
	@Suppress("UNCHECKED_CAST")
	private val jobCollection = ReflectionUtils.getValueFromField(
		ReflectionUtils.findField(this.javaClass.superclass, "jobCollection").get(), this
	) as MongoCollection<Document>

	override fun recurringJobExists(recurringJobId: String, vararg states: StateName): Boolean {
		if (states.isEmpty()) {
			return jobCollection.countDocuments(Filters.eq(Jobs.FIELD_RECURRING_JOB_ID, recurringJobId)) > 0
		}
		return jobCollection.countDocuments(Filters.and(
			Filters.`in`(Jobs.FIELD_STATE, states.map { it.name }),
			Filters.eq(Jobs.FIELD_RECURRING_JOB_ID, recurringJobId)
		)) > 0
	}
}
