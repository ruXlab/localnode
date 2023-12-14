# 🍴 LocalNode: get Hardhat and Foundry Anvil integrated in your Java/Kotlin app

[![build status](https://github.com/ruXlab/localnode/actions/workflows/tests.yml/badge.svg)](https://github.com/ruXlab/localnode/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Maven Central](https://img.shields.io/maven-central/v/vc.rux.pokefork/ktx-module.svg)](https://search.maven.org/artifact/vc.rux.localnode/localnode)


LocalNode allows to run a local development or forked node of the Ethereum-like network
using **Hardhat or Foundry Anvil** and interact with it from your Java/Kotlin/Scala code. 
Thanks to docker there won't be a port conflict and you can run multiple nodes at the same time. 

Developer gets full control over the management of the local node via the extended (see the table below) web3j interface.

## Motivation

While Rust and Typescript have well-established ecosystems for
EVM network development, the JVM ecosystem has been somewhat lacking.
PokeFork aims to bridge this gap by providing a powerful and
user-friendly toolkit for JVM developers, making it easier for them
to participate in the EVM network space.

## Supported methods

|               Local node method | HardHat 👷 | Foundry Anvil ⚒️ |
|--------------------------------:|:----------:|:----------------:|
|                          `mine` |     ✅      |        ✅         |
|                    `setBalance` |     ✅      |        ✅         |
|                     `forkBlock` |     ✅      |        ✅         |
|     `setNextBlockBaseFeePerGas` |     ✅      |        ✅         |
|                  `setStorageAt` |     ✅      |        ✅         |
|            `impersonateAccount` |     ✅      |        ✅         |
|      `stopImpersonatingAccount` |     ✅      |        ✅         |
| `chainSnapshot` / `chainRevert` |     ✅      |        ✅         |

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

Add dependency to your project:

```kotlin
implementation("vc.rux.localnodee:web3j:0.1.0")
implementation("org.web3j:core:4.10.3")
```

And off you go! Check [examples](examples) folder to get idea how to use this library.

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

------------------------

Happy forking!
