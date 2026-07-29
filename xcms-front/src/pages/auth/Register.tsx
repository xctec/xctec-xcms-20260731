import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { PageHeader, Form, FormItem, useForm, Input, Button, toast } from '@/components/ui';
import { authApi } from '@/api/auth';

type RegisterValues = {
  username: string;
  password: string;
  confirmPassword: string;
  name: string;
  email: string;
  phone: string;
  tenantCode: string;
};

export default function Register() {
  const navigate = useNavigate();
  const [submitting, setSubmitting] = useState(false);
  const form = useForm<RegisterValues>(
    { username: '', password: '', confirmPassword: '', name: '', email: '', phone: '', tenantCode: '' },
    {
      username: [{ required: '请输入用户名' }],
      password: [{ required: '请输入密码' }],
      confirmPassword: [{ required: '请确认密码' }],
    },
  );

  const handleSubmit = async () => {
    if (!form.validate()) return;
    const v = form.values;
    if (v.password !== v.confirmPassword) {
      toast.error('两次密码不一致');
      return;
    }
    setSubmitting(true);
    try {
      await authApi.register({
        username: v.username,
        password: v.password,
        name: v.name,
        email: v.email,
        phone: v.phone,
        tenantCode: v.tenantCode,
      });
      toast.success('注册成功，请登录');
      navigate('/login');
    } catch {
      toast.error('注册失败');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="mx-auto max-w-md space-y-4 py-10">
      <PageHeader title="注册账号" description="创建新用户" />
      <Form onSubmit={handleSubmit} className="space-y-4">
        <FormItem label="用户名" required error={form.errors.username}>
          <Input value={form.values.username} onChange={(e) => form.setField('username', e.target.value)} />
        </FormItem>
        <FormItem label="姓名">
          <Input value={form.values.name} onChange={(e) => form.setField('name', e.target.value)} />
        </FormItem>
        <FormItem label="密码" required error={form.errors.password}>
          <Input type="password" value={form.values.password} onChange={(e) => form.setField('password', e.target.value)} />
        </FormItem>
        <FormItem label="确认密码" required error={form.errors.confirmPassword}>
          <Input
            type="password"
            value={form.values.confirmPassword}
            onChange={(e) => form.setField('confirmPassword', e.target.value)}
          />
        </FormItem>
        <FormItem label="邮箱">
          <Input value={form.values.email} onChange={(e) => form.setField('email', e.target.value)} />
        </FormItem>
        <FormItem label="手机号">
          <Input value={form.values.phone} onChange={(e) => form.setField('phone', e.target.value)} />
        </FormItem>
        <FormItem label="租户编码">
          <Input
            value={form.values.tenantCode}
            onChange={(e) => form.setField('tenantCode', e.target.value)}
            placeholder="可选，留空使用默认租户"
          />
        </FormItem>
        <Button type="submit" disabled={submitting}>
          注册
        </Button>
        <div className="text-center text-sm">
          <Link to="/login">已有账号？去登录</Link>
        </div>
      </Form>
    </div>
  );
}
