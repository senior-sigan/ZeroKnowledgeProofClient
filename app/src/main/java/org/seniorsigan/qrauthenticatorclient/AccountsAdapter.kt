package org.seniorsigan.qrauthenticatorclient

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.seniorsigan.qrauthenticatorclient.persistence.AccountModel

class AccountsAdapter(val accounts: List<AccountModel>): RecyclerView.Adapter<ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setItem(accounts[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder? {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.account_view, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return accounts.size
    }
}

class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
    val name = view.findViewById(R.id.name) as TextView
    val domain = view.findViewById(R.id.domain) as TextView
    val updatedAt = view.findViewById(R.id.updatedAt) as TextView
    val tokensUsed = view.findViewById(R.id.tokensUsed) as TextView

    fun setItem(account: AccountModel) {
        Log.d(TAG, "ViewHolder should show $account")
        name.text = account.name
        domain.text = account.domain
        updatedAt.text = account.updatedAt.toString()
        tokensUsed.text = "${account.tokens.size}/${account.currentToken}"
    }
}