package com.example.util

import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Lightweight native XLSX generator without heavy external dependencies.
 * Creates valid Microsoft Excel (.xlsx) OpenXML spreadsheet files.
 */
class SimpleXlsxBuilder {

    private val rows = mutableListOf<List<CellValue>>()

    sealed class CellValue {
        data class Text(val value: String) : CellValue()
        data class Number(val value: Double) : CellValue()
    }

    fun addRow(vararg cells: Any?) {
        val rowCells = cells.map { cell ->
            when (cell) {
                null -> CellValue.Text("")
                is Number -> CellValue.Number(cell.toDouble())
                else -> CellValue.Text(cell.toString())
            }
        }
        rows.add(rowCells)
    }

    fun addRow(cells: List<Any?>) {
        val rowCells = cells.map { cell ->
            when (cell) {
                null -> CellValue.Text("")
                is Number -> CellValue.Number(cell.toDouble())
                else -> CellValue.Text(cell.toString())
            }
        }
        rows.add(rowCells)
    }

    fun writeToFile(targetFile: File) {
        FileOutputStream(targetFile).use { fos ->
            writeToStream(fos)
        }
    }

    fun writeToStream(outputStream: OutputStream) {
        val sharedStrings = mutableListOf<String>()
        val sharedStringMap = mutableMapOf<String, Int>()

        fun getOrAddSharedString(str: String): Int {
            return sharedStringMap.getOrPut(str) {
                val index = sharedStrings.size
                sharedStrings.add(str)
                index
            }
        }

        // Pre-scan all string cells into shared strings table
        val sheetXmlRows = StringBuilder()
        for ((rowIndex, row) in rows.withIndex()) {
            val rNum = rowIndex + 1
            sheetXmlRows.append("""<row r="$rNum">""")
            for ((colIndex, cell) in row.withIndex()) {
                val cellRef = "${toColumnName(colIndex)}$rNum"
                when (cell) {
                    is CellValue.Text -> {
                        val sIndex = getOrAddSharedString(cell.value)
                        sheetXmlRows.append("""<c r="$cellRef" t="s"><v>$sIndex</v></c>""")
                    }
                    is CellValue.Number -> {
                        // Check if integer
                        val numStr = if (cell.value % 1.0 == 0.0) {
                            cell.value.toLong().toString()
                        } else {
                            cell.value.toString()
                        }
                        sheetXmlRows.append("""<c r="$cellRef"><v>$numStr</v></c>""")
                    }
                }
            }
            sheetXmlRows.append("</row>")
        }

        // Shared strings xml
        val sharedStringsXml = buildString {
            append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
            append("""<sst xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" count="${sharedStrings.size}" uniqueCount="${sharedStrings.size}">""")
            for (s in sharedStrings) {
                append("<si><t xml:space=\"preserve\">").append(escapeXml(s)).append("</t></si>")
            }
            append("</sst>")
        }

        // Sheet xml
        val sheetXml = buildString {
            append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
            append("""<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">""")
            append("""<sheetViews><sheetView tabSelected="1" workbookViewId="0"/></sheetViews>""")
            append("""<sheetFormatPr defaultRowHeight="16"/>""")
            append("""<sheetData>""").append(sheetXmlRows).append("""</sheetData>""")
            append("""</worksheet>""")
        }

        val contentTypesXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
  <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
  <Override PartName="/xl/sharedStrings.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sharedStrings+xml"/>
</Types>"""

        val rootRelsXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>"""

        val workbookXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <sheets>
    <sheet name="Attendance" sheetId="1" r:id="rId1"/>
  </sheets>
</workbook>"""

        val workbookRelsXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/sharedStrings" Target="sharedStrings.xml"/>
</Relationships>"""

        ZipOutputStream(outputStream).use { zip ->
            writeZipEntry(zip, "[Content_Types].xml", contentTypesXml)
            writeZipEntry(zip, "_rels/.rels", rootRelsXml)
            writeZipEntry(zip, "xl/workbook.xml", workbookXml)
            writeZipEntry(zip, "xl/_rels/workbook.xml.rels", workbookRelsXml)
            writeZipEntry(zip, "xl/sharedStrings.xml", sharedStringsXml)
            writeZipEntry(zip, "xl/worksheets/sheet1.xml", sheetXml)
            zip.finish()
        }
    }

    private fun writeZipEntry(zip: ZipOutputStream, entryName: String, content: String) {
        val entry = ZipEntry(entryName)
        zip.putNextEntry(entry)
        zip.write(content.toByteArray(StandardCharsets.UTF_8))
        zip.closeEntry()
    }

    private fun escapeXml(text: String): String {
        return buildString {
            for (ch in text) {
                when (ch) {
                    '&' -> append("&amp;")
                    '<' -> append("&lt;")
                    '>' -> append("&gt;")
                    '"' -> append("&quot;")
                    '\'' -> append("&apos;")
                    else -> append(ch)
                }
            }
        }
    }

    private fun toColumnName(colIndex: Int): String {
        var num = colIndex
        val sb = StringBuilder()
        while (num >= 0) {
            sb.insert(0, ('A'.code + (num % 26)).toChar())
            num = (num / 26) - 1
        }
        return sb.toString()
    }
}
