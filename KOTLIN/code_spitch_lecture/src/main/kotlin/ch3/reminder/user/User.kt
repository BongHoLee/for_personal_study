package ch3.reminder.user

import ch3.reminder.item.Item
import kotlinx.datetime.Instant

class User {

    private val items = hashSetOf<Item>()

    fun addItem(vararg item: Item) {
        items += item
    }

    fun send(now: Instant) {
        items.forEach {it.send(now)}
    }

    fun search(title: String? = null, content: String? = null): Collection<Item> {
        var target: Collection<Item> = items
        if (title != null) target = target.filter { title in it.title }
        if (content != null) target = target.filter { content in it.content }
        return target
    }

}
