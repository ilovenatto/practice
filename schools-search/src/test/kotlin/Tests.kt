import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import java.io.File

class Tests {
    val L = getLogger("test")
    val SMALL_DATA = "./src/test/resources/school_data_small.csv"
    val BIG_DATA = "./src/main/resources/school_data.csv"

    @Before
    fun before() {
        // nuke the index
        if (File(INDEX_PATH).exists()) File(INDEX_PATH).delete()
    }

    //@Ignore
    @Test fun testCliFlags() {
        L.info("testCliFlags")
        main(arrayOf("count","-d",SMALL_DATA))
        main(arrayOf("search","-q","`jefferson`"))
    }

    //@Ignore
    @Test fun testCountSmall() {
        L.info("testCountSmall")
        // see if i can access the file
        assertTrue(File(SMALL_DATA).isFile)
        main(arrayOf("count", "-d", SMALL_DATA))
    }

    //@Ignore
    @Test fun testCountBig() {
        L.info("testCountBig -- all data")
        val smallData = "./src/main/resources/school_data.csv"
        assertTrue(File(smallData).isFile)
        main(arrayOf("count", "-d", smallData))
    }

    //@Ignore
    @Test fun testIndexing() {
        L.info("testIndexing")
        main(arrayOf("search", "-d", SMALL_DATA, "-q","douglas",))
        main(arrayOf("search", "-d", SMALL_DATA, "-q","douglas",)) // this time will use the existing index (not write a new one)
    }

    //@Ignore
    @Test fun testIndexingBig() {
        L.info("testIndexing_Big")
        main(arrayOf("search", "-d", BIG_DATA,  "-q","douglas",))
        main(arrayOf("search", "-d", BIG_DATA, "-q","douglas",)) // this time will use the existing index (not write a new one)
    }

    //@Ignore
    @Test fun testSearchFromCLI() {
        L.info("testSearchSmall1")
        main(arrayOf("search", "-d", SMALL_DATA, "-q","douglas middle school",)) // should be 1
    }

    //@Ignore
    @Test fun testSearchState() {
        L.info("testSearchSmall1")
        var res = Search(SMALL_DATA).search("alabama")
        assertEquals(1, res.topHits.size)
    }

    //@Ignore
    @Test fun testSearchSmall1() {
        L.info("testSearchSmall1")
        var expected = setOf("10000600877", "10000600878","10000601812")
        var res = Search(SMALL_DATA).search("douglas")
        assertEquals(3, res.topHits.size)
        assertTrue(res.topHits.all{expected.contains(it.rowId)})

        res = Search(SMALL_DATA).search("douglas middle")
        assertEquals("10000601812", res.topHits.first().rowId)

        res = Search(SMALL_DATA).search("douglas middle school")
        assertEquals("10000601812", res.topHits.first().rowId)

        res = Search(SMALL_DATA).search("Albertville alabama")
        assertEquals("10000500870", res.topHits.first().rowId)

        res = Search(SMALL_DATA).search("albertvil")
        assertTrue(res.topHits.isEmpty())

        res = Search(SMALL_DATA).search("kodiak east")
        assertEquals("20048000183", res.topHits.first().rowId)

        res = Search(SMALL_DATA).search("KLAWOCK")
        assertEquals("20045000179", res.topHits.first().rowId)
        assertEquals(1, res.topHits.size)

        res = Search(SMALL_DATA).search("warren high")
        assertEquals("50000601130", res.topHits.first().rowId)
    }

    //@Ignore
    @Test fun testSearchBig() {
        L.info("testSearchBig")
        var expected = setOf("010000600877", "010000600878","010000601812")
        var res = Search(BIG_DATA).search("douglas")
        assertEquals(3, res.topHits.size)
        assertTrue(res.topHits.all{expected.contains(it.rowId)})

        res = Search(BIG_DATA).search("douglas middle school")
        assertEquals("010000600877", res.topHits.first().rowId)

        res = Search(BIG_DATA).search("Albertville alabama")
        assertEquals("010000500870", res.topHits.first().rowId)

        res = Search(BIG_DATA).search("albertvil")
        assertTrue(res.topHits.isEmpty())

        res = Search(BIG_DATA).search("kodiak east")
        assertEquals("020048000183", res.topHits.first().rowId)

        res = Search(BIG_DATA).search("KLAWOCK")
        assertEquals("020045000179", res.topHits.first().rowId)
        assertEquals(1, res.topHits.size)

        //res = Search(BIG_DATA).search("warren high school")
        //assertEquals("050000601130", res.topHits.first().rowId)


    }

    //@Ignore
    @Test fun testSearchBig_CLI() {
        L.info("testIndexing")
        main(arrayOf("nuke")) // nuke the index file
        main(arrayOf("search", "-d", BIG_DATA, "-q","douglas",))
        main(arrayOf("search", "-d", BIG_DATA, "-q","albertvil",))
        main(arrayOf("search", "-d", BIG_DATA, "-q","klawock",))
        main(arrayOf("search", "-d", BIG_DATA, "-q","warren high",))
        main(arrayOf("search", "-d", BIG_DATA, "-q","al",))
        main(arrayOf("search", "-d", BIG_DATA, "-q","alabama",))
    }

    @Test fun testGranada() {
        L.info("testGranada")
        var res = Search(BIG_DATA).search("granada charter school")
        log.debug("Got res $res")
        assertEquals(3, res.topHits.size)
    }
}