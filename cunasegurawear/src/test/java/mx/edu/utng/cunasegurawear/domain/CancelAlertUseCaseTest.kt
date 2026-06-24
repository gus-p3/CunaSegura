package mx.edu.utng.cunasegurawear.domain

import kotlinx.coroutines.test.runTest
import mx.edu.utng.cunasegurawear.domain.usecase.CancelAlertUseCase
import org.junit.Assert.assertTrue
import org.junit.Test

class CancelAlertUseCaseTest {

    @Test
    fun `CancelAlert retorna success cuando BLE ok`() = runTest {
        val useCase = CancelAlertUseCase(FakeAlertRepository())
        assertTrue(useCase().isSuccess)
    }
}
