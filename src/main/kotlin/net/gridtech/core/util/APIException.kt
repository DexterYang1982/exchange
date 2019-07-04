package net.gridtech.core.util

import kotlin.reflect.KClass


class APIException(var code: String, var info: String, var ext: String? = null, var entity: String? = null) : RuntimeException() {
    fun getExceptionMessage() =
            mapOf("code" to code,
                    "info" to info,
                    "ext" to (ext ?: ""),
                    "entity" to (entity ?: ""))

    override val message: String?
        get() = info
}


enum class APIExceptionEnum(val info: String, private val type: ErrorEnum) {
    ERR01_ID_NOT_EXIST("cannot find entity by id", ErrorEnum.SHOULD_NOT_BE_NULL),
    ERR02_ID_EXISTS("id exists", ErrorEnum.SHOULD_BE_NULL),
    ERR08_ENTITIES_NOT_IN_SAME_NODECLASS("entities not in the same node class", ErrorEnum.SHOULD_BE_TRUE),
    ERR10_CAN_NOT_BE_DELETED("entity cannot be deleted", ErrorEnum.SHOULD_BE_TRUE),
    ERR20_ROOT_NODE_IS_SINGLETON("root node is singleton", ErrorEnum.SHOULD_BE_TRUE),
    ERR30_NODE_SECRET_INVALID("node secret invalid", ErrorEnum.SHOULD_BE_TRUE),
    ERR40_PARENT_HOST_NOT_RESPOND_CORRECTLY("parent host not respond correctly", ErrorEnum.SHOULD_BE_TRUE),
    ERR50_EXCHANGE_ERROR("exchange error", ErrorEnum.SHOULD_BE_TRUE);

    private fun throwException(entity: KClass<*>?, ext: String?): Nothing = throw APIException(name, info, ext, entity?.simpleName)

    fun <T> assert(obj: T?, entity: KClass<*>? = null, ext: String? = null): T? {
        when (type) {
            ErrorEnum.SHOULD_NOT_BE_NULL -> obj ?: throwException(entity, ext)
            ErrorEnum.SHOULD_BE_NULL -> obj?.let { throwException(entity, ext) }
            ErrorEnum.SHOULD_NOT_BE_EMPTY -> (obj as? String).isNullOrBlank().let { if (it) throwException(entity, ext) }
            ErrorEnum.SHOULD_BE_TRUE -> if (obj != true) throwException(entity, ext)
        }
        return obj
    }

    fun toException(ext: String? = null): APIException = APIException(name, info, ext)
    enum class ErrorEnum {
        SHOULD_NOT_BE_NULL,
        SHOULD_BE_NULL,
        SHOULD_NOT_BE_EMPTY,
        SHOULD_BE_TRUE
    }
}
