package com.bastian.macrobast.utils

object CodeDetector {
    enum class TipoCodigo { STANDARD_TARKOV, PROMO_TARKOV, PROMO_ARENA, NINGUNO }
    data class ResultadoDeteccion(val tipo: TipoCodigo, val codigo: String)

    // Diccionario extraído del historial de Battlestate Games (2024-2026)
    private val codigosHistoricos = listOf(
        "SUDAKTUDAK", "OMG150TARCOINS", "JUST150TARCOINS", "FREETARCOINS", "PROMOCODE", "WIPE",
        "INVEN", "PVEPRESTIGE", "YYSTV", "POLYGON", "AUTOMATON", "LVL1SCAV", "BACKTORAID",
        "SCAVYBOI", "SBEUKOMAR", "GOONSQUAD", "LIGHTSOUT", "PAY2WIN", "ELVISVISTA", "LVL80BOSS",
        "POWER", "ROGUESET", "BOATTRIP", "007ATHOME", "GAMESPARK", "3DMGAME", "PCGAMESN",
        "ITSADRILL", "INTELPLEASE", "SOMESTUFF", "PAWNSHOP", "MRPROPER", "BANHAMMER",
        "ONEMILLIONPLUS", "OEFT7THBDAY", "EAGLEEYE", "POWERMOVE", "SPECIALDEAL", "KILLA",
        "BITCOIN", "GROUNDZERO", "OBDOLBOS", "THICC", "MxgicFPSApr26"
    )

    // Raíces comunes que Nikita usa para crear códigos futuros dinámicos
    private val palabrasClaveTarkov = listOf(
        "TARCOIN", "SCAV", "BOSS", "WIPE", "LOOT", "ROUBLES", "KILLA", "BEAR", "USEC",
        "LABS", "TERRAGROUP", "PROMO", "GIVEAWAY"
    )

    fun analizarTweet(tweet: String): ResultadoDeteccion {
        // Detectar por contexto del tweet si se trata de Tarkov Arena
        val esArena = tweet.contains("Arena", ignoreCase = true) ||
                tweet.contains("Battle Points", ignoreCase = true) ||
                tweet.contains("Mxgic", ignoreCase = true)

        // Acepta entre 3 y 4 guiones, y bloques de entre 4 y 5 letras/números/asteriscos
        val standardPattern = "(?<![A-Za-z0-9*-])(?:[A-Za-z0-9*]{4,5}-){3,4}[A-Za-z0-9*]{4,5}(?![A-Za-z0-9*-])".toRegex()
        val standardMatches = standardPattern.findAll(tweet).toList()
        if (standardMatches.isNotEmpty()) {
            return ResultadoDeteccion(
                if (esArena) TipoCodigo.PROMO_ARENA else TipoCodigo.STANDARD_TARKOV,
                standardMatches.random().value
            )
        }

        // 2. Buscar códigos Promocionales (Extracción de palabras de entre 4 y 30 caracteres)
        val palabrasCandidatas = "\\b[A-Za-z0-9]{4,30}\\b".toRegex().findAll(tweet).map { it.value }.toList()
        var mejorCodigoPromo: String? = null

        // Filtro A: Búsqueda exacta en el historial
        mejorCodigoPromo = palabrasCandidatas.firstOrNull { candidata ->
            codigosHistoricos.any { it.equals(candidata, ignoreCase = true) }
        }

        // Filtro B: Palabras nuevas que contengan terminología de Tarkov (Ej: NUEVO150TARCOINS)
        if (mejorCodigoPromo == null) {
            mejorCodigoPromo = palabrasCandidatas.firstOrNull { candidata ->
                palabrasClaveTarkov.any { clave -> candidata.contains(clave, ignoreCase = true) } && candidata.length > 5
            }
        }

        // Filtro C: Descarte. Busca la palabra más larga en MAYÚSCULAS que parezca un código
        if (mejorCodigoPromo == null) {
            val soloMayusculas = "\\b[A-Z0-9]{6,25}\\b".toRegex().findAll(tweet).map { it.value }.toList()
            mejorCodigoPromo = soloMayusculas.maxByOrNull { it.length }
        }

        if (mejorCodigoPromo != null) {
            return ResultadoDeteccion(
                if (esArena) TipoCodigo.PROMO_ARENA else TipoCodigo.PROMO_TARKOV,
                mejorCodigoPromo
            )
        }

        return ResultadoDeteccion(TipoCodigo.NINGUNO, "")
    }

    fun tienePalabraClave(tweet: String): Boolean {
        val keywordPattern = "\\b(keys?|codes?|giveaway|free|tarcoins?|promo|promocode|activations?)\\b".toRegex(RegexOption.IGNORE_CASE)
        return keywordPattern.containsMatchIn(tweet)
    }
}