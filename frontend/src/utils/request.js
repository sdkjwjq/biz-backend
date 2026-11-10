import { adminTestFetch } from '@/utils/adminTest';
import { clearWorksCache } from './worksCache';
import { passwordState } from './passwordPolicy';
import { isJsonResponse, isHtmlResponse, responseErrorMessage } from './fileResponses.mjs';
/**
 * HTTP请求工具类
 * 封装fetch请求，统一处理token、错误等
 */

// 开发环境使用代理（通过vite.config.js配置），生产环境使用完整URL
const BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';

/**
 * 401 时统一跳转登录页（避免停留在当前页反复报错）
 */
const redirectToLogin = () => {
  try {
    const path = window.location?.pathname || '';
    if (path === '/login') return;
    // 记录回跳地址（登录页也可用 query.redirect，这里作为兜底）
    const full = `${window.location.pathname || ''}${window.location.search || ''}${window.location.hash || ''}`;
    try { sessionStorage.setItem('redirectAfterLogin', full); } catch (e) { // eslint-disable-line no-unused-vars
      // 忽略存储错误
    }
    window.location.replace(`/login?redirect=${encodeURIComponent(full)}`);
  } catch (e) { // eslint-disable-line no-unused-vars
    // ignore
  }
};

/**
 * 获取token（从localStorage）
 */
const getToken = () => {
  return localStorage.getItem('token') || '';
};

/**
 * 统一请求方法
 */
const request = async (url, options = {}) => {
  const { handleAuthFailure = true, responseType, ...fetchOptions } = options;
  const token = getToken();
  const __DEV__ = import.meta.env.DEV;

  const defaultOptions = {
    headers: {
      'Content-Type': 'application/json',
      ...(token && { 'Authorization': token }) // 后端期望纯token，不需要Bearer前缀
    }
  };

  const config = {
    ...defaultOptions,
    ...fetchOptions,
    headers: {
      ...defaultOptions.headers,
      ...options.headers
    }
  };

  if (config.body instanceof FormData) {
    delete config.headers['Content-Type'];
  }

  try {
    const response = await adminTestFetch(`${BASE_URL}${url}`, config);

    // 处理响应
    const contentType = response.headers.get('content-type');
    let data;

    if (isJsonResponse(contentType)) {
      data = await response.json();
    } else if (response.ok && responseType === 'blob' && !isHtmlResponse(contentType)) {
      data = await response.blob();
    } else {
      data = await response.text();
    }

    /**
     * 将后端message（可能是英文或技术信息）翻译为更友好的中文提示
     */
    const translateBackendMessage = (message, code) => {
      const msg = responseErrorMessage(message, '');
      const c = Number(code);
      // 未登录/登录过期
      if (c === 401) {
        if (!handleAuthFailure) return msg || '账号或原密码错误';
        if (msg.includes('No Token') || msg.includes('token不存在') || msg.toLowerCase().includes('token')) {
          return '未登录或登录已过期，请先登录';
        }
        return '身份验证失败，请重新登录';
      }
      // 无权限
      if (c === 403) {
        return /[\u4e00-\u9fff]/.test(msg) ? msg : '该用户无权限执行此操作';
      }
      // 常见英文/技术错误兜底
      if (msg.includes('No Token')) return '未登录或登录已过期，请先登录';
      if (msg.includes('Invalid token') || msg.includes('Invalid Token')) return '登录已过期或token无效，请重新登录';
      if (msg.toLowerCase().includes('permission') || msg.includes('Forbidden')) return '该用户无权限执行此操作';
      return msg || '请求失败';
    };

    // 处理后端"业务错误"但HTTP仍为200的场景：ErrorVO {message, code}
    if (data && typeof data === 'object' && data.code !== undefined && data.code !== null) {
      const bizCode = Number(data.code);
      if (!Number.isNaN(bizCode) && bizCode >= 400) {
        if (bizCode === 423 && data.reason === 'PLATFORM_NOTICE_REQUIRED' && getToken() === token) window.dispatchEvent(new Event('platform-notice-required'));
        if (bizCode === 428 && getToken() === token) passwordState.required = true;
        // 401：清除token
        if (bizCode === 401 && handleAuthFailure) {
          if (getToken() === token) {
            clearWorksCache();
            localStorage.removeItem('token');
            redirectToLogin();
          }
        }
        throw Object.assign(new Error(translateBackendMessage(data.message, bizCode)), { responseCode: bizCode });
      }
    }

    // 如果响应不成功，抛出错误（部分"可选接口"允许静默失败）
    if (!response.ok) {
      // 可选能力：操作日志接口后端可能未实现。遇到 404 直接返回空数组，避免页面报错/控制台刷屏。
      // 注意：即便 JS 不报错，浏览器仍可能提示 404 资源失败；因此前端还会在业务层做"只尝试一次"的降噪。
      if (response.status === 404 && typeof url === 'string' && url.includes('/biz/audit/logs/')) {
        return [];
      }

      // 403 权限不足：对于某些可选接口（如 /system/allUsers），普通用户无法访问是正常情况
      // 这些错误应该由业务层处理，不在 request 层打印错误日志
      if (response.status === 403 && typeof url === 'string' && (url.includes('/system/allUsers') || url.includes('/system/users'))) {
        // 静默处理，不打印错误日志，由业务层处理
        const error = new Error('该用户无权限执行此操作');
        error.isPermissionError = true; // 标记为权限错误，业务层可以据此判断
        error.responseCode = 403;
        throw error;
      }

      let errorMessage = responseErrorMessage(data, response.status >= 500
        ? '服务暂时无法完成请求，请稍后重试' : `请求失败: ${response.status}`);

      // 检查是否是数据库表不存在的错误
      if (typeof errorMessage === 'string' && errorMessage.includes('token_blacklist') && errorMessage.includes("doesn't exist")) {
        errorMessage = '后端数据库配置错误：缺少 token_blacklist 表。请联系后端开发人员创建该表。';
      }

      // 检查是否是token无效的错误
      if (response.status === 401 && handleAuthFailure) {
        // 清除无效的token
        if (getToken() === token) {
          clearWorksCache();
          localStorage.removeItem('token');
          redirectToLogin();
        }
        // 如果是token相关错误，提示用户重新登录
        if (typeof errorMessage === 'string' && (errorMessage.includes('Token') || errorMessage.includes('token'))) {
          errorMessage = '登录已过期或token无效，请重新登录';
        }
      }

      // 进一步做中文化兜底
      if (typeof errorMessage === 'string') {
        errorMessage = translateBackendMessage(errorMessage, response.status);
      }

      throw Object.assign(new Error(errorMessage), { responseCode: response.status });
    }

    if (responseType === 'blob' && !(data instanceof Blob)) {
      throw new Error('下载失败，服务器未返回文件，请稍后重试');
    }
    if (responseType === 'blob' && data.size === 0) throw new Error('下载文件为空，请重试');
    return data;
  } catch (error) {
    // 生产环境减少控制台噪音；错误仍会向上抛出，由页面用 ElMessage 处理
    // 权限错误（403）完全静默，不打印日志，因为这是预期的行为（普通用户无法访问某些接口）
    if (__DEV__ && !error.isPermissionError && error.name !== 'AbortError') {
      console.error('请求错误:', error);
    }
    // 如果是权限错误，完全静默，不打印任何日志
    throw error;
  }
};

