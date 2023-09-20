# 🍴 PokeFork: EVM Network Forking Library for Kotlin, Scala, and Java Developers

PokeFork is an open-source library project designed to empower
developers working with Ethereum Virtual Machine (EVM) networks.
It offers the ability to fork off Ethereum-like networks and enables
the massive swarm of JVM to test their smart contracts and applications.

## Motivation

While Rust and Typescript have well-established ecosystems for
EVM network development, the JVM ecosystem has been somewhat lacking.
PokeFork aims to bridge this gap by providing a powerful and
user-friendly toolkit for JVM developers, making it easier for them
to participate in the EVM network space.

## Supported methods

| Local node method | HardHat 👷 |
|-------------------|------------|
| `mine`            | ✅          |
| `setBalance`      | ✅          |
| `forkBlock`       | ✅          |

## Goals

Have a neat and simple interface to the local hardhat or anvil node from your Java/Kotlin/Scala code. As simple as:

```kotlin
  val node = HardhatNode.fork(config)
  val web3 = LocalWeb3jNode.from(node)

  web3.forkBlock(blockNumber)
  web3.mine(42)
  web3.setBalance(MY_WALLET, UNICORN_DOLLARS)
```

Seriously, Java devs deserve it.



- **Productivity**: The primary goal is to enable developers using
  Kotlin, Scala, or Java to be highly productive when interacting with EVM networks.

- **Extensibility**: Initially, PokeFork works seamlessly with
  Hardhat, a popular development environment for Ethereum.
  However, the roadmap includes plans to extend support anvil. Your PRs are welcome!

- **Docker Container**: PokeFork operates within a Docker
  container, making it a hard dependency for the library.
  This containerization ensures a consistent and reproducible
  environment for your EVM network forking needs.

- **Network Diversity**: Obviously, Ethereum is _still_ Mainnet, the old and very important guy. 
  But we can't ignore other chains where so much activities are happening.

## Getting Started

Clone and build the PokeFork project:

```bash
git clone 
cd pokefork
./gradlew build
```

TBD

## Contributing

Feel free to create PR or issues.

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
