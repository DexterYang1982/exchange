package net.gridtech.core.data

import io.reactivex.subjects.PublishSubject
import java.util.concurrent.ConcurrentHashMap
import javax.annotation.PostConstruct

val dataChangedPublisher: PublishSubject<DataChangedMessage> = PublishSubject.create()

abstract class IBaseService<T : IBaseData>(enableCache: Boolean, protected val dao: IBaseDao<T>) {
    val serviceName: String = javaClass.simpleName
    private val cache: ConcurrentHashMap<String, T>? = if (enableCache) ConcurrentHashMap() else null

    @PostConstruct
    fun register() {
        services[serviceName] = this
    }

    companion object {
        private val services = HashMap<String, IBaseService<*>>()
        fun get(name: String): IBaseService<*> = services[name]!!
    }

    open fun getAll(): List<T> = dao.getAll()
    open fun getById(id: String): T? {
        var data = cache?.get(id)
        if (data == null) {
            data = dao.getById(id)
        }
        if (data != null) {
            cache?.put(id, data)
        }
        return data
    }
    open fun save(data: T, direction: ChangedDirection? = null) {
        cache?.put(data.id, data)
        dao.save(data)
        dataChangedPublisher.onNext(DataChangedMessage(
                data.id,
                serviceName,
                ChangedType.UPDATE,
                direction ?: ChangedDirection.DOWN
        ))
    }

    open fun delete(id: String) {
        cache?.remove(id)
        dao.delete(id)
        dataChangedPublisher.onNext(DataChangedMessage(
                id,
                serviceName,
                ChangedType.DELETE,
                ChangedDirection.DOWN
        ))
    }
}

class NodeClassService(enableCache: Boolean, dao: INodeClassDao) : IBaseService<INodeClass>(enableCache, dao) {
}

class FieldService(enableCache: Boolean, dao: IFieldDao) : IBaseService<IField>(enableCache, dao) {
}

class NodeService(enableCache: Boolean, dao: INodeDao) : IBaseService<INode>(enableCache, dao) {
}

class FieldValueService(enableCache: Boolean, dao: IFieldValueDao) : IBaseService<IFieldValue>(enableCache, dao) {
}