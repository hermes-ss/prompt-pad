package com.hermes.promptpad

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class Note(val id: Long, var folder: String, var title: String, var body: String)
data class Task(val id: Long, val text: String, val done: Boolean)

fun visibleNoteFolder(folder: String): String = if (folder == "Journals") "Personal" else folder

/** ponytail: JSON in SharedPreferences. Notes/tasks are tens of rows on a personal phone; no Room. */
class Store(ctx: Context) {
    private val p = ctx.getSharedPreferences("promptpad_data", Context.MODE_PRIVATE)

    fun notes(): List<Note> {
        val a = JSONArray(p.getString("notes", "[]"))
        return List(a.length()) {
            val o = a.getJSONObject(it)
            Note(o.getLong("id"), visibleNoteFolder(o.getString("folder")), o.getString("title"), o.getString("body"))
        }
    }

    fun saveNotes(list: List<Note>) {
        val a = JSONArray()
        list.forEach { a.put(JSONObject().put("id", it.id).put("folder", it.folder).put("title", it.title).put("body", it.body)) }
        p.edit().putString("notes", a.toString()).apply()
    }

    fun tasks(): List<Task> {
        val a = JSONArray(p.getString("tasks", "[]"))
        return List(a.length()) {
            val o = a.getJSONObject(it)
            Task(o.getLong("id"), o.getString("text"), o.getBoolean("done"))
        }
    }

    fun saveTasks(list: List<Task>) {
        val a = JSONArray()
        list.forEach { a.put(JSONObject().put("id", it.id).put("text", it.text).put("done", it.done)) }
        p.edit().putString("tasks", a.toString()).apply()
    }

    companion object { val FOLDERS = listOf("Personal", "Work", "Ideas") }
}
