package org.seniorsigan.zkpauthenticatorclient.impl.repository

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import org.seniorsigan.zkpauthenticatorclient.TAG

private const val DATABASE_VERSION = 1
private const val DATABASE_NAME = "accounts_2.db"
private const val TABLE_NAME = "account"

class DatabaseOpenHelper
private constructor(ctx: Context): SQLiteOpenHelper(
        ctx, DATABASE_NAME, null, DATABASE_VERSION
) {
    companion object {
        private var instance: DatabaseOpenHelper? = null

        fun getInstance(ctx: Context): DatabaseOpenHelper {
            synchronized(this, {
                if (instance == null) {
                    instance = DatabaseOpenHelper(ctx.applicationContext)
                }
                return instance!!
            })
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {
        Log.d(TAG, "Create table '$TABLE_NAME'")
        db?.execSQL("""
            CREATE TABLE $TABLE_NAME (
            ${AccountEntry.ID} INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            ${AccountEntry.NAME} TEXT NOT NULL,
            ${AccountEntry.DOMAIN} TEXT NOT NULL,
            ${AccountEntry.SECRET} TEXT NOT NULL,
            ${AccountEntry.ALGORITHM} TEXT NOT NULL,
            ${AccountEntry.CREATED_AT} TIMESTAMP NOT NULL DEFAULT current_timestamp,
            ${AccountEntry.UPDATED_AT} TIMESTAMP NOT NULL DEFAULT current_timestamp,
            UNIQUE(${AccountEntry.NAME}, ${AccountEntry.DOMAIN})
        )""")

        Log.d(TAG, "Create trigger 'updated_at_trigger'")
        db?.execSQL("""
            CREATE TRIGGER updated_at_trigger
            AFTER UPDATE ON $TABLE_NAME
            FOR EACH ROW BEGIN
                UPDATE $TABLE_NAME
                SET ${AccountEntry.UPDATED_AT} = current_timestamp
                WHERE ${AccountEntry.ID} = old.${AccountEntry.ID};
            END
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        //nothing to do - only one db version
    }

}