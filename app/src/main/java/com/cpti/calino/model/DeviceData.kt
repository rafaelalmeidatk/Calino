package com.cpti.calino.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by rafael on 07/12/17.
 */
open class DeviceData (
        @PrimaryKey var id: Int = 0,
        var value: Long = 0
) : RealmObject()