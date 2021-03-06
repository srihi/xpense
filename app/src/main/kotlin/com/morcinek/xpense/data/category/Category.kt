package com.morcinek.xpense.data.category

import android.os.Parcel
import android.os.Parcelable
import com.morcinek.xpense.common.utils.createParcel
import com.morcinek.xpense.data.expense.Expense
import com.orm.SugarRecord

/**
 * Copyright 2016 Tomasz Morcinek. All rights reserved.
 */
class Category(var name: String = "", var color: Int? = null, id: Long? = null) : SugarRecord(), Comparable<Category>, Parcelable {

    init {
        setId(id)
    }

    override fun compareTo(other: Category) = name.compareTo(other.name)

    override fun toString() = name

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(name)
        dest.writeValue(color)
        dest.writeValue(id)
    }

    override fun describeContents() = 0

    companion object {
        @JvmField final val CREATOR = createParcel {
            Category(it.readString(), it.readValue(null) as Int?, it.readValue(null) as Long?)
        }
    }

    override fun equals(other: Any?): Boolean {
        other as Category
        return id.equals(other.id)
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}