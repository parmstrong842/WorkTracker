package com.example.worktracker.mock

import com.example.worktracker.data.Shift
import com.example.worktracker.data.ShiftsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow

class ShiftsRepositoryMock : ShiftsRepository {
    var currentShift: Shift? = null
    private val shifts = mutableListOf<Shift>()
    private val _shiftsFlow = MutableStateFlow<List<Shift>>(emptyList())
    private val shiftsFlow: Flow<List<Shift>> = _shiftsFlow.asStateFlow()

    private var id = 1

    override suspend fun getAllItems(): List<Shift> {
        return shifts
    }

    override fun getAllItemsFlow(): Flow<List<Shift>> {
        return shiftsFlow
    }

    override suspend fun getItemStream(id: Int): Shift? {
        return shifts.find { it.id == id }
    }
    override suspend fun insertItem(item: Shift) {
        currentShift = item
        shifts.add(item.copy(id = id++))
        _shiftsFlow.emit(shifts.toList())
    }
    override suspend fun deleteItem(item: Shift) {
        shifts.removeIf { it.id == item.id }
        _shiftsFlow.emit(shifts.toList())
    }
    override suspend fun updateItem(item: Shift) {
        val oldShift = shifts.find { it.id == item.id }
        if (oldShift != null) {
            shifts[shifts.indexOf(oldShift)] = oldShift.copy(
                date = item.date,
                shiftSpan = item.shiftSpan,
                breakTotal = item.breakTotal,
                shiftTotal = item.shiftTotal
            )
            _shiftsFlow.emit(shifts.toList())
        }
    }
}