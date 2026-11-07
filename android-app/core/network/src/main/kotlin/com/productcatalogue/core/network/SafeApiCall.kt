package com.productcatalogue.core.network

import com.productcatalogue.core.model.result.DataError
import com.productcatalogue.core.model.result.DataResult
import com.productcatalogue.core.network.dto.ApiErrorDto
import com.productcatalogue.core.network.dto.FieldIssueDto
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * Runs one API call and turns every outcome into a [DataResult]: the success value, the backend
 * error envelope, or a transport failure. The only place in the app that knows about HTTP status
 * codes and exceptions; everything above it works with [DataError].
 */
suspend fun <T> safeApiCall(json: Json, call: suspend () -> T): DataResult<T> = try {
    DataResult.Success(call())
} catch (e: CancellationException) {
    // Never swallow cancellation: it is how coroutines stop work (e.g. a newer search replaced this one).
    throw e
} catch (e: HttpException) {
    DataResult.Failure(e.toDataError(json))
} catch (e: SocketTimeoutException) {
    DataResult.Failure(DataError.Timeout)
} catch (e: IOException) {
    DataResult.Failure(DataError.NoConnection)
} catch (e: SerializationException) {
    // A 2xx body that does not match our DTOs.
    DataResult.Failure(DataError.Unexpected)
}

private fun HttpException.toDataError(json: Json): DataError {
    val body = try {
        response()?.errorBody()?.string()?.let { json.decodeFromString<ApiErrorDto>(it).error }
    } catch (e: SerializationException) {
        null
    } catch (e: IllegalArgumentException) {
        null
    }
    return when (code()) {
        400 -> DataError.Invalid(
            message = body?.message ?: "Some details are invalid",
            fieldErrors = body?.details?.let { details ->
                try {
                    json.decodeFromJsonElement<List<FieldIssueDto>>(details)
                        // The first message per field is the one to show.
                        .groupBy({ it.field }, { it.message })
                        .mapValues { it.value.first() }
                } catch (e: SerializationException) {
                    null
                } catch (e: IllegalArgumentException) {
                    null
                }
            }.orEmpty(),
        )
        404 -> DataError.NotFound
        429 -> DataError.RateLimited
        in 500..599 -> DataError.Server(body?.code)
        else -> DataError.Unexpected
    }
}
