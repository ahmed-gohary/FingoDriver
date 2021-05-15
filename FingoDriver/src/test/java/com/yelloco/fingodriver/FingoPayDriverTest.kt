package com.yelloco.fingodriver

import com.yelloco.fingodriver.enums.FingoKeys
import org.junit.Test

class FingoPayDriverTest
{
    @Test
    fun myTest(){
        val myVar: String = FingoKeys.FINGO_TEMPLATE_KEY_SEED.value

        val byteArray = myVar.toByteArray()
    }
}