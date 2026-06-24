package mx.edu.utng.cunasegurawear.domain

import kotlinx.coroutines.test.runTest
import mx.edu.utng.cunasegurawear.domain.usecase.TriggerSosUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TriggerSosUseCaseTest {

    @Test
    fun `TriggerSos retorna los contactos notificados cuando BLE ok`() = runTest {
        val repo = FakeAlertRepository()
        repo.sendResult = Result.success(3)
        val useCase = TriggerSosUseCase(repo)
        val result = useCase("Calle Morelos #48")
        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrNull())
    }

    @Test
    fun `TriggerSos retorna error cuando BLE falla`() = runTest {
        val repo = FakeAlertRepository()
        repo.sendResult = Result.failure(Exception("BLE disconnected"))
        val useCase = TriggerSosUseCase(repo)
        val result = useCase("Calle Morelos #48")
        assertTrue(result.isFailure)
    }
}
