package web3j

import vc.rux.localnode.NodeMode
import vc.rux.localnode.hardhat.HardHatNodeConfig
import vc.rux.localnode.hardhat.HardhatNode

/**
 * Since junit5 doesn't support tests with multiple paramterised parameters in constructor and
 * inside function body, we have to create a separate class for each test of the implementation.
 *
 * While this approach works, it reduces readability, so feel free to make PR to address this issue.
 */
class HardhatForkWeb3Test : CommonForkNodeWeb3JTest() {
    override fun forkImplementationFactory(mode: NodeMode): HardhatNode =
        HardhatNode.start(
            HardHatNodeConfig(nodeMode = mode)
        )
}