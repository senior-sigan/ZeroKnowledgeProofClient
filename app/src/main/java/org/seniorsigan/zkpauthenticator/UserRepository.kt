package org.seniorsigan.zkpauthenticator

interface UserRepository {
    fun findAll(): List<UserModel>
    fun findByDomain(domain: String, algorithm: String): List<UserModel>
    fun find(domain: String, name: String): UserModel?
    fun create(name: String, domainName: String, algorithmName: String, secretJson: String): UserModel
    fun update(model: UserModel): UserModel
    fun delete(model: UserModel)
    fun deleteAll()
}