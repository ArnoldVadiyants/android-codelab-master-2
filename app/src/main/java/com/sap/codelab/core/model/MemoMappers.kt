package com.sap.codelab.core.model

import com.sap.codelab.core.data.MemoEntity

internal fun MemoEntity.toDomain(): Memo = Memo(
    id = id,
    title = title,
    description = description,
    reminderDate = reminderDate,
    reminderLatitude = reminderLatitude,
    reminderLongitude = reminderLongitude,
    isDone = isDone
)

internal fun Memo.toEntity(): MemoEntity = MemoEntity(
    id = id,
    title = title,
    description = description,
    reminderDate = reminderDate,
    reminderLatitude = reminderLatitude,
    reminderLongitude = reminderLongitude,
    isDone = isDone
)
