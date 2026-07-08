package mx.edu.utng.cunasegura.di

import android.content.Context
import mx.edu.utng.cunasegura.data.local.db.AppDatabase
import mx.edu.utng.cunasegura.data.repository.UsuarioRepositoryImpl
import mx.edu.utng.cunasegura.domain.usecase.GuardarUsuarioUseCase
import mx.edu.utng.cunasegura.domain.usecase.ObtenerUsuarioUseCase
import mx.edu.utng.cunasegura.domain.usecase.ObtenerUsuarioActualUseCase
import mx.edu.utng.cunasegura.data.repository.ContactoRepositoryImpl
import mx.edu.utng.cunasegura.domain.usecase.AgregarContactoUseCase
import mx.edu.utng.cunasegura.domain.usecase.EliminarContactoUseCase
import mx.edu.utng.cunasegura.domain.usecase.ObtenerContactosUseCase
import mx.edu.utng.cunasegura.data.repository.AlertaRepositoryImpl
import mx.edu.utng.cunasegura.domain.repository.IAlertaRepository
import mx.edu.utng.cunasegura.domain.usecase.ActivarAlertaUseCase
import mx.edu.utng.cunasegura.domain.usecase.CancelarAlertaUseCase
import mx.edu.utng.cunasegura.domain.usecase.ValidarAdminUseCase

/**
 * Contenedor manual de dependencias.
 * Cada pantalla obtiene sus UseCases a través de este objeto.
 */
object AppModule {

    fun provideDatabase(context: Context): AppDatabase =
        AppDatabase.getInstance(context)

    fun provideUsuarioRepository(context: Context) =
        UsuarioRepositoryImpl(provideDatabase(context).usuarioDao())

    fun provideGuardarUsuarioUseCase(context: Context) =
        GuardarUsuarioUseCase(provideUsuarioRepository(context))

    fun provideObtenerUsuarioUseCase(context: Context) =
        ObtenerUsuarioUseCase(provideUsuarioRepository(context))

    fun provideObtenerUsuarioActualUseCase(context: Context) =
       ObtenerUsuarioActualUseCase(provideUsuarioRepository(context))

    fun provideContactoRepository(context: Context) =
        ContactoRepositoryImpl(
            provideDatabase(context).contactoDao()
        )

    fun provideAgregarContactoUseCase(context: Context) =
        AgregarContactoUseCase(provideContactoRepository(context))

    fun provideEliminarContactoUseCase(context: Context) =
        EliminarContactoUseCase(provideContactoRepository(context))

    fun provideObtenerContactosUseCase(context: Context) =
        ObtenerContactosUseCase(provideContactoRepository(context))

    // Alertas
    fun provideAlertaRepository(context: Context): IAlertaRepository =
        AlertaRepositoryImpl(
            provideDatabase(context).alertaDao()
        )

    fun provideActivarAlertaUseCase(context: Context) =
        ActivarAlertaUseCase(provideAlertaRepository(context))

    fun provideCancelarAlertaUseCase(context: Context) =
        CancelarAlertaUseCase(provideAlertaRepository(context))

    fun provideValidarAdminUseCase(context: Context) =
        ValidarAdminUseCase(provideUsuarioRepository(context))
}