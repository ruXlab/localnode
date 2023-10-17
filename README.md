# 🍴 PokeFork: Seamless integration of Hardhat and Foundry Anvil with your Java/Kotlin app

![build status](https://github.com/ruXlab/pokefork/actions/workflows/tests.yml/badge.svg)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)



PokeFork is an open-source library project designed to empower
developers working with Ethereum Virtual Machine (EVM) based networks.
It allows to run a local development or forked node of the Ethereum-like network 
using **Hardhat or Foundry Anvil** and interact with it from your Java/Kotlin/Scala code.

## Motivation

While Rust and Typescript have well-established ecosystems for
EVM network development, the JVM ecosystem has been somewhat lacking.
PokeFork aims to bridge this gap by providing a powerful and
user-friendly toolkit for JVM developers, making it easier for them
to participate in the EVM network space.

## Supported methods

|           Local node method | HardHat 👷 | Foundry Anvil ⚒️ |
|----------------------------:|:----------:|:----------------:|
|                      `mine` |     ✅      |        ✅         |
|                `setBalance` |     ✅      |        ✅         |
|                 `forkBlock` |     ✅      |        ✅         |
| `setNextBlockBaseFeePerGas` |     ✅      |        ✅         |
|              `setStorageAt` |     ✅      |        ✅         |
|        `impersonateAccount` |     ✅      |        ✅         |
|  `stopImpersonatingAccount` |     ✅      |        ✅         |

## Goals

Have a neat and simple interface to the local hardhat or anvil node from your Java/Kotlin/Scala code. As simple as:

```kotlin
  // start local node in fork mode
  val node = HardhatNode.fork(config)
  val node = AnvilNode.fork(config)

  // initialise local node RPC client
  val web3 = LocalWeb3jNode.from(node)

  web3.forkBlock(blockNumber)
  web3.mine(42)
  web3.setBalance(MY_WALLET, UNICORN_DOLLARS)

  web3.impersonateAccount(HONEYPOT)
  . . .
  web3.stopImpersonatingAccount(HONEYPOT)
```

Seriously, Java devs deserve it.



- **Productivity**: The primary goal is to enable developers using
  Kotlin, Scala, or Java to be highly productive when interacting with EVM networks, 
  testing their smart contracts, interacting with external protocols or collecting data.

- **Extensibility**: PokeFork works seamlessly with
  Hardhat node and Foundry's Anvil, both are popular development local nodes for the Ethereum developers.

- **Docker Container**: PokeFork runs local node **inside** docker container. This removes headache of installing
  and configuring local node on your machine, greatly improving portability. It also allows concurrent run
  of multiple nodes without ports conflict.

- **Network Diversity**: Ethereum is _still_ the Mainnet, the old and very important guy. 
  It would be naive to say that in 2023 it's the only blockchain that matters.

## Getting Started

Clone and build the PokeFork project:

```bash
git clone 
cd pokefork
./gradlew test build
```

TBD

## Contributing

Feel free to create PR or issues.

Please kindly provide a end-to-end test for the feature you are adding.

## Disclaimer

**Note**: The authors of PokeFork take no responsibility for any
consequences or issues arising from the use of this library.
Please use it responsibly and consider the risks associated with
EVM network interactions.

Seriously, it's clear that number of bad actors in blockchain
space is crazy. Trust no one.

## Punning Around

We couldn't resist the opportunity for some wordplay with "PokeFork."
Just like a fork in the road, we're here to help you choose the
path that leads to success in EVM network development.

Happy forking!
