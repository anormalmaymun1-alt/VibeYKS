package com.example.yksaisinavkocu.ui

import com.example.yksaisinavkocu.ui.components.MarkdownBlock
import com.example.yksaisinavkocu.ui.components.parseMarkdownBlocks
import org.junit.Assert.*
import org.junit.Test

class RichTextVisualizerTest {

    @Test
    fun testAiCoachMessageParsing() {
        val rawAiMessage = """
            * **Ölçülebilir Hedef:** Bu üç konudan üçer test (toplam 60 soru) çözerek yanlışlarını kontrol et.

            3. **Coğrafya (İklim Elemanları - %100 Hata Oranı):**
               * **Eylem:** TYT Coğrafya'nın en çok soru getiren yeri olan "Sıcaklık, Basınç, Rüzgarlar, Nem ve Yağış" konusunu dünya haritası üzerinde çalış. Dinamik ve termik basınç kuşaklarının yerlerini öğren.
               * **Ölçülebilir Hedef:** Boş bir dünya haritası üzerinde çöl ve bol yağış alan bölgeleri elinle çizerek göster ve 30 soru çöz.

            4. **Türkçe (Paragraf & Cümle Anlamı - 7 Boş ve 4 Yanlış):**
               * **Eylem:** Türkçe netin (28,00 net) potansiyeline göre düşük kalmış.
               * **Ölçülebilir Hedef:** Her gün sabah ilk iş olarak 25 dakikalık süre sınırı koyarak 20 paragraf sorusu çöz.
        """.trimIndent()

        val blocks = parseMarkdownBlocks(rawAiMessage)

        // Block 0: Bullet level 0
        assertTrue(blocks[0] is MarkdownBlock.Bullet)
        val bullet0 = blocks[0] as MarkdownBlock.Bullet
        assertEquals(0, bullet0.level)
        assertTrue(bullet0.text.startsWith("**Ölçülebilir Hedef:**"))

        // Block 1: Space
        assertTrue(blocks[1] is MarkdownBlock.Space)

        // Block 2: Numbered item 3
        assertTrue(blocks[2] is MarkdownBlock.Numbered)
        val num3 = blocks[2] as MarkdownBlock.Numbered
        assertEquals("3", num3.number)
        assertEquals(0, num3.level)
        assertEquals("**Coğrafya (İklim Elemanları - %100 Hata Oranı):**", num3.text)

        // Block 3: Sub-bullet "Eylem" (level 1)
        assertTrue(blocks[3] is MarkdownBlock.Bullet)
        val subBullet1 = blocks[3] as MarkdownBlock.Bullet
        assertEquals(1, subBullet1.level)
        assertTrue(subBullet1.text.startsWith("**Eylem:**"))

        // Block 4: Sub-bullet "Ölçülebilir Hedef" (level 1)
        assertTrue(blocks[4] is MarkdownBlock.Bullet)
        val subBullet2 = blocks[4] as MarkdownBlock.Bullet
        assertEquals(1, subBullet2.level)
        assertTrue(subBullet2.text.startsWith("**Ölçülebilir Hedef:**"))

        // Block 5: Space
        assertTrue(blocks[5] is MarkdownBlock.Space)

        // Block 6: Numbered item 4
        assertTrue(blocks[6] is MarkdownBlock.Numbered)
        val num4 = blocks[6] as MarkdownBlock.Numbered
        assertEquals("4", num4.number)
        assertEquals(0, num4.level)
        assertEquals("**Türkçe (Paragraf & Cümle Anlamı - 7 Boş ve 4 Yanlış):**", num4.text)

        // Block 7: Sub-bullet level 1
        assertTrue(blocks[7] is MarkdownBlock.Bullet)
        val subBullet3 = blocks[7] as MarkdownBlock.Bullet
        assertEquals(1, subBullet3.level)
        assertTrue(subBullet3.text.startsWith("**Eylem:**"))
    }

    @Test
    fun testHeadingsAndFormatting() {
        val markdown = """
            # Ana Başlık
            ## İkinci Başlık
            ### Üçüncü Başlık
            
            Düz bir paragraf metni.
            
            ---
            
            > Motivasyon sözü burada.
        """.trimIndent()

        val blocks = parseMarkdownBlocks(markdown)

        assertTrue(blocks.any { it is MarkdownBlock.Heading && it.level == 1 && it.text == "Ana Başlık" })
        assertTrue(blocks.any { it is MarkdownBlock.Heading && it.level == 2 && it.text == "İkinci Başlık" })
        assertTrue(blocks.any { it is MarkdownBlock.Heading && it.level == 3 && it.text == "Üçüncü Başlık" })
        assertTrue(blocks.any { it is MarkdownBlock.Paragraph && it.text == "Düz bir paragraf metni." })
        assertTrue(blocks.any { it is MarkdownBlock.HorizontalRule })
        assertTrue(blocks.any { it is MarkdownBlock.Quote && it.text == "Motivasyon sözü burada." })
    }

    @Test
    fun testCodeBlockAndTable() {
        val markdown = """
            ```python
            def cozum():
                return 42
            ```

            | Ders | Net |
            |---|---|
            | Matematik | 32.5 |
            | Türkçe | 35.0 |
        """.trimIndent()

        val blocks = parseMarkdownBlocks(markdown)

        val codeBlock = blocks.filterIsInstance<MarkdownBlock.CodeBlock>().firstOrNull()
        assertNotNull(codeBlock)
        assertEquals("python", codeBlock?.language)
        assertTrue(codeBlock?.code?.contains("def cozum():") == true)

        val tableBlock = blocks.filterIsInstance<MarkdownBlock.Table>().firstOrNull()
        assertNotNull(tableBlock)
        assertEquals(listOf("Ders", "Net"), tableBlock?.headers)
        assertEquals(2, tableBlock?.rows?.size)
        assertEquals(listOf("Matematik", "32.5"), tableBlock?.rows?.get(0))
        assertEquals(listOf("Türkçe", "35.0"), tableBlock?.rows?.get(1))
    }

    @Test
    fun testHtmlCommentsStripped() {
        val markdown = """
            Tebrikler! Çok iyi ilerliyorsun.
            <!-- ACTIONS: [{"type": "OPEN_SCANNER"}] -->
        """.trimIndent()

        val blocks = parseMarkdownBlocks(markdown)
        assertEquals(1, blocks.size)
        assertTrue(blocks[0] is MarkdownBlock.Paragraph)
        assertEquals("Tebrikler! Çok iyi ilerliyorsun.", (blocks[0] as MarkdownBlock.Paragraph).text)
    }
}
