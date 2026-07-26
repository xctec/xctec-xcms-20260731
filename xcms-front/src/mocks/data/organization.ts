import type { DepartmentTreeDTO, UserPositionDTO, UserGroupDTO } from '@/types/organization';

export const mockDepartments: DepartmentTreeDTO[] = [
  {
    id: 1,
    deptName: '集团总部',
    deptCode: 'HQ',
    managerId: 1,
    children: [
      {
        id: 2,
        deptName: '技术部',
        deptCode: 'TECH',
        managerId: 2,
        children: [
          { id: 4, deptName: '前端组', deptCode: 'FE', managerId: 3, children: [] },
          { id: 5, deptName: '后端组', deptCode: 'BE', managerId: 4, children: [] },
        ],
      },
      { id: 3, deptName: '市场部', deptCode: 'MKT', managerId: 5, children: [] },
    ],
  },
];

export const mockPositions: Record<number, UserPositionDTO[]> = {
  1: [{ id: 1, userId: 1, deptId: 1, deptName: '集团总部', positionId: 1, positionName: '总经理' }],
  2: [{ id: 2, userId: 2, deptId: 2, deptName: '技术部', positionId: 2, positionName: '技术总监' }],
  4: [{ id: 3, userId: 3, deptId: 4, deptName: '前端组', positionId: 3, positionName: '前端工程师' }],
  5: [{ id: 4, userId: 4, deptId: 5, deptName: '后端组', positionId: 4, positionName: '后端工程师' }],
};

export const mockGroups: UserGroupDTO[] = [
  { id: 1, groupName: '项目A组', description: '负责核心项目A', type: 'SYSTEM', memberCount: 5 },
  { id: 2, groupName: '临时支援组', description: '临时任务支援', type: 'CUSTOM', memberCount: 3 },
];
