package com.example.forceapp

class Sredstva (
    val user_username: String,
    val stevilo: Int,
    val denar: Double,
    val date: String,
    val cistilo: String
) {

    override fun toString(): String {
        return "Sredstva(user_username='$user_username', stevilo=$stevilo, denar=$denar, date='$date', cistila='$cistilo')"
    }

}