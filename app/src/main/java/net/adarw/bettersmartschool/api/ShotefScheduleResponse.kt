package net.adarw.bettersmartschool.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ShotefScheduleResponse(
    val status: Boolean,
    val data: List<DaySchedule>?,
    val message: String? = null,
    val errorId: String? = null,
    val errorDescription: String? = null,
    val errorHTML: String? = null
)

@Serializable
data class DaySchedule(
    val dayIndex: Int,
    val hoursData: List<HourData> = emptyList()
)

@Serializable
data class HourData(
    val hour: Int,
    val hourName: String? = null,
    val scheduale: List<Lesson> = emptyList(),
    val changes: List<JsonElement> = emptyList(),
    val events: List<Event> = emptyList(),
    val exams: List<JsonElement> = emptyList()
)

@Serializable
data class Lesson(
    val day: Int,
    val hour: Int,
    val roomID: Int? = null,
    val studyGroupID: Int? = null,
    val subject: String? = null,
    val subjectLevel: String? = null,
    val room: String? = null,
    val teacherPrivateName: String? = null,
    val teacherLastName: String? = null,
    val classes: String? = null,
    val capsule: String? = null,
    val isPartani: Boolean? = null,
    val changes: List<JsonElement> = emptyList()
)

@Serializable
data class Event(
    val id: Int,
    val title: String? = null,
    val cancel_classes_last_day_until_end: Boolean? = null,
    val type: Int? = null,
    val textualType: String? = null,
    val from_date: String? = null,
    val to_date: String? = null,
    val from_hour: Int? = null,
    val to_hour: Int? = null,
    val job: String? = null,
    val payment: String? = null,
    val note: String? = null,
    val classes: String? = null,
    val accompaniers: String? = null,
    val groups: String? = null,
    val rooms: String? = null,
    val relatedStudents: String? = null
)