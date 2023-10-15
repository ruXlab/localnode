package vc.rux.pokefork.web3j

import org.web3j.protocol.core.Response
import vc.rux.pokefork.errors.PokeForkError

class PokeForkRpcCallError : PokeForkError {
    val error: Response.Error


    constructor(message: String, error: Response.Error) : super(message) {
        this.error = error
    }

    constructor(error: Response.Error) : this("RPC call error: ${error.message}", error)

}

internal fun Response<*>.throwIfErrored() {
    if (hasError()) {
        throw PokeForkRpcCallError(error)
    }
}
internal fun Response<*>.throwIfResultIsNotTrue() {
    if (result != null && result != "true") {
        throw PokeForkError("No errors were reported but server returned '$result' instead of expected 'true'")
    }
}

internal fun Response<*>.throwIfErroredOrResultIsNotTrue() {
    throwIfErrored()
    throwIfResultIsNotTrue()
}