package ch4

import kotlin.reflect.KProperty

class DelegateSample {
}

interface Base {
    fun print()
    fun test()
}

class BaseImpl: Base {
    override fun print() {
        println("this is print func in BaseImpl class")
    }

    override fun test() {
        println("this is test func in BaseImpl class")
    }
}

class BaseImpl2: Base {
    override fun print() {
        println("this is print func in BaseImpl2 class")
    }

    override fun test() {
        println("this is test func in BaseImpl2 class")
    }

}

class DelegateSP(val del: Base): Base by del {

    override fun test() {
        println("this is test func in DelegateSP class")
    }
}

class DelegateProperty<T> {
    operator fun getValue(thisRef: T, property: KProperty<*>): T {

    }

}

class PropertySP {
    val prop by DelegateProperty()
}

class PropertySP2 {
    val prop by DelegateProperty()
}

fun main() {
    val delegateSP = DelegateSP(BaseImpl())
    delegateSP.print()
    delegateSP.test()

    val delegateSP2 = DelegateSP(BaseImpl2())
    delegateSP2.print()
    delegateSP2.test()
}

