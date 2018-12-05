import api.predef.*
import io.luna.game.model.def.ItemDefinition
import io.luna.game.model.item.Item
import io.luna.game.model.mob.Player
import io.luna.game.model.mob.inter.StandardInterface
import io.luna.net.msg.out.WidgetItemsMessageWriter
import io.luna.net.msg.out.WidgetTextMessageWriter
import kotlin.streams.toList

/**
 * A standard interface representing the result of a search.
 */
class SearchResultInterface(val searchToken: String) : StandardInterface(5292) {

    /**
     * A list of empty text widget messages.
     */
    val clearWidgets = mutableListOf(5388, 5389, 5390, 5391, 8132, 8133)
        .map { WidgetTextMessageWriter("", it) }

    override fun onOpen(plr: Player) {

        // Perform search here, filter items based on search token.
        val itemsFound = ItemDefinition.ALL
            .stream()
            .filter { it.id > 0 && !it.isNoted && it.name.toLowerCase().contains(searchToken) }
            .map { Item(it.id, Int.MAX_VALUE) }
            .toList()

        // Display the items, truncating results if necessary.
        fun display(items: List<Item>) =
            plr.queue(WidgetItemsMessageWriter(5382, items))

        val resultCount = itemsFound.size
        val maxResults = plr.bank.capacity()
        if (resultCount > maxResults) {
            // Truncate results.
            plr.sendMessage("Too many results ($resultCount) to be displayed! The search has been truncated.")
            display(itemsFound.dropLast(resultCount - maxResults))
        } else {
            // Display as usual.
            plr.sendMessage("Found $resultCount results for search term '$searchToken'.")
            display(itemsFound)
        }
        clearWidgets.forEach { plr.queue(it) }
        plr.sendText("Search results for '$searchToken' ...", 5383)
    }

    override fun onClose(plr: Player) {
        plr.sendText("The Bank of Runescape", 5383)
        plr.sendText("Withdraw as:", 5388)
        plr.sendText("Item", 5389)
        plr.sendText("Rearrange mode:", 5390)
        plr.sendText("Note", 5391)
        plr.sendText("Insert", 8132)
        plr.sendText("Swap", 8133)
    }
}

/**
 * A command that displays items on the banking interface, for easier item spawning.
 */
cmd("search_item", RIGHTS_DEV) {
    val plr = it.plr
    val search = it.replace(0, '_', ' ')
    if (search.length > 1) {
        plr.openInterface(SearchResultInterface(search))
    } else {
        plr.sendMessage("Search term must be more than 1 character.")
    }
}