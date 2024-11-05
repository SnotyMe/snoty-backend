package me.snoty.integration.common.wiring.data

import kotlinx.coroutines.flow.FlowCollector

typealias EmitNodeOutputContext = FlowCollector<IntermediateData>
