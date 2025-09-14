package bong.training.static_proxy

import bong.training.real.UserService

class UserLogService(
    private val userService: UserService,
    private val logProxy: LogProxy
) : UserService {

    override fun findUser(id: Long): String {
        return logProxy.executeWithLog {
            userService.findUser(id)
        }
    }

    override fun saveUser(name: String): Long {
        return logProxy.executeWithLog {
            userService.saveUser(name)
        }
    }

    override fun deleteUser(id: Long): Boolean {
        return logProxy.executeWithLog { userService.deleteUser(id) }
    }
}
