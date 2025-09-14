package bong.training.real

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

interface UserService {
    fun findUser(id: Long): String
    fun saveUser(name: String): Long
    fun deleteUser(id: Long): Boolean
}

class UserServiceImpl : UserService {
    private val users: ConcurrentHashMap<Long, String> = ConcurrentHashMap()
    private var id: AtomicLong = AtomicLong(0)

    override fun findUser(id: Long): String {
        return users[id] ?: throw NoSuchElementException("User with id $id not found")
    }

    override fun saveUser(name: String): Long {
        val id = generateId()
        users[id] = name
        return id
    }

    override fun deleteUser(id: Long): Boolean {
        return users.remove(id) != null
    }

    private fun generateId(): Long {
        return id.incrementAndGet()
    }
}