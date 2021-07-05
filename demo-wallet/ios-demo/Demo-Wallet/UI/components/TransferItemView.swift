//
//  TransferView.swift
//  Demo-Wallet
//
//  Created by Andrew Carlson on 7/5/21.
//

import Foundation
import SwiftUI
import DemoWalletKotlin

struct TransferItemView: View {
    let transfer: Transfer
    
    var body: some View {
        return AnyView(VStack(alignment: HorizontalAlignment.leading) {
            switch (transfer.direction) {
            case TransferDirection.sent:
                Text("To: \(transfer.target?.description() ?? "")")
                    .lineLimit(1)
                Text("-\(transfer.amount)")
                    .foregroundColor(Color.red)
            case TransferDirection.received:
                Text("From: \(transfer.source?.description() ?? "")")
                    .lineLimit(1)
                Text("\(transfer.amount)")
                    .foregroundColor(Color.green)
            case TransferDirection.recovered:
                Text("Recovered: \(transfer.source?.description() ?? "")")
                    .lineLimit(1)
                Text("\(transfer.amount)")
                    .foregroundColor(Color.black)
            default:
                Text("error rendering transfer")
            }
        }.padding(2))
    }
}
