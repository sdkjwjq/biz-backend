import { reactive } from 'vue'

export const passwordState = reactive({ required: false })
export const PASSWORD_RULE = '新密码至少8位，并同时包含大写字母、小写字母和数字。'
export const isPasswordValid = password => typeof password === 'string' && password.length >= 8
  && /[A-Z]/.test(password) && /[a-z]/.test(password) && /[0-9]/.test(password)
