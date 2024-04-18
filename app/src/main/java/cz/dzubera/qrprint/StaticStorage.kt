package cz.dzubera.qrprint


object StaticStorage {

    var pressLimit: Int = 0
    var autoConnectTry = false

    var scannedTextProductNumber = "<žádné>"
    var scannedTextProductionCommandNumber = "<žádné>"
    var productCount = 0
    var productCountTotal = 0
    var productTimer = 10
    var printedCount = 0

    var datamaxIp = "0.0.0.0"
    var datamaxPort = 0

    var qrCodeSize = 250
    var qrCodeTextSize = qrCodeSize / 6f

    var xCalibration = 143
    var yCalibration = 0


}