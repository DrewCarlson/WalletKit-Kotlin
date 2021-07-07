//
//  ViewAddressScreen.swift
//  Demo-Wallet
//
//  Created by Andrew Carlson on 7/6/21.
//

import SwiftUI
import Foundation
import CoreImage
import DemoWalletKotlin

struct ViewAddressScreen: View {
    @EnvironmentObject private var observable: SystemObservable
    @EnvironmentObject private var router: Router

    @State var wallet: Wallet
    
    init(wallet: Wallet) {
        self.wallet = wallet
    }
    
    var body: some View {
        let scheme: AddressScheme
        if (wallet.manager.network.supportsAddressScheme(addressScheme: AddressScheme.btclegacy)) {
            scheme = AddressScheme.btclegacy
        } else {
            scheme = AddressScheme.native
        }
        let address = wallet.getTargetForScheme(scheme: scheme).description()
        
        return AnyView(VStack(spacing:0) {
            Color.black
                .opacity(0.7)
                .onTapGesture { router.popCurrentRoute() }
                .frame(maxHeight:.infinity)
            VStack(spacing:0) {
                HStack {
                    Spacer()
                    Button(action: { router.popCurrentRoute() }) { Text("Close") }
                }.padding(4)
                Text(address)
                    .frame(maxWidth:.infinity)
                    .padding(4)
                Image(uiImage: UIImage(data: ViewAddressScreen.getQRCodeData(text: address))!)
                    .resizable()
                    .frame(width: 200, height: 200)
                    .padding(4)
            }.frame(alignment:.bottom)
            .background(Color.white)
            .onTapGesture {
                UIPasteboard.general.string = address
            }
        }).onReceive(self.observable.$wallets.map { wallets in
            wallets.first { $0 == wallet } ?? wallet
        }, perform: { wallet in
            self.wallet = wallet
        })
    }

    static func getQRCodeData(text: String) -> Data {
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
