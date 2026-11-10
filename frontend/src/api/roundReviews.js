import {get,post} from '@/utils/request'
export const reviewBase=(roundId,history=false)=>`${history?'/review-history/work':'/review-work'}/${roundId}`
const scope=value=>{if(typeof value==='string'){try{return JSON.parse(value)}catch{return {tasks:[]}}}return value||{tasks:[]}}
export function normalizeRoundReview(value){
 const revision=r=>r?{...r,revisionId:r.revision_id,revisionNo:r.revision_no,year:value.round?.review_year||'全',text:r.review_text,result:r.review_result,scope:scope(r.scope_json),createTime:r.create_time,expertName:r.expert_name,operatorId:r.operator_id,delegated:false}:null
 return {...value,year:value.round?.review_year||'全',expertName:value.expertName||'',scope:scope(value.scope),formal:revision(value.formal),history:(value.history||[]).map(revision)}
}
export const loadRoundReview=(roundId,taskId,history,config)=>get(`${reviewBase(roundId,history)}/tasks/${taskId}`,{},config).then(normalizeRoundReview)
export const saveRoundReview=(roundId,taskId,body)=>post(`${reviewBase(roundId)}/tasks/${taskId}`,body).then(normalizeRoundReview)
