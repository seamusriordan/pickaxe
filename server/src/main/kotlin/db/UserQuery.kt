package db

import UserDTO

class UserQuery {
    fun getActiveUsers(): ArrayList<UserDTO> {
        val results = ArrayList<UserDTO>(1)
        results.add(UserDTO("Seamus"))
        return results
    }
}