import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

const val NUM_PLAYERS = 5
var VICTORY = 0;
var allCombination = 0L

var winningActions = HashSet<List<Action>>()

class Game(var allCards: MutableList<List<Card>>, var table: Array<Card>, var missions: List<Mission>, var actions: MutableList<Action>) {
    fun canPlay(player: Int, cardIndex: Int, tableIndex: Int): Boolean {
        val card = allCards[player][cardIndex]
        val tableCard = table[tableIndex];
        return tableCard.value == card.value || tableCard.color == card.color
    }

    fun play(player: Int, cardIndex: Int, tableIndex: Int) {
        val playerCards = allCards[player]
        val card = playerCards[cardIndex]
        allCards[player] = playerCards.filter { it !== card}
        table[tableIndex] = card
        actions.add(Move(player, card, tableIndex))
//        actions.add(Move(player, card, table, tableIndex))
        val missionsCompleted = missions.filter { it.won(this) }
        if(missionsCompleted.isNotEmpty()) {
            actions.add(MissionsCompleted(missionsCompleted))
        }
        missions = missions.filter { !missionsCompleted.contains(it) }
    }

    fun finished(): Boolean {
        return allCards.all{it.isEmpty()}
    }

    fun won(): Boolean{
        return missions.isEmpty()
    }

    fun clone(): Game{
        val allCardsClone: MutableList<List<Card>> = allCards.map { it.toMutableList() }.toMutableList()
        return Game(allCardsClone, table.clone(), missions.toMutableList(), ArrayList(actions))
    }
}

fun main(){
    val threeGreen = object: Mission {
        override fun won(game: Game): Boolean {
            return game.table.count { it.color == COLOR.GREEN } == 3
        }

        override fun name(): String {
            return "Three green"
        }
    }
    val onlyRedAndGreen = object: Mission{
        override fun won(game: Game): Boolean {
            return game.table.all {it.color == COLOR.GREEN || it.color == COLOR.RED}
        }

        override fun name(): String {
            return "Only red and green"
        }

    }
    val allDifferent = object: Mission{
        override fun won(game: Game): Boolean {
            val values = HashSet<Int>()
            val colors = HashSet<COLOR>()
            for (card in game.table) {
                values.add(card.value)
                colors.add(card.color)
            }

            return values.size == 4 && colors.size == 4
        }

        override fun name(): String {
            return "All different"
        }
    }
    val lessThan4 =  object: Mission{
        override fun won(game: Game): Boolean {
            return game.table.all {it.value < 4}
        }

        override fun name(): String {
            return "less than 4"
        }
    }

    val missions = listOf(threeGreen, onlyRedAndGreen, allDifferent, lessThan4)
    val clem = listOf(Card(3, COLOR.GREEN), Card(5, COLOR.GREEN), Card(1, COLOR.BLUE), Card(1, COLOR.RED))
    val oli = listOf(Card(7, COLOR.RED), Card(3, COLOR.RED), Card(1, COLOR.YELLOW))
    val mat = listOf(Card(4, COLOR.GREEN), Card(2, COLOR.GREEN), Card(5, COLOR.BLUE))
    val nico = listOf(Card(3, COLOR.BLUE), Card(5, COLOR.RED), Card(5, COLOR.YELLOW))
    val coz = listOf(Card(1, COLOR.GREEN), Card(1, COLOR.GREEN), Card(5, COLOR.BLUE))

    val table = arrayOf(Card(4, COLOR.YELLOW), Card(6, COLOR.YELLOW), Card(2, COLOR.YELLOW), Card(6, COLOR.RED))


    val allCards = listOf(clem, oli, mat, nico, coz)
    val game = Game(allCards.toMutableList(), table, missions, ArrayList())

    backTrack(game, 0)

    println(winningActions.size)
    println(allCombination)
}

interface Action

data class Move(val player: Int, val card: Card,
//                val table: Array<Card>,
                val pos: Int): Action{
    override fun toString(): String {
        val playerName = when(player){
            0 -> "Clem"
            1 -> "Oli"
            2 -> "Mat"
            3-> "Nico"
            4 -> "Coz"
            else -> throw RuntimeException()
        }

//        return """
//            $playerName plays card $card on ${pos + 1}
//            table is : ${
//            Arrays.toString(table)}
//        """.trimIndent()

        return """
            $playerName plays card $card on ${pos + 1}
        """.trimIndent()
    }
}

data class MissionsCompleted(val missions: List<Mission>): Action {
    override fun toString(): String {
        return "Mission completed: ${missions.map { it.name() }.joinToString(", ") }"
    }
}






fun backTrack(game: Game, player: Int): Boolean{
    if(game.won()) {
//        game.actions.forEach {
//            println(it)
//        }
//        return true
//        println("won!")
        VICTORY++
        winningActions.add(game.actions);
    }

    if(game.finished()) {
        allCombination++;
        return false
    }

    for((cardIndex, _) in game.allCards[player].withIndex()){
        for (tableIndex in 0..3){
            if(game.canPlay(player, cardIndex, tableIndex)){
                val clone = game.clone()
                clone.play(player, cardIndex, tableIndex)
                backTrack(clone, (player +1) % NUM_PLAYERS)
            }
        }
    }
    return false
}

data class Card(val value: Int, val color: COLOR) {
    override fun toString(): String {
        return "($value, $color)"
    }
}

enum class COLOR {
    RED, GREEN, BLUE, YELLOW;
}

interface Mission{
    fun won(game: Game): Boolean
    fun name(): String
}
