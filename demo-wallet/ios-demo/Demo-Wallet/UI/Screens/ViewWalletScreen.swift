//
//  ViewWalletScreen.swift
//  Demo-Wallet
//
//  Created by Andrew Carlson on 7/5/21.
//

import Foundation
import CoreImage
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
    }
    
    var body: some View {
        let scheme: AddressScheme
        if (wallet.manager.network.supportsAddressScheme(addressScheme: AddressScheme.btclegacy)) {
            scheme = AddressScheme.btclegacy
        } else {
            scheme = AddressScheme.native
        }
        let address = wallet.getTargetForScheme(scheme: scheme).description()
        
        return AnyView(ZStack {
            VStack {
                ZStack {
                    HStack {
                        Button(action: { router.popCurrentRoute() }) { Text("Back") }
                        Spacer()
                    }
                    
                    Text(wallet.name)
                        .fontWeight(.bold)
                        .frame(maxWidth:.infinity)
                }.frame(maxWidth:.infinity)
                .padding(4)
                
                HStack {
                    Text("Manager State:")
                    Spacer()
                    Text(manager.state.description)
                }.padding(4)
                
                HStack {
                    Text("Height:")
                    Spacer()
                    Text(wallet.manager.network.height.description)
                }.padding(4)
                
                HStack {
                    Text("Balance:")
                    Spacer()
                    Text(wallet.balance.asString(unit: wallet.unit) ?? "")
                }.padding(4)

                
                Text("Transfers")
                ScrollView {
                    LazyVStack(spacing:4) {
                        ForEach(observable.transfers[wallet]?.reversed() ?? [], id: \.self) { transfer in
                            TransferItemView(transfer: transfer)
                                .padding(4)
                        }
                    }
                }
                
                HStack {
                    Spacer()
                    Button(action: { showReceive = true }) { Text("Receive") }
                    Spacer()
                    Button(action: {}) { Text("Send") }
                    Spacer()
                }.padding(4)
            }.frame(maxHeight:.infinity)
            
            if (showReceive) {
                VStack(spacing:0) {
                    Color.black
                        .opacity(0.7)
                        .onTapGesture { showReceive = false }
                        .frame(maxHeight:.infinity)
                    VStack(spacing:0) {
                        HStack {
                            Spacer()
                            Button(action: { showReceive = false }) {
                                Text("Close")
                            }
                        }.padding(4)
                        Text(address)
                            .frame(maxWidth:.infinity)
                            .padding(4)
                        Image(uiImage: UIImage(data: getQRCodeData(text: address))!)
                            .resizable()
                            .frame(width: 200, height: 200)
                            .padding(4)
                    }.frame(alignment:.bottom)
                    .background(Color.white)
                    .onTapGesture {
                        UIPasteboard.general.string = address
                    }
                }
            }
        }).onReceive(self.observable.$wallets.map { wallets in
            wallets.first { $0 == wallet } ?? wallet
        }, perform: { wallet in
            self.wallet = wallet
        }).onReceive(self.observable.$managers.map { managers in
            managers.first { $0 == manager } ?? manager
        }, perform: { manager in
            self.manager = manager
        })
    }

    func getQRCodeData(text: String) -> Data {
        let filter = CIFilter(name: "CIQRCodeGenerator")!
        let data = text.data(using: .ascii, allowLossyConversion: false)
        filter.setValue(data, forKey: "inputMessage")
        let ciimage = filter.outputImage!
        let transform = CGAffineTransform(scaleX: 10, y: 10)
        let scaledCIImage = ciimage.transformed(by: transform)
        let uiimage = UIImage(ciImage: scaledCIImage)
        return uiimage.pngData()!
    }
}
