package vc.rux.localnode.anvil.internal

internal data class AnvilDockerfile(
    val commandLineParams: List<String>,
    val baseImage: String = "debian:12.2-slim"
) {

    fun toDockerfileContent(): String {
        return """
            FROM $baseImage 

            EXPOSE 8545

            RUN useradd --create-home --shell /bin/bash appuser 
            
            RUN apt update && apt install -y curl git

            USER appuser

            RUN curl -L https://foundry.paradigm.xyz | bash

            RUN /home/appuser/.foundry/bin/foundryup
            WORKDIR /home/appuser

            ENTRYPOINT [${commandLineParams.joinToString(",") { "\"$it\"" }}]
        """.trimIndent()
    }

}