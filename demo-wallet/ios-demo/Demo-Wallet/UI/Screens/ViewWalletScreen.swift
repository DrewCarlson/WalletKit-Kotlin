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
    let wallet: Wallet
    @EnvironmentObject private var observable: SystemObservable
    @EnvironmentObject private var router: Router
    
    var body: some View {
        return AnyView(VStack {
            ZStack {
                HStack {
                    Button(action: {
                        router.popCurrentRoute()
                    }) {
                        Text("Back")
                    }
                    Spacer()
                }
                
                Text("\(wallet.name) Transfers")
                    .frame(maxWidth:.infinity)
            }.frame(maxWidth:.infinity)
            .padding(4)

            ScrollView {
                LazyVStack {
                    ForEach(observable.transfers[wallet] ?? [], id: \.self) { transfer in
                        TransferItemView(transfer: transfer)
                    }
                }
            }
        }.padding(4))
    }
}
