package net.gridtech.core.util

import io.reactivex.subjects.PublishSubject
import net.gridtech.core.data.DataChangedMessage
import net.gridtech.core.data.HostInfo


val hostInfoPublisher = PublishSubject.create<HostInfo>()
val dataChangedPublisher = PublishSubject.create<DataChangedMessage>()