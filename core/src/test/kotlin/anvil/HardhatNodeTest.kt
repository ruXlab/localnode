package anvil

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import vc.rux.pokefork.anvil.AnvilNode
import vc.rux.pokefork.anvil.AnvilNodeConfig
import vc.rux.pokefork.defaultDockerClient

class AnvilNodeTest {
    @ValueSource(strings = ["local", "fork"])
    @ParameterizedTest(name = "{0} mode: container must be shut down after stop called")
    fun `container must be shut down after stop called`(nodeMode: String) {
        // given
        val config = when(nodeMode) {
            "fork" -> AnvilNodeConfig.fork("https://rpc.ankr.com/eth", 31337)
            "local" -> AnvilNodeConfig.local()
            else -> throw IllegalArgumentException("Unsupported new nodeMode: $nodeMode, please update the test")
        }
        val connection = AnvilNode.start(config)

        // given precondition: given container is running
        var container = defaultDockerClient.listContainersCmd().exec()
            .singleOrNull { it.id == connection.containerId }
        assertThat(container)
            .isNotNull()
            .transform { it.state }
            .isEqualTo("running")


        // when
        connection.stop()

        // then
        container = defaultDockerClient.listContainersCmd().exec()
            .singleOrNull { it.id == connection.containerId }
        assertThat(container).isNull()
    }
}