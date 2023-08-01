package com.programmersbox.dynamictest

import com.programmersbox.dynamiccodeloading.RandomNumbers
import kotlin.random.Random

object RandomNumber : RandomNumbers() {
    override fun getNumber(): Int = Random.nextInt(1, 1000)

    override fun toString(): String {
        return super.toString() + " please work?"
    }
}