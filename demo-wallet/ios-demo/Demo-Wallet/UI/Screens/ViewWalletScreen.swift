//
//  ViewWalletScreen.swift
//  Demo-Wallet
//
//  Created by Andrew Carlson on 7/5/21.
//

import Foundation
import SwiftUI
import DemoWalletKotlin

struct ViewWalletScreen: View {
    @EnvironmentObject private var observable: SystemObservable
    @EnvironmentObject private var router: Router
    
    @State var showReceive = false
    @State var wallet: Wallet
    @State var manager: WalletManager
    
    init(wallet: Wallet) {
        self.wallet = wallet
        self.manager = wallet.manager
        DispatchQueue.global().async {
            _ = ViewAddressScreen.getQRCodeData(text: "")
        }
    }
    
    var body: some View {
        return AnyView(ZStack {
            VStack(spacing:0) {
                ZStack {
                    HStack {
                        Button(action: { router.popCurrentRoute() }) { Text("Back") }
                            .padding(4)
                        Spacer()
                    }
                    
                    Text(wallet.name)
                        .fontWeight(.bold)
                        .frame(maxWidth:.infinity)
                }.padding(4)
                .frame(maxWidth:.infinity)
                
                VStack(spacing:0) {
                    HStack {
                        Text("Manager State:")
                        Spacer()
                        Text(manager.state.description)
                    }
                    
                    HStack {
                        Text("Height:")
                        Spacer()
                        Text(manager.network.height.description)
                    }
                    
                    HStack {
                        Text("Balance:")
                        Spacer()
                        Text(wallet.balance.asString(unit: wallet.unit) ?? "")
                    }
                    
                    HStack {
                        Text("Mode:")
                        Spacer()
                        Text(manager.mode.description())
                    }
                }.padding(.horizontal, 8)

                Text("(\(wallet.transfers.count)) Transfers")
                ScrollView {
                    LazyVStack(spacing:4) {
                        ForEach(observable.transfers[wallet]?.reversed() ?? [], id: \.self) { transfer in
                            TransferItemView(transfer: transfer)
                                .padding(.horizontal, 8)
                        }
                    }
                }
                
                HStack {
                    Spacer()
                    Button(action: { router.pushRoute(Route.ViewAddress(wallet: wallet)) }) { Text("Receive") }
                    Spacer()
                    Button(action: {}) { Text("Send") }
                    Spacer()
                }.padding(8)
            }.frame(maxHeight:.infinity)
        }).frame(maxWidth: .infinity, maxHeight:.infinity)
        .onReceive(self.observable.$wallets.map { wallets in
            wallets.first { $0 == wallet } ?? wallet
        }, perform: { wallet in
            self.wallet = wallet
        }).onReceive(self.observable.$managers.map { managers in
            managers.first { $0 == manager } ?? manager
        }, perform: { manager in
            self.manager = manager
        })
    }
}
