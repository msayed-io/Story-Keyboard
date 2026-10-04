package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.LibraryConfig
import com.example.data.MediaCategory
import com.example.data.MediaLibraryRepository
import com.example.data.MediaType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MediaLibraryRepositoryTest {

    private lateinit var context: Context
    private lateinit var repository: MediaLibraryRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        repository = MediaLibraryRepository(context)
    }

    @Test
    fun `buildAssetUrl generates valid raw and cdn URLs for original and curated paths`() {
        val originalFile = "assets/images/01_033cb1ef7be969ed4aee4d5bf6e0e2c0.jpg"
        val curatedFile = "assets/images/curated/realistic_01_81e7612feb580314b01cbce6f907c0b8.jpg"

        val rawOriginal = MediaLibraryRepository.buildAssetUrl(originalFile, useCdn = false)
        val cdnOriginal = MediaLibraryRepository.buildAssetUrl(originalFile, useCdn = true)

        assertEquals("https://raw.githubusercontent.com/msayed-io/hikayat-keyboard-library/v1.3.1/assets/images/01_033cb1ef7be969ed4aee4d5bf6e0e2c0.jpg", rawOriginal)
        assertEquals("https://cdn.jsdelivr.net/gh/msayed-io/hikayat-keyboard-library@v1.3.1/assets/images/01_033cb1ef7be969ed4aee4d5bf6e0e2c0.jpg", cdnOriginal)

        val rawCurated = MediaLibraryRepository.buildAssetUrl(curatedFile, useCdn = false)
        val cdnCurated = MediaLibraryRepository.buildAssetUrl(curatedFile, useCdn = true)

        assertEquals("https://raw.githubusercontent.com/msayed-io/hikayat-keyboard-library/v1.3.1/assets/images/curated/realistic_01_81e7612feb580314b01cbce6f907c0b8.jpg", rawCurated)
        assertEquals("https://cdn.jsdelivr.net/gh/msayed-io/hikayat-keyboard-library@v1.3.1/assets/images/curated/realistic_01_81e7612feb580314b01cbce6f907c0b8.jpg", cdnCurated)
    }

    @Test
    fun `parseJsonManifest merges both assets and curated_assets into 77 unique items`() {
        // Build mock JSON representing the manifest structure with 27 assets and 50 curated assets
        val assetsJson = (1..27).joinToString(",") { i ->
            val numStr = if (i < 10) "0$i" else "$i"
            """{"file": "assets/images/${numStr}_sample$i.jpg", "type": "image", "sha256": "hash_orig_$i", "bytes": 50000}"""
        }
        val curatedJson = (1..50).joinToString(",") { i ->
            val numStr = if (i < 10) "0$i" else "$i"
            """{"file": "assets/images/curated/realistic_${numStr}_sample$i.jpg", "type": "image", "sha256": "hash_curated_$i", "bytes": 80000}"""
        }

        val fullJson = """
        {
            "assets": [$assetsJson],
            "curated_assets": [$curatedJson]
        }
        """.trimIndent()

        val items = repository.parseJsonManifest(fullJson)

        assertEquals(77, items.size)
        assertEquals(27, items.count { it.category == MediaCategory.ORIGINAL_BOARD })
        assertEquals(50, items.count { it.category == MediaCategory.CURATED_LANDSCAPES })
        assertTrue(items.all { it.type == MediaType.IMAGE })
        assertTrue(items.all { it.downloadUrl.startsWith("https://raw.githubusercontent.com/msayed-io/hikayat-keyboard-library/v1.3.1/assets/") })
        assertTrue(items.all { it.previewUrl.startsWith("https://cdn.jsdelivr.net/gh/msayed-io/hikayat-keyboard-library@v1.3.1/assets/") })
    }

    @Test
    fun `parseJsonManifest filters out markdown, preview images, and directory traversal`() {
        val invalidJson = """
        {
            "assets": [
                {"file": "assets/images/01_valid.jpg", "type": "image", "sha256": "h1"},
                {"file": "assets/images/README.md", "type": "image", "sha256": "h2"},
                {"file": "../secret.jpg", "type": "image", "sha256": "h3"},
                {"file": "curated-preview.jpg", "type": "image", "sha256": "h4"}
            ],
            "curated_assets": [
                {"file": "assets/images/curated/02_valid.jpg", "type": "image", "sha256": "h5"},
                {"file": "assets/images/curated/curated-preview.jpg", "type": "image", "sha256": "h6"}
            ]
        }
        """.trimIndent()

        val items = repository.parseJsonManifest(invalidJson)
        assertEquals(2, items.size)
        assertEquals("assets/images/01_valid.jpg", items[0].file)
        assertEquals("assets/images/curated/02_valid.jpg", items[1].file)
    }

    @Test
    fun `parseJsonManifest deduplicates items with matching sha256 or matching files`() {
        val dupJson = """
        {
            "assets": [
                {"file": "assets/images/01_a.jpg", "type": "image", "sha256": "same_hash"},
                {"file": "assets/images/02_b.jpg", "type": "image", "sha256": "same_hash"}
            ],
            "curated_assets": [
                {"file": "assets/images/curated/03_c.jpg", "type": "image", "sha256": "diff_hash"}
            ]
        }
        """.trimIndent()

        val items = repository.parseJsonManifest(dupJson)
        assertEquals(2, items.size)
        assertEquals("assets/images/01_a.jpg", items[0].file)
        assertEquals("assets/images/curated/03_c.jpg", items[1].file)
    }

    @Test
    fun `verifySha256 correctly verifies file content against expected hash`() {
        val testFile = File(context.cacheDir, "test_sha.txt")
        testFile.writeText("Hikayat Keyboard Test String")

        // Calculated SHA-256 for "Hikayat Keyboard Test String"
        val expectedHash = "79b2d55e4da1f0c37988b123f406bac5dc0a570756664a8dfd581eac6d436e70"

        assertTrue(repository.verifySha256(testFile, expectedHash))
        assertFalse(repository.verifySha256(testFile, "wrong_hash_12345"))

        testFile.delete()
    }
}
