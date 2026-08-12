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
import mx.edu.utng.cunasegura.domain.usecase.LimpiarSesionLocalUseCase

/**
 * Contenedor de Inyección de Dependencias Manual (Service Locator / Module).
 *
 * Centraliza la creación y provisión de instancias de bases de datos, repositorios
 * y casos de uso (UseCases) a lo largo de toda la arquitectura de la aplicación móvil.
 */
object AppModule {

    /**
     * Provee la instancia Singleton de la base de datos local SQLite Room ([AppDatabase]).
     *
     * @param context Contexto de la aplicación Android.
     * @return Instancia única de [AppDatabase].
     */
    fun provideDatabase(context: Context): AppDatabase =
        AppDatabase.getInstance(context)

    /**
     * Provee la implementación del repositorio de usuarios ([UsuarioRepositoryImpl]).
     *
     * @param context Contexto de la aplicación Android.
     * @return Instancia de [UsuarioRepositoryImpl] para operaciones en Firebase y Room.
     */
    fun provideUsuarioRepository(context: Context) =
        UsuarioRepositoryImpl()

    /**
     * Provee el caso de uso para guardar o actualizar la información de un usuario.
     *
     * @param context Contexto de la aplicación Android.
     * @return Instancia de [GuardarUsuarioUseCase].
     */
    fun provideGuardarUsuarioUseCase(context: Context) =
        GuardarUsuarioUseCase(provideUsuarioRepository(context))

    /**
     * Provee el caso de uso para consultar un usuario por su ID local.
     *
     * @param context Contexto de la aplicación Android.
     * @return Instancia de [ObtenerUsuarioUseCase].
     */
    fun provideObtenerUsuarioUseCase(context: Context) =
        ObtenerUsuarioUseCase(provideUsuarioRepository(context))

    /**
     * Provee el caso de uso para consultar el usuario actualmente autenticado (en sesión activa).
     *
     * @param context Contexto de la aplicación Android.
     * @return Instancia de [ObtenerUsuarioActualUseCase].
     */
    fun provideObtenerUsuarioActualUseCase(context: Context) =
       ObtenerUsuarioActualUseCase(provideUsuarioRepository(context))

    /**
     * Provee el caso de uso para purgar la sesión local y caché de usuario al cerrar sesión.
     *
     * @param context Contexto de la aplicación Android.
     * @return Instancia de [LimpiarSesionLocalUseCase].
     */
    fun provideLimpiarSesionLocalUseCase(context: Context) =
        LimpiarSesionLocalUseCase(provideUsuarioRepository(context))

    /**
     * Provee la implementación del repositorio de contactos de emergencia ([ContactoRepositoryImpl]).
     *
     * @param context Contexto de la aplicación Android.
     * @return Instancia de [ContactoRepositoryImpl] conectada a Firebase Realtime Database.
     */
    fun provideContactoRepository(context: Context) =
        ContactoRepositoryImpl()

    /**
     * Provee el caso de uso para agregar o sincronizar un nuevo contacto de confianza.
     *
     * @param context Contexto de la aplicación Android.
     * @return Instancia de [AgregarContactoUseCase].
     */
    fun provideAgregarContactoUseCase(context: Context) =
        AgregarContactoUseCase(provideContactoRepository(context))

    /**
     * Provee el caso de uso para remover un contacto de emergencia por su ID.
     *
     * @param context Contexto de la aplicación Android.
     * @return Instancia de [EliminarContactoUseCase].
     */
    fun provideEliminarContactoUseCase(context: Context) =
        EliminarContactoUseCase(provideContactoRepository(context))

    /**
     * Provee el caso de uso para consultar el flujo reactivo de contactos de emergencia del usuario.
     *
     * @param context Contexto de la aplicación Android.
     * @return Instancia de [ObtenerContactosUseCase].
     */
    fun provideObtenerContactosUseCase(context: Context) =
        ObtenerContactosUseCase(provideContactoRepository(context))

    /**
     * Provee la implementación del repositorio de alertas ciudadanas ([IAlertaRepository]).
     *
     * @param context Contexto de la aplicación Android.
     * @return Instancia de [AlertaRepositoryImpl] con persistencia Room y emisión MQTT/Firebase.
     */
    fun provideAlertaRepository(context: Context): IAlertaRepository =
        AlertaRepositoryImpl(
            provideDatabase(context).alertaDao()
        )

    /**
     * Provee el caso de uso para detonar y registrar una alerta de emergencia SOS.
     *
     * @param context Contexto de la aplicación Android.
     * @return Instancia de [ActivarAlertaUseCase].
     */
    fun provideActivarAlertaUseCase(context: Context) =
        ActivarAlertaUseCase(provideAlertaRepository(context))

    /**
     * Provee el caso de uso para cancelar o marcar como falsa alarma una alerta activa.
     *
     * @param context Contexto de la aplicación Android.
     * @return Instancia de [CancelarAlertaUseCase].
     */
    fun provideCancelarAlertaUseCase(context: Context) =
        CancelarAlertaUseCase(provideAlertaRepository(context))

    /**
     * Provee el caso de uso para validar credenciales y privilegios de administrador.
     *
     * @param context Contexto de la aplicación Android.
     * @return Instancia de [ValidarAdminUseCase].
     */
    fun provideValidarAdminUseCase(context: Context) =
        ValidarAdminUseCase(provideUsuarioRepository(context))

    /**
     * Provee el caso de uso para autenticación general de usuarios (ciudadano o administrador).
     *
     * @param context Contexto de la aplicación Android.
     * @return Instancia de [mx.edu.utng.cunasegura.domain.usecase.ValidarLoginUseCase].
     */
    fun provideValidarLoginUseCase(context: Context) =
        mx.edu.utng.cunasegura.domain.usecase.ValidarLoginUseCase(provideUsuarioRepository(context))

    /**
     * Provee la implementación del repositorio de redes vecinales y configuraciones globales ([mx.edu.utng.cunasegura.domain.repository.INetworkRepository]).
     *
     * @param context Contexto de la aplicación Android.
     * @return Instancia de [mx.edu.utng.cunasegura.data.repository.NetworkRepositoryImpl].
     */
    fun provideNetworkRepository(context: Context): mx.edu.utng.cunasegura.domain.repository.INetworkRepository =
        mx.edu.utng.cunasegura.data.repository.NetworkRepositoryImpl()
}