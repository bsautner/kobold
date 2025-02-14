package io.github.bsautner.kobold.util

class Multimap<K, V> {
    private val map: MutableMap<K, MutableList<V>> = mutableMapOf()

    fun put(key: K, value: V) {
        map.getOrPut(key) { mutableListOf() }.add(value)
    }

    fun get(key: K): List<V>? {
        return map[key]
    }

    fun getAll(): Map<K, List<V>> {
        return map
    }

    fun remove(key: K, value: V): Boolean {
        return map[key]?.remove(value) ?: false
    }

    fun removeAll(key: K): List<V>? {
        return map.remove(key)
    }
}