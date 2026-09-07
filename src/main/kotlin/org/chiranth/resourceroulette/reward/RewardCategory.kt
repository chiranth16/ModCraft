package org.chiranth.resourceroulette.reward

enum class RewardCategory(val weight: Int) {
    LOSS(15),     // "you lost this bet" — nothing given back
    SMALL(45),    // minor consolation items
    DECENT(30),   // solid, worthwhile items
    JACKPOT(10)   // rare, powerful items
}