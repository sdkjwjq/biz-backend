/** 从当前权限内的记录生成部门选项，避免把无权查看的部门作为筛选提示。 */
export const departmentOptions = (rows, getId, getName) => [
  { value: '', label: '全部部门' },
  ...new Map(rows.filter(row => getId(row) != null).map(row => {
    const id = getId(row);
    return [String(id), { value: String(id), label: getName(id) || `部门${id}` }];
  })).values()
];

/** 从当前权限内的记录生成账号选项，仅显示实际出现在列表数据中的人员。 */
export const personOptions = (rows, getId, getName) => [
  { value: '', label: '全部人员' },
  ...new Map(rows.filter(row => getId(row) != null).map(row => {
    const id = getId(row);
    return [String(id), { value: String(id), label: getName(id) || `用户${id}` }];
  })).values()
];

export const handlerDepartment = (handlerId, users) => {
  if (handlerId == null) return null;
  const user = users.find(row => String(row.userId ?? row.user_id) === String(handlerId));
  return user?.deptId ?? user?.dept_id ?? null;
};
