package org.example.service;
import org.springframework.jdbc.core.*;
import java.sql.*;
import java.util.*;
import java.util.function.Consumer;
/** Forward-only MySQL cursor: at most one projection is retained by this reader. */
public final class JdbcProjectionStream {
 public static void read(JdbcTemplate jdbc,String sql,List<?> args,Consumer<Map<String,Object>> consumer){
  var mapper=new ColumnMapRowMapper();
  jdbc.query(connection->{var statement=connection.prepareStatement(sql,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);statement.setFetchSize(Integer.MIN_VALUE);for(int i=0;i<args.size();i++)statement.setObject(i+1,args.get(i));return statement;},(RowCallbackHandler)rows->consumer.accept(mapper.mapRow(rows,0)));
 }
 /** Match MyBatis DateTypeHandler even when getObject(DATETIME) yields LocalDateTime. */
 public static java.util.Date legacyDate(ResultSet row,String column)throws SQLException {var value=row.getTimestamp(column);return value==null?null:new java.util.Date(value.getTime());}
 private JdbcProjectionStream(){}
}
