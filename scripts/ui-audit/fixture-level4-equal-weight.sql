-- 四级目标等权汇总浏览器验收数据，仅导入随机隔离数据库。
INSERT INTO sys_file (file_id,file_name,file_path,file_url,file_suffix,upload_by)
VALUES (961100,'level4-equal-weight.pdf','level4-equal-weight.pdf','/uploads/level4-equal-weight.pdf','pdf',910001);

INSERT INTO biz_level4_task
  (task_id,parent_id,phase,task_name,leader_id,dept_id,data_type,target_value,current_value,progress,status,is_delete)
VALUES
  (961001,930002,2026,'超额完成目标',910001,920001,'1',10,15,100,'1',0),
  (961002,930002,2026,'完成一半目标',910001,920001,'2',100,50,50,'1',0),
  (961003,930002,2026,'刚好完成目标',910001,920001,'1',1,1,100,'1',0);

UPDATE biz_task
SET current_value=8.3333,progress=83,status='1'
WHERE task_id=930002;
