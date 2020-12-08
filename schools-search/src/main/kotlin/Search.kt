import kotlin.math.min
import kotlin.system.measureTimeMillis

val NUM_TOP_HITS = 3 // hits to show
val NUM_REFINEMENT_HITS = NUM_TOP_HITS * 3
val NUM_COMMON_WORDS = 5

class Search(private val dataPath: String) {
    private val log = getLogger("search")

    // Preprocessing state
    private var word2Rows : Map<String, List<String>>
    private var id2Rows : MutableMap<String, Row> = mutableMapOf()
    private var mostCommonWords : MutableSet<String> = mutableSetOf()
    private var preprocessingMillis: Long = 0

    /**
     * Preprocess the data:
     * - Load index of words from file into memory
     * - Load all row content from file into memory
     */
    init {
        measureTimeMillis {
            word2Rows = getIndex(dataPath)
            fillSchoolData(dataPath, id2Rows, mostCommonWords)
        }.let {preprocessingMillis = it}
    }

    /**
     * ## Iniitial Algorithm
     * This search algorithm examines only the full words of search query (e.g token) and matches
     * that against the full words found in the data row. We don't attempt to take into account which data field a
     * word is related to.
     *
     * We also have the strategy of indexing the data file first into a map of words to all rows that contain that word.
     *
     * ```"jefferson" -> "413421234", "2341324","2341324```
     *
     * A row is considered a strong match if many of the search tokens are amongst the words in the row. Simple :).
     *
     * ## Refinement of search results
     * Finally, say the initial results are difficult to distinguish because they all have the same "hit count". In that case, we disregard any
     * search tokens that are amongst the most common words found in the data set. This eliminates words like "school"
     * or "high" from over-weighting the search results
     *
     * We do this by having created an index of most common words.
     *
     * ## Measurements
     * There's two times that are measured
     * _Preprocessing time_
     * - Load data rows (that has content) from file system into memory
     *
     * _Search time_
     * - Load index from file system into memory
     * - Look at in-memory mappings and determine which results are topHits
     *
     * ## Terminology
     * - search query: set of tokens in the query, eg `jefferson belleville'
     * - search token: individual token within the query, eg `jefferson`
     * - word: the individual words found in a data row. eg 'Jefferson' is a word in the row 'Jefferson Middle School
     * in Cupertino CA'.
     * - row: row in the CSV file
     *
     * ## Trade-offs
     * - Basically this is a high-memory endeavor (b/c of the in-memory maps), but is computationally faster
     * - Also, there's the time cost of indexing the data file beforehand (for each new data file)
     *
     * ## Disadvantages
     * - Does not match by phrases, where the order of the search term impacts how strong the match is.
     * For example, the search term `jefferson middle" will just as easily match a row with 'Jefferson Middle School`,
     * as well as that of 'Jefferson High School in Middle, IL'
     *
     * - Ignores partial match of a search token with a word from the data row
     *
     * @return Top matches in descending order [Row]
     */
    fun search(query: String): Results {
        var results = Results(0,preprocessingMillis,mutableListOf<Row>())

        measureTimeMillis {
            // Sorted list of row-ids. These are the rows that match any of the search tokens.
            // Duplicate ids are expected
            var matchingRows = mutableListOf<String>()
            for (tokenRaw in query.split(SP)) {
                val token = tokenRaw.toUpperCase()
                // Add all the row-ids that are associated with the current token to 'matchingRows'
                word2Rows[token]?.let { ids ->
                    matchingRows.addAll(ids.filter { it.isNotEmpty() })
                }
            }

            val topHits = matchingRows.groupingBy { it }
                .eachCount()   // Group all the duplicate row IDS and then count them.
                .filter { it.value > 0 } // only keep row IDS that appeared at least once
                .toList()      // then sort by count in descending order
                .sortedByDescending { it.second }
                .let { sorted->
                    val topCount = if (sorted.isNotEmpty()) sorted.first().second else 0
                    // If all the top hits have the same hit count,
                    // refine the search using a larger group of results
                    if (sorted.take(NUM_TOP_HITS).all { it.second == topCount}) {
                        log.debug("Before refinement, results are: ${sorted.take(min(NUM_REFINEMENT_HITS,sorted.size))}")
                        refineSearch(query, sorted.map { it.first }.toSet(), min(NUM_REFINEMENT_HITS, sorted.size))
                    } else {
                        log.debug("Using initial results. No refinement necessary")
                        sorted // else use the unrefined results
                    }
                }

            if (topHits.isNotEmpty()) {
                loadTopHits(results, topHits.take(min(NUM_TOP_HITS, topHits.size)))
            }
        }.let {results.searchMills = it}

        log.debug("For query '$query', \n$results")
        return results
    }

    /**
     * Finally, say the initial results are difficult to distinguish because they all have the same "hit count".
     * In that case, we disregard any search tokens that are amongst the most common words found in the data set.
     * This eliminates words like "school" or "high" from over-weighting the search results
     *
     * @return topHits after another search
     */
    private fun refineSearch(query: String, topHitRowIds: Set<String>, numRefinementWindow: Int) :  List<Pair<String, Int>>{
        // build up map of word -> row-ids from the initial search results.
        // e.g "jefferson" -> "12341234", "4545241", "54345435"
        var word2RowIds = mutableMapOf<String, MutableList<String>>()
        id2Rows
            .filter { it.key in topHitRowIds }
            .forEach { id2Row ->// for each row, extract its words and do mapping
                id2Row.value.allWords()
                    .toSet() // filter to only the unique words so that duplicate words don't over-weight the results
                    .forEach { rowWord->
                        var ids = word2RowIds.getOrDefault(rowWord, mutableListOf())
                        ids.add(id2Row.key)
                        word2RowIds[rowWord] = ids
                }
            }

        var matchingRowIds = mutableListOf<String>()
        // Only iterate over tokens that are NOT amongst the most common words
        val refinedTokens = query.split(SP).filterNot { it.toUpperCase() in mostCommonWords }
        log.debug("Refined tokens are : $refinedTokens, from common: $mostCommonWords")
        for (token in refinedTokens) {
            // Use our smaller, more constrained mapping "word2RowIds"
            // Add all the row-ids that are associated with the current token to 'matchingRows'
            word2RowIds[token.toUpperCase()]?.let { ids ->
                matchingRowIds.addAll(ids.filter { it.isNotEmpty() })
            }
        }

        return matchingRowIds.groupingBy { it }
            .eachCount()   // Group all the duplicate row IDS and then count them.
            .filter { it.value > 0 } // only keep row IDS that appeared at least once
            .toList()      // then sort by count in descending order
            .sortedByDescending { it.second }
    }


    fun loadTopHits(results: Results, topHits: List<Pair<String,Int>>) {
        for (hit in topHits) {
            id2Rows[hit.first]?.let {row->
                // Create copy of Row instance and copy over the hit count
                results.topHits.add(row.copy(hitCount = hit.second))}
        }
    }

}