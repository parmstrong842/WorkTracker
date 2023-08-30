package com.example.worktracker.mock

import com.example.worktracker.data.Shift
import com.example.worktracker.data.ShiftsRepository

class ShiftsRepositoryMock : ShiftsRepository {
    var shift: Shift? = null

    override suspend fun getAllItemsStream(): List<Shift> { return listOf() }
    override suspend fun getItemStream(id: Int): Shift? { return null }
    override suspend fun insertItem(item: Shift) {
        shift = item
    }
    override suspend fun deleteItem(item: Shift) {}
    override suspend fun updateItem(item: Shift) {}
}