package drewcarlson.walletkit


public expect class TransferAttribute {

    public val key: String
    public val isRequired: Boolean
    public var value: String?
}
