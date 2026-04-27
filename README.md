# Seminar PCD Actor Pekko Code

Hands-on Scala 3 examples for learning actor-based concurrency with Apache Pekko Typed.

The repository is organized as a seminar playground:

- `es00`: focused actor fundamentals and communication patterns
- `es01`: a richer end-to-end coffee shop workflow modeled with multiple collaborating actors

## Tech Stack

- Scala `3.7.4`
- Apache Pekko Typed `1.5.0`
- sbt `1.11.x`
- ScalaTest + Pekko typed testkit

## Project Structure

- `build.sbt`: root build and shared dependencies
- `basics/src/main/scala/io/github/nicolasfara/es00`: actor basics and small runnable demos
- `basics/src/main/scala/io/github/nicolasfara/es01`: coffee shop domain model and actor workflow
- `basics/src/test/scala`: tests for both exercises

## Prerequisites

Install:

1. JDK 21
2. sbt

Verify:

```bash
java -version
sbt --version
```

## Run Examples

Run commands from the repository root.

### es00: Actor Fundamentals

```bash
sbt 'basics / runMain io.github.nicolasfara.es00.run'
sbt 'basics / runMain io.github.nicolasfara.es00.runLifecycle'
sbt 'basics / runMain io.github.nicolasfara.es00.runObjectOrientedActor'
sbt 'basics / runMain io.github.nicolasfara.es00.runFunctionalActor'
sbt 'basics / runMain io.github.nicolasfara.es00.runRequesterResponser'
sbt 'basics / runMain io.github.nicolasfara.es00.runParentFailureNotification'
sbt 'basics / runMain io.github.nicolasfara.es00.runFailure'
sbt 'basics / runMain io.github.nicolasfara.es00.runAskOperator'
sbt 'basics / runMain io.github.nicolasfara.es00.runStashing'
```

### es01: Coffee Shop Workflow

```bash
sbt 'basics / runMain io.github.nicolasfara.es01.CoffeeShopApp.app'
```

The coffee shop demo showcases:

- order placement and validation
- inventory reservation
- asynchronous preparation timing
- state transitions (`PendingInventory -> InPreparation -> Ready`)
- order status lookup

## Run Tests

Run all tests:

```bash
sbt 'basics / test'
```

Run only coffee shop workflow tests:

```bash
sbt 'basics / testOnly io.github.nicolasfara.es01.CoffeeShopWorkflowSpec'
```
