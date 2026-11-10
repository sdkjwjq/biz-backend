/**
 * 用户相关API
 */
import { get, post } from '@/utils/request';

/**
 * 用户登录
 */
export const login = (username, password) => {
  return post('/system/login', {
    user_id: username,
    password: password
  });
};

export const getSsoStatus = () => get('/system/sso/status', {}, { handleAuthFailure: false });
export const startSso = () => post('/system/sso/start', {}, { handleAuthFailure: false });
export const exchangeSso = (code, state) => post('/system/sso/exchange', { code, state }, { handleAuthFailure: false });

/**
 * 用户登出
 */
export const logout = () => {
  return post('/system/logout', {}, { handleAuthFailure: false });
};

/**
 * 修改密码
 */
export const changePassword = (newPassword) => {
  return post('/system/password', {
    new_password: newPassword
  });
};

export const getPasswordStatus = () => get('/system/password/status');

/** 登录前验证原密码，不把验证失败当作登录会话过期。 */
export const resetPassword = (data) => post('/system/password/reset', data, { handleAuthFailure: false });

/**
 * 获取所有用户
 */
export const getAllUsers = (config = {}) => {
  return get('/system/allUsers', {}, config);
};

/**
 * 根据部门ID获取部门信息
 * 注意：后端接口返回类型可能有问题，需要确认
 */
export const getDeptById = (deptId) => {
  return get(`/system/dept/${deptId}`);
};

/**
 * 添加用户
 * @param {Object} user - 用户信息对象
 * @param {Long} user.userId - 用户ID
 * @param {Long} user.deptId - 所属部门ID
 * @param {String} user.userName - 账号
 * @param {String} user.nickName - 姓名
 * @param {String} user.email - 邮箱
 * @param {String} user.password - 密码
 * @param {String} user.role - 角色 0:admin 1:user 2:leader
 * @param {String} user.status - 兼容线上既有数据：1正常，0停用
 */
export const addUser = (user) => {
  return post('/system/users/add', user);
};

/**
 * 更新用户
 * @param {Object} user - 用户信息对象
 * @param {Long} user.userId - 用户ID
 * @param {Long} user.deptId - 所属部门ID
 * @param {String} user.userName - 账号
 * @param {String} user.nickName - 姓名
 * @param {String} user.email - 邮箱
 * @param {String} user.password - 密码
 * @param {String} user.role - 角色 0:admin 1:user 2:leader
 * @param {String} user.status - 兼容线上既有数据：1正常，0停用
 */
export const updateUser = (user) => {
  return post('/system/users/update', user);
};

/**
 * 删除用户
 * @param {Long} userId - 用户ID
 */
export const deleteUser = (userId) => {
  return post(`/system/users/delete/${userId}`);
};

