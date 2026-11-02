package org.example.service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.List;

/** Legacy task export endpoint uses the same detailed, permission-checked workbook. */
@Service
public class TaskExportService {
    private final DetailedExportService exports;
    public TaskExportService(DetailedExportService exports){this.exports=exports;}
    public void exportTasks(List<Long> requestedIds,Long actor,HttpServletResponse response)throws IOException{
        exports.selectedTasks(actor,requestedIds,response);
    }
}
