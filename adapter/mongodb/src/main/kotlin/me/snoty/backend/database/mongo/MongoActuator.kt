package me.snoty.backend.database.mongo

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.mongo.MongoConnectionHealthCheck
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import com.mongodb.client.MongoClient as SyncMongoClient
import com.mongodb.kotlin.client.coroutine.MongoClient as CoroutineMongoClient

@Single
@Named("mongoConnectionHealthCheck")
fun provideMongoConnectionHealthCheck(mongoClient: SyncMongoClient): HealthCheck =
	MongoConnectionHealthCheck(mongoClient, "mongo_sync_connection")

@Single
@Named("mongoCoroutineConnectionHealthCheck")
fun provideMongoCoroutineConnectionHealthCheck(mongoClient: CoroutineMongoClient): HealthCheck =
	MongoConnectionHealthCheck(mongoClient, "mongo_coroutine_connection")
