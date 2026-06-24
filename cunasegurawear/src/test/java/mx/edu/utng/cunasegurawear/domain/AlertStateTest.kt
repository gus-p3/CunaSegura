package mx.edu.utng.cunasegurawear.domain

import mx.edu.utng.cunasegurawear.domain.model.AlertPhase
import mx.edu.utng.cunasegurawear.domain.model.AlertState
import org.junit.Assert.assertEquals
import org.junit.Test

class AlertStateTest {

    @Test
    fun `estado inicial es IDLE`() {
        val state = AlertState()
        assertEquals(AlertPhase.IDLE, state.phase)
    }

    @Test
    fun `countdown inicial es 5 segundos`() {
        assertEquals(5, AlertState().countdownSeconds)
    }

    @Test
    fun `copy no muta el original`() {
        val original = AlertState()
        val copia = original.copy(phase = AlertPhase.ACTIVE)
        assertEquals(AlertPhase.IDLE, original.phase)
        assertEquals(AlertPhase.ACTIVE, copia.phase)
    }
}
