/**
 * 通知相关API
 */
import { get, post } from '@/utils/request';

/**
 * 获取当前用户收到的通知
 */
export const getNotices = (config = {}) => {
  return get('/system/notice', {}, config);
};

/**
 * 发送通知
 */
export const sendNotice = (data) => {
  return post('/system/notice', data);
};

/**
 * 设置通知为已读
 */
export const setNoticeRead = (noticeId) => {
  return post(`/system/notice/${noticeId}`);
};

/**
 * 按昵称发送预警
 */
export const sendAlert = (data) => {
  return post('/system/alert', data);
};



// Badge count shares the list endpoint permissions and business predicates.
export const getUnreadNoticeCount = () => get('/system/notice', { countOnly: true });
