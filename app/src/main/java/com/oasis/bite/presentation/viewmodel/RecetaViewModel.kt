package com.oasis.bite.presentation.viewmodel

import android.R
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oasis.bite.data.RetrofitInstance
import com.oasis.bite.data.model.CommentRequest
import com.oasis.bite.data.model.FavParams
import com.oasis.bite.data.model.PasoRecetaRequest
import com.oasis.bite.data.model.RecetaRequest
import com.oasis.bite.data.model.RecetaSearchParams
import com.oasis.bite.data.repository.RecetaRepository
import com.oasis.bite.domain.models.Receta
import com.oasis.bite.domain.models.Ingrediente
import com.oasis.bite.domain.models.MediaItem
import com.oasis.bite.domain.models.PasoReceta
import com.oasis.bite.domain.models.RecetaStatus
import kotlinx.coroutines.launch

class RecetaViewModel(private val repository: RecetaRepository) : ViewModel() {

    private val apiService = RetrofitInstance.apiService

    private val _receta = MutableLiveData<Receta?>()
    val receta: MutableLiveData<Receta?> get() = _receta

    private val _ingredientes = MutableLiveData<List<Ingrediente>>()
    val ingredientes: LiveData<List<Ingrediente>> get() = _ingredientes

    private val _pasos = MutableLiveData<List<PasoReceta>>()
    val pasos: LiveData<List<PasoReceta>> get() = _pasos
    val favoritoLiveData = MutableLiveData<List<Receta>?>()

    fun cargarReceta(id: String) {
        viewModelScope.launch {
            try {
                val recetaCargada = repository.getRecetaPorId(id)
                _receta.value = recetaCargada
                _ingredientes.value = recetaCargada?.ingredientes ?: emptyList()
                _pasos.value = recetaCargada?.pasos ?: emptyList()
            } catch (e: Exception) {
                Log.e("RecetaViewModel", "Error al cargar receta", e)
            }
        }
    }

    fun getRecetasFavoritos(email: String, onResult: (List<Receta>) -> Unit) {
        viewModelScope.launch {
            val favoritos = repository.getRecetasFavoritos(email)
            favoritoLiveData.postValue(favoritos ?:emptyList())
        }
    }

    fun eliminarRecetaFavorito(email: String, receta: Int){
        viewModelScope.launch{
            var params = FavParams(email = email, recetaId =receta)
            repository.deleteFavorito(params)}
    }

    fun agregarRecetaFavorito(email: String, receta: Int){
        viewModelScope.launch{
            var params = FavParams(email = email, recetaId =receta)
            repository.addFavorito(params)}
    }

    fun agregarComentario(email: String, titulo: String, reseña: String, valoracion: Int, receta: Int): Int{
        var response = 0
        viewModelScope.launch{
            var params = CommentRequest(usuarioEmail = email, recetaId =receta, titulo = titulo, reseña = reseña, valoracion = valoracion)
                response =  repository.addComentario(params)}
        return response
    }

    fun verificarSiRecetaExiste(
        titulo: String,
        usuario: String,
        callback: (Int) -> Unit
    ) {
        viewModelScope.launch {
            val params = RecetaSearchParams(
                userName = usuario,
                limit = 20,
                offset = 0
            )
            val recetas = repository.getRecetasSearch(params)
            Log.d("verificarReceta", titulo)
            val recetaExistente = recetas?.find {
                it.nombre.equals(titulo, ignoreCase = true)
            }
            Log.d("verificarReceta", recetaExistente?.id.toString())
            callback(recetaExistente?.id ?: 0)
        }
    }


    fun agregarReceta(nombre: String, descripcion: String, tiempo: String, porciones: String, dificultad: String, imagen:String, imagenes: List<String>, creadorEmail: String, ingredientes: List<Ingrediente>, pasos : List<PasoRecetaRequest>, categoria: String){
        viewModelScope.launch{
            var params = RecetaRequest(nombre = nombre, descripcion =descripcion, tiempo = tiempo, porciones = porciones, dificultad = dificultad, imagen = imagen, imagenes =imagenes, creadorEmail = creadorEmail, ingredientes = ingredientes, pasos = pasos, estado = "Aprobada", categoriaId = categoria)
             repository.addReceta(params)}

    }

    fun agregarRecetaOffline(nombre: String, descripcion: String, tiempo: String, porciones: String, dificultad: String, imagen:String, imagenes: List<String>, creadorEmail: String, ingredientes: List<Ingrediente>, pasos : List<PasoRecetaRequest>, categoria: String){
        viewModelScope.launch {
            var params = RecetaRequest(nombre = nombre, descripcion =descripcion, tiempo = tiempo, porciones = porciones, dificultad = dificultad, imagen = imagen, imagenes =imagenes, creadorEmail = creadorEmail, ingredientes = ingredientes, pasos = pasos, estado = "Aprobada", categoriaId = categoria)
            repository.addRecetaLocal(params)
        }
    }

    fun subirImagen(context: Context, uri: Uri, callback: (String?) -> Unit) {
        viewModelScope.launch {
            val url = repository.subirImagen(context, uri)
            callback(url)
        }
    }

    fun eliminarReceta(id: String){
        viewModelScope.launch {
            repository.deleteReceta(id = id)
        }
    }

}

