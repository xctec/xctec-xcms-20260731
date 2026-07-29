import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { PageHeader, Form, FormItem, useForm, Input, Button, toast } from '@/components/ui';
import { authApi } from '@/api/auth';

export default function ForgotPassword() {
  const navigate = useNavigate();
  const [step, setStep] = useState(1);
  const [token, setToken] = useState('');

  const emailForm = useForm<{ email: string }>({ email: '' }, { email: [{ required: '请输入邮箱' }] });
  const pwdForm = useForm<{ newPassword: string; confirmPassword: string }>(
    { newPassword: '', confirmPassword: '' },
    { newPassword: [{ required: '请输入新密码' }], confirmPassword: [{ required: '请确认新密码' }] },
  );

  const sendCode = async () => {
    if (!emailForm.validate()) return;
    await authApi.forgotPassword({ email: emailForm.values.email });
    toast.success('重置链接已发送，请查收邮件');
    setStep(2);
  };

  const doReset = async () => {
    if (!pwdForm.validate()) return;
    if (pwdForm.values.newPassword !== pwdForm.values.confirmPassword) {
      toast.error('两次密码不一致');
      return;
    }
    await authApi.resetPassword({ token, newPassword: pwdForm.values.newPassword });
    toast.success('密码已重置，请登录');
    navigate('/login');
  };

  return (
    <div className="mx-auto max-w-md space-y-4 py-10">
      <PageHeader title="找回密码" description="通过邮箱重置密码" />
      {step === 1 ? (
        <Form onSubmit={sendCode} className="space-y-4">
          <FormItem label="邮箱" required error={emailForm.errors.email}>
            <Input value={emailForm.values.email} onChange={(e) => emailForm.setField('email', e.target.value)} />
          </FormItem>
          <Button type="submit">发送重置链接</Button>
          <div className="text-center text-sm">
            <Link to="/login">返回登录</Link>
          </div>
        </Form>
      ) : (
        <Form onSubmit={doReset} className="space-y-4">
          <FormItem label="重置令牌">
            <Input value={token} onChange={(e) => setToken(e.target.value)} placeholder="请输入邮件中的重置令牌" />
          </FormItem>
          <FormItem label="新密码" required error={pwdForm.errors.newPassword}>
            <Input type="password" value={pwdForm.values.newPassword} onChange={(e) => pwdForm.setField('newPassword', e.target.value)} />
          </FormItem>
          <FormItem label="确认新密码" required error={pwdForm.errors.confirmPassword}>
            <Input
              type="password"
              value={pwdForm.values.confirmPassword}
              onChange={(e) => pwdForm.setField('confirmPassword', e.target.value)}
            />
          </FormItem>
          <Button type="submit">重置密码</Button>
        </Form>
      )}
    </div>
  );
}
