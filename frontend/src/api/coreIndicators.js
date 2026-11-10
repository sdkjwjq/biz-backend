import { get, post, put } from '@/utils/request';
export const getCoreCapabilities=()=>get('/core-indicators/capabilities');
export const getCoreIndicators=params=>get('/core-indicators',params);
export const getCoreHistory=(id,params)=>get(`/core-indicators/${id}/history`,params);
export const exportCoreIndicators=body=>post('/core-indicators/export',body,{responseType:'blob'});
export const createCoreDraft=(id,data)=>post(`/core-indicators/${id}/draft`,data);
export const getCoreReport=id=>get(`/core-indicators/reports/${id}`);
export const saveCoreDraft=(id,data)=>put(`/core-indicators/reports/${id}`,data);
export const submitCoreDraft=(id,version)=>post(`/core-indicators/reports/${id}/submit`,{version});
export const withdrawCoreReport=(id,version)=>post(`/core-indicators/reports/${id}/withdraw`,{version});
export const getTodoCoreAudits=()=>get('/core-indicators/audit/todo');
export const getCoreAuditRecords=()=>get('/core-indicators/audit/records');
export const auditCoreReport=(id,data)=>post(`/core-indicators/reports/${id}/audit`,data);

// Badge count shares the list endpoint permissions and business predicates.
export const getTodoCoreAuditCount = () => get('/core-indicators/audit/todo', { countOnly: true });
