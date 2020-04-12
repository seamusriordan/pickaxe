fun getEnvOrDefault(env: String, default: String): String {
    var value = System.getenv(env)
    if (value == null) {
        value = default
    }
    return value
}