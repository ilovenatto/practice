import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import java.io.File
import java.lang.Integer.min
import kotlin.system.measureTimeMillis

val log = getLogger("indexer")

/**
 * Fills id2Row and commonWords with values from the raw school data in the CSV file
 */
fun fillSchoolData(dataPath: String, id2Rows : MutableMap<String, Row>, commonWords : MutableSet<String>) {
    measureTimeMillis {
        var wordToCount = mutableMapOf<String,Int>()
        csvReader().open(dataPath) {
            readAllWithHeaderAsSequence().forEach { row ->
                // Load school data into in-memory map
                row[Col.ID.key]?.let {
                    val dataRow = Row(
                        row.getOrDefault(Col.NAME.key, ""),
                        row.getOrDefault(Col.CITY.key, ""),
                        row.getOrDefault(Col.STATE.key, ""),
                        row.getOrDefault(Col.ID.key, ""),
                        0
                    )
                    id2Rows[it] = dataRow
                    // Capture the words found in each school name, city, state
                    // Doign it this way to save memory, instead of using convenience Kotlin
                    // collection methods
                    for (word in dataRow.allWords()) {
                        wordToCount.putIfAbsent(word,1)
                        wordToCount.computeIfPresent(word) {_,count->
                            count + 1
                        }
                    }
                }
            }
            val commonSorted = wordToCount.toList().sortedByDescending { it.second }
            commonSorted
                .take(min(NUM_COMMON_WORDS, commonSorted.size))
                .also {
                    commonWords.addAll(it.map {pair->
                        pair.first
                    })}
        }
        log.debug("Loaded ${id2Rows.size} rows from $dataPath")
        log.debug("Found these top common words $commonWords")
    }.let { log.debug("Loading data rows took $it ms") }
}

/**
 * @return Map of word -> list of row ids. This is derived from an index in the file-system that
 * had been previously created.
 */
fun getIndex(dataPath: String): Map<String, List<String>> {
    if (!File(INDEX_PATH).exists()) {
        return writeIndex(dataPath)
    }

    // Next time, use the index found in filesystem
    var word2Rows = mutableMapOf<String, List<String>>()
    measureTimeMillis {
        csvReader().open(INDEX_PATH) {
            readAllWithHeaderAsSequence().forEach { row ->
                row[IndexCol.WORD.name]?.let { word ->
                    row[IndexCol.ROWS.name]?.let { ids ->
                        // Map word -> rowId1, rowId2...rowId_N
                        word2Rows[word] = ids.split(SP).filter { it.isNotEmpty() }
                    }
                }
            }
        }
    }.let { log.debug("Time to read index $it ms") }
    log.debug("Using existing search index of size ${word2Rows.size} entries")

    return word2Rows
}

fun writeIndex(dataPath: String): MutableMap<String, MutableList<String>> {
    // build up map of data words -> row ids
    var word2Rows = mutableMapOf<String, MutableList<String>>()

    measureTimeMillis {
        // Read giant data file
        csvReader().open(dataPath) {
            readAllWithHeaderAsSequence().forEach { row ->
                row[Col.ID.key]?.let { rowId ->
                    val words = mutableSetOf<String>()

                    row[Col.NAME.key]?.split(SP)?.forEach { words.add(it.toUpperCase()) }
                    row[Col.CITY.key]?.split(SP)?.forEach { words.add(it.toUpperCase()) }
                    row[Col.STATE.key]?.let { abbrev -> //eg "CA"
                        words.add(abbrev.toUpperCase())
                        // Add the full name of a state, eg California
                        // so that search token "California" matches to "CA"
                        try {
                            words.add(State.valueOf(abbrev).full.toUpperCase())
                        } catch (e:Exception) {
                            log.debug("Unrecognized state abbreviation $abbrev.")
                        }
                    }

                    // add each word to the mapping
                    words.forEach {
                        word2Rows.getOrDefault(it, mutableListOf()).also { rowIds ->
                            rowIds.add(rowId)
                            word2Rows[it] = rowIds
                        }
                    }
                }
            }
        }
        log.debug("Created index of size ${word2Rows.size}")

        // Sort it by word and then write to file
        csvWriter().open(INDEX_PATH) {
            writeRow(IndexCol.WORD.name, IndexCol.ROWS.name)
            word2Rows.forEach { word, ids ->
                val idsRaw = ids.fold("") { all, cur ->
                    all + " " + cur
                }
                // Very important that you normalize all words to uppercase.
                // Search tokens will also be normalized to uppercase
                writeRow(word.toUpperCase(), idsRaw)
            }
        }
    }.let {
        log.debug("Writing index took $it ms")
        println("Created data index (Use '--nuke' to re-index)")
    }

    return word2Rows
}