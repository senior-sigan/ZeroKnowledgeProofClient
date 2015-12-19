package org.seniorsigan.zkpauthenticatorclient

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.TextView
import org.jetbrains.anko.find
import org.jetbrains.anko.onClick

class SuccessActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_success)
        Log.d(TAG, "In success activity")
        val message = intent.getSerializableExtra(SUCCESS_INTENT) as String
        val btn = find<Button>(R.id.ok_button)
        val text = find<TextView>(R.id.success_message)
        text.text = message
        btn.onClick {
            finish()
        }
    }
}
