package vc.rux.localnode.hardhat.internal

import com.fasterxml.jackson.databind.ObjectMapper

internal class HardHatConfigJs(
    private val chainId: Long = 31337,
    private val blockNumber: Long? = null
) {
    /**
     * Generates the hardhat config file content
     */
    fun toJsConfigConfig(): String =
        "module.exports = " + ObjectMapper().writeValueAsString(
            mapOf(
                "networks" to mapOf(
                    "hardhat" to mapOf(
                        "chainId" to chainId,
                        "blockNumber" to blockNumber
                    )
                )
            )
        )
}
