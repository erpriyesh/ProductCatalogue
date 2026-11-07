# kotlinx.serialization and Ktor ship their own consumer rules.
# Silence warnings for optional classes Ktor references but Android does not have.
-dontwarn org.slf4j.**
-dontwarn java.lang.management.**
