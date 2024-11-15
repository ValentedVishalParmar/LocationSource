package com.libertyinfosace.locationsource.data.local.model

abstract class RequestResponseData {

    suspend fun requestData(call: suspend () -> Any?): RequestResponse {
        val response = call.invoke()

        val requestResponse = RequestResponse()
                try {
                    requestResponse.success = true
                    requestResponse.message = "success"
                    requestResponse.data = response

                } catch (e: Exception) {
                   requestResponse.success = false
                   requestResponse.message = e.message
                   requestResponse.data = null
                }

        return requestResponse
    }
}
