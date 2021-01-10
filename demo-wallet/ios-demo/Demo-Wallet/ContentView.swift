import SwiftUI
import Foundation
import DemoWalletKotlin


class Listener : WalletListener {
    var cb: (Wallet) -> Void
    init(cb: @escaping (Wallet) -> Void) {
        self.cb = cb
    }
    func handleWalletEvent(system: System, manager: WalletManager, wallet: Wallet, event: WalletEvent) {
        cb(wallet)
    }
}

struct ContentView: View {
    @ObservedObject var dataSource: TransfersObservable
    
    init() {
        self.dataSource = TransfersObservable()
    }
    
    var body: some View {
        let transfers = dataSource.transfers
        
        return AnyView(VStack {
            Text("Demo Wallet")
                .bold()
            
            Text("Transfers")
                .padding(4)
            
            List {
                ForEach(transfers, id: \.self) { transfer in
                    TransferView(transfer: transfer)
                }
            }
        }.padding(4))
    }
}

struct TransferView: View {
    let transfer: Transfer
    init(transfer: Transfer) {
        self.transfer = transfer
    }
    
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
            default:
                Text("")
            }
        })
    }
}

final class TransfersObservable: ObservableObject {
    @Published var transfers: [Transfer] = []
    
    init() {
        DemoApplication.init().start(walletListener: Listener { (wallet: Wallet) in
            DispatchQueue.main.async {
                self.transfers = wallet.transfers
            }
        })
    }
}

