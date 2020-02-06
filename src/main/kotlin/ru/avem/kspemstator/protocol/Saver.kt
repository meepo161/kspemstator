package ru.avem.kspemstator.protocol

import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.xssf.usermodel.XSSFRichTextString
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import ru.avem.kspemstator.app.KspemStator
import ru.avem.kspemstator.database.entities.Protocol
import ru.avem.kspemstator.utils.copyFileFromStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException

fun saveProtocolAsWorkbook(protocol: Protocol, path: String = "protocol.xlsx") {
    val template = File(path)
    copyFileFromStream(KspemStator::class.java.getResource("protocol.xlsx").openStream(), template)

    try {
        XSSFWorkbook(template).use {
            val sheet = it.getSheetAt(0)
            for (iRow in 0 until 100) {
                val row = sheet.getRow(iRow)
                if (row != null) {
                    for (iCell in 0 until 100) {
                        val cell = row.getCell(iCell)
                        if (cell != null && (cell.cellType == CellType.STRING)) {
                            when (cell.stringCellValue) {
                                "#PROTOCOL_NUMBER#" -> cell.setCellValue(protocol.id.toString())
                                "#OBJECT#" -> cell.setCellValue(protocol.objectType)
                                "#SERIAL_NUMBER#" -> cell.setCellValue(protocol.factoryNumber)
                                "#DATE#" -> cell.setCellValue(protocol.date)
                                "#TIME#" -> cell.setCellValue(protocol.time)
                                "#101#" -> cell.setCellValue(protocol.objectType)
                                "#102#" -> cell.setCellValue(protocol.outsideD)
                                "#103#" -> cell.setCellValue(protocol.insideD)
                                "#104#" -> cell.setCellValue(protocol.ironLength)
                                "#106#" -> cell.setCellValue(protocol.backHeight)
                                "#107#" -> cell.setCellValue(protocol.material)
                                "#108#" -> cell.setCellValue(protocol.insulation)
                                "#109#" -> cell.setCellValue(protocol.mark)
                                "#111#" -> cell.setCellValue(protocol.density)
                                "#112#" -> cell.setCellValue(protocol.losses)
                                "#113#" -> cell.setCellValue(protocol.intensity)
                                "#116#" -> cell.setCellValue(protocol.i1)
                                "#117#" -> cell.setCellValue(protocol.u1)
                                "#118#" -> cell.setCellValue(protocol.p1)
                                "#119#" -> cell.setCellValue(protocol.bf)
                                "#120#" -> cell.setCellValue(protocol.pf)
                                "#121#" -> cell.setCellValue(protocol.pt)
                                "#122#" -> cell.setCellValue(protocol.hf)
                                "#RESULT#" -> {
                                    val font = it.createFont()
                                    font.italic = true
                                    font.fontName = "Arial"
                                    font.fontHeight = 9 * 20
                                    val phrase = XSSFRichTextString("Пройдено успешно\nPassed successfull")
                                    phrase.applyFont(17, 35, font)
                                    cell.setCellValue(phrase)
                                }
                                else -> {
                                    if (cell.stringCellValue.contains("#")) {
                                        cell.setCellValue("")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            val outStream = ByteArrayOutputStream()
            it.write(outStream)
            outStream.close()
        }
    } catch (e: FileNotFoundException) {
    }
}