/**
 * GET请求
 */
export const get = (url, params = {}, customConfig = {}) => {
  const queryParams = new URLSearchParams();
  for (const [key, value] of Object.entries(params)) {
    for (const item of Array.isArray(value) ? value : [value]) if (item != null && item !== '') queryParams.append(key, item);
  }
  const queryString = queryParams.toString();
  const fullUrl = queryString ? `${url}?${queryString}` : url;
  return request(fullUrl, { ...customConfig, method: 'GET' });
};

/**
 * POST请求 - 支持多种数据格式
 * @param {string} url - 请求地址
 * @param {object|FormData|URLSearchParams} data - 请求数据
 * @param {object} customConfig - 额外配置，如 headers
 */
export const post = (url, data = {}, customConfig = {}) => {
  const options = {
    method: 'POST',
    ...customConfig
  };

  // 判断数据类型并设置合适的请求体
  if (data instanceof FormData) {
    // FormData 类型，用于文件上传等
    options.body = data;
    // 删除 Content-Type，让浏览器自动设置（包含 boundary）
    if (options.headers) {
      delete options.headers['Content-Type'];
    }
  } else if (data instanceof URLSearchParams) {
    // URLSearchParams 类型，用于 application/x-www-form-urlencoded
    options.body = data;
    // 确保 Content-Type 正确设置
    if (!options.headers) {
      options.headers = {};
    }
    options.headers['Content-Type'] = 'application/x-www-form-urlencoded';
  } else {
    // 普通对象，序列化为 JSON
    options.body = JSON.stringify(data);
    if (!options.headers) {
      options.headers = {};
    }
    if (!options.headers['Content-Type']) {
      options.headers['Content-Type'] = 'application/json';
    }
  }

  return request(url, options);
};

/**
 * PUT请求
 */
export const put = (url, data = {}) => {
  return request(url, {
    method: 'PUT',
    body: JSON.stringify(data)
  });
};

/**
 * DELETE请求
 */
export const del = (url) => {
  return request(url, { method: 'DELETE' });
};

/**
 * 文件上传
 */
export const upload = async (url, file) => {
  const token = getToken();
  const formData = new FormData();
  formData.append('file', file);

  let response;
  try {
    response = await adminTestFetch(`${BASE_URL}${url}`, {
    method: 'POST',
    headers: {
      ...(token && { 'Authorization': token }) // 后端期望纯token，不需要Bearer前缀
    },
    body: formData
    });
  } catch (error) {
    if (error?.name === 'AbortError') throw error;
    throw new Error('文件上传失败，请检查文件大小与网络连接后重试');
  }

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: '上传失败' }));
    throw new Error(error.message || '上传失败');
  }

  return response.json();
};

export default request;
