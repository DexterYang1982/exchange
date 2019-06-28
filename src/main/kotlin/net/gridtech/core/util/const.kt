package net.gridtech.core.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

val PEER_ID = generateId()

const val COMPOSE_ID_SEPARATOR = "^"
const val ID_NODE_ROOT = "ROOT"
const val ID_NODE_CLASS_ROOT = "ROOT-CLASS"

const val KEY_FIELD_RUNNING_STATUS = "RUNNING-STATUS"
const val KEY_FIELD_SECRET = "SECRET"

const val INTERVAL_TRY_CONNECT_TO_PARENT = 3000L
const val INTERVAL_RUNNING_STATUS_REPORT = 20000L