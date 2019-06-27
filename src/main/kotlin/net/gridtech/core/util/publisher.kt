package net.gridtech.core.util

import io.reactivex.subjects.PublishSubject
import net.gridtech.core.data.DataChangedMessage
import net.gridtech.core.data.IHostInfo


val hostInfoPublisher = PublishSubject.create<IHostInfo>()
val dataChangedPublisher = PublishSubject.create<DataChangedMessage>()