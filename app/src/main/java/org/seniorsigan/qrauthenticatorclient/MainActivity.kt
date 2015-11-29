package org.seniorsigan.qrauthenticatorclient

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import org.jetbrains.anko.find
import org.jetbrains.anko.onClick
import org.seniorsigan.qrauthenticatorclient.persistence.AccountsOpenHelper

class MainActivity : AppCompatActivity() {
    lateinit var accountsDb: AccountsOpenHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = find<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val fab = find<FloatingActionButton>(R.id.fab)
        fab.onClick { view ->
            val intent = Intent(this, QRCodeScannerActivity::class.java)
            startActivity(intent)
        }

        accountsDb = AccountsOpenHelper(this)
        val accounts = accountsDb.findAllAccounts()

        val accountsView = find<RecyclerView>(R.id.accounts_recycler_view)
        accountsView.setHasFixedSize(true)
        accountsView.layoutManager = LinearLayoutManager(this)
        accountsView.adapter = AccountsAdapter(accounts)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}
