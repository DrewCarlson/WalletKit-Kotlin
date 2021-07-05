//
//  Router.swift
//  Demo-Wallet
//
//  Created by Andrew Carlson on 7/5/21.
//

import Foundation
import Combine
import DemoWalletKotlin
import SwiftUI

class Router: ObservableObject {
    @Published private(set) var stack: [Route] = [Route.WalletList()]

    private let routeSubject = PassthroughSubject<StackEvent, Never>()
    private var cancellable: AnyCancellable?
     
    init() {
        cancellable = self.routeSubject
            .receive(on: DispatchQueue.main)
            .sink(receiveValue: { [unowned self] in
                switch ($0) {
                case .push(route: let route):
                    stack.append(route)
                case .replaceTop(route: let route):
                    stack.removeLast()
                    stack.append(route)
                case .replaceStack(routes: let routes):
                    stack = routes
                case .popCurrent:
                    stack.removeLast()
                }
            })
    }
    
    func pushRoute(_ route: Route) {
        routeSubject.send(StackEvent.push(route: route))
    }
    
    func replaceTop(_ route: Route) {
        routeSubject.send(StackEvent.push(route: route))
    }
    
    func replaceStack(_ routes: [Route]) {
        routeSubject.send(StackEvent.replaceStack(routes: routes))
    }
    
    func popCurrentRoute() {
        routeSubject.send(StackEvent.popCurrent)
    }
}

private enum StackEvent {
    case push(route: Route)
    case replaceTop(route: Route)
    case replaceStack(routes: [Route])
    case popCurrent
}
