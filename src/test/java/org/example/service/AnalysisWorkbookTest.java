package org.example.service;

import org.junit.jupiter.api.Test;
import org.apache.poi.ss.usermodel.*;
import java.io.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class AnalysisWorkbookTest {
    @Test void preservesLongTextIdsNumbersAndFormulaLikeStrings()throws Exception{
        String text="段落🧾".repeat(9000);byte[] bytes;
        try(var out=new ByteArrayOutputStream();var book=new AnalysisWorkbook()){
            book.sheet("业务明细","recordId:记录ID|number:完成值|text:正文|title:标题",List.of(Map.of("recordId",9007199254740993L,"number",15.75,"text",text,"title","=HYPERLINK(\"https://example.com\")")));
            book.write(out);bytes=out.toByteArray();
        }
        try(var book=WorkbookFactory.create(new ByteArrayInputStream(bytes))){
            var row=book.getSheetAt(0).getRow(1);assertEquals("9007199254740993",row.getCell(0).getStringCellValue());assertEquals(15.75,row.getCell(1).getNumericCellValue());assertEquals(CellType.STRING,row.getCell(3).getCellType());
            var joined=new StringBuilder();for(var part:book.getSheet("长文本续页"))if(part.getRowNum()>0)joined.append(part.getCell(4).getStringCellValue());assertEquals(text,joined.toString());
        }
    }
}
