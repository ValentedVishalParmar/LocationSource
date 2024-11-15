package com.libertyinfosace.locationsource.data.local.model

import java.io.Serializable

data class RequestResponse(
    var data: Any? = null,
    var message: String? = null,
    var success: Boolean? = null,
): Serializable
