-- Synthetic accounts in serve.py's random schema, not user/production data.
INSERT INTO sys_dept(dept_id,dept_name,status,is_delete) VALUES(100,'双高办测试','0',0),(104,'财务测试','0',0),(106,'普通部门测试','0',0);
INSERT INTO sys_user(user_id,dept_id,user_name,nick_name,password,role,status,is_delete,force_password_change) VALUES
(121362,106,'synthetic-password-case','改密测试账号','review-fixture-password','1','1',0,1),
(980100,100,'synthetic-office','双高办测试账号','review-fixture-password','1','1',0,0),
(980102,106,'synthetic-head','部门负责人测试账号','review-fixture-password','2','1',0,0),
(980104,104,'synthetic-finance','财务测试账号','review-fixture-password','1','1',0,0),
(980200,106,'synthetic-leader','校领导测试账号','review-fixture-password','1','1',0,0);
INSERT INTO sys_leadership_access(user_id,scope,version) VALUES(980200,'ALL',0);
