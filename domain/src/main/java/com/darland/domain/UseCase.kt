package com.darland.domain

abstract class UseCase<T>() {
    abstract suspend fun execute(): T
}

interface ApiHelper {
    fun hasInternet(): Boolean
}
