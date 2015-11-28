package org.seniorsigan.qrauthenticatorclient.persistence

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import org.seniorsigan.qrauthenticatorclient.TAG

private const val DATABASE_VERSION = 1
private const val DATABASE_NAME = "accounts.db"
private const val TABLE_NAME = "account"

class AccountsOpenHelper(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("""
            CREATE TABLE $TABLE_NAME (
            ${AccountEntry._ID} INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            ${AccountEntry.NAME} TEXT NOT NULL,
            ${AccountEntry.DOMAIN} TEXT NOT NULL,
            ${AccountEntry.TOKENS} TEXT NOT NULL,
            ${AccountEntry.CURRENT_TOKEN} INTEGER NOT NULL,
            ${AccountEntry.CREATED_AT} TIMESTAMP NOT NULL DEFAULT current_timestamp,
            ${AccountEntry.UPDATED_AT} TIMESTAMP NOT NULL DEFAULT current_timestamp,
            UNIQUE(${AccountEntry.NAME}, ${AccountEntry.DOMAIN})
        )""")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        //nothing to do - only one db version
    }

    fun findAccounts(domain: String): List<AccountModel> {
        Log.d(TAG, "Try to find accounts for $domain")

        val cursor = readableDatabase?.query(
                TABLE_NAME, arrayOf(
                    AccountEntry._ID,
                    AccountEntry.NAME,
                    AccountEntry.DOMAIN,
                    AccountEntry.TOKENS,
                    AccountEntry.CURRENT_TOKEN,
                    AccountEntry.CREATED_AT,
                    AccountEntry.UPDATED_AT),
                "${AccountEntry.DOMAIN} = ?",
                arrayOf(domain),
                null, null, null, null)

        return convert(cursor)
    }

    fun findAccount(domain: String, name: String): AccountModel? {
        Log.d(TAG, "Try to find account for $domain and $name")

        val cursor = readableDatabase.query(
                TABLE_NAME, arrayOf(
                AccountEntry._ID,
                AccountEntry.NAME,
                AccountEntry.DOMAIN,
                AccountEntry.TOKENS,
                AccountEntry.CURRENT_TOKEN,
                AccountEntry.CREATED_AT,
                AccountEntry.UPDATED_AT),
                "${AccountEntry.DOMAIN} = ? AND ${AccountEntry.NAME} = ?",
                arrayOf(domain, name),
                null, null, null, null)

        val accounts = convert(cursor)

        if (accounts.size > 1) {
            throw PersistenceException("Find more than one accounts, it's strange. DB might be inconsistent")
        }

        if (accounts.size == 0) {
            return null
        }

        return accounts.first()
    }

    fun convert(cursor: Cursor?): List<AccountModel> {
        val accounts: MutableList<AccountModel> = arrayListOf()
        if (cursor != null) {
            Log.d(TAG, "Loaded ${cursor.count} elements")
            if (cursor.moveToFirst()) {
                do {
                    val rawTokens = cursor.getString(3)
                    var tokens = emptyList<String>()
                    if (rawTokens != null) {
                        tokens = rawTokens.split(",")
                    }
                    accounts.add(AccountModel(
                            id = cursor.getLong(0),
                            name = cursor.getString(1),
                            domain = cursor.getString(2),
                            tokens = tokens,
                            currentToken = cursor.getInt(4)))
                } while (cursor.moveToNext())
            }
        }

        return accounts
    }

    fun saveAccount(model: AccountModel) {
        writableDatabase.beginTransaction()
        try {
            val account = findAccount(model.domain, model.name)
            if (account != null) {
                throw PersistenceException("Account for $model already exists")
            }
            val values = ContentValues()
            values.put(AccountEntry.NAME, model.name)
            values.put(AccountEntry.DOMAIN, model.domain)
            values.put(AccountEntry.CURRENT_TOKEN, model.currentToken)
            values.put(AccountEntry.TOKENS, model.tokens.joinToString(","))
            val id = writableDatabase.insertOrThrow(TABLE_NAME, null, values)
            model.id = id
            writableDatabase.setTransactionSuccessful()
        } catch(e: Exception) {
            throw PersistenceException("Something went wrong while saving account $model: ${e.message}", e)
        } finally {
            writableDatabase.endTransaction()
        }
    }

    fun incrementTokenCount(model: AccountModel) {
        writableDatabase.beginTransaction()
        try {
            val account = findAccount(model.domain, model.name) ?: throw PersistenceException("Account for $model not found in db")
            val values = ContentValues()
            values.put(AccountEntry.CURRENT_TOKEN, model.currentToken + 1)
            writableDatabase.update(TABLE_NAME, values, "${AccountEntry._ID} = ?", arrayOf(model.id.toString()))
            writableDatabase.setTransactionSuccessful()
        } catch(e: Exception) {
            throw PersistenceException("Something went wrong while incrementing account's token count $model: ${e.message}", e)
        } finally {
            writableDatabase.endTransaction()
        }
    }
}