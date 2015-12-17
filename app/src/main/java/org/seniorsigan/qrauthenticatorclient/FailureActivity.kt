package org.seniorsigan.qrauthenticatorclient

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import org.jetbrains.anko.find
import org.jetbrains.anko.onClick
import org.seniorsigan.qrauthenticatorclient.persistence.AccountModel

class FailureActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_failure)
        val error = intent.getSerializableExtra(FAILURE_INTENT) as String
        val btn = find<Button>(R.id.fail_button)
        val text = find<TextView>(R.id.fail_message)
        text.text = error
        btn.onClick {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}
