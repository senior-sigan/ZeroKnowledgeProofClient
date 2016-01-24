package org.seniorsigan.zkpauthenticatorclient

import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.jetbrains.anko.find
import org.jetbrains.anko.layoutInflater
import org.jetbrains.anko.onClick
import org.seniorsigan.zkpauthenticatorclient.impl.repository.AccountModel

class AccountsAdapter(val accounts: List<AccountModel>): RecyclerView.Adapter<ViewHolder>() {
    var onItemClickListener: ((AccountModel) -> Unit)? = null

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setItem(accounts[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder? {
        val view = parent.context.layoutInflater.inflate(R.layout.account_view, parent, false)
        return ViewHolder(view, onItemClickListener)
    }

    override fun getItemCount(): Int {
        return accounts.size
    }
}

class ViewHolder(view: View, var onItemClickListener: ((AccountModel) -> Unit)?): RecyclerView.ViewHolder(view) {
    val name = view.find<TextView>(R.id.name)
    val domain = view.find<TextView>(R.id.domain)
    val updatedAt = view.find<TextView>(R.id.updatedAt)
    val algorithm = view.find<TextView>(R.id.algorithm)

    fun setItem(account: AccountModel) {
        Log.d(TAG, "ViewHolder should show ${account.name}@${account.domain}")
        itemView?.onClick { onItemClickListener?.invoke(account) }
        name.text = account.name
        domain.text = account.domain
        algorithm.text = account.algorithm
        updatedAt.text = DateUtils.getRelativeTimeSpanString(
                account.updatedAt.time,
                System.currentTimeMillis(),
                DateUtils.SECOND_IN_MILLIS)
    }
}