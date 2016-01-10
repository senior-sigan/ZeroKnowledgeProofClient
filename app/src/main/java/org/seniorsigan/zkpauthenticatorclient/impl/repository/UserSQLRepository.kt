package org.seniorsigan.zkpauthenticatorclient.impl.repository

import android.content.ContentValues
import android.database.Cursor
import android.util.Log
import org.seniorsigan.zkpauthenticator.UserModel
import org.seniorsigan.zkpauthenticator.UserRepository
import org.seniorsigan.zkpauthenticatorclient.TAG
import java.text.SimpleDateFormat
import java.util.*

private const val TABLE_NAME = "account"

class UserSQLRepository(val db: DatabaseOpenHelper): UserRepository {
    private val iso8601Format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    private val projection = arrayOf(
            AccountEntry.ID,
            AccountEntry.NAME,
            AccountEntry.DOMAIN,
            AccountEntry.SECRET,
            AccountEntry.ALGORITHM,
            AccountEntry.CREATED_AT,
            AccountEntry.UPDATED_AT)

    private fun convert(cursor: Cursor?): List<AccountModel> {
        iso8601Format.timeZone = TimeZone.getTimeZone("UTC")
        val accounts: MutableList<AccountModel> = arrayListOf()
        if (cursor != null) {
            Log.d(TAG, "Loaded ${cursor.count} elements")
            if (cursor.moveToFirst()) {
                do {
                    accounts.add(AccountModel(
                            _id = cursor.getLong(0),
                            name = cursor.getString(1),
                            domain = cursor.getString(2),
                            secret = cursor.getString(3),
                            algorithm = cursor.getString(4),
                            updatedAt = iso8601Format.parse(cursor.getString(5)),
                            createdAt = iso8601Format.parse(cursor.getString(6))))
                } while (cursor.moveToNext())
            }
        }

        return accounts
    }

    override fun findAll(): List<AccountModel> {
        Log.d(TAG, "Try to find all accounts")

        val cursor = db.readableDatabase?.query(
                TABLE_NAME, projection,
                null, null, null, null, null, null)

        return convert(cursor)
    }

    override fun findByDomain(domain: String, algorithm: String): List<AccountModel> {
        Log.d(TAG, "Try to find accounts for $domain and algorithm $algorithm")

        val cursor = db.readableDatabase?.query(
                TABLE_NAME, projection,
                "${AccountEntry.DOMAIN} = ? AND ${AccountEntry.ALGORITHM} = ?",
                arrayOf(domain, algorithm),
                null, null, null, null)

        return convert(cursor)
    }

    override fun find(domain: String, name: String): AccountModel? {
        Log.d(TAG, "Try to find account $name:$domain")

        val cursor = db.readableDatabase?.query(
                TABLE_NAME, projection,
                "${AccountEntry.DOMAIN} = ? AND ${AccountEntry.NAME} = ?",
                arrayOf(domain, name),
                null, null, null, null)

        val accounts = convert(cursor)
        if (accounts.size > 1) {
            throw Exception("Find more than one accounts, it's strange. DB might be inconsistent")
        }

        if (accounts.size == 0) {
            return null
        }

        return accounts.first()
    }

    fun find(id: Long): AccountModel? {
        Log.d(TAG, "Try to find account by id $id")

        val cursor = db.readableDatabase?.query(
                TABLE_NAME, projection,
                "${AccountEntry.ID}",
                arrayOf(id.toString()),
                null, null, null, null)

        val accounts = convert(cursor)
        if (accounts.size > 1) {
            throw Exception("Find more than one accounts, it's strange. DB might be inconsistent")
        }

        if (accounts.size == 0) {
            return null
        }

        return accounts.first()
    }

    override fun create(name: String, domainName: String, algorithmName: String, secretJson: String): AccountModel {
        var newAccount: AccountModel
        db.writableDatabase.beginTransaction()
        try {
            val account = find(domainName, name)
            if (account != null) {
                throw Exception("Account for $name:$domainName already exists")
            }
            val values = ContentValues()
            values.put(AccountEntry.NAME, name)
            values.put(AccountEntry.DOMAIN, domainName)
            values.put(AccountEntry.SECRET, secretJson)
            values.put(AccountEntry.ALGORITHM, algorithmName)
            val id = db.writableDatabase.insertOrThrow(TABLE_NAME, null, values)
            newAccount = AccountModel(id, name, domainName, secretJson, algorithmName)
            db.writableDatabase.setTransactionSuccessful()
        } catch(e: Exception) {
            throw Exception("Something went wrong while saving account $name:$domainName : ${e.message}", e)
        } finally {
            db.writableDatabase.endTransaction()
        }
        return newAccount
    }

    override fun update(model: UserModel): AccountModel {
        var account: AccountModel
        db.writableDatabase.beginTransaction()
        try {
            find(model.domain, model.name) ?: throw Exception("Account for $model not found in db")
            val values = ContentValues()
            values.put(AccountEntry.SECRET, model.secret)
            db.writableDatabase.update(TABLE_NAME, values, "${AccountEntry.NAME} = ? AND ${AccountEntry.DOMAIN} = ?", arrayOf(model.name, model.domain))
            account = find(model.domain, model.name) ?: throw Exception("Very strange! Account for $model not found in db after updating")
            db.writableDatabase.setTransactionSuccessful()
        } catch(e: Exception) {
            throw Exception("Something went wrong while incrementing account's token count $model: ${e.message}", e)
        } finally {
            db.writableDatabase.endTransaction()
        }
        return account
    }

    override fun delete(model: UserModel) {
        throw UnsupportedOperationException()
    }

    override fun deleteAll() {
        db.writableDatabase.delete(TABLE_NAME, null, emptyArray())
        Log.i(TAG, "All accounts was deleted")
    }

}