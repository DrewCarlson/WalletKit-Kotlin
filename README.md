# WalletKit Kotlin

![Bintray](https://img.shields.io/bintray/v/drewcarlson/WalletKit-Kotlin/WalletKit-Kotlin?color=blue)
![](https://img.shields.io/maven-metadata/v?label=artifactory&logoColor=lightgrey&metadataUrl=https%3A%2F%2Foss.jfrog.org%2Fartifactory%2Foss-snapshot-local%2Fdrewcarlson%2Fwalletkit%2Fwalletkit%2Fmaven-metadata.xml&color=lightgrey)
![](https://github.com/DrewCarlson/WalletKit-Kotlin/workflows/Jvm/badge.svg)
![](https://github.com/DrewCarlson/WalletKit-Kotlin/workflows/Native/badge.svg)

[WalletKit](https://github.com/blockset-corp/walletkit) SDK implementation providing  cryptocurrency wallet features.

## About

WalletKit-Kotlin is written in common Kotlin to support multiplatform development.

## Usage

Kotlin
```kotlin
// handleManagerEvent, handleNetworkEvent, handleTransferEvent, handleWalletEvent
val listener = object : SystemListener {
    override fun handleSystemEvent(system: System, event: SystemEvent) {
        println("System Event: $event")
    }
}

val uids = "..."
val timestamp = 0
val account = Account.createFromPhrase(PHRASE_BYTES, timestamp, uids)

val isMainnet = false
val storagePath = "/path/to/data"

val system = System.create(
    listener,
    checkNotNull(account),
    isMainnet,
    storagePath,
    BdbToken.createForTest(bdbToken)
)

system.configure(emptyList())
```
Swift
```swift
``` 

## Download

![](https://img.shields.io/static/v1?label=&message=Platforms&color=grey)
![](https://img.shields.io/static/v1?label=&message=Jvm&color=blue)
![](https://img.shields.io/static/v1?label=&message=Android&color=blue)
![](https://img.shields.io/static/v1?label=&message=iOS&color=blue)
![](https://img.shields.io/static/v1?label=&message=macOS&color=blue)
![](https://img.shields.io/static/v1?label=&message=Linux&color=red)
![](https://img.shields.io/static/v1?label=&message=Windows&color=red)
![](https://img.shields.io/static/v1?label=&message=tvOS&color=red)
![](https://img.shields.io/static/v1?label=&message=watchOS&color=red)
![](https://img.shields.io/static/v1?label=&message=Js&color=red)

Artifacts are available on [Bintray](https://bintray.com/drewcarlson/Blockset-Kotlin).

```kotlin
repositories {
  jcenter()
  // Or snapshots
  maven { setUrl("http://oss.jfrog.org/artifactory/oss-snapshot-local") }
}

dependencies {
  implementation("drewcarlson.walletkit:walletkit:$blockset_version")
}
```

## License
```
Copyright (c) 2020 Andrew Carlson

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
