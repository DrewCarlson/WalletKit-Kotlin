import SwiftUI
import Foundation
import DemoWalletKotlin

struct ContentView: View {
    let observable = SystemObservable()
    @StateObject var router = Router()
    
    var body: some View {
        var overlay: Route? = nil
        var top = router.stack.last!
        if !top.singleView {
            overlay = top
            top = router.stack[router.stack.count - 2]
        }

        print("top: \(top.description)")
        print("overlay: \(overlay?.description ?? "<none>")")
        return AnyView(ZStack(alignment:.center) {
            displayRoute(route: top)
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            if (overlay != nil) {
                displayRoute(route: overlay!)
            }
        }.frame(maxWidth: .infinity, maxHeight: .infinity))
    }
    
    @ViewBuilder func displayRoute(route: Route) -> some View {
        switch route {
        case _ as Route.WalletList:
            WalletScreen()
                .environmentObject(observable)
                .environmentObject(router)
        case let route as Route.ViewWallet:
            ViewWalletScreen(wallet: route.wallet)
                .environmentObject(observable)
                .environmentObject(router)
        case let route as Route.ViewAddress:
            ViewAddressScreen(wallet: route.wallet)
                .environmentObject(observable)
                .environmentObject(router)
        default: Text("unknown route")
        }
    }
}

