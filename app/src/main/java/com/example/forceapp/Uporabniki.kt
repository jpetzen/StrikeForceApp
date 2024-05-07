package com.example.forceapp

class Uporabniki (
    private val id: Int,
    private val username: String,
    private val email: String,
    private val password: String,
    private val role: String
)
{
    override fun toString(): String {
        return "User(id=$id, username='$username', password='$password' ,email='$email', role='$role')"
    }
}