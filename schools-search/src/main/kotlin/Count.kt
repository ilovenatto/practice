import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import kotlin.system.measureTimeMillis

data class Summary (
    var totalSchools:Int = 0,
    var byState: MutableMap<String,Int> = mutableMapOf(),
    var byLocale: MutableMap<String,Int> = mutableMapOf(),
    var byCity : MutableMap<String,Int> = mutableMapOf()) {

    val cityMostSchools : Pair<String, Int>
        get() = byCity.toList().maxByOrNull { it.second }!!

    val totalCityWithSchools : Int
        get() = byCity.toList().filter { it.second>0 }.count()

    fun print() {
        println("Total schools: $totalSchools")
        println("\nSchools by state:")
        byState.toList().sortedBy { it.first }.forEach {
            println("\t${it.first}: \t${it.second}")
        }
        println("\nSchools by Metro-centric locale:")
        byLocale.toList().sortedBy { it.first }.forEach {
            println("\t${it.first}: \t${it.second}")
        }

        println("\nCity with most schools: ${cityMostSchools.first} (${cityMostSchools.second} schools)")
        println("Unique cities with at least one school: $totalCityWithSchools\n\n")
    }
}

class Count {
    val log = getLogger("count")

    /**
     * Count by iterating through rows and building up map of City->Count.
     *
     * Simplest way is still pretty efficient:
     *  Computationally, O(n) to iterate, and O(1) to fetch from and update a map.
     *  In memory, O(1..ish) for the size of the city, locale maps.
     */
    fun count(dataPath: String) : Summary {
        val s = Summary()
        measureTimeMillis {
            csvReader().open(dataPath) {
                readAllWithHeaderAsSequence().forEach { row ->
                    // Map city
                    row[Col.CITY.key]?.let { city ->
                        s.byCity[city] = s.byCity.getOrPut(city) { 0 } + 1
                    }

                    // Map locale
                    row[Col.LOCALE.key]?.let { locale ->
                        s.byLocale[locale] = s.byLocale.getOrPut(locale) { 0 } + 1
                    }

                    // Map state
                    row[Col.STATE.key]?.let { state ->
                        s.byState[state] = s.byState.getOrPut(state) { 0 } + 1
                    }
                }
            }
            csvReader().open(dataPath) {
                s.totalSchools = readAllWithHeaderAsSequence().count()
            }
        }.let{log.debug("Count took $it ms")}

        return s
    }
}