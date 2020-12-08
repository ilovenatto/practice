import kotlinx.cli.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

private val DATA_PATH_DEFAULT = "school_data.csv"
val INDEX_PATH = "school_index.csv"
val SP = "\\s".toRegex() // any whitespace

// Global variable
var isDebug_global = false


/**
 * ## School CLI
 * Entry point for CLI execution and supports the following commands.
 * - count
 * - search
 * - search-interactive
 */
fun main(args: Array<String>) {
    val log = getLogger("main")
    val parser = ArgParser("school")
    val data by parser.option(
        ArgType.String,
        shortName = "d",
        fullName = "data",
        description = "Full path to the school data file, including name of the file. If not set, looks for data file at $DATA_PATH_DEFAULT"
    ).default(DATA_PATH_DEFAULT)

    class CountCmd : Subcommand("count", "Print counts") {
        override fun execute() {
            try {
                Count().count(data).print()
            } catch (e: Exception) {
                log.error("Count command failed ${e.message}")
            }
        }
    }

    class SearchCmd : Subcommand("search", "Run specified query") {
        val query by parser.option(
            ArgType.String,
            description = "Search query terms",
            fullName = "query",
            shortName = "q"
        )

        override fun execute() {
            try {
                query?.let {
                    val results = Search(data).search(it)
                    if (results.topHits.isEmpty()) println("No results found for query: $query")
                    else results.print(it, true)
                }
            } catch (e: Exception) {
                log.error("Search command failed ${e.message}")
            }
        }

    }

    class SearchInteractiveCmd :
        Subcommand("search-interactive", "Enter search terms interactively (preprocesses only one time)") {
        override fun execute() {
            // Preprocesses data and then handles queries
            val searchHelper = Search(data)

            while (true) {
                print(">>> Enter query (^C to exit): ")
                val cmd = readLine()
                if (cmd?.isNullOrBlank() == true) break
                searchHelper
                    .search(cmd)
                    .print(cmd, false)
            }
        }
    }

    class NukeCmd :
        Subcommand("nuke", "Nukes any existing index and redoes the indexing") {
        override fun execute() {
            if (File(INDEX_PATH).exists() && File(INDEX_PATH).delete()) println("Deleted index file $INDEX_PATH")
            else println("No index file to nuke")
        }
    }
    parser.subcommands(CountCmd(), SearchCmd(), SearchInteractiveCmd(), NukeCmd())
    parser.parse(args)

    // Valdiate data flag value
    try {
        if (data.isNotEmpty()
            && data != DATA_PATH_DEFAULT
            && !(File(data).isFile)
        ) log.warn("Data file path is invalid $data")
    } catch (e: Exception) {
        log.error(e.message)
    }
}


/**
 * Helpers
 * ======
 */
fun getLogger(name: String): Logger {
    if (isDebug_global) System
        .setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "DEBUG")
    return LoggerFactory.getLogger(name)
}

/**
 * Enum of header keys from the data file
 */
enum class Col(val key: String) {
    ID("NCESSCH"),
    NAME("SCHNAM05"),
    CITY("LCITY05"),
    STATE("LSTATE05"),
    LOCALE("MLOCALE"),
}


enum class IndexCol {
    WORD,
    ROWS // space-delimited list of row ids
}

data class Row(
    val name: String,
    val city: String,
    val state: String,
    val rowId: String,
    val hitCount: Int = 0
) {
    fun print(ordinal: Int) {
        println("$ordinal.\t$name\n\t$city, $state ($hitCount)")
    }

    /**
     * @return list of all words, including dupes, found in the name and city
     */
    fun allWords() : List<String> =
        mutableListOf<String>()?.apply {
            addAll(name.split(SP))
            addAll(city.split(SP))
        }.toList()

    override fun toString(): String =
        "$name, $city $state (hits: $hitCount, id: $rowId)"

}

data class Results(
    var searchMills: Long,
    var preprocessingMillis: Long,
    val topHits: MutableList<Row> // could be empty list (not null)
) {
    override fun toString(): String {
        var out = "Found ${topHits.size} results (Search time: $searchMills ms, Preprocessing: $preprocessingMillis ms)"
        topHits.forEach { out = out + "\n\t$it" }
        return out
    }


    fun print(query: String, showIndexTime: Boolean) {
        if (topHits.isEmpty()) {
            println("No results found for query: $query")
            return
        }
        var i = 1
        if (showIndexTime) println("Results for `$query` (search took: ${searchMills} ms / preprocessing: ${preprocessingMillis} ms)\n")
        else println("Results for `$query` (search took: ${searchMills} ms)\n")
        for (match in topHits) {
            match.print(i++)
        }
    }
}

enum class State(val full: String) {
    AL("Alabama"),
    AK("Alaska"),
    AB("Alberta"),
    AZ("Arizona"),
    AR("Arkansas"),
    BC("British Columbia"),
    CA("California"),
    CO("Colorado"),
    CT("Connecticut"),
    DE("Delaware"),
    DC("District Of Columbia"),
    FL("Florida"),
    GA("Georgia"),
    GU("Guam"),
    HI("Hawaii"),
    ID("Idaho"),
    IL("Illinois"),
    IN("Indiana"),
    IA("Iowa"),
    KS("Kansas"),
    KY("Kentucky"),
    LA("Louisiana"),
    ME("Maine"),
    MB("Manitoba"),
    MD("Maryland"),
    MA("Massachusetts"),
    MI("Michigan"),
    MN("Minnesota"),
    MS("Mississippi"),
    MO("Missouri"),
    MT("Montana"),
    NE("Nebraska"),
    NV("Nevada"),
    NB("New Brunswick"),
    NH("New Hampshire"),
    NJ("New Jersey"),
    NM("New Mexico"),
    NY("New York"),
    NF("Newfoundland"),
    NC("North Carolina"),
    ND("North Dakota"),
    NT("Northwest Territories"),
    NS("Nova Scotia"),
    NU("Nunavut"),
    OH("Ohio"),
    OK("Oklahoma"),
    ON("Ontario"),
    OR("Oregon"),
    PA("Pennsylvania"),
    PE("Prince Edward Island"),
    PR("Puerto Rico"),
    QC("Quebec"),
    RI("Rhode Island"),
    SK("Saskatchewan"),
    SC("South Carolina"),
    SD("South Dakota"),
    TN("Tennessee"),
    TX("Texas"),
    UT("Utah"),
    VT("Vermont"),
    VI("Virgin Islands"),
    VA("Virginia"),
    WA("Washington"),
    WV("West Virginia"),
    WI("Wisconsin"),
    WY("Wyoming"),
    YT("Yukon Territory")
}