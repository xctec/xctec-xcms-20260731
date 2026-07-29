import { useEffect, useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { PageHeader, TableCard, Form, FormItem, Input, Button, toast } from '@/components/ui';
import { authApi } from '@/api/auth';

export default function Profile() {
  const queryClient = useQueryClient();
  const { data } = useQuery({ queryKey: ['profile'], queryFn: () => authApi.getUserInfo() });
  const user = data?.user;

  const [realName, setRealName] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [oldPwd, setOldPwd] = useState('');
  const [newPwd, setNewPwd] = useState('');

  useEffect(() => {
    if (user) {
      setRealName(user.realName ?? user.username ?? '');
      setEmail(user.email ?? '');
      setPhone(user.phone ?? '');
    }
  }, [user]);

  const saveProfile = async () => {
    await authApi.updateProfile({ realName, email, phone });
    toast.success('资料已更新');
    queryClient.invalidateQueries({ queryKey: ['profile'] });
  };
  const savePwd = async () => {
    if (!oldPwd || !newPwd) {
      toast.error('请填写密码');
      return;
    }
    await authApi.changePassword({ oldPassword: oldPwd, newPassword: newPwd });
    toast.success('密码已修改');
    setOldPwd('');
    setNewPwd('');
  };

  return (
    <div className="space-y-4">
      <PageHeader title="个人中心" description="查看与维护个人资料" />
      <TableCard>
        <div className="mb-2 font-medium">基本资料</div>
        <Form onSubmit={saveProfile} className="max-w-md space-y-4">
          <FormItem label="用户名">
            <Input value={user?.username ?? ''} />
          </FormItem>
          <FormItem label="姓名">
            <Input value={realName} onChange={(e) => setRealName(e.target.value)} />
          </FormItem>
          <FormItem label="邮箱">
            <Input value={email} onChange={(e) => setEmail(e.target.value)} />
          </FormItem>
          <FormItem label="手机号">
            <Input value={phone} onChange={(e) => setPhone(e.target.value)} />
          </FormItem>
          <Button type="submit">保存资料</Button>
        </Form>
      </TableCard>
      <TableCard>
        <div className="mb-2 font-medium">修改密码</div>
        <Form onSubmit={savePwd} className="max-w-md space-y-4">
          <FormItem label="原密码">
            <Input type="password" value={oldPwd} onChange={(e) => setOldPwd(e.target.value)} />
          </FormItem>
          <FormItem label="新密码">
            <Input type="password" value={newPwd} onChange={(e) => setNewPwd(e.target.value)} />
          </FormItem>
          <Button type="submit">修改密码</Button>
        </Form>
      </TableCard>
    </div>
  );
}
