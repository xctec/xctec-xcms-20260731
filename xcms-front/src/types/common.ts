/** 后端统一响应格式 */
export interface ApiResponse<T> {
  errorCode: string;
  errorMsg: string;
  data: T;
}

/** 分页请求 */
export interface PageQuery {
  page: number;
  size: number;
}

/** 分页结果 */
export interface PageResult<T> {
  list: T[];
  total: number;
}
