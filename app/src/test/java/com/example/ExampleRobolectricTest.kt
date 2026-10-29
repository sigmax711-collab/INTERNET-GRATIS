package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.VpnRepository
import com.example.model.VpnState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("INTERNET GRATIS", appName)
  }

  @Test
  fun `check default categories and servers`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = VpnRepository(context)
    val categories = repository.getCachedCategories()
    assertEquals(2, categories.size)
    assertEquals("Servidores Personal 🩵", categories[0].name)
    assertEquals(2, categories[0].items.size)
    assertEquals("Personal WebSocket (HTTP Custom) 🩵", categories[0].items[0].name)
    assertEquals("Servidores Claro 🔴", categories[1].name)
    assertEquals(2, categories[1].items.size)
    assertEquals("Claro Argentina (Sin Saldo) 🔴", categories[1].items[0].name)
  }

  @Test
  fun `vpn state labels`() {
    assertEquals("CONECTADO", VpnState.CONNECTED.label)
    assertEquals("DESCONECTADO", VpnState.DISCONNECTED.label)
  }
}

