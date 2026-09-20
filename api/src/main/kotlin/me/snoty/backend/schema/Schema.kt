package me.snoty.backend.schema

typealias ObjectSchema = List<SchemaField>

/**
 * Describes no input or output fields for a node.
 */
object NoSchema

/**
 * Describes an empty [ObjectSchema]. This is useful when a node does have input or output fields, but they are not known at compile time.
 * The frontend will have to handle autocomplete and validation in such cases explicitly.
 */
object EmptySchema
