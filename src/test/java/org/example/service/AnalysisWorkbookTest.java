package org.example.service;

import org.junit.jupiter.api.Test;
import org.apache.poi.ss.usermodel.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

class AnalysisWorkbookTest {
    @Test void writesInAFreshJvmWithoutAUsableFontManager()throws Exception{
        String java=Path.of(System.getProperty("java.home"),"bin",System.getProperty("os.name").startsWith("Windows")?"java.exe":"java").toString();
        String classpath=System.getProperty("surefire.test.class.path",System.getProperty("java.class.path"));
        Path log=Files.createTempFile("fontless-workbook-",".log");
        Process process=new ProcessBuilder(java,"-Djava.awt.headless=true","-Dsun.font.fontmanager=missing.FontManager", "-cp",classpath,FontlessProbe.class.getName()).redirectErrorStream(true).redirectOutput(log.toFile()).start();
        try{
            assertTrue(process.waitFor(30,TimeUnit.SECONDS),"Fontless export process timed out");
            String output=Files.readString(log);
            assertEquals(0,process.exitValue(),output);
            assertTrue(output.contains("FONTLESS_EXPORT_OK"),output);
        }finally{if(process.isAlive())process.destroyForcibly();Files.deleteIfExists(log);}
    }

    public static class FontlessProbe {
        public static void main(String[] args)throws Exception{
            boolean unavailable=false;
            try{new java.awt.Font("Dialog",java.awt.Font.PLAIN,11).canDisplayUpTo("0");}
            catch(InternalError|NoClassDefFoundError|UnsatisfiedLinkError expected){unavailable=true;}
            if(!unavailable)throw new AssertionError("The child JVM must have an unavailable font manager");
            try(var out=new ByteArrayOutputStream();var book=new AnalysisWorkbook()){
                var rows=new ArrayList<Map<String,?>>();
                for(int i=0;i<301;i++)rows.add(Map.of("recordId",9007199254740993L+i,"text","无字体导出 "+i));
                book.sheet("明细","recordId:记录ID|text:说明",rows);
                book.sheet("空表","id:记录ID",List.of());
                book.write(out);
                try(var parsed=WorkbookFactory.create(new ByteArrayInputStream(out.toByteArray()))){
                    if(parsed.getSheet("明细").getPhysicalNumberOfRows()!=302)throw new AssertionError("Streaming rows lost");
                    if(!parsed.getSheet("明细").getRow(301).getCell(1).getStringCellValue().equals("无字体导出 300"))throw new AssertionError("Text changed");
                    if(parsed.getSheet("空表").getPhysicalNumberOfRows()!=1)throw new AssertionError("Empty sheet header missing");
                }
            }
            System.out.println("FONTLESS_EXPORT_OK");
        }
    }

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
