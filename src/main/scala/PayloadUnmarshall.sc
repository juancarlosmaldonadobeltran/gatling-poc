import java.io.FileInputStream

import com.nordstrom.inventory.model.InventoryViewProto.InventoryViews

val filename = "/home/juan/workspace/gatling-poc/src/main/resources/inventory-payload"
val stream = new FileInputStream(filename)
val inventoryViews: InventoryViews = InventoryViews.parseFrom(stream)
println(inventoryViews.getInventoryViewCount)
val inventoryView = inventoryViews.getInventoryViewList.get(0)
println(inventoryView.getRmsSkuId)
println(inventoryView.getLocationId)
println(inventoryView.getImmediatelySellableQty)
println(inventoryView.getStockOnHandQty)
