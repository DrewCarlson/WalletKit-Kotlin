import SwiftUI
import Foundation
import DemoWalletKotlin

struct ContentView: View {
    @ObservedObject var observable: SystemObservable
    @StateObject var router = Router()
    
    init() {
        self.observable = SystemObservable()
    }
    
    var body: some View {
        return AnyView(VStack {
            switch router.stack.last {
            case _ as Route.WalletList:
                WalletScreen()
                    .environmentObject(observable)
                    .environmentObject(router)
            case let route as Route.ViewWallet:
                ViewWalletScreen(wallet: route.wallet)
                    .environmentObject(observable)
                    .environmentObject(router)
            default: Text("unknown route")
            }
        })
    }
}

