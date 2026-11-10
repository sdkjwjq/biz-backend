import {get} from '@/utils/request'
import {reviewBase} from '@/api/roundReviews'
export const taskPageCapabilities=context=>({business:context.kind==='business',review:context.kind!=='business',canBusinessWrite:context.kind==='business',canReview:context.kind==='review'&&!context.readOnly})
export function createTaskPageSource(context){
 const business=context.kind==='business',base=business?'':reviewBase(context.roundId,context.kind==='reviewHistory')
 return {queryEndpoint:business?'/list-filters/tasks':`${base}/tasks`,optionsEndpoint:business?undefined:`${base}/tasks/filter-options`,materialUrl:id=>business?`/api/system/download/${id}`:`/api${base}/files/${id}`,loadDetail:(id,{signal}={})=>get(business?`/biz/tasks/${id}`:`${base}/tasks/${id}/page-detail`,{},{signal})}
}
