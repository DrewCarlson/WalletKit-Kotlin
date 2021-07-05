//
//  WalletItemView.swift
//  Demo-Wallet
//
//  Created by Andrew Carlson on 7/5/21.
//

import Foundation
import SwiftUI
import DemoWalletKotlin

struct WalletItemView: View {
    let wallet: Wallet
    let syncing: Bool
    
    var body: some View {
        return AnyView(VStack(alignment: .leading) {
            HStack(alignment: .top) {
                Text(wallet.name)
                Text("(\(wallet.currency.code.uppercased()))")
            }
            HStack(alignment: .top) {
                Text(wallet.balance.asString(unit: wallet.unit) ?? "")
                ActivityIndicator(shouldAnimate: Binding.constant(syncing))
            }
        }
        .frame(maxWidth:.infinity, alignment: .leading)
        .contentShape(Rectangle()))
    }
}
