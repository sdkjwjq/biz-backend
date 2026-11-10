/**
 * 部门相关API
 */
import { get } from '@/utils/request';

/**
 * 根据部门ID获取部门信息（SysDept）
 * GET /system/dept/{deptId}
 */
export const getDeptById = (deptId) => {
  return get(`/system/dept/${deptId}`);
};


