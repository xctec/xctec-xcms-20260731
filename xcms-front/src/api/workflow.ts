import { http } from './http';
import { Schemas, Unwrap } from '@/types/api-helpers';

export const workflowApi = {
  startProcess: (processKey: string, variables: Record<string, unknown>) =>
    http.post<unknown, Unwrap<Schemas['ApiResponseWorkflowInstanceDTO']>>('/api/workflow/process/start', {
      processKey,
      variables,
    }),
  getMyTasks: (params: { page: number; size: number }) =>
    http.post<unknown, Unwrap<Schemas['ApiResponsePageResultWorkflowTaskDTO']>>('/api/workflow/task/my-list', params),
  completeTask: (taskId: string, comment: string) =>
    http.post<unknown, Unwrap<Schemas['ApiResponseVoid']>>('/api/workflow/task/complete', { taskId, comment }),
  getProcessDefinition: (processKey: string) =>
    http.post<unknown, Unwrap<Schemas['ApiResponseWorkflowDefinitionDTO']>>('/api/workflow/definition/get', {
      processKey,
    }),
};
