package com.example.forceapp

class Opravila(
    val user_username: String,
    val done: String,
    val datum: String,
    val opravilo: String,
){

    override fun toString(): String {
        return "Opravila( user_username='$user_username', done=$done, datum='$datum', opravilo='$opravilo')"
    }
}