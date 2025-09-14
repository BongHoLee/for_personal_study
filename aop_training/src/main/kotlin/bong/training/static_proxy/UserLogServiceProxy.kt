package bong.training.static_proxy

class UserLogServiceProxy(
    private val userService: UserService,
    private val logInvocator: LogInvocator
) : UserService {

    override fun findUser(id: Long): String {
        return logInvocator.executeWithLog {
            userService.findUser(id)
        }
    }

    override fun saveUser(name: String): Long {
        return logInvocator.executeWithLog {
            userService.saveUser(name)
        }
    }

    override fun deleteUser(id: Long): Boolean {
        return logInvocator.executeWithLog { userService.deleteUser(id) }
    }
}
