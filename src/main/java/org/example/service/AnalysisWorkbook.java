package org.example.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import java.io.*;
import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Streaming, explicitly selected columns. Text is always a string, never a formula. */
final class AnalysisWorkbook implements AutoCloseable {
    final SXSSFWorkbook book=new SXSSFWorkbook(100);
    private final CellStyle header,body;
    private final List<Map<String,Object>> continuations=new ArrayList<>();
    private static final ZoneId ZONE=ZoneId.of("Asia/Shanghai");
    AnalysisWorkbook(){
        book.setCompressTempFiles(true);
        header=book.createCellStyle();var font=book.createFont();font.setBold(true);header.setFont(font);
        header.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        body=book.createCellStyle();body.setWrapText(true);body.setVerticalAlignment(VerticalAlignment.TOP);
    }
    void sheet(String name,String columns,List<? extends Map<String,?>> rows){
        String[][] fields=Arrays.stream(columns.split("\\|")).map(s->s.split(":",2)).toArray(String[][]::new);
        Sheet sheet=null;int index=0,part=0;
        // Include headers even for an empty result; no artificial business row.
        for(int n=0;n<Math.max(1,rows.size());n++){
            if(sheet==null||index>=1_000_000){
                if(sheet!=null)sheet.setAutoFilter(new CellRangeAddress(0,index-1,0,fields.length-1));
                sheet=book.createSheet(name+(part++==0?"":"-"+part));index=1;var head=sheet.createRow(0);
                for(int c=0;c<fields.length;c++){head.createCell(c).setCellValue(fields[c][1]);head.getCell(c).setCellStyle(header);sheet.setColumnWidth(c,Math.min(65,Math.max(16,fields[c][1].length()*2+4))*256);}
                sheet.createFreezePane(0,1);sheet.setAutoFilter(new CellRangeAddress(0,0,0,fields.length-1));
            }
            if(rows.isEmpty())break;
            Map<String,?> data=rows.get(n);var row=sheet.createRow(index++);
            for(int c=0;c<fields.length;c++){
                Object value=data.get(fields[c][0]);Cell cell=row.createCell(c);cell.setCellStyle(body);
                if(value==null)continue;
                // IDs remain text so spreadsheet numeric precision cannot corrupt them.
                if(value instanceof Number number&&!fields[c][0].toLowerCase(Locale.ROOT).endsWith("id")){
                    BigDecimal decimal=new BigDecimal(number.toString());if(decimal.precision()<=15){cell.setCellValue(decimal.doubleValue());continue;}
                }
                String text=format(value);
                if(text.length()>32000){
                    int seq=1;for(int start=0;start<text.length();){int end=Math.min(start+30000,text.length());if(end<text.length()&&Character.isHighSurrogate(text.charAt(end-1)))end--;continuations.add(Map.of("sheet",sheet.getSheetName(),"row",index,"column",fields[c][1],"part",seq++,"text",text.substring(start,end)));start=end;}
                    text=text.substring(0,1000)+"\n【完整内容见“长文本续页”，Excel行号 "+index+"】";
                }
                cell.setCellValue(text);
            }
        }
        if(sheet!=null)sheet.setAutoFilter(new CellRangeAddress(0,index-1,0,fields.length-1));
    }
    void write(OutputStream out)throws IOException{
        if(!continuations.isEmpty()){var values=new ArrayList<>(continuations);continuations.clear();sheet("长文本续页","sheet:原工作表|row:Excel行号|column:原列名|part:分段序号|text:完整内容分段",values);}
        book.write(out);
    }
    static String format(Object value){
        if(value instanceof java.sql.Date date)return date.toLocalDate().toString();
        if(value instanceof java.util.Date date)return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(date.toInstant().atZone(ZONE));
        if(value instanceof LocalDateTime date)return date.toString().replace('T',' ');
        if(value instanceof Collection<?> values)return String.join("、",values.stream().map(AnalysisWorkbook::format).toList());
        return value==null?"":String.valueOf(value);
    }
    // dispose flushes pending streaming rows before deleting their temporary files.
    // Running it after close tries to flush a closed writer when generation failed.
    @Override public void close()throws IOException{try{book.dispose();}finally{book.close();}}
}
