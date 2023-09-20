package vc.rux.pokefork.hardhat.internal

import java.util.Base64


internal class HardHatDockerfile(
    val jsConfigJs: HardHatConfigJs,
    val hardhatVersion: String,
    val commandLineParams: List<String>,
    val baseImage: String = "node:16-alpine"
) {
    fun toDockerfileContent(): String {
        // encode the JSON config to base64 so we can pass it as a single line without escaping
        val jsConfigBase64 = Base64.getEncoder().encode(jsConfigJs.toJsConfigConfig().toByteArray()).decodeToString()
        
        return """
            FROM $baseImage 

            EXPOSE 8545

            RUN adduser -S appuser && mkdir -p app && chown -R appuser /app

            WORKDIR /app

            RUN echo "$jsConfigBase64" | base64 -d > /app/hardhat.config.js

            USER appuser

            ## Install hardhat - without the global installation we'd need to have package.json as well
            RUN yarn global add hardhat@$hardhatVersion --non-interactive --frozen-lockfile
            RUN yarn add hardhat@$hardhatVersion --non-interactive --frozen-lockfile


            ENTRYPOINT [${commandLineParams.joinToString(",") { "\"$it\"" }}]
        """.trimIndent()
    }
}
