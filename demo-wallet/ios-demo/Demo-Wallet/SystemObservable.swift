//
//  SystemObservable.swift
//  Demo-Wallet
//
//  Created by Andrew Carlson on 7/5/21.
//

import Foundation
import SwiftUI
import DemoWalletKotlin

final class SystemObservable: ObservableObject {
    @Published var transfers: [Wallet:[Transfer]] = [:]
    @Published var wallets: [Wallet] = []
    @Published var managers: [WalletManager] = []
    @Published var syncStates: [Bool] = []
    
    init() {
        class Listener : DefaultSystemListener {
            let observable: SystemObservable
            init(observable: SystemObservable) {
                self.observable = observable
            }
            
            override func handleManagerEvent(system: System, manager: WalletManager, event: WalletManagerEvent) {
                DispatchQueue.main.async {
                    self.observable.managers = system.walletManagers
                    
                    if (event is WalletManagerEvent.WalletAdded || event is WalletManagerEvent.WalletChanged || event is WalletManagerEvent.WalletDeleted) {
                        self.observable.wallets = system.wallets
                    }
                    
                    if (event is WalletManagerEvent.SyncStarted || event is WalletManagerEvent.SyncStopped) {
                        self.observable.syncStates = system.wallets.map { wallet in
                            wallet.manager.state is WalletManagerState.SYNCING
                        }
                    }
                }
            }
            
            override func handleTransferEvent(system: System, manager: WalletManager, wallet: Wallet, transfer: Transfer, event: TransferEvent) {
                DispatchQueue.main.async {
                    self.observable.transfers[wallet] = wallet.transfers
                }
            }
        }
        DemoApplication.init().start(listener: Listener(observable: self))
    }
}
