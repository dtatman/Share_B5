package com.example.share_b5

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity() {

    private val PREFS_NAME = "task_prefs"
    private val KEY_TASKS = "tasks"
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var taskList: MutableList<Task>
    private lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etTask = findViewById<EditText>(R.id.etTask)
        val btnAddTask = findViewById<Button>(R.id.btnAddTask)
        listView = findViewById(R.id.lvTasks)

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        // Load danh sách tác vụ từ SharedPreferences
        taskList = loadTasks()

        // Tạo Adapter cho ListView
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, taskList.map { it.description })
        listView.adapter = adapter

        // Thêm tác vụ mới
        btnAddTask.setOnClickListener {
            val taskDescription = etTask.text.toString()
            if (taskDescription.isNotEmpty()) {
                addTask(Task(taskDescription))
                etTask.text.clear()
            }
        }

        // Xử lý sự kiện nhấn vào tác vụ để chỉnh sửa hoặc xóa
        listView.setOnItemClickListener { _, _, position, _ ->
            showEditDeleteDialog(position)
        }
    }

    // Hàm thêm tác vụ
    private fun addTask(task: Task) {
        taskList.add(task)
        saveTasks()
        updateListView()
    }

    // Hiển thị dialog chỉnh sửa hoặc xóa tác vụ
    private fun showEditDeleteDialog(position: Int) {
        val task = taskList[position]

        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Chỉnh sửa hoặc xóa tác vụ")

        val input = EditText(this)
        input.setText(task.description)
        dialog.setView(input)

        dialog.setPositiveButton("Lưu") { _, _ ->
            task.description = input.text.toString()
            saveTasks()
            updateListView()
        }

        dialog.setNegativeButton("Xóa") { _, _ ->
            taskList.removeAt(position)
            saveTasks()
            updateListView()
        }

        dialog.setNeutralButton("Hủy", null)
        dialog.show()
    }

    // Hàm cập nhật ListView
    private fun updateListView() {
        adapter.clear()
        adapter.addAll(taskList.map { it.description })
        adapter.notifyDataSetChanged()
    }

    // Hàm lưu danh sách tác vụ vào SharedPreferences
    private fun saveTasks() {
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(taskList)
        editor.putString(KEY_TASKS, json)
        editor.apply()
    }

    // Hàm tải danh sách tác vụ từ SharedPreferences
    private fun loadTasks(): MutableList<Task> {
        val gson = Gson()
        val json = sharedPreferences.getString(KEY_TASKS, null)
        val type = object : TypeToken<MutableList<Task>>() {}.type
        return if (json != null) gson.fromJson(json, type) else mutableListOf()
    }
}
