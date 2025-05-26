package com.oasis.bite.data.model

data class RecetaSearchParams(
    val name: String? = null,
    val userName: String? = null,
    val type: String? = null,
    val includeIngredients: List<String>? = null,
    val excludeIngredients: List<String>? = null,
    val orderBy: String = "newest",
    val direction: String = "asc",
    val limit: Int = 10,
    val offset: Int = 0
) {
    // Convierte las listas de ingredientes a strings separados por coma
    fun getIncludeIngredientsString(): String? = includeIngredients?.joinToString(",")
    fun getExcludeIngredientsString(): String? = excludeIngredients?.joinToString(",")
}
