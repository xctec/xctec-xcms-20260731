import { http } from './http';

export const workflowApi = {
  startProcess: (processKey: string, variables: Record<string, unknown>) => http.post('/api/workflow/process/start', { processKey, variables }),
  getMyTasks: (params: { page: number; size: number }) => http.post('/api/workflow/task/my-list', params),
  completeTask: (taskId: string, comment: string) => http.post('/api/workflow/task/complete', { taskId, comment }),
  getProcessDefinition: (processKey: string) => http.post('/api/workflow/definition/get', { processKey }),
};
