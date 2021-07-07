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
    @State var manager: WalletManager
    
    @EnvironmentObject private var observable: SystemObservable
    
    init(transfer: Transfer) {
        self.transfer = transfer
        self.manager = transfer.wallet.manager
    }
    
    var body: some View {
        let description: String
        let amount: String
        let amountColor: Color
        switch (transfer.direction) {
        case TransferDirection.sent:
            description = "To: \(transfer.target?.description() ?? "")"
            amount = "-\(transfer.amount)"
            amountColor = Color.red
        case TransferDirection.received:
            description = "From: \(transfer.source?.description() ?? "")"
            amount = transfer.amount.description()
            amountColor = Color.green
        case TransferDirection.recovered:
            description = "Recovered: \(transfer.source?.description() ?? "")"
            amount = "\(transfer.amount)"
            amountColor = Color.black
        default:
            description = "error"
            amount = "error"
            amountColor = Color.purple
        }
        
        return AnyView(VStack(alignment: HorizontalAlignment.leading, spacing:0) {
            Text("Hash: \(transfer.txHash?.description() ?? "")")
                .lineLimit(1)
            Text(description)
                .lineLimit(1)
            ZStack {
                HStack {
                    Text(amount)
                        .foregroundColor(amountColor)
                        .lineLimit(1)
                    Spacer()
                }
                
                HStack {
                    Spacer()
                    switch transfer.state {
                    case let state as TransferState.INCLUDED:
                        if (state.confirmation.success) {
                            let confs = transfer.confirmations?.uint64Value ?? 0
                            let confsUntilFinal = manager.network.confirmationsUntilFinal
                            if (confs >= confsUntilFinal) {
                                Text("Confirmed").foregroundColor(.green)
                            } else {
                                Text("Confirming: \(confs)/\(confsUntilFinal)")
                                    .foregroundColor(.blue)
                            }
                        } else if (state.confirmation.error != nil) {
                            Text("Failed: \(state.confirmation.error ?? "")")
                                .foregroundColor(.red)
                        }
                    case _ as TransferState.FAILED:
                        Text("Failed").foregroundColor(.red)
                    case _ as TransferState.DELETED:
                        Text("Deleted").foregroundColor(.red)
                    default:
                        Text("Pending").foregroundColor(.blue)
                    }
                }
            }
            Color.black
                .opacity(0.5)
                .frame(height: 1)
        }).padding(2)
            .onTapGesture {
                UIPasteboard.general.string = transfer.txHash?.description() ?? ""
            }.onReceive(self.observable.$managers.map({ managers in
                managers.first { $0 == manager } ?? manager
            }), perform: { manager in
                self.manager = manager
            })
    }
}
