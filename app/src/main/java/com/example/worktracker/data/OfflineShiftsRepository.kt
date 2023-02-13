package com.example.worktracker.data

class OfflineShiftsRepository(private val shiftDao: ShiftDao) : ShiftsRepository {
    override suspend fun getAllItemsStream(): List<Shift> = shiftDao.getAllItems()

    override suspend fun getItemStream(id: Int): Shift = shiftDao.getItem(id)

    override suspend fun insertItem(item: Shift) = shiftDao.insert(item)

    override suspend fun deleteItem(item: Shift) = shiftDao.delete(item)

    override suspend fun updateItem(item: Shift) = shiftDao.update(item)
}