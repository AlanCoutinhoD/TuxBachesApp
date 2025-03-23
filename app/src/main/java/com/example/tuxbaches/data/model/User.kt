data class User(
    val username: String = "",
    val email: String = "",
    val password: String = ""
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val token: String
)